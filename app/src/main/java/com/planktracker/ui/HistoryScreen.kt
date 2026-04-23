package com.planktracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planktracker.data.PlankRecord
import com.planktracker.ui.theme.*
import com.planktracker.viewmodel.PlankViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(vm: PlankViewModel) {
    val state by vm.uiState.collectAsState()
    val records = state.allRecords.sortedByDescending { it.date }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
            Text(
                "ALL SESSIONS",
                fontFamily = PlusJakartaSans,
                fontSize = 10.sp,
                color = PaperInk2,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.1.sp
            )
            Text(
                "History",
                fontFamily = InstrumentSerif,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontSize = 32.sp,
                color = PaperInk,
                lineHeight = 40.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        HorizontalDivider(thickness = 2.dp, color = PaperInk, modifier = Modifier.padding(horizontal = 22.dp))

        if (records.isNotEmpty()) {
            val recentRecords = records.take(8).reversed()
            val maxDuration = recentRecords.maxOfOrNull { it.durationSeconds } ?: 1

            Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
                Text(
                    "LAST ${recentRecords.size} SESSIONS",
                    fontFamily = PlusJakartaSans,
                    fontSize = 9.sp,
                    color = PaperInk3,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.1.sp
                )
                
                // Sparkline Chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    recentRecords.forEach { record ->
                        val metTarget = record.metTarget
                        val heightFraction = (record.durationSeconds.toFloat() / maxDuration).coerceIn(0.1f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(heightFraction)
                                .padding(horizontal = 2.dp)
                                .background(if (metTarget) PaperAccent else PaperInk3.copy(alpha = 0.5f))
                        )
                    }
                }
                
                // Chart Labels
                if (recentRecords.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val firstDate = LocalDate.parse(recentRecords.first().date).format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())).lowercase()
                        val lastDate = LocalDate.parse(recentRecords.last().date).format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())).lowercase()
                        
                        Text(
                            firstDate,
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.sp,
                            color = PaperInk3
                        )
                        Text(
                            lastDate,
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.sp,
                            color = PaperInk3
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 2.dp, color = PaperInk, modifier = Modifier.padding(horizontal = 22.dp))

            // List
            Text(
                "${records.size} PLANKS LOGGED",
                fontFamily = PlusJakartaSans,
                fontSize = 9.sp,
                color = PaperInk2,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.12.sp,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 0.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    PlankHistoryItem(record = record)
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No planks yet",
                    fontFamily = InstrumentSerif,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 24.sp,
                    color = PaperInk3
                )
            }
        }
    }
}

@Composable
fun PlankHistoryItem(record: PlankRecord) {
    val date = LocalDate.parse(record.date)
    val dateStr = date.format(DateTimeFormatter.ofPattern("EE d MMM", Locale.getDefault())).lowercase()
    val metTarget = record.metTarget

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = dateStr,
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = PaperInk,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${record.durationSeconds}s",
                    fontFamily = CormorantGaramond,
                    fontSize = 18.sp,
                    color = PaperInk,
                    modifier = Modifier.padding(end = 6.dp)
                )
                val diff = record.durationSeconds - record.targetSeconds
                val diffText = if (diff >= 0) "+${diff}s" else "${diff}s"
                val diffColor = if (diff >= 0) PaperAccent else PaperDanger
                Text(
                    text = diffText,
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp,
                    color = diffColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
        
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(PaperLine))
            val progress = (record.durationSeconds.toFloat() / record.targetSeconds).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .height(2.dp)
                    .background(if (metTarget) PaperAccent else PaperInk3)
            )
        }
        
        Text(
            text = "Target: ${record.targetSeconds}s",
            fontFamily = PlusJakartaSans,
            fontSize = 9.sp,
            color = PaperInk3,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 1.dp, color = PaperLine)
    }
}
