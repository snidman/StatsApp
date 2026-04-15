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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                            onDeleteSelectedMatch = viewModel::deleteSelectedMatch,
                            onDeleteSelectedSet = viewModel::deleteSelectedSet,
                            onCreatePlayer = viewModel::addPlayer,
                            onDeletePlayer = viewModel::deletePlayer,
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
    onCreateMatch: (String) -> Unit,
    onDeleteSelectedMatch: () -> Unit,
    onDeleteSelectedSet: () -> Unit,
    onCreatePlayer: (String, Int) -> Unit,
    onDeletePlayer: (Long) -> Unit,
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
                FilterAndActionsCard(
                    matches = state.matches,
                    selectedMatchId = state.selectedMatchId,
                    selectedSet = state.selectedSet,
                    totalEvents = state.filteredEvents.size,
                    selectedMatchDeleteCount = state.selectedMatchDeleteEventCount,
                    selectedSetDeleteCount = state.selectedSetDeleteEventCount,
                    onSelectMatch = onSelectMatch,
                    onSelectSet = onSelectSet,
                    onCreateMatch = onCreateMatch,
                    onDeleteSelectedMatch = onDeleteSelectedMatch,
                    onDeleteSelectedSet = onDeleteSelectedSet,
                    onExportCsv = onExportCsv
                )
            }

            item {
                AddPlayerCard(onCreatePlayer = onCreatePlayer)
            }

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
                            Text("Manage")
                        }
                    }
                }
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
                    playerDeleteEventCount = state.playerDeleteEventCounts[playerState.player.id] ?: 0,
                    onRecordStat = onRecordStat,
                    onDeletePlayer = onDeletePlayer
                )
            }

            if (state.players.isEmpty()) {
                item {
                    Text("No players yet. Add one above.")
                }
            }

            item {
                EventFeedCard(
                    state = state,
                    players = state.players.map { it.player }
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
    onSelectMatch: (Long) -> Unit,
    onSelectSet: (Int?) -> Unit,
    onCreateMatch: (String) -> Unit,
    onDeleteSelectedMatch: () -> Unit,
    onDeleteSelectedSet: () -> Unit,
    onExportCsv: () -> Unit
) {
    var matchName by remember { mutableStateOf("") }
    var showDeleteMatchDialog by remember { mutableStateOf(false) }
    var showDeleteSetDialog by remember { mutableStateOf(false) }
    val selectedMatchName = matches.firstOrNull { it.id == selectedMatchId }?.name ?: "this match"

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
                        Text(if (selected) "${match.name} *" else match.name)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = matchName,
                    onValueChange = { matchName = it },
                    label = { Text("New match name") },
                    singleLine = true
                )
                Button(onClick = {
                    val trimmed = matchName.trim()
                    if (trimmed.isNotEmpty()) {
                        onCreateMatch(trimmed)
                        matchName = ""
                    }
                }) {
                    Text("Add")
                }
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
    playerDeleteEventCount: Int,
    onRecordStat: (Long, String, String) -> Unit,
    onDeletePlayer: (Long) -> Unit
) {
    val player = playerState.player
    var showDeletePlayerDialog by remember { mutableStateOf(false) }

    if (showDeletePlayerDialog) {
        ConfirmDeleteDialog(
            title = "Delete Player?",
            message = "This will permanently remove ${player.name} and $playerDeleteEventCount event(s).",
            onConfirm = {
                showDeletePlayerDialog = false
                onDeletePlayer(player.id)
            },
            onDismiss = { showDeletePlayerDialog = false }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${player.name} #${player.jerseyNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { showDeletePlayerDialog = true }) {
                    Text("Delete Player")
                }
            }

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

            items(teams, key = { it.team.id }) { roster ->
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

            items(players.sortedWith(compareBy(PlayerEntity::jerseyNumber, PlayerEntity::name)), key = { it.id }) { player ->
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
            onCreateMatch = {},
            onDeleteSelectedMatch = {},
            onDeleteSelectedSet = {},
            onCreatePlayer = { _, _ -> },
            onDeletePlayer = {},
            onOpenTeamManager = {},
            onClearExportMessage = {},
            onExportCsv = {}
        )
    }
}
