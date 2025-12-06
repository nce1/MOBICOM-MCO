package com.mobdeve.s18.group5.bayanihanspots.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTeal,
    secondary = SecondarySage,
    tertiary = AccentCoral,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2A2A2A),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF404040),
    outlineVariant = Color(0xFF353535),
    scrim = Color(0xFF000000),
    error = AccentCoral,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    secondary = SecondarySage,
    tertiary = AccentCoral,
    background = SurfaceOffWhite,
    surface = Color.White,
    surfaceVariant = SurfaceOffWhite,
    onPrimary = Color.White,
    onSecondary = TextCharcoal,
    onTertiary = Color.White,
    onBackground = TextCharcoal,
    onSurface = TextCharcoal,
    onSurfaceVariant = Color(0xFF5F5F5F),
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF0F0F0),
    scrim = Color(0xFF000000),
    error = AccentCoral,
    onError = Color.White
)

@Composable
fun BayanihanSpotsTheme(
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