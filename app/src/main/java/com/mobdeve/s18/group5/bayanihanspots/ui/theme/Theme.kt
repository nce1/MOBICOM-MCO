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
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    secondary = SecondarySage,
    tertiary = AccentCoral,
    background = SurfaceOffWhite,
    surface = SurfaceOffWhite,
    onPrimary = Color.White,
    onSecondary = TextCharcoal,
    onTertiary = Color.White,
    onBackground = TextCharcoal,
    onSurface = TextCharcoal
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