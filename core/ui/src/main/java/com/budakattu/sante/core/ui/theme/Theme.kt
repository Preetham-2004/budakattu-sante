package com.budakattu.sante.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = LeafAccent,
    secondary = AmberHarvest,
    background = Color(0xFF1A2410),
    surface = Color(0xFF243318),
    tertiary = MilletGold,
)

private val LightScheme = lightColorScheme(
    primary = ForestPrimary,
    secondary = AmberHarvest,
    background = Parchment,
    surface = MistVeil,
    tertiary = BarkBrown,
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
