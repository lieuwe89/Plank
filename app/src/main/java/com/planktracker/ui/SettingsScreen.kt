package com.planktracker.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planktracker.ui.theme.*
import com.planktracker.viewmodel.PlankViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: PlankViewModel) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showResetDialog by remember { mutableStateOf(false) }
    var baseTargetText by remember(state.baseTarget) { mutableStateOf(state.baseTarget.toString()) }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.setReminderEnabled(true)
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    "Reset All Progress?",
                    fontFamily = InstrumentSerif,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 28.sp,
                    color = PaperInk
                )
            },
            text = {
                Text(
                    "This will delete all your plank history and reset your streak. This cannot be undone.",
                    fontFamily = PlusJakartaSans,
                    color = PaperInk2
                )
            },
            confirmButton = {
                TextButton(onClick = { vm.resetProgress(); showResetDialog = false }) {
                    Text("RESET", color = PaperDanger, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL", color = PaperInk3, fontFamily = PlusJakartaSans)
                }
            },
            containerColor = PaperBg
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
            Text(
                "PREFERENCES",
                fontFamily = PlusJakartaSans,
                fontSize = 10.sp,
                color = PaperInk2,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.1.sp
            )
            Text(
                "Settings",
                fontFamily = InstrumentSerif,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontSize = 32.sp,
                color = PaperInk,
                lineHeight = 40.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // --- Daily Reminder ---
        SettingsSectionHeader("DAILY REMINDER")
        
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) {
            // Enable toggle
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enable reminder", fontFamily = PlusJakartaSans, fontSize = 16.sp, color = PaperInk)
                    Text("Get notified daily", fontFamily = PlusJakartaSans, fontSize = 13.sp, color = PaperInk3)
                }
                
                // Square-ish switch styling
                Switch(
                    checked = state.reminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val granted = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                                    PackageManager.PERMISSION_GRANTED
                            if (!granted) {
                                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@Switch
                            }
                        }
                        vm.setReminderEnabled(enabled)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PaperBg,
                        checkedTrackColor = PaperAccent,
                        uncheckedThumbColor = PaperBg,
                        uncheckedTrackColor = PaperLine,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
            
            HorizontalDivider(thickness = 1.dp, color = PaperLine)

            // Reminder Time
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reminder time", fontFamily = PlusJakartaSans, fontSize = 16.sp, color = PaperInk)
                    Text(
                        formatReminderTime(state.reminderHour, state.reminderMinute),
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = PaperInk3
                    )
                }
                if (state.reminderEnabled) {
                    Text(
                        "Change",
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = PaperAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> vm.setReminderTime(hour, minute) },
                                state.reminderHour,
                                state.reminderMinute,
                                true
                            ).show()
                        }.padding(8.dp)
                    )
                }
            }
        }

        // --- Progression ---
        SettingsSectionHeader("PROGRESSION")
        
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) {
            // Daily increase
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daily increase", fontFamily = PlusJakartaSans, fontSize = 16.sp, color = PaperInk, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.background(PaperInk).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("+${state.dailyIncrement}s", fontFamily = PlusJakartaSans, fontSize = 12.sp, color = PaperBg)
                }
            }
            
            Slider(
                value = state.dailyIncrement.toFloat(),
                onValueChange = { vm.setDailyIncrement(it.toInt()) },
                valueRange = 1f..30f,
                steps = 28,
                colors = SliderDefaults.colors(
                    thumbColor = PaperAccent,
                    activeTrackColor = PaperLine,
                    inactiveTrackColor = PaperLine
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1s", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = PaperInk3)
                Text("30s", fontFamily = PlusJakartaSans, fontSize = 11.sp, color = PaperInk3)
            }
            
            HorizontalDivider(thickness = 1.dp, color = PaperLine, modifier = Modifier.padding(vertical = 16.dp))

            // Starting target
            Text("Starting target", fontFamily = PlusJakartaSans, fontSize = 16.sp, color = PaperInk, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = baseTargetText,
                onValueChange = { v ->
                    baseTargetText = v
                    v.toIntOrNull()?.let { if (it in 5..300) vm.setBaseTarget(it) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = LocalTextStyle.current.copy(fontFamily = CormorantGaramond, fontSize = 20.sp, color = PaperInk),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PaperBg2,
                    unfocusedContainerColor = PaperBg2,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp)
            )
        }

        // --- Overview ---
        SettingsSectionHeader("OVERVIEW")
        
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) {
            SettingsInfoRow("Today's target", "${state.todayTarget}s")
            HorizontalDivider(thickness = 1.dp, color = PaperLine)
            SettingsInfoRow("Daily increment", "+${state.dailyIncrement}s / day")
            HorizontalDivider(thickness = 1.dp, color = PaperLine)
            SettingsInfoRow("Current streak", "${state.streak} day${if(state.streak != 1) "s" else ""}")
            HorizontalDivider(thickness = 1.dp, color = PaperLine)
            SettingsInfoRow("Total planks", "${state.totalPlanks}")
        }

        // --- Data ---
        SettingsSectionHeader("DATA")
        
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 32.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Export data", fontFamily = PlusJakartaSans, fontSize = 16.sp, color = PaperInk)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PaperInk3)
            }
            HorizontalDivider(thickness = 1.dp, color = PaperLine)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).clickable { showResetDialog = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reset all data", fontFamily = PlusJakartaSans, fontSize = 16.sp, color = PaperDanger)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PaperInk3)
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    HorizontalDivider(thickness = 2.dp, color = PaperInk, modifier = Modifier.padding(horizontal = 22.dp))
    Text(
        title,
        fontFamily = PlusJakartaSans,
        fontSize = 9.sp,
        color = PaperInk2,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.12.sp,
        modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)
    )
}

@Composable
fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = PlusJakartaSans, fontSize = 14.sp, color = PaperInk2)
        Text(value, fontFamily = CormorantGaramond, fontSize = 18.sp, color = PaperInk)
    }
}

fun formatReminderTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = if (hour % 12 == 0) 12 else hour % 12
    return "%d:%02d %s".format(h, minute, amPm)
}
