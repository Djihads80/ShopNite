package com.djihad.shopnite.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

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
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkScheme
        else -> LightScheme
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = colorScheme.surfaceContainer.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
