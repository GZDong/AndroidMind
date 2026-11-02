package com.gzd.androidmind.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF315DA8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4EDFC),
    onPrimaryContainer = Color(0xFF234A87),
    secondary = Color(0xFF54647A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7ECF3),
    onSecondaryContainer = Color(0xFF29384C),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF202B3A),
    surface = Color(0xFFF5F7FA),
    onSurface = Color(0xFF202B3A),
    surfaceVariant = Color(0xFFE7ECF3),
    onSurfaceVariant = Color(0xFF596779),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFEEF2F7),
    surfaceContainerHigh = Color(0xFFE7ECF3),
    surfaceContainerHighest = Color(0xFFDFE6EF),
    outline = Color(0xFF7C899B),
    outlineVariant = Color(0xFFD9E1EB),
    surfaceTint = Color(0xFF315DA8),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C7FF),
    onPrimary = Color(0xFF123365),
    primaryContainer = Color(0xFF263E60),
    onPrimaryContainer = Color(0xFFD8E6FF),
    secondary = Color(0xFFB5C4D9),
    onSecondary = Color(0xFF253347),
    secondaryContainer = Color(0xFF303E52),
    onSecondaryContainer = Color(0xFFDCE6F5),
    background = Color(0xFF11161E),
    onBackground = Color(0xFFE1E7EF),
    surface = Color(0xFF11161E),
    onSurface = Color(0xFFE1E7EF),
    surfaceVariant = Color(0xFF303B4B),
    onSurfaceVariant = Color(0xFFB0BDCE),
    surfaceContainerLowest = Color(0xFF0D1219),
    surfaceContainerLow = Color(0xFF1B2330),
    surfaceContainer = Color(0xFF202A38),
    surfaceContainerHigh = Color(0xFF293444),
    surfaceContainerHighest = Color(0xFF333F50),
    outline = Color(0xFF8593A6),
    outlineVariant = Color(0xFF384658),
    surfaceTint = Color(0xFFA8C7FF),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(4.dp),
    extraLarge = RoundedCornerShape(4.dp),
)

@Composable
fun AndroidMindTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        shapes = AppShapes,
        content = content,
    )
}
