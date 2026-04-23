package com.planktracker.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PaperAccent,
    onPrimary = Color.White,
    primaryContainer = PaperAccent,
    onPrimaryContainer = Color.White,
    secondary = PaperInk2,
    onSecondary = Color.White,
    background = PaperBg,
    surface = PaperBg,
    surfaceVariant = PaperBg2,
    onBackground = PaperInk,
    onSurface = PaperInk,
    onSurfaceVariant = PaperInk2,
    outline = PaperLine,
    outlineVariant = PaperLine,
    error = PaperDanger,
    onError = Color.White
)

@Composable
fun PlankTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
