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

private val DarkColorScheme = darkColorScheme(
    primary = IndustrialTertiaryFixed,
    onPrimary = IndustrialOnTertiaryFixed,
    secondary = IndustrialSecondary,
    onSecondary = IndustrialOnSecondary,
    tertiary = IndustrialTertiaryFixed,
    background = IndustrialPrimary,
    onBackground = IndustrialBackground,
    surface = IndustrialPrimary,
    onSurface = IndustrialBackground,
    surfaceVariant = IndustrialPrimaryContainer,
    onSurfaceVariant = IndustrialOnPrimaryContainer,
    outline = IndustrialOutline
)

private val LightColorScheme = lightColorScheme(
    primary = IndustrialPrimary,
    onPrimary = IndustrialOnPrimary,
    secondary = IndustrialSecondary,
    onSecondary = IndustrialOnSecondary,
    tertiary = IndustrialTertiary,
    onTertiary = IndustrialOnTertiary,
    background = IndustrialBackground,
    onBackground = IndustrialOnBackground,
    surface = IndustrialSurface,
    onSurface = IndustrialOnSurface,
    surfaceVariant = IndustrialSurfaceContainerHighest,
    onSurfaceVariant = IndustrialOnSurfaceVariant,
    outline = IndustrialOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to enforce our polished custom theme!
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
