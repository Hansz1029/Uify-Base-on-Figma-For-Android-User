package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun UinifyTheme(
    accentColor: Long = 0xFF0D99FF,
    content: @Composable () -> Unit
) {
    val dynamicColors = DarkColorScheme.copy(
        primary = Color(accentColor)
    )

    MaterialTheme(
        colorScheme = dynamicColors,
        typography = Typography,
        content = content
    )
}
