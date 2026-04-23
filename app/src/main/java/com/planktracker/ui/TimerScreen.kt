package com.planktracker.ui

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planktracker.ui.theme.*
import com.planktracker.viewmodel.PlankViewModel
import com.planktracker.viewmodel.TimerState

@Composable
fun TimerScreen(vm: PlankViewModel, onBack: () -> Unit) {
    val state by vm.uiState.collectAsState()
    val elapsed = state.elapsedSeconds
    val target = state.todayTarget
    val progress = (elapsed.toFloat() / target).coerceIn(0f, 1.5f)
    val goalReached = elapsed >= target

    var showSaveDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Keep screen on while the timer is active (running or paused)
    val keepScreenOn = state.timerState == TimerState.RUNNING || state.timerState == TimerState.PAUSED
    val activity = LocalActivity.current
    DisposableEffect(keepScreenOn) {
        if (keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Navigate back after saving
    LaunchedEffect(state.timerState) {
        if (state.timerState == TimerState.IDLE && elapsed == 0 && showSaveDialog) {
            showSaveDialog = false
            onBack()
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    "Save Plank?",
                    fontFamily = InstrumentSerif,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 28.sp,
                    color = PaperInk
                )
            },
            text = {
                Column {
                    Text("Duration: ${formatTimerDisplay(elapsed)}", fontFamily = PlusJakartaSans, color = PaperInk2)
                    Text("Target: ${formatTimerDisplay(target)}", fontFamily = PlusJakartaSans, color = PaperInk2)
                    Spacer(Modifier.height(8.dp))
                    if (goalReached) {
                        Text("Goal reached!", color = PaperAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                    } else {
                        Text("${target - elapsed}s short of goal", color = PaperDanger, fontFamily = PlusJakartaSans)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.savePlank()
                    showSaveDialog = false
                    onBack()
                }) { Text("SAVE", color = PaperAccent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false; vm.resetTimer() }) {
                    Text("DISCARD", color = PaperInk3, fontFamily = PlusJakartaSans)
                }
            },
            containerColor = PaperBg,
            titleContentColor = PaperInk,
            textContentColor = PaperInk2
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    "Discard plank?",
                    fontFamily = InstrumentSerif,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 28.sp,
                    color = PaperInk
                )
            },
            text = { Text("Your current plank will not be saved.", fontFamily = PlusJakartaSans, color = PaperInk2) },
            confirmButton = {
                TextButton(onClick = { vm.resetTimer(); showDiscardDialog = false; onBack() }) {
                    Text("DISCARD", color = PaperDanger, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("CANCEL", color = PaperInk2, fontFamily = PlusJakartaSans)
                }
            },
            containerColor = PaperBg
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (state.timerState == TimerState.IDLE) {
                    onBack()
                } else {
                    showDiscardDialog = true
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PaperInk)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Timer",
                fontFamily = InstrumentSerif,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontSize = 24.sp,
                color = PaperInk
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(20.dp))

        // Timer circle
        TimerCircle(
            elapsed = elapsed,
            target = target,
            progress = progress,
            goalReached = goalReached,
            isRunning = state.timerState == TimerState.RUNNING
        )

        // Target info
        Box(modifier = Modifier.padding(vertical = 16.dp)) {
            if (goalReached) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(20.dp).height(1.dp).background(PaperAccent))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "GOAL REACHED",
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = PaperAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp
                    )
                }
            } else {
                Text(
                    text = "TARGET: ${target}s",
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp,
                    color = PaperInk2,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Control buttons
        Box(modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()) {
            when (state.timerState) {
                TimerState.IDLE -> {
                    Button(
                        onClick = { vm.startTimer() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PaperInk, contentColor = PaperBg)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "START",
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.12.sp
                        )
                    }
                }
                TimerState.RUNNING -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { vm.pauseTimer() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PaperInk),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, PaperLine)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "PAUSE",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.12.sp
                            )
                        }
                        Button(
                            onClick = { vm.stopTimer(); showSaveDialog = true },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PaperDanger, contentColor = PaperBg)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "STOP",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.12.sp
                            )
                        }
                    }
                }
                TimerState.PAUSED -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { vm.resumeTimer() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PaperInk, contentColor = PaperBg)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "RESUME",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.12.sp
                            )
                        }
                        Button(
                            onClick = { vm.stopTimer(); showSaveDialog = true },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PaperDanger, contentColor = PaperBg)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "STOP",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.12.sp
                            )
                        }
                    }
                }
                TimerState.STOPPED -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { vm.resetTimer() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PaperInk2),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, PaperLine)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "RESET",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.12.sp
                            )
                        }
                        Button(
                            onClick = { vm.savePlank(); onBack() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PaperAccent, contentColor = PaperBg)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "SAVE",
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerCircle(
    elapsed: Int,
    target: Int,
    progress: Float,
    goalReached: Boolean,
    isRunning: Boolean
) {
    val primaryColor = PaperAccent
    val surfaceVariant = PaperLine

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "timer_progress"
    )

    // Pulse animation when running
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val alpha by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(260.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            val bgStrokeWidth = 2.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = surfaceVariant,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(bgStrokeWidth, cap = StrokeCap.Round)
            )
            if (animatedProgress > 0f) {
                drawArc(
                    color = primaryColor.copy(alpha = alpha),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val textToDisplay = formatTimerDisplay(elapsed)
            Text(
                text = textToDisplay,
                fontSize = 72.sp,
                fontFamily = InstrumentSerif,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = PaperInk,
                lineHeight = 72.sp
            )
        }
    }
}

fun formatTimerDisplay(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}
