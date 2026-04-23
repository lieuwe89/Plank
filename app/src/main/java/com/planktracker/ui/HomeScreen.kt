package com.planktracker.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planktracker.ui.theme.*
import com.planktracker.viewmodel.PlankViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(vm: PlankViewModel, onStartPlank: () -> Unit) {
    val state by vm.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val todayDone = state.todayRecord != null
    val todayDuration = state.todayRecord?.durationSeconds ?: 0
    val target = state.todayTarget
    val progress = if (todayDone) (todayDuration.toFloat() / target).coerceIn(0f, 1f) else 0f

    val now = LocalDate.now()
    val dateString = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())).uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = dateString,
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp,
                    color = PaperInk2,
                    letterSpacing = 0.1.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Plank",
                    fontFamily = InstrumentSerif,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 28.sp,
                    color = PaperInk,
                    lineHeight = 32.sp
                )
            }
            if (todayDone) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(PaperAccent, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "DONE",
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        color = PaperAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.08.sp
                    )
                }
            }
        }

        HorizontalDivider(thickness = 2.dp, color = PaperInk, modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp))

        // Hero Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Arc
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(128.dp)
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
                    label = "progress"
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 2.dp.toPx()
                    val bgStrokeWidth = 1.5.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                    val arcSize = Size(diameter, diameter)

                    drawArc(
                        color = PaperLine,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(bgStrokeWidth, cap = StrokeCap.Round)
                    )
                    if (animatedProgress > 0f) {
                        drawArc(
                            color = PaperAccent,
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
                        text = if (todayDone) "TIME" else "TARGET",
                        fontFamily = CormorantGaramond,
                        fontSize = 13.sp,
                        color = PaperInk3,
                        letterSpacing = 0.04.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (todayDone) todayDuration.toString() else target.toString(),
                            fontFamily = InstrumentSerif,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 38.sp,
                            color = PaperInk,
                            lineHeight = 38.sp
                        )
                        Text(
                            text = "s",
                            fontFamily = InstrumentSerif,
                            fontSize = 18.sp,
                            color = PaperInk,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(20.dp))

            // Right side info
            Column(modifier = Modifier.weight(1f).padding(bottom = 6.dp)) {
                if (todayDone) {
                    Text(
                        text = "Goal\nreached.",
                        fontFamily = InstrumentSerif,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 22.sp,
                        color = PaperInk,
                        lineHeight = 26.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Box(modifier = Modifier.width(20.dp).height(1.dp).background(PaperAccent))
                        Spacer(Modifier.width(6.dp))
                        val extra = todayDuration - target
                        Text(
                            text = if (extra >= 0) "+${extra}S OVER TARGET" else "${-extra}S UNDER TARGET",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = if (extra >= 0) PaperAccent else PaperDanger,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1.sp
                        )
                    }
                } else {
                    Text(
                        text = "Ready\nto plank.",
                        fontFamily = InstrumentSerif,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 22.sp,
                        color = PaperInk,
                        lineHeight = 26.sp
                    )
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "TODAY'S GOAL",
                            fontFamily = PlusJakartaSans,
                            fontSize = 10.sp,
                            color = PaperInk2,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.08.sp
                        )
                        Text(
                            text = "${target}s",
                            fontFamily = CormorantGaramond,
                            fontSize = 20.sp,
                            color = PaperInk,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Progress Bar
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
                label = "bar_progress"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(PaperLine)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animatedProgress)
                        .height(2.dp)
                        .background(PaperAccent)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0",
                    fontFamily = PlusJakartaSans,
                    fontSize = 9.sp,
                    color = PaperInk3,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.08.sp
                )
                Text(
                    text = "${target}s goal",
                    fontFamily = PlusJakartaSans,
                    fontSize = 9.sp,
                    color = PaperInk3,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.08.sp
                )
            }
        }

        // CTA
        Box(modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            if (!todayDone) {
                Button(
                    onClick = onStartPlank,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PaperInk, contentColor = PaperBg)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "START PLANK",
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.12.sp
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onStartPlank,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PaperInk2),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PaperLine)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "REDO PLANK",
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.12.sp
                    )
                }
            }
        }

        // Stats Table
        HorizontalDivider(thickness = 2.dp, color = PaperInk, modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp))
        Column(modifier = Modifier.padding(horizontal = 22.dp)) {
            Text(
                text = "YOUR STATS",
                fontFamily = PlusJakartaSans,
                fontSize = 9.sp,
                color = PaperInk2,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            StatRow("Day streak", "${state.streak} day${if(state.streak != 1) "s" else ""}", "🔥")
            HorizontalDivider(thickness = 1.dp, color = PaperLine)
            StatRow("Personal best", "${state.bestDuration}s", null)
            HorizontalDivider(thickness = 1.dp, color = PaperLine)
            StatRow("Total planks", "${state.totalPlanks}", null)
        }

        // Tomorrow's Target
        Box(
            modifier = Modifier
                .padding(horizontal = 22.dp, vertical = 16.dp)
                .fillMaxWidth()
                .background(PaperBg2)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.width(3.dp).height(72.dp).background(PaperAccent))
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                    Text(
                        text = "TOMORROW'S TARGET",
                        fontFamily = PlusJakartaSans,
                        fontSize = 9.sp,
                        color = PaperInk2,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${state.todayTarget + state.dailyIncrement}",
                                fontFamily = InstrumentSerif,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 28.sp,
                                color = PaperInk
                            )
                            Text(
                                text = "s",
                                fontFamily = CormorantGaramond,
                                fontSize = 20.sp,
                                color = PaperInk,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Text(
                            text = "+${state.dailyIncrement}s daily",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = PaperAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.06.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, icon: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontSize = 13.sp,
            color = PaperInk2
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Text(
                    text = icon,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
            Text(
                text = value,
                fontFamily = CormorantGaramond,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = PaperInk
            )
        }
    }
}
