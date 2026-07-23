package com.zyb.studyflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4338CA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF0F766E),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurface = Color(0xFF172033),
    onSurfaceVariant = Color(0xFF5B6475),
    outlineVariant = Color(0xFFDCE2EA),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    primaryContainer = Color(0xFF312E81),
    secondary = Color(0xFF5EEAD4),
    background = Color(0xFF101521),
    surface = Color(0xFF171E2C),
    surfaceVariant = Color(0xFF20293A),
)

@Composable
fun StudyFlowTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
