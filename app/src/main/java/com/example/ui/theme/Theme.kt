package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UltronColorScheme = darkColorScheme(
    primary = UltronNeonCyan,
    onPrimary = Color.Black,
    primaryContainer = UltronCyanDim,
    onPrimaryContainer = Color.White,
    secondary = UltronCrimson,
    onSecondary = Color.White,
    secondaryContainer = UltronCrimsonDim,
    onSecondaryContainer = Color.White,
    tertiary = UltronTitanium,
    background = UltronBackground,
    onBackground = UltronTitanium,
    surface = UltronSurface,
    onSurface = UltronTitanium,
    surfaceVariant = UltronSurfaceVariant,
    onSurfaceVariant = UltronTextMuted,
    outline = UltronCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = UltronColorScheme,
        typography = Typography,
        content = content
    )
}

