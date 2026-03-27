package com.planktracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.planktracker.data.PlankRecord
import com.planktracker.ui.theme.PlankGreen
import com.planktracker.ui.theme.PlankRed
import com.planktracker.viewmodel.PlankViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(vm: PlankViewModel) {
    val state by vm.uiState.collectAsState()
    val records = state.allRecords

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text(
                "History",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "${records.size} plank${if (records.size != 1) "s" else ""} logged",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (records.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No planks yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Complete your first plank to see history",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    PlankHistoryItem(record = record)
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun PlankHistoryItem(record: PlankRecord) {
    val date = LocalDate.parse(record.date)
    val dateStr = date.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))
    val metTarget = record.metTarget

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Icon(
                imageVector = if (metTarget) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (metTarget) PlankGreen else PlankRed,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.width(12.dp))

            // Date and info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Target: ${formatTime(record.targetSeconds)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Duration
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTime(record.durationSeconds),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (metTarget) PlankGreen else MaterialTheme.colorScheme.onSurface
                )
                if (metTarget) {
                    val extra = record.durationSeconds - record.targetSeconds
                    if (extra > 0) {
                        Text(
                            text = "+${extra}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PlankGreen
                        )
                    }
                } else {
                    val short = record.targetSeconds - record.durationSeconds
                    Text(
                        text = "-${short}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PlankRed
                    )
                }
            }
        }
    }
}
