package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val BgCanvasDark = Color(0xFF131317)
val BgPanelDark = Color(0xF0202027)
val BgInputDark = Color(0xFF141418)
val BorderSubtle = Color(0x26FFFFFF)

val FigmaBlue = Color(0xFF0D99FF)
val AccentTeal = Color(0xFF00D1B2)
val AccentYellow = Color(0xFFFFD166)
val AccentPurple = Color(0xFF7038F4)
val DangerRed = Color(0xFFEF4444)

val TextPrimary = Color(0xFFF8F9FA)
val TextSecondary = Color(0xFFB3B3B3)
val TextMuted = Color(0xFF757575)

val DarkColorScheme = darkColorScheme(
    primary = FigmaBlue,
    secondary = AccentTeal,
    tertiary = AccentPurple,
    background = BgCanvasDark,
    surface = BgPanelDark,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)
