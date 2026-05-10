package com.budakattu.sante.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = LeaderPrimary,
    onPrimary = Color.White,
    secondary = LeaderSecondary,
    onSecondary = Color.White,
    background = Color(0xFF1A1A1A),
    onBackground = Color.White,
    surface = Color(0xFF242424),
    onSurface = Color.White,
    tertiary = LeaderAccent,
    onTertiary = Color.Black,
    error = LeaderError,
    onError = Color.White,
)

private val LightScheme = lightColorScheme(
    primary = LeaderPrimary,
    onPrimary = Color.White,
    secondary = LeaderSecondary,
    onSecondary = Color.White,
    background = LeaderBackground,
    onBackground = CharcoalInk,
    surface = Color.White,
    onSurface = CharcoalInk,
    tertiary = LeaderAccent,
    onTertiary = Color.White,
    error = LeaderError,
    onError = Color.White,
)

@Composable
fun BudakattuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = BudakattuTypography,
        content = content,
    )
}
