package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// QueueFuel is dark-only by design: dark navy canvas, deep-teal primary.
private val QueueFuelDarkScheme = darkColorScheme(
    primary = QfDeepTeal,
    onPrimary = Color.White,
    primaryContainer = QfTealContainer,
    onPrimaryContainer = QfOnTealContainer,
    secondary = QfTurquoise,
    onSecondary = QfNavy,
    background = QfNavy,
    onBackground = QfTextPrimary,
    surface = QfSurface,
    onSurface = QfTextPrimary,
    surfaceVariant = QfSurfaceVariant,
    onSurfaceVariant = QfTextSecondary,
    outline = QfBorder,
    error = QfError,
    onError = Color.White
)

@Composable
fun QueueFuelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = QueueFuelDarkScheme, typography = Typography, content = content)
}
