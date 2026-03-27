package com.djihad.shopnite.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = MidnightNavy,
    secondary = GoldLoot,
    onSecondary = MidnightNavy,
    tertiary = AuroraBlue,
    background = MidnightNavy,
    onBackground = MistWhite,
    surface = DeepSpace,
    onSurface = MistWhite,
    surfaceVariant = CardBlue,
    onSurfaceVariant = SoftText,
    outline = AuroraBlue.copy(alpha = 0.5f),
)

private val LightScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = MidnightNavy,
    secondary = GoldLoot,
    onSecondary = MidnightNavy,
    tertiary = DeepSpace,
    background = MistWhite,
    onBackground = MidnightNavy,
    surface = Color(0xFFF8FAFF),
    onSurface = MidnightNavy,
    surfaceVariant = Color(0xFFE7EEFF),
    onSurfaceVariant = StormSlate,
    outline = Color(0xFF7B92C9),
)

@Composable
fun ShopNiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
