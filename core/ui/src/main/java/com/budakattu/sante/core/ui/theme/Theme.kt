package com.budakattu.sante.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = LeaderPrimary,
    onPrimary = Color.White,
    secondary = LeaderSecondary,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = CharcoalInk,
    surface = Color.White,
    onSurface = CharcoalInk,
    surfaceVariant = Color(0xFFF8F9FA),
    onSurfaceVariant = Color(0xFF495057),
    tertiary = LeaderAccent,
    onTertiary = Color.White,
    outline = Color(0xFFDEE2E6),
    error = LeaderError,
    onError = Color.White,
)

@Composable
fun BudakattuTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightScheme,
        typography = BudakattuTypography,
        content = content,
    )
}
