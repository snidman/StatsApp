package com.snidman.statsapp

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snidman.statsapp.data.AppDatabase
import com.snidman.statsapp.data.MatchEntity
import com.snidman.statsapp.data.PlayerEntity
import com.snidman.statsapp.data.StatEventEntity
import com.snidman.statsapp.data.StatsRepository
import com.snidman.statsapp.data.TeamEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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

data class StatsUiState(
    val players: List<PlayerCardState> = emptyList(),
    val allPlayers: List<PlayerEntity> = emptyList(),
    val teams: List<TeamRosterState> = emptyList(),
    val matches: List<MatchEntity> = emptyList(),
    val selectedMatchId: Long? = null,
    val selectedSet: Int? = null,
    val filteredEvents: List<StatEventEntity> = emptyList(),
    val selectedMatchDeleteEventCount: Int = 0,
    val selectedSetDeleteEventCount: Int = 0,
    val playerDeleteEventCounts: Map<Long, Int> = emptyMap(),
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
        eventsFlow
    ) { staticData, selectedMatchId, selectedSet, events ->
        val players = staticData.players
        val teams = staticData.teams
        val matches = staticData.matches
        val effectiveMatchId = selectedMatchId ?: matches.firstOrNull()?.id

        val playerCards = players.map { player ->
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
            selectedSet = selectedSet,
            filteredEvents = events
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

    fun addMatch(name: String) {
        viewModelScope.launch {
            val newMatchId = repository.addMatch(name)
            selectedMatchIdFlow.value = newMatchId
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
        val playerLookup = state.players.associate { it.player.id to it.player }
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
