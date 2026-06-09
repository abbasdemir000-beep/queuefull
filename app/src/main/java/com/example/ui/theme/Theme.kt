package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkAutomotiveScheme = darkColorScheme(
    primary = CosmicAccent,
    onPrimary = Color.Black,
    primaryContainer = CosmicLightBlue,
    onPrimaryContainer = CosmicDarkBlue,
    secondary = CosmicAmber,
    onSecondary = Color.Black,
    background = CosmicBackground,
    onBackground = CosmicText,
    surface = CosmicSurface,
    onSurface = CosmicText,
    surfaceVariant = CosmicSecondaryBg,
    onSurfaceVariant = CosmicTextLight,
    outline = CosmicBorder,
    error = FuelRed,
    onError = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = DarkAutomotiveScheme, typography = Typography, content = content)
}
