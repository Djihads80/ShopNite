package com.djihad.shopnite.ui

import android.content.Context
import androidx.annotation.DrawableRes

@DrawableRes
fun Context.findRarityBackgroundRes(
    rarityKey: String?,
    rarityLabel: String?,
    seriesName: String?,
): Int? {
    val aliases = listOfNotNull(rarityKey, rarityLabel, seriesName)
        .flatMap { value -> mappedAliases(normalizeRarityToken(value)) }
        .distinct()

    return aliases.asSequence()
        .map { alias -> resources.getIdentifier("rarity_$alias", "drawable", packageName) }
        .firstOrNull { it != 0 }
}

private fun mappedAliases(token: String): List<String> = when (token) {
    "common" -> listOf("common")
    "uncommon" -> listOf("uncommon")
    "rare" -> listOf("rare")
    "epic" -> listOf("epic")
    "legendary" -> listOf("legendary")
    "mythic" -> listOf("mythic")
    "dark", "darkseries", "dark_series" -> listOf("dark")
    "marvel", "marvelseries", "marvel_series" -> listOf("marvel")
    "dc", "dcseries", "dc_series" -> listOf("dc")
    "icon", "iconseries", "icon_series" -> listOf("icon")
    "frozen", "frozenseries", "frozen_series" -> listOf("frozen")
    "lava", "lavaseries", "lava_series" -> listOf("lava")
    "shadow", "shadowseries", "shadow_series" -> listOf("shadow")
    "slurp", "slurpseries", "slurp_series" -> listOf("slurp")
    "gaminglegends", "gaming_legends", "gaminglegendsseries", "gaming_legends_series" -> listOf("gaming_legends")
    "crew", "fortnitecrew", "fortnite_crew" -> listOf("crew")
    "alanwalker", "alan_walker" -> listOf("alan_walker")
    "astonmartin", "aston_martin", "aston_martin_series" -> listOf("aston_martin_series")
    "mercedesbenz", "mercedes_benz" -> listOf("mercedes_benz")
    "starwars", "star_wars", "star_wars_series" -> listOf("star_wars")
    "rivian" -> listOf("rivian")
    "defender" -> listOf("defender")
    "pontiac" -> listOf("pontiac")
    "bugatti" -> listOf("bugatti")
    "chevrolet" -> listOf("chevrolet")
    "ferrari" -> listOf("ferrari")
    "ford" -> listOf("ford")
    "ram" -> listOf("ram")
    "jeep" -> listOf("jeep")
    "dodge" -> listOf("dodge")
    "porsche" -> listOf("porsche")
    "puma" -> listOf("puma")
    "adidas" -> listOf("adidas")
    "bmw" -> listOf("bmw")
    "tesla" -> listOf("tesla")
    "nissan" -> listOf("nissan")
    "mclaren" -> listOf("mclaren")
    "lamborghini" -> listOf("lamborghini")
    else -> fuzzyAliases(token) + token
}

private fun fuzzyAliases(token: String): List<String> = buildList {
    if ("star_wars" in token) add("star_wars")
    if ("alan_walker" in token) add("alan_walker")
    if ("aston_martin" in token) add("aston_martin_series")
    if ("mercedes" in token) add("mercedes_benz")
    if ("rivian" in token) add("rivian")
    if ("defender" in token || "land_rover" in token) add("defender")
    if ("pontiac" in token) add("pontiac")
    if ("bugatti" in token) add("bugatti")
    if ("chevrolet" in token) add("chevrolet")
    if ("ferrari" in token) add("ferrari")
    if ("ford" in token) add("ford")
    if ("ram" in token) add("ram")
    if ("jeep" in token) add("jeep")
    if ("dodge" in token) add("dodge")
    if ("porsche" in token) add("porsche")
    if ("puma" in token) add("puma")
    if ("adidas" in token) add("adidas")
    if ("bmw" in token) add("bmw")
    if ("tesla" in token) add("tesla")
    if ("nissan" in token) add("nissan")
    if ("mclaren" in token) add("mclaren")
    if ("lamborghini" in token) add("lamborghini")
}

private fun normalizeRarityToken(value: String): String =
    value.lowercase()
        .replace("&", "and")
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
