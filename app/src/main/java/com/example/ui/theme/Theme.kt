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

private val CleanMinimalColorScheme = lightColorScheme(
    primary = CosmicAccent,
    onPrimary = Color.White,
    primaryContainer = CosmicLightBlue,
    onPrimaryContainer = CosmicDarkBlue,
    background = CosmicBackground,
    onBackground = CosmicText,
    surface = CosmicSurface,
    onSurface = CosmicText,
    surfaceVariant = CosmicSecondaryBg,
    onSurfaceVariant = CosmicTextLight,
    outline = CosmicBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, // Set to false to enforce the strict Clean Minimalism branding
  content: @Composable () -> Unit,
) {
  // Enforce Clean Minimalism color scheme consistently
  val colorScheme = CleanMinimalColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

