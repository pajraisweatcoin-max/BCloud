package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SleekColorScheme = lightColorScheme(
    primary = M3PurplePrimary,
    onPrimary = SleekSurface,
    primaryContainer = M3PurplePrimaryContainer,
    onPrimaryContainer = M3PurpleOnPrimaryContainer,
    secondary = M3PurpleSecondaryContainer,
    onSecondary = M3PurpleOnSecondaryContainer,
    background = SleekBackground,
    onBackground = TextPrimary,
    surface = SleekSurface,
    onSurface = TextPrimary,
    surfaceVariant = SleekSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SleekBorder,
    error = RoseError,
    onError = SleekSurface
)

@Composable
fun BarraCloudTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SleekColorScheme,
        typography = Typography,
        content = content
    )
}

