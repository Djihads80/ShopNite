package com.djihad.shopnite.model

enum class AccountType(val apiValue: String, val label: String) {
    Epic("epic", "Epic"),
    Psn("psn", "PlayStation"),
    Xbox("xbl", "Xbox");

    companion object {
        fun fromApiValue(value: String?): AccountType =
            entries.firstOrNull { it.apiValue == value } ?: Epic
    }
}

data class LanguageOption(
    val tag: String,
    val label: String,
)

object SupportedLanguages {
    val api = listOf(
        LanguageOption("en", "English"),
        LanguageOption("de", "Deutsch"),
        LanguageOption("es", "Espanol"),
        LanguageOption("es-419", "Espanol (LatAm)"),
        LanguageOption("fr", "Francais"),
        LanguageOption("id", "Bahasa Indonesia"),
        LanguageOption("it", "Italiano"),
        LanguageOption("ja", "Japanese"),
        LanguageOption("ko", "Korean"),
        LanguageOption("pl", "Polski"),
        LanguageOption("pt-BR", "Portuguese (Brazil)"),
        LanguageOption("ru", "Russian"),
        LanguageOption("th", "Thai"),
        LanguageOption("tr", "Turkish"),
        LanguageOption("vi", "Vietnamese"),
        LanguageOption("zh-Hans", "Chinese (Simplified)"),
        LanguageOption("zh-Hant", "Chinese (Traditional)"),
        LanguageOption("ar", "Arabic"),
    )

    val app = listOf(
        LanguageOption("system", "Follow system"),
        LanguageOption("en", "English"),
        LanguageOption("es", "Espanol"),
        LanguageOption("fr", "Francais"),
    )
}

data class NewsCard(
    val id: String,
    val title: String,
    val tabTitle: String,
    val body: String,
    val imageUrl: String,
)

data class BrSummary(
    val playerName: String,
    val accountType: AccountType,
    val battlePassLevel: Int?,
    val statTiles: List<SummaryStat>,
)

data class SummaryStat(
    val label: String,
    val value: String,
)

data class CosmeticCardItem(
    val id: String,
    val name: String,
    val subtitle: String?,
    val description: String?,
    val typeLabel: String,
    val typeValue: String,
    val filterLabel: String,
    val rarityLabel: String,
    val rarityKey: String,
    val seriesName: String?,
    val seriesImage: String?,
    val paletteHexes: List<String>,
    val imageUrl: String?,
    val addedDate: String?,
    val shopHistory: List<String>,
    val lastAppearance: String?,
    val isNew: Boolean,
    val source: CosmeticSource,
)

data class ShopItem(
    val cosmeticId: String,
    val offerId: String,
    val name: String,
    val subtitle: String?,
    val description: String?,
    val typeLabel: String,
    val typeValue: String,
    val filterLabel: String,
    val rarityLabel: String,
    val rarityKey: String,
    val seriesName: String?,
    val seriesImage: String?,
    val paletteHexes: List<String>,
    val tileHexes: List<String>,
    val textBackgroundHex: String?,
    val imageUrl: String?,
    val price: Int,
    val regularPrice: Int?,
    val vbuckIconUrl: String?,
    val inDate: String?,
    val outDate: String?,
    val bannerText: String?,
    val sectionName: String?,
    val addedDate: String?,
)

data class ShopSnapshot(
    val shopDate: String?,
    val hash: String?,
    val vbuckIconUrl: String?,
    val items: List<ShopItem>,
)

data class CatalogSnapshot(
    val items: List<CosmeticCardItem>,
    val newIds: Set<String>,
)

data class CosmeticDetail(
    val cosmetic: CosmeticCardItem,
    val currentShopItem: ShopItem?,
    val occurrences: Int?,
)

enum class CosmeticSource {
    BattleRoyale,
    Cars,
    Tracks,
    Lego,
    LegoKits,
    Kicks,
    Instruments,
}

object CosmeticFilters {
    const val All = "All"

    private val ordered = listOf(
        "Outfits",
        "Emotes",
        "Pickaxes",
        "Backblings",
        "Gliders",
        "Sidekicks",
        "Kicks",
        "Wraps",
        "Loadings",
        "Music",
        "Contrails",
        "Sprays",
        "Banners",
        "Bundles",
        "Cars",
        "Decals",
        "Wheels",
        "Trails",
        "Boost",
        "Jam Tracks",
        "Guitars",
        "Basses",
        "Drums",
        "Keytars",
        "Mics",
        "Auras",
        "Lego Builds",
        "Lego Decor sets",
    )

    fun orderedOptions(present: Collection<String>): List<String> {
        val labels = present.toSet()
        val known = ordered.filter(labels::contains)
        val extras = labels
            .filterNot(ordered::contains)
            .sorted()
        return listOf(All) + known + extras
    }
}
