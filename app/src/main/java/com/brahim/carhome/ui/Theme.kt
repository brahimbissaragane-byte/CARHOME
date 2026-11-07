package com.brahim.carhome.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object CarColors {
    val Blue = Color(0xFF1E88E5)
    val Amber = Color(0xFFFFB300)
    val Green = Color(0xFF43A047)
    val Gray = Color(0xFF616161)
    val Red = Color(0xFFE53935)
}

private val scheme = darkColorScheme(
    primary = CarColors.Blue,
    onPrimary = Color.White,
    background = Color(0xFF000000),
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFE0E0E0),
)

@Composable
fun CarHomeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
