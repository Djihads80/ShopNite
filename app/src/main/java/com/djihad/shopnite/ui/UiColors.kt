package com.djihad.shopnite.ui

import androidx.compose.ui.graphics.Color

fun colorFromHex(hex: String?, fallback: Color = Color(0xFF182033)): Color {
    val value = hex?.removePrefix("#")?.trim().orEmpty()
    val normalized = when (value.length) {
        6 -> "FF$value"
        8 -> value
        else -> return fallback
    }
    return try {
        Color(normalized.toULong(16))
    } catch (_: Exception) {
        fallback
    }
}

fun List<String>.toComposeColors(defaultColors: List<Color>): List<Color> {
    if (isEmpty()) return defaultColors
    return map { colorFromHex(it, defaultColors.last()) }
}
