package com.snidman.statsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snidman.statsapp.data.MatchEntity
import com.snidman.statsapp.data.PlayerEntity
import com.snidman.statsapp.ui.theme.StatsAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val exportLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument("text/csv")
        ) { uri ->
            if (uri != null) {
                viewModel.exportCsv(contentResolver, uri)
            }
        }

        setContent {
            StatsAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    var showingManagementScreen by rememberSaveable { mutableStateOf(false) }
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(Date())
                    if (showingManagementScreen) {
                        TeamManagementScreen(
                            teams = uiState.teams,
                            players = uiState.allPlayers,
                            onBack = { showingManagementScreen = false },
                            onCreateTeam = viewModel::addTeam,
                            onCreatePlayer = viewModel::addPlayer,
                            onUpdateTeam = viewModel::updateTeam,
                            onDeleteTeam = viewModel::deleteTeam,
                            onUpdatePlayer = viewModel::updatePlayer,
                            onDeletePlayer = viewModel::deletePlayer
                        )
                    } else {
                        StatCaptureScreen(
                            state = uiState,
                            onRecordStat = viewModel::recordStat,
                            onSelectMatch = viewModel::selectMatch,
                            onSelectSet = viewModel::selectSet,
                            onCreateMatch = viewModel::addMatch,
                            onUpdateMatch = viewModel::updateMatch,
                            onDeleteSelectedMatch = viewModel::deleteSelectedMatch,
                            onDeleteSelectedSet = viewModel::deleteSelectedSet,
                            onSaveSetLineup = viewModel::saveSelectedSetLineup,
                            onOpenTeamManager = { showingManagementScreen = true },
                            onClearExportMessage = viewModel::clearExportMessage,
                            onExportCsv = {
                                exportLauncher.launch("volleyball_stats_$timestamp.csv")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCaptureScreen(
    state: StatsUiState,
    onRecordStat: (Long, String, String) -> Unit,
    onSelectMatch: (Long) -> Unit,
    onSelectSet: (Int?) -> Unit,
    onCreateMatch: (String, Long, String) -> Unit,
    onUpdateMatch: (Long, String, Long, String) -> Unit,
    onDeleteSelectedMatch: () -> Unit,
    onDeleteSelectedSet: () -> Unit,
    onSaveSetLineup: (SetRotationLineupState) -> Unit,
    onOpenTeamManager: () -> Unit,
    onClearExportMessage: () -> Unit,
    onExportCsv: () -> Unit
) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("Volleyball Stat Capture", style = MaterialTheme.typography.headlineSmall)
                Text("Capture every player on one screen", style = MaterialTheme.typography.bodyMedium)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Teams and player profiles")
                        Button(onClick = onOpenTeamManager) {
                            Text("Manage Teams & Players")
                        }
                    }
                }
            }

            item {
                FilterAndActionsCard(
                    matches = state.matches,
                    selectedMatchId = state.selectedMatchId,
                    selectedSet = state.selectedSet,
                    totalEvents = state.filteredEvents.size,
                    selectedMatchDeleteCount = state.selectedMatchDeleteEventCount,
                    selectedSetDeleteCount = state.selectedSetDeleteEventCount,
                    teams = state.teams,
                    players = state.players.map { it.player },
                    selectedMatchTeamId = state.selectedMatchTeamId,
                    selectedSetLineup = state.selectedSetLineup,
                    onSelectMatch = onSelectMatch,
                    onSelectSet = onSelectSet,
                    onCreateMatch = onCreateMatch,
                    onUpdateMatch = onUpdateMatch,
                    onDeleteSelectedMatch = onDeleteSelectedMatch,
                    onDeleteSelectedSet = onDeleteSelectedSet,
                    onSaveSetLineup = onSaveSetLineup,
                    onExportCsv = onExportCsv
                )
            }

            if (state.lastExportMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(state.lastExportMessage)
                            TextButton(onClick = onClearExportMessage) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            }

            items(state.players, key = { it.player.id }) { playerState ->
                PlayerStatCard(
                    playerState = playerState,
                    onRecordStat = onRecordStat
                )
            }

            if (state.players.isEmpty()) {
                item {
                    Text("No players for this match. Select a team on the match first, then add players to that team in Team & Player Manager.")
                }
            }

            item {
                EventFeedCard(
                    state = state,
                    players = state.allPlayers
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterAndActionsCard(
    matches: List<MatchEntity>,
    selectedMatchId: Long?,
    selectedSet: Int?,
    totalEvents: Int,
    selectedMatchDeleteCount: Int,
    selectedSetDeleteCount: Int,
    teams: List<TeamRosterState>,
    players: List<PlayerEntity>,
    selectedMatchTeamId: Long?,
    selectedSetLineup: SetRotationLineupState?,
    onSelectMatch: (Long) -> Unit,
    onSelectSet: (Int?) -> Unit,
    onCreateMatch: (String, Long, String) -> Unit,
    onUpdateMatch: (Long, String, Long, String) -> Unit,
    onDeleteSelectedMatch: () -> Unit,
    onDeleteSelectedSet: () -> Unit,
    onSaveSetLineup: (SetRotationLineupState) -> Unit,
    onExportCsv: () -> Unit
) {
    var matchName by remember { mutableStateOf("") }
    var opponentTeamName by remember { mutableStateOf("") }
    var createMatchTeamId by remember(teams) { mutableStateOf(teams.firstOrNull()?.team?.id) }
    var showEditMatchDialog by remember { mutableStateOf(false) }
    var showDeleteMatchDialog by remember { mutableStateOf(false) }
    var showDeleteSetDialog by remember { mutableStateOf(false) }
    var showEditLineupDialog by remember { mutableStateOf(false) }
    val teamNameById = teams.associate { it.team.id to it.team.name }
    val selectedMatch = matches.firstOrNull { it.id == selectedMatchId }
    val selectedMatchName = selectedMatch?.name ?: "this match"

    if (showEditMatchDialog && selectedMatch != null) {
        EditMatchDialog(
            match = selectedMatch,
            teams = teams,
            onSave = { name, teamId, opponent ->
                onUpdateMatch(selectedMatch.id, name, teamId, opponent)
                showEditMatchDialog = false
            },
            onDismiss = { showEditMatchDialog = false }
        )
    }

    if (showDeleteMatchDialog) {
        ConfirmDeleteDialog(
            title = "Delete Match?",
            message = "This will permanently remove $selectedMatchName and $selectedMatchDeleteCount event(s).",
            onConfirm = {
                showDeleteMatchDialog = false
                onDeleteSelectedMatch()
            },
            onDismiss = { showDeleteMatchDialog = false }
        )
    }

    if (showDeleteSetDialog) {
        val setLabel = selectedSet?.toString() ?: "selected"
        ConfirmDeleteDialog(
            title = "Delete Set?",
            message = "This will permanently remove $selectedSetDeleteCount event(s) in set $setLabel for the selected match.",
            onConfirm = {
                showDeleteSetDialog = false
                onDeleteSelectedSet()
            },
            onDismiss = { showDeleteSetDialog = false }
        )
    }

    if (showEditLineupDialog && selectedMatchId != null && selectedSet != null) {
        RotationLineupDialog(
            matchId = selectedMatchId,
            setNumber = selectedSet,
            players = players,
            initialLineup = selectedSetLineup,
            onSave = {
                onSaveSetLineup(it)
                showEditLineupDialog = false
            },
            onDismiss = { showEditLineupDialog = false }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Filters", style = MaterialTheme.typography.titleLarge)

            Text("Match", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                matches.forEach { match ->
                    val selected = match.id == selectedMatchId
                    Button(onClick = { onSelectMatch(match.id) }) {
                        val ownTeamLabel = teamNameById[match.teamId]
                        val label = if (match.opponentTeamName.isBlank()) {
                            if (ownTeamLabel.isNullOrBlank()) {
                                match.name
                            } else {
                                "${match.name} (${ownTeamLabel})"
                            }
                        } else {
                            if (ownTeamLabel.isNullOrBlank()) {
                                "${match.name} vs ${match.opponentTeamName}"
                            } else {
                                "${match.name} (${ownTeamLabel}) vs ${match.opponentTeamName}"
                            }
                        }
                        Text(if (selected) "$label *" else label)
                    }
                }
            }

            Button(
                enabled = selectedMatchId != null && selectedSet != null,
                onClick = { showEditLineupDialog = true }
            ) {
                Text("Edit Starting Rotation")
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = matchName,
                onValueChange = { matchName = it },
                label = { Text("New match name") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = opponentTeamName,
                onValueChange = { opponentTeamName = it },
                label = { Text("Opposing team name") },
                singleLine = true
            )
            Text("Team playing this match", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                teams.forEach { roster ->
                    val team = roster.team
                    val selected = createMatchTeamId == team.id
                    Button(onClick = { createMatchTeamId = team.id }) {
                        Text(if (selected) "${team.name} *" else team.name)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    val trimmed = matchName.trim()
                    val teamId = createMatchTeamId
                    if (trimmed.isNotEmpty() && teamId != null) {
                        onCreateMatch(trimmed, teamId, opponentTeamName.trim())
                        matchName = ""
                        opponentTeamName = ""
                    }
                }) {
                    Text("Add Match")
                }
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = selectedMatch?.opponentTeamName ?: "",
                    onValueChange = {},
                    label = { Text("Selected opponent") },
                    enabled = false,
                    singleLine = true
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = selectedMatchTeamId?.let { teamNameById[it] } ?: "",
                onValueChange = {},
                label = { Text("Selected team") },
                enabled = false,
                singleLine = true
            )

            Button(
                enabled = selectedMatch != null,
                onClick = { showEditMatchDialog = true }
            ) {
                Text("Edit Selected Match")
            }

            Text("Set", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { onSelectSet(null) }) {
                    Text(if (selectedSet == null) "All *" else "All")
                }
                (1..5).forEach { setValue ->
                    Button(onClick = { onSelectSet(setValue) }) {
                        Text(if (selectedSet == setValue) "$setValue *" else "$setValue")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filtered events: $totalEvents")
                Button(onClick = onExportCsv) {
                    Text("Export CSV")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = selectedMatchId != null,
                    onClick = { showDeleteMatchDialog = true }
                ) {
                    Text("Delete Match")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = selectedSet != null,
                    onClick = { showDeleteSetDialog = true }
                ) {
                    Text("Delete Set")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditMatchDialog(
    match: MatchEntity,
    teams: List<TeamRosterState>,
    onSave: (String, Long, String) -> Unit,
    onDismiss: () -> Unit
) {
    var matchName by remember(match.id) { mutableStateOf(match.name) }
    var opponentName by remember(match.id) { mutableStateOf(match.opponentTeamName) }
    var selectedTeamId by remember(match.id, teams) {
        mutableStateOf(match.teamId ?: teams.firstOrNull()?.team?.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Match") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = matchName,
                    onValueChange = { matchName = it },
                    label = { Text("Match name") },
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = opponentName,
                    onValueChange = { opponentName = it },
                    label = { Text("Opposing team name") },
                    singleLine = true
                )
                Text("Team playing this match", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    teams.forEach { roster ->
                        val team = roster.team
                        val selected = selectedTeamId == team.id
                        Button(onClick = { selectedTeamId = team.id }) {
                            Text(if (selected) "${team.name} *" else team.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val trimmed = matchName.trim()
                val teamId = selectedTeamId
                if (trimmed.isNotEmpty() && teamId != null) {
                    onSave(trimmed, teamId, opponentName.trim())
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private enum class RotationSlotField {
    FRONT,
    BACK
}

private data class RotationPickerTarget(
    val position: Int,
    val field: RotationSlotField
)

@Composable
private fun RotationLineupDialog(
    matchId: Long,
    setNumber: Int,
    players: List<PlayerEntity>,
    initialLineup: SetRotationLineupState?,
    onSave: (SetRotationLineupState) -> Unit,
    onDismiss: () -> Unit
) {
    val sortedPlayers = players.sortedWith(compareBy(PlayerEntity::jerseyNumber, PlayerEntity::name))
    val playerNameById = sortedPlayers.associate { it.id to "${it.name} #${it.jerseyNumber}" }

    var positions by remember(matchId, setNumber, initialLineup, players) {
        val existingByPosition = initialLineup?.positions?.associateBy { it.position }.orEmpty()
        mutableStateOf(
            (1..6).map { position ->
                existingByPosition[position] ?: RotationPositionLineup(position = position)
            }
        )
    }
    var liberoIds by remember(matchId, setNumber, initialLineup, players) {
        mutableStateOf(initialLineup?.liberoPlayerIds?.filter { it in playerNameById }?.toSet().orEmpty())
    }
    var setterIds by remember(matchId, setNumber, initialLineup, players) {
        mutableStateOf(initialLineup?.setterPlayerIds?.filter { it in playerNameById }?.toSet().orEmpty())
    }
    var pickerTarget by remember { mutableStateOf<RotationPickerTarget?>(null) }

    fun normalizeServing(slot: RotationPositionLineup): RotationPositionLineup {
        val servingPlayerId = when {
            slot.frontPlayerId != null && slot.backPlayerId != null -> {
                slot.servingPlayerId?.takeIf { it == slot.frontPlayerId || it == slot.backPlayerId }
                    ?: slot.frontPlayerId
            }

            slot.frontPlayerId != null -> slot.frontPlayerId
            slot.backPlayerId != null -> slot.backPlayerId
            else -> null
        }
        return slot.copy(servingPlayerId = servingPlayerId)
    }

    pickerTarget?.let { target ->
        val currentSlot = positions.firstOrNull { it.position == target.position }
        val currentSelectedPlayerId = when (target.field) {
            RotationSlotField.FRONT -> currentSlot?.frontPlayerId
            RotationSlotField.BACK -> currentSlot?.backPlayerId
        }
        PlayerSelectionDialog(
            title = "Set $setNumber - Position ${target.position}",
            selectedPlayerId = currentSelectedPlayerId,
            players = sortedPlayers,
            onSelect = { selectedId ->
                positions = positions.map { slot ->
                    if (slot.position != target.position) {
                        slot
                    } else {
                        val updated = when (target.field) {
                            RotationSlotField.FRONT -> slot.copy(frontPlayerId = selectedId)
                            RotationSlotField.BACK -> slot.copy(backPlayerId = selectedId)
                        }
                        normalizeServing(updated)
                    }
                }
                pickerTarget = null
            },
            onDismiss = { pickerTarget = null }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Starting Rotation - Set $setNumber") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                positions.sortedBy { it.position }.forEach { slot ->
                    val frontLabel = slot.frontPlayerId?.let { playerNameById[it] } ?: "Select front-row player"
                    val backLabel = slot.backPlayerId?.let { playerNameById[it] } ?: "Select back-row player"
                    val servingLabel = slot.servingPlayerId?.let { playerNameById[it] } ?: "None"

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Position ${slot.position}", fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        pickerTarget = RotationPickerTarget(slot.position, RotationSlotField.FRONT)
                                    }
                                ) {
                                    Text(frontLabel)
                                }
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        pickerTarget = RotationPickerTarget(slot.position, RotationSlotField.BACK)
                                    }
                                ) {
                                    Text(backLabel)
                                }
                            }

                            if (slot.frontPlayerId != null && slot.backPlayerId != null) {
                                Text("Serving player")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val frontSelected = slot.servingPlayerId == slot.frontPlayerId
                                    val backSelected = slot.servingPlayerId == slot.backPlayerId
                                    Button(onClick = {
                                        positions = positions.map {
                                            if (it.position == slot.position) it.copy(servingPlayerId = slot.frontPlayerId) else it
                                        }
                                    }) {
                                        Text(if (frontSelected) "Front *" else "Front")
                                    }
                                    Button(onClick = {
                                        positions = positions.map {
                                            if (it.position == slot.position) it.copy(servingPlayerId = slot.backPlayerId) else it
                                        }
                                    }) {
                                        Text(if (backSelected) "Back *" else "Back")
                                    }
                                }
                            } else {
                                Text("Serving: $servingLabel", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Text("Liberos (up to 2)", style = MaterialTheme.typography.titleMedium)
                sortedPlayers.forEach { player ->
                    val checked = player.id in liberoIds
                    val canCheckMore = checked || liberoIds.size < 2
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            enabled = canCheckMore,
                            onCheckedChange = {
                                liberoIds = if (checked) {
                                    liberoIds - player.id
                                } else if (liberoIds.size < 2) {
                                    liberoIds + player.id
                                } else {
                                    liberoIds
                                }
                            }
                        )
                        Text("${player.name} #${player.jerseyNumber}")
                    }
                }

                Text("Setters", style = MaterialTheme.typography.titleMedium)
                sortedPlayers.forEach { player ->
                    val checked = player.id in setterIds
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                setterIds = if (checked) {
                                    setterIds - player.id
                                } else {
                                    setterIds + player.id
                                }
                            }
                        )
                        Text("${player.name} #${player.jerseyNumber}")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    SetRotationLineupState(
                        matchId = matchId,
                        setNumber = setNumber,
                        positions = positions.map(::normalizeServing),
                        liberoPlayerIds = liberoIds,
                        setterPlayerIds = setterIds
                    )
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PlayerSelectionDialog(
    title: String,
    selectedPlayerId: Long?,
    players: List<PlayerEntity>,
    onSelect: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { onSelect(null) }) {
                    Text(if (selectedPlayerId == null) "None *" else "None")
                }
                players.forEach { player ->
                    val selected = selectedPlayerId == player.id
                    Button(onClick = { onSelect(player.id) }) {
                        Text(if (selected) "${player.name} #${player.jerseyNumber} *" else "${player.name} #${player.jerseyNumber}")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun AddPlayerCard(onCreatePlayer: (String, Int) -> Unit) {
    var playerName by remember { mutableStateOf("") }
    var jerseyText by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Add Player", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Player name") },
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier.width(110.dp),
                    value = jerseyText,
                    onValueChange = { jerseyText = it.filter(Char::isDigit) },
                    label = { Text("Jersey") },
                    singleLine = true
                )
                Button(onClick = {
                    val name = playerName.trim()
                    val jersey = jerseyText.toIntOrNull()
                    if (name.isNotEmpty() && jersey != null) {
                        onCreatePlayer(name, jersey)
                        playerName = ""
                        jerseyText = ""
                    }
                }) {
                    Text("Add")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerStatCard(
    playerState: PlayerCardState,
    onRecordStat: (Long, String, String) -> Unit
) {
    val player = playerState.player

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "${player.name} #${player.jerseyNumber}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            StatRow(
                title = "Serve (0-4)",
                options = listOf("0", "1", "2", "3", "4"),
                counts = playerState.counters.serves,
                onTap = { onRecordStat(player.id, "SERVE", it) }
            )

            StatRow(
                title = "Serve Receive (0-3)",
                options = listOf("0", "1", "2", "3"),
                counts = playerState.counters.serveReceives,
                onTap = { onRecordStat(player.id, "SERVE_RECEIVE", it) }
            )

            StatRow(
                title = "Attack",
                options = listOf("KILL", "ATTEMPT", "ERROR"),
                counts = playerState.counters.attacks,
                onTap = { onRecordStat(player.id, "ATTACK", it) }
            )

            StatRow(
                title = "Set",
                options = listOf("ASSIST", "ATTEMPT", "ERROR"),
                counts = playerState.counters.sets,
                onTap = { onRecordStat(player.id, "SET", it) }
            )
        }
    }
}

@Composable
private fun TeamManagementScreen(
    teams: List<TeamRosterState>,
    players: List<PlayerEntity>,
    onBack: () -> Unit,
    onCreateTeam: (String, List<Long>) -> Unit,
    onCreatePlayer: (String, Int) -> Unit,
    onUpdateTeam: (Long, String, List<Long>) -> Unit,
    onDeleteTeam: (Long) -> Unit,
    onUpdatePlayer: (Long, String, Int) -> Unit,
    onDeletePlayer: (Long) -> Unit
) {
    val teamNameById = teams.associate { it.team.id to it.team.name }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Team & Player Manager", style = MaterialTheme.typography.headlineSmall)
                    Text("Create, edit, and assign players", style = MaterialTheme.typography.bodyMedium)
                }
                Button(onClick = onBack) {
                    Text("Back to Stats")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AddPlayerCard(onCreatePlayer = onCreatePlayer)
            }

            item {
                AddTeamCard(
                    players = players,
                    teamNameById = teamNameById,
                    onCreateTeam = onCreateTeam
                )
            }

            item {
                Text("Teams", style = MaterialTheme.typography.titleLarge)
            }

            if (teams.isEmpty()) {
                item {
                    Text("No teams yet.")
                }
            }

            itemsIndexed(teams, key = { index, roster -> "team-${roster.team.id}-$index" }) { _, roster ->
                TeamCard(
                    roster = roster,
                    players = players,
                    teamNameById = teamNameById,
                    onUpdateTeam = onUpdateTeam,
                    onDeleteTeam = onDeleteTeam
                )
            }

            item {
                Text("Players", style = MaterialTheme.typography.titleLarge)
            }

            itemsIndexed(
                players.sortedWith(compareBy(PlayerEntity::jerseyNumber, PlayerEntity::name)),
                key = { index, player -> "player-${player.id}-$index" }
            ) { _, player ->
                PlayerManagementCard(
                    player = player,
                    teamName = player.teamId?.let { teamNameById[it] },
                    onUpdatePlayer = onUpdatePlayer,
                    onDeletePlayer = onDeletePlayer
                )
            }
        }
    }
}

@Composable
private fun AddTeamCard(
    players: List<PlayerEntity>,
    teamNameById: Map<Long, String>,
    onCreateTeam: (String, List<Long>) -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    var selectedPlayerIds by remember { mutableStateOf(setOf<Long>()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Add Team", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = teamName,
                onValueChange = { teamName = it },
                label = { Text("Team name") },
                singleLine = true
            )
            Text("Select players to assign", style = MaterialTheme.typography.titleMedium)
            PlayerSelector(
                players = players,
                selectedPlayerIds = selectedPlayerIds,
                teamNameById = teamNameById,
                onToggle = { playerId ->
                    selectedPlayerIds = if (selectedPlayerIds.contains(playerId)) {
                        selectedPlayerIds - playerId
                    } else {
                        selectedPlayerIds + playerId
                    }
                }
            )
            Text(
                "If a selected player is already on another team, they will be moved.",
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = {
                val trimmedName = teamName.trim()
                if (trimmedName.isNotEmpty()) {
                    onCreateTeam(trimmedName, selectedPlayerIds.toList())
                    teamName = ""
                    selectedPlayerIds = emptySet()
                }
            }) {
                Text("Create Team")
            }
        }
    }
}

@Composable
private fun TeamCard(
    roster: TeamRosterState,
    players: List<PlayerEntity>,
    teamNameById: Map<Long, String>,
    onUpdateTeam: (Long, String, List<Long>) -> Unit,
    onDeleteTeam: (Long) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditTeamDialog(
            roster = roster,
            players = players,
            teamNameById = teamNameById,
            onSave = { teamId, name, playerIds ->
                onUpdateTeam(teamId, name, playerIds)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete Team?",
            message = "This will delete ${roster.team.name}. ${roster.players.size} player(s) will become unassigned.",
            onConfirm = {
                showDeleteDialog = false
                onDeleteTeam(roster.team.id)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(roster.team.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showEditDialog = true }) { Text("Edit") }
                    TextButton(onClick = { showDeleteDialog = true }) { Text("Delete") }
                }
            }

            if (roster.players.isEmpty()) {
                Text("No players assigned.")
            } else {
                roster.players.forEach { player ->
                    Text("${player.name} #${player.jerseyNumber}")
                }
            }
        }
    }
}

@Composable
private fun EditTeamDialog(
    roster: TeamRosterState,
    players: List<PlayerEntity>,
    teamNameById: Map<Long, String>,
    onSave: (Long, String, List<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    var teamName by remember(roster.team.id) { mutableStateOf(roster.team.name) }
    var selectedPlayerIds by remember(roster.team.id) {
        mutableStateOf(roster.players.map { it.id }.toSet())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Team") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Team name") },
                    singleLine = true
                )
                Text("Players", style = MaterialTheme.typography.titleMedium)
                PlayerSelector(
                    players = players,
                    selectedPlayerIds = selectedPlayerIds,
                    teamNameById = teamNameById,
                    onToggle = { playerId ->
                        selectedPlayerIds = if (selectedPlayerIds.contains(playerId)) {
                            selectedPlayerIds - playerId
                        } else {
                            selectedPlayerIds + playerId
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(roster.team.id, teamName.trim(), selectedPlayerIds.toList())
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerSelector(
    players: List<PlayerEntity>,
    selectedPlayerIds: Set<Long>,
    teamNameById: Map<Long, String>,
    onToggle: (Long) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        players.sortedWith(compareBy(PlayerEntity::jerseyNumber, PlayerEntity::name)).forEach { player ->
            val isSelected = selectedPlayerIds.contains(player.id)
            val assignedTeamName = player.teamId?.let { teamNameById[it] }
            val suffix = if (assignedTeamName != null) " ($assignedTeamName)" else ""
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle(player.id) }
                )
                Text("${player.name} #${player.jerseyNumber}$suffix")
            }
        }
    }
}

@Composable
private fun PlayerManagementCard(
    player: PlayerEntity,
    teamName: String?,
    onUpdatePlayer: (Long, String, Int) -> Unit,
    onDeletePlayer: (Long) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditPlayerDialog(
            player = player,
            onSave = { name, jersey ->
                onUpdatePlayer(player.id, name, jersey)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete Player?",
            message = "This will delete ${player.name} #${player.jerseyNumber} and their stats.",
            onConfirm = {
                showDeleteDialog = false
                onDeletePlayer(player.id)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${player.name} #${player.jerseyNumber}", fontWeight = FontWeight.Bold)
                Text("Team: ${teamName ?: "Unassigned"}", style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { showEditDialog = true }) {
                    Text("Edit")
                }
                TextButton(onClick = { showDeleteDialog = true }) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun EditPlayerDialog(
    player: PlayerEntity,
    onSave: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(player.id) { mutableStateOf(player.name) }
    var jerseyText by remember(player.id) { mutableStateOf(player.jerseyNumber.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Player") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Player name") },
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = jerseyText,
                    onValueChange = { jerseyText = it.filter(Char::isDigit) },
                    label = { Text("Jersey number") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val jersey = jerseyText.toIntOrNull()
                val trimmed = name.trim()
                if (trimmed.isNotEmpty() && jersey != null) {
                    onSave(trimmed, jersey)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatRow(
    title: String,
    options: List<String>,
    counts: Map<String, Int>,
    onTap: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val count = counts[option] ?: 0
                Button(onClick = { onTap(option) }) {
                    Text("$option ($count)")
                }
            }
        }
    }
}

@Composable
private fun EventFeedCard(state: StatsUiState, players: List<PlayerEntity>) {
    val playerLookup = players.associateBy { it.id }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Recent Events", style = MaterialTheme.typography.titleLarge)
            if (state.filteredEvents.isEmpty()) {
                Text("No events for current filter.")
                return@Column
            }

            state.filteredEvents.take(20).forEach { event ->
                val player = playerLookup[event.playerId]
                val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(Date(event.createdAt))
                Text(
                    text = "${player?.name ?: "Unknown"} #${player?.jerseyNumber ?: "?"} | ${event.skill} ${event.outcome} | Set ${event.setNumber} | $timestamp",
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatCaptureScreenPreview() {
    StatsAppTheme {
        StatCaptureScreen(
            state = StatsUiState(),
            onRecordStat = { _, _, _ -> },
            onSelectMatch = {},
            onSelectSet = {},
            onCreateMatch = { _, _, _ -> },
            onUpdateMatch = { _, _, _, _ -> },
            onDeleteSelectedMatch = {},
            onDeleteSelectedSet = {},
            onSaveSetLineup = {},
            onOpenTeamManager = {},
            onClearExportMessage = {},
            onExportCsv = {}
        )
    }
}
