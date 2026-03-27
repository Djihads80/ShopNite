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
        LanguageOption("es", "Español"),
        LanguageOption("es-419", "Español (LatAm)"),
        LanguageOption("fr", "Français"),
        LanguageOption("id", "Bahasa Indonesia"),
        LanguageOption("it", "Italiano"),
        LanguageOption("ja", "日本語"),
        LanguageOption("ko", "한국어"),
        LanguageOption("pl", "Polski"),
        LanguageOption("pt-BR", "Português (Brasil)"),
        LanguageOption("ru", "Русский"),
        LanguageOption("th", "ไทย"),
        LanguageOption("tr", "Türkçe"),
        LanguageOption("vi", "Tiếng Việt"),
        LanguageOption("zh-Hans", "简体中文"),
        LanguageOption("zh-Hant", "繁體中文"),
        LanguageOption("ar", "العربية"),
    )

    val app = listOf(LanguageOption("system", "Follow system")) + api
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
    val battlePassProgress: Int?,
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
