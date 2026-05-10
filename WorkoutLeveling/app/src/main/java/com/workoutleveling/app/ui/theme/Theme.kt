package com.workoutleveling.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GateColorScheme = darkColorScheme(
    primary = GateCyan,
    secondary = GateViolet,
    tertiary = GateViolet,
    background = GateBackground,
    surface = GateSurface,
    onBackground = GateOnDark,
    onSurface = GateOnDark,
)

@Composable
fun WorkoutLevelingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GateColorScheme,
        typography = Typography,
        content = content,
    )
}
