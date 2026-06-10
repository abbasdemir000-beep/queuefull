package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// QueueFuel Design System — "One application for all queues"
// Source of truth: docs/DESIGN_SYSTEM.md (master branding spec)
// Dark navy canvas, deep-teal primary, turquoise secondary. No other hues
// except the four semantic queue-status colors below.
// ============================================================================

// --- Brand ---
val QfDeepTeal = Color(0xFF0D9488)        // Primary — actions, selection, brand pin
val QfTurquoise = Color(0xFF2DD4BF)       // Secondary — highlights, glow, accents
val QfTealContainer = Color(0xFF11403B)   // Filled container tint of primary
val QfOnTealContainer = Color(0xFF5EEAD4) // Content on QfTealContainer

// --- Canvas layers ---
val QfNavy = Color(0xFF0B1622)            // Background — dark navy
val QfSurface = Color(0xFF152535)         // Cards / sheets — dark blue
val QfSurfaceVariant = Color(0xFF1C2F42)  // Inputs, chips, secondary panels
val QfBorder = Color(0xFF24405A)          // Dividers / card borders

// --- Text hierarchy ---
val QfTextPrimary = Color(0xFFFFFFFF)     // Primary — white
val QfTextSecondary = Color(0xFFB6C2CE)   // Secondary — light gray
val QfTextTertiary = Color(0xFF7C8B99)    // Muted captions

// --- Semantic / queue status ---
val QfSuccess = Color(0xFF22C55E)         // Empty queue — green
val QfSuccessLight = Color(0xFF4ADE80)    // Short queue — light green
val QfWarning = Color(0xFFF59E0B)         // Medium queue / warnings — orange
val QfError = Color(0xFFEF4444)           // Crowded queue / errors — red
val QfClosed = Color(0xFF6B7280)          // Closed — gray
val QfGold = Color(0xFFF59E0B)            // Points, badges, trophies (= warning hue)

// ============================================================================
// Legacy aliases — older composables reference these names; they must always
// resolve to the canonical tokens above so the whole app stays on-palette.
// ============================================================================
val FuelGreen = QfSuccess
val FuelShortGreen = QfSuccessLight
val FuelYellow = QfWarning
val FuelRed = QfError
val FuelClosed = QfClosed

val CosmicBackground = QfNavy
val CosmicSurface = QfSurface
val CosmicSecondaryBg = QfSurfaceVariant
val CosmicBorder = QfBorder

val CosmicAccent = QfDeepTeal
val CosmicAmber = QfGold
val CosmicTeal = QfDeepTeal

val CosmicText = QfTextPrimary
val CosmicTextLight = QfTextSecondary
val CosmicTextGray = QfTextTertiary

val CosmicLightBlue = QfTealContainer
val CosmicDarkBlue = QfOnTealContainer
