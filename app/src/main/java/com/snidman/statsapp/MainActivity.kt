package com.snidman.statsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.snidman.statsapp.ui.theme.StatsAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StatEntry(
    val category: String,
    val rating: String,
    val timestamp: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StatsAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    StatCaptureScreen()
                }
            }
        }
    }
}

@Composable
private fun StatCaptureScreen() {
    val events = remember { mutableStateListOf<StatEntry>() }

    fun addStat(category: String, rating: String) {
        events.add(
            0,
            StatEntry(
                category = category,
                rating = rating,
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            )
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("Volleyball Stat Capture", style = MaterialTheme.typography.headlineSmall)
                Text("Tap a result after each play", style = MaterialTheme.typography.bodyMedium)
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
                StatCategoryCard(
                    title = "Serve",
                    subtitle = "Rate serve quality from 0-4",
                    options = listOf("0", "1", "2", "3", "4"),
                    onTap = { addStat("Serve", it) }
                )
            }
            item {
                StatCategoryCard(
                    title = "Serve Receive",
                    subtitle = "Rate pass quality from 0-3",
                    options = listOf("0", "1", "2", "3"),
                    onTap = { addStat("Serve Receive", it) }
                )
            }
            item {
                StatCategoryCard(
                    title = "Attack",
                    subtitle = "Select attack outcome",
                    options = listOf("Kill", "Attempt", "Error"),
                    onTap = { addStat("Attack", it) }
                )
            }
            item {
                StatCategoryCard(
                    title = "Set",
                    subtitle = "Select set outcome",
                    options = listOf("Assist", "Attempt", "Error"),
                    onTap = { addStat("Set", it) }
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Captured Events", style = MaterialTheme.typography.titleMedium)
                        Text("Total: ${events.size}", style = MaterialTheme.typography.bodyMedium)

                        if (events.isNotEmpty()) {
                            TextButton(onClick = { events.clear() }) {
                                Text("Clear Session")
                            }
                        }
                    }
                }
            }

            if (events.isEmpty()) {
                item {
                    Text(
                        "No stats captured yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                itemsIndexed(events) { _, event ->
                    EventRow(event)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatCategoryCard(
    title: String,
    subtitle: String,
    options: List<String>,
    onTap: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    Button(onClick = { onTap(option) }) {
                        Text(option)
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: StatEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                text = "${event.category}: ${event.rating}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = event.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Divider(modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatCaptureScreenPreview() {
    StatsAppTheme {
        StatCaptureScreen()
    }
}
