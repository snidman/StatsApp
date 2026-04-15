package com.snidman.statsapp

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snidman.statsapp.data.AppDatabase
import com.snidman.statsapp.data.MatchEntity
import com.snidman.statsapp.data.PlayerEntity
import com.snidman.statsapp.data.SetLineupEntity
import com.snidman.statsapp.data.SetPlayerRoleEntity
import com.snidman.statsapp.data.StatEventEntity
import com.snidman.statsapp.data.StatsRepository
import com.snidman.statsapp.data.TeamEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class PlayerCounters(
    val serves: Map<String, Int> = emptyMap(),
    val serveReceives: Map<String, Int> = emptyMap(),
    val attacks: Map<String, Int> = emptyMap(),
    val sets: Map<String, Int> = emptyMap()
)

data class PlayerCardState(
    val player: PlayerEntity,
    val counters: PlayerCounters
)

data class TeamRosterState(
    val team: TeamEntity,
    val players: List<PlayerEntity>
)

data class RotationPositionLineup(
    val position: Int,
    val frontPlayerId: Long? = null,
    val backPlayerId: Long? = null,
    val servingPlayerId: Long? = null
)

data class SetRotationLineupState(
    val matchId: Long,
    val setNumber: Int,
    val positions: List<RotationPositionLineup>,
    val liberoPlayerIds: Set<Long> = emptySet(),
    val setterPlayerIds: Set<Long> = emptySet()
)

data class StatsUiState(
    val players: List<PlayerCardState> = emptyList(),
    val allPlayers: List<PlayerEntity> = emptyList(),
    val teams: List<TeamRosterState> = emptyList(),
    val matches: List<MatchEntity> = emptyList(),
    val selectedMatchId: Long? = null,
    val selectedMatchTeamId: Long? = null,
    val selectedSet: Int? = null,
    val filteredEvents: List<StatEventEntity> = emptyList(),
    val selectedMatchDeleteEventCount: Int = 0,
    val selectedSetDeleteEventCount: Int = 0,
    val playerDeleteEventCounts: Map<Long, Int> = emptyMap(),
    val selectedSetLineup: SetRotationLineupState? = null,
    val lastExportMessage: String? = null
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StatsRepository(AppDatabase.getInstance(application))

    private val selectedMatchIdFlow = MutableStateFlow<Long?>(null)
    private val selectedSetFlow = MutableStateFlow<Int?>(null)
    private val exportMessageFlow = MutableStateFlow<String?>(null)

    private val playersFlow = repository.playersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val teamsFlow = repository.teamsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val matchesFlow = repository.matchesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val eventsFlow = combine(selectedMatchIdFlow, selectedSetFlow) { matchId, setNumber ->
        matchId to setNumber
    }.flatMapLatest { (matchId, setNumber) ->
        if (matchId == null) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            repository.getEventsFlow(matchId, setNumber)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val selectedMatchDeleteCountFlow = selectedMatchIdFlow
        .flatMapLatest { matchId ->
            if (matchId == null) {
                kotlinx.coroutines.flow.flowOf(0)
            } else {
                repository.getEventCountForMatchFlow(matchId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val selectedSetDeleteCountFlow = combine(selectedMatchIdFlow, selectedSetFlow) { matchId, setNumber ->
        matchId to setNumber
    }.flatMapLatest { (matchId, setNumber) ->
        if (matchId == null || setNumber == null) {
            kotlinx.coroutines.flow.flowOf(0)
        } else {
            repository.getEventCountForMatchSetFlow(matchId, setNumber)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val playerDeleteCountsFlow = playersFlow
        .flatMapLatest { players ->
            if (players.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyMap())
            } else {
                combine(players.map { player ->
                    repository.getEventCountForPlayerFlow(player.id).map { count -> player.id to count }
                }) { pairs ->
                    pairs.associate { it }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val selectedSetLineupFlow = combine(selectedMatchIdFlow, selectedSetFlow) { matchId, setNumber ->
        matchId to setNumber
    }.flatMapLatest { (matchId, setNumber) ->
        if (matchId == null || setNumber == null) {
            flowOf(null)
        } else {
            combine(
                repository.getSetLineupsFlow(matchId, setNumber),
                repository.getSetPlayerRolesFlow(matchId, setNumber)
            ) { lineups, roles ->
                val byPosition = lineups.associateBy { it.position }
                val positions = (1..6).map { position ->
                    val slot = byPosition[position]
                    RotationPositionLineup(
                        position = position,
                        frontPlayerId = slot?.frontPlayerId,
                        backPlayerId = slot?.backPlayerId,
                        servingPlayerId = slot?.servingPlayerId
                    )
                }
                SetRotationLineupState(
                    matchId = matchId,
                    setNumber = setNumber,
                    positions = positions,
                    liberoPlayerIds = roles.filter { it.isLibero }.map { it.playerId }.toSet(),
                    setterPlayerIds = roles.filter { it.isSetter }.map { it.playerId }.toSet()
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private data class StaticData(
        val players: List<PlayerEntity>,
        val teams: List<TeamEntity>,
        val matches: List<MatchEntity>
    )

    private val staticDataFlow = combine(playersFlow, teamsFlow, matchesFlow) { players, teams, matches ->
        StaticData(players = players, teams = teams, matches = matches)
    }

    private val baseStateCoreFlow = combine(
        staticDataFlow,
        selectedMatchIdFlow,
        selectedSetFlow,
        eventsFlow,
        selectedSetLineupFlow
    ) { staticData, selectedMatchId, selectedSet, events, selectedSetLineup ->
        val players = staticData.players
        val teams = staticData.teams
        val matches = staticData.matches
        val effectiveMatchId = selectedMatchId ?: matches.firstOrNull()?.id
        val selectedMatch = matches.firstOrNull { it.id == effectiveMatchId }
        val selectedMatchTeamId = selectedMatch?.teamId
        val matchPlayers = players.filter { it.teamId == selectedMatchTeamId }

        val playerCards = matchPlayers.map { player ->
            val playerEvents = events.filter { it.playerId == player.id }
            PlayerCardState(
                player = player,
                counters = PlayerCounters(
                    serves = countByOutcome(playerEvents, "SERVE"),
                    serveReceives = countByOutcome(playerEvents, "SERVE_RECEIVE"),
                    attacks = countByOutcome(playerEvents, "ATTACK"),
                    sets = countByOutcome(playerEvents, "SET")
                )
            )
        }

        val teamRosters = teams.map { team ->
            TeamRosterState(
                team = team,
                players = players
                    .filter { it.teamId == team.id }
                    .sortedWith(compareBy(PlayerEntity::jerseyNumber, PlayerEntity::name))
            )
        }

        StatsUiState(
            players = playerCards,
            allPlayers = players,
            teams = teamRosters,
            matches = matches,
            selectedMatchId = effectiveMatchId,
            selectedMatchTeamId = selectedMatchTeamId,
            selectedSet = selectedSet,
            filteredEvents = events,
            selectedSetLineup = selectedSetLineup
        )
    }

    private val baseUiStateFlow = combine(
        baseStateCoreFlow,
        selectedMatchDeleteCountFlow,
        selectedSetDeleteCountFlow,
        playerDeleteCountsFlow
    ) { base, selectedMatchDeleteCount, selectedSetDeleteCount, playerDeleteCounts ->
        base.copy(
            selectedMatchDeleteEventCount = selectedMatchDeleteCount,
            selectedSetDeleteEventCount = selectedSetDeleteCount,
            playerDeleteEventCounts = playerDeleteCounts
        )
    }

    val uiState: StateFlow<StatsUiState> = combine(
        baseUiStateFlow,
        exportMessageFlow
    ) { base, exportMessage ->
        base.copy(lastExportMessage = exportMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
        }

        viewModelScope.launch {
            matchesFlow.collect { matches ->
                if (matches.isNotEmpty() && selectedMatchIdFlow.value == null) {
                    selectedMatchIdFlow.value = matches.first().id
                }
            }
        }
    }

    fun selectMatch(matchId: Long) {
        selectedMatchIdFlow.value = matchId
    }

    fun selectSet(setNumber: Int?) {
        selectedSetFlow.value = setNumber
    }

    fun addPlayer(name: String, jerseyNumber: Int) {
        viewModelScope.launch {
            repository.addPlayer(name, jerseyNumber)
        }
    }

    fun updatePlayer(playerId: Long, name: String, jerseyNumber: Int) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.updatePlayer(playerId, trimmed, jerseyNumber)
        }
    }

    fun deletePlayer(playerId: Long) {
        viewModelScope.launch {
            repository.deletePlayer(playerId)
        }
    }

    fun addMatch(name: String, teamId: Long, opponentTeamName: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        viewModelScope.launch {
            val newMatchId = repository.addMatch(trimmedName, teamId, opponentTeamName.trim())
            selectedMatchIdFlow.value = newMatchId
        }
    }

    fun updateMatch(matchId: Long, name: String, teamId: Long, opponentTeamName: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        viewModelScope.launch {
            repository.updateMatch(matchId, trimmedName, teamId, opponentTeamName.trim())
        }
    }

    fun addTeam(name: String, playerIds: List<Long>) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.addTeam(trimmed, playerIds.distinct())
        }
    }

    fun updateTeam(teamId: Long, name: String, playerIds: List<Long>) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.updateTeam(teamId, trimmed, playerIds.distinct())
        }
    }

    fun deleteTeam(teamId: Long) {
        viewModelScope.launch {
            repository.deleteTeam(teamId)
        }
    }

    fun deleteSelectedMatch() {
        val matchId = selectedMatchIdFlow.value ?: return
        viewModelScope.launch {
            repository.deleteMatch(matchId)
            selectedMatchIdFlow.value = null
            selectedSetFlow.value = null
        }
    }

    fun deleteSelectedSet() {
        val matchId = selectedMatchIdFlow.value ?: return
        val setNumber = selectedSetFlow.value ?: return
        viewModelScope.launch {
            repository.deleteSetEvents(matchId, setNumber)
            repository.saveSetLineup(matchId, setNumber, emptyList(), emptyList())
        }
    }

    fun saveSelectedSetLineup(lineup: SetRotationLineupState) {
        val selectedMatchId = selectedMatchIdFlow.value ?: return
        val selectedSet = selectedSetFlow.value ?: return
        if (lineup.matchId != selectedMatchId || lineup.setNumber != selectedSet) return

        val selectedMatchTeamId = matchesFlow.value.firstOrNull { it.id == selectedMatchId }?.teamId
        val validPlayerIds = playersFlow.value
            .filter { it.teamId == selectedMatchTeamId }
            .map { it.id }
            .toSet()
        val normalizedPositions = (1..6).map { position ->
            val slot = lineup.positions.firstOrNull { it.position == position }
                ?: RotationPositionLineup(position = position)
            val frontPlayerId = slot.frontPlayerId?.takeIf { it in validPlayerIds }
            val backPlayerId = slot.backPlayerId?.takeIf { it in validPlayerIds }
            val servingPlayerId = slot.servingPlayerId?.takeIf {
                it == frontPlayerId || it == backPlayerId
            }
            RotationPositionLineup(
                position = position,
                frontPlayerId = frontPlayerId,
                backPlayerId = backPlayerId,
                servingPlayerId = servingPlayerId
            )
        }

        val liberoPlayerIds = lineup.liberoPlayerIds
            .filter { it in validPlayerIds }
            .take(2)
            .toSet()
        val setterPlayerIds = lineup.setterPlayerIds
            .filter { it in validPlayerIds }
            .toSet()

        val lineupEntities = normalizedPositions.map { slot ->
            SetLineupEntity(
                matchId = selectedMatchId,
                setNumber = selectedSet,
                position = slot.position,
                frontPlayerId = slot.frontPlayerId,
                backPlayerId = slot.backPlayerId,
                servingPlayerId = slot.servingPlayerId
            )
        }
        val roleEntities = (liberoPlayerIds + setterPlayerIds).map { playerId ->
            SetPlayerRoleEntity(
                matchId = selectedMatchId,
                setNumber = selectedSet,
                playerId = playerId,
                isLibero = playerId in liberoPlayerIds,
                isSetter = playerId in setterPlayerIds
            )
        }

        viewModelScope.launch {
            repository.saveSetLineup(
                matchId = selectedMatchId,
                setNumber = selectedSet,
                lineups = lineupEntities,
                roles = roleEntities
            )
        }
    }

    fun recordStat(playerId: Long, skill: String, outcome: String) {
        val matchId = selectedMatchIdFlow.value ?: return
        val setNumber = selectedSetFlow.value ?: 1

        viewModelScope.launch {
            repository.insertEvent(
                StatEventEntity(
                    playerId = playerId,
                    matchId = matchId,
                    setNumber = setNumber,
                    skill = skill,
                    outcome = outcome,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun clearExportMessage() {
        exportMessageFlow.value = null
    }

    fun exportCsv(contentResolver: ContentResolver, uri: Uri) {
        val state = uiState.value
        val selectedMatchId = state.selectedMatchId
        val selectedMatchName = state.matches.firstOrNull { it.id == selectedMatchId }?.name ?: "Unknown"
        val playerLookup = state.allPlayers.associateBy { it.id }
        val rows = mutableListOf<String>()
        rows += "player_name,jersey_number,match,set,skill,outcome,timestamp"

        state.filteredEvents
            .sortedBy { it.createdAt }
            .forEach { event ->
                val player = playerLookup[event.playerId]
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(event.createdAt))
                rows += listOf(
                    csv(player?.name ?: "Unknown"),
                    csv(player?.jerseyNumber?.toString() ?: ""),
                    csv(selectedMatchName),
                    csv(event.setNumber.toString()),
                    csv(event.skill),
                    csv(event.outcome),
                    csv(timestamp)
                ).joinToString(",")
            }

        viewModelScope.launch {
            runCatching {
                contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                    writer.write(rows.joinToString("\n"))
                } ?: error("Could not open export destination")
            }.onSuccess {
                exportMessageFlow.value = "CSV export complete"
            }.onFailure { throwable ->
                exportMessageFlow.value = "Export failed: ${throwable.message ?: "unknown error"}"
            }
        }
    }

    private fun countByOutcome(events: List<StatEventEntity>, skill: String): Map<String, Int> {
        return events.filter { it.skill == skill }
            .groupingBy { it.outcome }
            .eachCount()
    }

    private fun csv(value: String): String {
        return "\"${value.replace("\"", "\"\"")}\""
    }
}
