package com.planktracker.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PlankGreen,
    onPrimary = Color.White,
    primaryContainer = PlankGreenDark,
    onPrimaryContainer = PlankGreenLight,
    secondary = PlankBlue,
    onSecondary = Color.White,
    secondaryContainer = PlankBlueDark,
    onSecondaryContainer = PlankBlueLight,
    tertiary = PlankOrange,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = OnDarkSurface,
    onSurface = OnDarkSurface,
    onSurfaceVariant = OnDarkSurfaceVariant,
    error = PlankRed,
)

@Composable
fun PlankTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
