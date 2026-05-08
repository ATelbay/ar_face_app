package com.arystan.arface.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = ARAccent,
    onPrimary = ARWhite,
    background = ARBlack,
    onBackground = ARWhite,
    surface = ARSurface,
    onSurface = ARWhite,
)

@Composable
fun ARFaceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = ARTypography,
        content = content,
    )
}
