package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TurfGreenPrimary,
    secondary = SoftGold,
    tertiary = NeonAccent,
    background = PitchDarkBg,
    surface = SoftDarkCard,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    primaryContainer = TurfGreenPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TurfLightPrimary,
    secondary = SoftGold,
    tertiary = TurfLightPrimary,
    background = TurfLightBg,
    surface = SoftLightCard,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF1B2321),
    onSurface = Color(0xFF1B2321),
    primaryContainer = TurfLightPrimary.copy(alpha = 0.15f),
    onPrimaryContainer = TurfLightPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to ensure our beautiful Pitch-green brand styling is consistent!
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
