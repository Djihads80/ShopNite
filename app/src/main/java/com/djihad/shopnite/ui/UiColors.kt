package com.djihad.shopnite.ui

import android.graphics.Color.parseColor
import androidx.compose.ui.graphics.Color

fun colorFromHex(hex: String?, fallback: Color = Color(0xFF182033)): Color {
    val value = hex?.removePrefix("#")?.trim().orEmpty()
    val normalized = when (value.length) {
        3 -> value.map { "$it$it" }.joinToString("").let { "FF$it" }
        6 -> "FF$value"
        8 -> value
        else -> return fallback
    }
    return try {
        Color(parseColor("#$normalized"))
    } catch (_: IllegalArgumentException) {
        fallback
    }
}

fun List<String>.toComposeColors(defaultColors: List<Color>): List<Color> {
    if (isEmpty()) return defaultColors
    return map { colorFromHex(it, defaultColors.last()) }
}

data class RarityPillColors(
    val background: Color,
    val content: Color,
)

fun rarityPillColors(
    rarityLabel: String?,
    fallback: RarityPillColors = RarityPillColors(
        background = Color(0xFF182033),
        content = Color(0xFFE6ECF5),
    ),
): RarityPillColors {
    val colors = when (normalizeRarityToken(rarityLabel)) {
        "common" -> "#40464d" to "#B7BFC5"
        "uncommon" -> "#024F03" to "#61BF00"
        "rare" -> "#00458A" to "#00AFFF"
        "epic" -> "#4C197B" to "#CE59FF"
        "legendary" -> "#DE6E0E" to "#FFE8CF"
        "marvel_series" -> "#64040C" to "#E61B23"
        "dark_series" -> "#5C1C7C" to "#F74AC1"
        "dc_series" -> "#003169" to "#007AF1"
        "frozen_series" -> "#0059ED" to "#D4E7FB"
        "gaming_legends_series" -> "#2C0D5E" to "#8279FA"
        "lava_series" -> "#5C045C" to "#F4C336"
        "shadow_series" -> "#151515" to "#FFFFFF"
        "slurp_series" -> "#1C96FC" to "#1CF7F5"
        "star_wars_series" -> "#080F13" to "#F3D404"
        "icon_series" -> "#025253" to "#57ECEC"
        "crew_series" -> "#3C3B6D" to "#B7C0ED"
        "lamborghini_series" -> "#260948" to "#CFBEE1"
        "mclaren_series" -> "#801F13" to "#E7D9DA"
        "nissan_series" -> "#0D4994" to "#05B5F5"
        "tesla_series" -> "#515A62" to "#E7D9DA"
        "bmw_series" -> "#02021C" to "#3068F0"
        "alan_walker_series" -> "#162A88" to "#56CADB"
        "adidas_series" -> "#DAE4F2" to "#15131B"
        "puma_series" -> "#E10C0E" to "#FCEBF0"
        "porsche_series" -> "#BCB489" to "#262317"
        "dodge_series" -> "#CF2D2B" to "#FFEFEF"
        "jeep_series" -> "#4C6315" to "#E6EEE3"
        "ram_series" -> "#F61210" to "#FEF2ED"
        "ford_series" -> "#101A5E" to "#F1F5FC"
        "ferrari_series" -> "#DB4025" to "#F1F5FC"
        "chevrolet_series" -> "#e1bd71" to "#f1f5fc"
        "mercedes_benz_series" -> "#030406" to "#84868a"
        "bugatti_series" -> "#db1b2e" to "#e8e3e2"
        "pontiac_series" -> "#ac3746" to "#fffcf8"
        "aston_martin_series" -> "#00b7c5" to "#c9fafb"
        "defender_series" -> "#1b5b42" to "#fff"
        "rivian_series" -> "#fdb301" to "#fff"
        "chrysler_series" -> "#17244C" to "#E3E9EE"
        "hyundai_series" -> "#0c1c4e" to "#d6e3ec"
        else -> return fallback
    }
    return RarityPillColors(
        background = colorFromHex(colors.first, fallback.background),
        content = colorFromHex(colors.second, fallback.content),
    )
}

private fun normalizeRarityToken(value: String?): String =
    value.orEmpty()
        .lowercase()
        .replace("&", "and")
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
