package com.example.alfabetica.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = md_primary,
    onPrimary = md_onPrimary,
    secondary = md_secondary,
    onSecondary = md_onSecondary,
    tertiary = md_tertiary,
    onTertiary = md_onTertiary,
    background = md_background,
    onBackground = md_onBackground,
    surface = md_surface,
    onSurface = md_onSurface,
    surfaceVariant = md_surfaceVariant,
    outline = md_outline
)

@Composable
fun AlfabeticaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography  = Typography, // deixa seu Type.kt como está
        content     = content
    )
}
