package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DuskDarkAccent,
    onPrimary = DuskDarkAccentInk,
    primaryContainer = DuskDarkSoft,
    onPrimaryContainer = DuskDarkInk,
    secondary = DuskDarkMute,
    onSecondary = DuskDarkInk,
    tertiary = DuskDarkMoon,
    onTertiary = DuskDarkAccentInk,
    background = DuskDarkBg,
    onBackground = DuskDarkInk,
    surface = DuskDarkSurface,
    onSurface = DuskDarkInk,
    surfaceVariant = DuskDarkSoft,
    onSurfaceVariant = DuskDarkMute,
    outline = DuskDarkLine
)

private val LightColorScheme = lightColorScheme(
    primary = DuskLightAccent,
    onPrimary = DuskLightAccentInk,
    primaryContainer = DuskLightSoft,
    onPrimaryContainer = DuskLightInk,
    secondary = DuskLightMute,
    onSecondary = DuskLightInk,
    tertiary = DuskLightMoon,
    onTertiary = DuskLightAccentInk,
    background = DuskLightBg,
    onBackground = DuskLightInk,
    surface = DuskLightSurface,
    onSurface = DuskLightInk,
    surfaceVariant = DuskLightSoft,
    onSurfaceVariant = DuskLightMute,
    outline = DuskLightLine
)

@Composable
fun DuskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
