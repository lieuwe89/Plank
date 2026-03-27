package com.planktracker.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planktracker.ui.theme.PlankGreen
import com.planktracker.ui.theme.PlankOrange
import com.planktracker.ui.theme.PlankRed
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
            title = { Text("Save Plank?") },
            text = {
                Column {
                    Text("Duration: ${formatTimerDisplay(elapsed)}")
                    Text("Target: ${formatTimerDisplay(target)}")
                    Spacer(Modifier.height(8.dp))
                    if (goalReached) {
                        Text("Goal reached!", color = PlankGreen, fontWeight = FontWeight.Bold)
                    } else {
                        Text("${target - elapsed}s short of goal", color = PlankOrange)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.savePlank()
                    showSaveDialog = false
                    onBack()
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false; vm.resetTimer() }) {
                    Text("Discard")
                }
            }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard plank?") },
            text = { Text("Your current plank will not be saved.") },
            confirmButton = {
                Button(
                    onClick = { vm.resetTimer(); showDiscardDialog = false; onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = PlankRed)
                ) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Daily Plank",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
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
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    tint = if (goalReached) PlankGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Target: ${formatTimerDisplay(target)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (goalReached) PlankGreen else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (goalReached) FontWeight.Bold else FontWeight.Normal
                )
                if (goalReached) {
                    Text("Reached!", color = PlankGreen, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Control buttons
        when (state.timerState) {
            TimerState.IDLE -> {
                Button(
                    onClick = { vm.startTimer() },
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 8.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PlankGreen)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start",
                        modifier = Modifier.size(36.dp))
                }
            }
            TimerState.RUNNING -> {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Pause
                    OutlinedButton(
                        onClick = { vm.pauseTimer() },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                    }
                    // Stop & save
                    Button(
                        onClick = { vm.stopTimer(); showSaveDialog = true },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PlankRed)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop")
                    }
                }
            }
            TimerState.PAUSED -> {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Resume
                    Button(
                        onClick = { vm.resumeTimer() },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PlankGreen)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                    }
                    // Stop & save
                    Button(
                        onClick = { vm.stopTimer(); showSaveDialog = true },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PlankRed)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop")
                    }
                }
            }
            TimerState.STOPPED -> {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { vm.resetTimer() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reset")
                    }
                    Button(
                        onClick = { vm.savePlank(); onBack() },
                        colors = ButtonDefaults.buttonColors(containerColor = PlankGreen)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
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
    val primaryColor = if (goalReached) PlankGreen else MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "timer_progress"
    )

    // Pulse animation when running
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val alpha by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 0.6f else 1f,
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
            val strokeWidth = 20.dp.toPx()
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
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
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
            Text(
                text = formatTimerDisplay(elapsed),
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (goalReached) PlankGreen else MaterialTheme.colorScheme.onBackground
            )
            if (elapsed > 0 && !goalReached) {
                Text(
                    text = "${target - elapsed}s to go",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun formatTimerDisplay(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
