package com.aslam.healthmate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    secondary = WaterBlue,
    background = Color(0xFFF5F7F6),
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = TealDark,
    onPrimary = Color.Black,
    secondary = Color(0xFF64B5F6)
)

// darkMode comes from the switch in Settings, not from the phone setting
@Composable
fun HealthMateTheme(darkMode: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkMode) DarkColors else LightColors,
        content = content
    )
}
