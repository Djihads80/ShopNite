package com.djihad.shopnite.data.repository

import com.djihad.shopnite.data.remote.BattleRoyaleMotd
import com.djihad.shopnite.data.remote.CarCosmeticItem
import com.djihad.shopnite.data.remote.CosmeticImages
import com.djihad.shopnite.data.remote.CosmeticItem
import com.djihad.shopnite.data.remote.NewCosmeticsItems
import com.djihad.shopnite.data.remote.ShopColors
import com.djihad.shopnite.data.remote.ShopEntry
import com.djihad.shopnite.data.remote.TrackCosmeticItem
import com.djihad.shopnite.data.remote.FortniteApiService
import com.djihad.shopnite.model.AccountType
import com.djihad.shopnite.model.BrSummary
import com.djihad.shopnite.model.CatalogSnapshot
import com.djihad.shopnite.model.CosmeticCardItem
import com.djihad.shopnite.model.CosmeticDetail
import com.djihad.shopnite.model.CosmeticSource
import com.djihad.shopnite.model.NewsCard
import com.djihad.shopnite.model.ShopItem
import com.djihad.shopnite.model.ShopSnapshot
import com.djihad.shopnite.model.SummaryStat
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.NumberFormat
import java.util.Locale

class FortniteRepository(
    private val apiService: FortniteApiService,
) {
    suspend fun getBattleRoyaleNews(language: String): List<NewsCard> {
        val data = apiService.getBattleRoyaleNews(language).data
        return data.motds
            .filterNot { it.hidden == true }
            .sortedByDescending { it.sortingPriority ?: 0 }
            .map { it.toNewsCard(data.image) }
    }

    suspend fun getBattleRoyaleSummary(
        apiKey: String,
        playerName: String,
        accountType: AccountType,
    ): BrSummary {
        val response = apiService.getBattleRoyaleStats(
            apiKey = apiKey,
            name = playerName,
            accountType = accountType.apiValue,
        )

        val data = response.objectAt("data")
        val account = data?.objectAt("account")
        val battlePass = data?.objectAt("battlePass")
        val overall = data?.objectAt("stats")?.objectAt("all")?.objectAt("overall")

        val statTiles = buildList {
            add(SummaryStat("Wins", formatWholeNumber(overall?.numberAt("wins"))))
            add(SummaryStat("Matches", formatWholeNumber(overall?.numberAt("matches"))))
            add(SummaryStat("Kills", formatWholeNumber(overall?.numberAt("kills"))))
            add(SummaryStat("K/D", formatDecimal(overall?.numberAt("kd"))))
            add(SummaryStat("Win Rate", formatPercent(overall?.numberAt("winRate"))))
            add(SummaryStat("Minutes", formatWholeNumber(overall?.numberAt("minutesPlayed"))))
        }

        return BrSummary(
            playerName = account?.stringAt("name").orEmpty().ifBlank { playerName },
            accountType = accountType,
            battlePassLevel = battlePass?.numberAt("level")?.toInt(),
            battlePassProgress = battlePass?.numberAt("progress")?.toInt(),
            statTiles = statTiles,
        )
    }

    suspend fun getShop(language: String): ShopSnapshot {
        val data = apiService.getShop(language).data
        val items = data.entries.flatMap { entry ->
            buildList {
                entry.brItems.forEach { add(entry.toShopItem(it, data.vbuckIcon, CosmeticSource.BattleRoyale)) }
                entry.cars.forEach { add(entry.toShopItem(it, data.vbuckIcon, CosmeticSource.Cars)) }
                entry.tracks.forEach { add(entry.toShopItem(it, data.vbuckIcon, CosmeticSource.Tracks)) }
                entry.legoKits.forEach { add(entry.toShopItem(it, data.vbuckIcon, CosmeticSource.LegoKits)) }
                entry.instruments.forEach { add(entry.toShopItem(it, data.vbuckIcon, CosmeticSource.Instruments)) }
                entry.beans.forEach { add(entry.toShopItem(it, data.vbuckIcon, CosmeticSource.Kicks)) }
            }
        }.sortedWith(compareBy({ it.sectionName.orEmpty() }, { it.name }))

        return ShopSnapshot(
            shopDate = data.date,
            hash = data.hash,
            vbuckIconUrl = data.vbuckIcon,
            items = items,
        )
    }

    suspend fun getCatalog(language: String): CatalogSnapshot {
        val all = apiService.getAllCosmetics(language).data
        val newIds = apiService.getNewCosmetics(language).data.items.allIds()

        val items = buildList {
            addAll(all.br.map { it.toCatalogItem(CosmeticSource.BattleRoyale, it.id in newIds) })
            addAll(all.cars.map { it.toCatalogItem(CosmeticSource.Cars, it.id in newIds) })
            addAll(all.tracks.map { it.toCatalogItem(CosmeticSource.Tracks, it.id in newIds) })
            addAll(all.lego.map { it.toCatalogItem(CosmeticSource.Lego, it.id in newIds) })
            addAll(all.legoKits.map { it.toCatalogItem(CosmeticSource.LegoKits, it.id in newIds) })
            addAll(all.beans.map { it.toCatalogItem(CosmeticSource.Kicks, it.id in newIds) })
            addAll(all.instruments.map { it.toCatalogItem(CosmeticSource.Instruments, it.id in newIds) })
        }.sortedBy { it.name.lowercase(Locale.getDefault()) }

        return CatalogSnapshot(items = items, newIds = newIds)
    }

    suspend fun getCosmeticDetail(language: String, cosmeticId: String): CosmeticDetail? {
        val catalog = getCatalog(language)
        val shop = getShop(language)
        val cosmetic = catalog.items.firstOrNull { it.id == cosmeticId } ?: return null
        val currentShopItem = shop.items.firstOrNull { it.cosmeticId == cosmeticId }
        val occurrences = cosmetic.shopHistory.takeIf { it.isNotEmpty() }?.size
        return CosmeticDetail(
            cosmetic = cosmetic,
            currentShopItem = currentShopItem,
            occurrences = occurrences,
        )
    }

    suspend fun getWishlistMatches(language: String, wishlist: Set<String>): ShopSnapshot {
        val shop = getShop(language)
        return shop.copy(items = shop.items.filter { it.cosmeticId in wishlist })
    }

    private fun BattleRoyaleMotd.toNewsCard(fallbackImage: String?): NewsCard = NewsCard(
        id = id ?: title.orEmpty(),
        title = title.orEmpty().ifBlank { tabTitle.orEmpty() },
        tabTitle = tabTitle.orEmpty(),
        body = body.orEmpty(),
        imageUrl = tileImage ?: image ?: fallbackImage.orEmpty(),
    )

    private fun CosmeticItem.toCatalogItem(source: CosmeticSource, isNew: Boolean): CosmeticCardItem =
        CosmeticCardItem(
            id = id,
            name = name.orEmpty(),
            subtitle = set?.text ?: introduction?.text,
            description = description,
            typeLabel = type?.displayValue.orEmpty().ifBlank { source.defaultTypeLabel() },
            rarityLabel = series?.value ?: rarity?.displayValue.orEmpty().ifBlank { "Unknown" },
            rarityKey = series?.backendValue ?: rarity?.value.orEmpty(),
            seriesName = series?.value,
            seriesImage = series?.image,
            paletteHexes = series?.colors.takeUnless { it.isNullOrEmpty() } ?: rarityPalette(rarity?.displayValue),
            imageUrl = images.bestImageUrl(),
            addedDate = added,
            shopHistory = shopHistory,
            lastAppearance = lastAppearance,
            isNew = isNew,
            source = source,
        )

    private fun CarCosmeticItem.toCatalogItem(source: CosmeticSource, isNew: Boolean): CosmeticCardItem =
        CosmeticCardItem(
            id = id,
            name = name.orEmpty(),
            subtitle = vehicleId,
            description = description,
            typeLabel = type?.displayValue.orEmpty().ifBlank { source.defaultTypeLabel() },
            rarityLabel = series?.value ?: rarity?.displayValue.orEmpty().ifBlank { "Unknown" },
            rarityKey = series?.backendValue ?: rarity?.value.orEmpty(),
            seriesName = series?.value,
            seriesImage = series?.image,
            paletteHexes = series?.colors.takeUnless { it.isNullOrEmpty() } ?: rarityPalette(rarity?.displayValue),
            imageUrl = images.bestImageUrl(),
            addedDate = added,
            shopHistory = shopHistory,
            lastAppearance = lastAppearance,
            isNew = isNew,
            source = source,
        )

    private fun TrackCosmeticItem.toCatalogItem(source: CosmeticSource, isNew: Boolean): CosmeticCardItem =
        CosmeticCardItem(
            id = id,
            name = title.orEmpty().ifBlank { devName.orEmpty() },
            subtitle = artist,
            description = listOfNotNull(artist, releaseYear?.toString()).joinToString(" - ").ifBlank { null },
            typeLabel = "Jam Track",
            rarityLabel = "Music",
            rarityKey = "jam-track",
            seriesName = null,
            seriesImage = null,
            paletteHexes = listOf("2B5876FF", "4E4376FF", "161D29FF"),
            imageUrl = albumArt,
            addedDate = added,
            shopHistory = shopHistory,
            lastAppearance = lastAppearance,
            isNew = isNew,
            source = source,
        )

    private fun ShopEntry.toShopItem(
        item: CosmeticItem,
        vbuckIconUrl: String?,
        source: CosmeticSource,
    ): ShopItem = ShopItem(
        cosmeticId = item.id,
        offerId = offerId,
        name = item.name.orEmpty(),
        subtitle = bundle?.name ?: layout?.name,
        description = item.description,
        typeLabel = item.type?.displayValue.orEmpty().ifBlank { source.defaultTypeLabel() },
        rarityLabel = item.series?.value ?: item.rarity?.displayValue.orEmpty().ifBlank { "Unknown" },
        rarityKey = item.series?.backendValue ?: item.rarity?.value.orEmpty(),
        seriesName = item.series?.value,
        seriesImage = item.series?.image,
        paletteHexes = item.series?.colors.takeUnless { it.isNullOrEmpty() } ?: rarityPalette(item.rarity?.displayValue),
        tileHexes = colors.toTileHexes(),
        textBackgroundHex = colors?.textBackgroundColor,
        imageUrl = item.images.bestShopImageUrl(newDisplayAsset?.renderImages?.firstOrNull()?.image, bundle?.image),
        price = finalPrice ?: regularPrice ?: 0,
        regularPrice = regularPrice,
        vbuckIconUrl = vbuckIconUrl,
        inDate = inDate,
        outDate = outDate,
        bannerText = banner?.value ?: offerTag?.text,
        sectionName = layout?.name ?: layout?.category,
        addedDate = item.added,
    )

    private fun ShopEntry.toShopItem(
        item: CarCosmeticItem,
        vbuckIconUrl: String?,
        source: CosmeticSource,
    ): ShopItem = ShopItem(
        cosmeticId = item.id,
        offerId = offerId,
        name = item.name.orEmpty(),
        subtitle = bundle?.name ?: layout?.name,
        description = item.description,
        typeLabel = item.type?.displayValue.orEmpty().ifBlank { source.defaultTypeLabel() },
        rarityLabel = item.series?.value ?: item.rarity?.displayValue.orEmpty().ifBlank { "Unknown" },
        rarityKey = item.series?.backendValue ?: item.rarity?.value.orEmpty(),
        seriesName = item.series?.value,
        seriesImage = item.series?.image,
        paletteHexes = item.series?.colors.takeUnless { it.isNullOrEmpty() } ?: rarityPalette(item.rarity?.displayValue),
        tileHexes = colors.toTileHexes(),
        textBackgroundHex = colors?.textBackgroundColor,
        imageUrl = item.images.bestShopImageUrl(newDisplayAsset?.renderImages?.firstOrNull()?.image, bundle?.image),
        price = finalPrice ?: regularPrice ?: 0,
        regularPrice = regularPrice,
        vbuckIconUrl = vbuckIconUrl,
        inDate = inDate,
        outDate = outDate,
        bannerText = banner?.value ?: offerTag?.text,
        sectionName = layout?.name ?: layout?.category,
        addedDate = item.added,
    )

    private fun ShopEntry.toShopItem(
        item: TrackCosmeticItem,
        vbuckIconUrl: String?,
        source: CosmeticSource,
    ): ShopItem = ShopItem(
        cosmeticId = item.id,
        offerId = offerId,
        name = item.title.orEmpty().ifBlank { item.devName.orEmpty() },
        subtitle = item.artist ?: layout?.name,
        description = item.artist,
        typeLabel = source.defaultTypeLabel(),
        rarityLabel = "Music",
        rarityKey = "jam-track",
        seriesName = null,
        seriesImage = null,
        paletteHexes = listOf("0D3B66FF", "3772FFFF", "090E17FF"),
        tileHexes = colors.toTileHexes(),
        textBackgroundHex = colors?.textBackgroundColor,
        imageUrl = item.albumArt ?: newDisplayAsset?.renderImages?.firstOrNull()?.image,
        price = finalPrice ?: regularPrice ?: 0,
        regularPrice = regularPrice,
        vbuckIconUrl = vbuckIconUrl,
        inDate = inDate,
        outDate = outDate,
        bannerText = banner?.value ?: offerTag?.text,
        sectionName = layout?.name ?: layout?.category,
        addedDate = item.added,
    )

    private fun NewCosmeticsItems.allIds(): Set<String> = buildSet {
        addAll(br.map { it.id })
        addAll(tracks.map { it.id })
        addAll(cars.map { it.id })
        addAll(lego.map { it.id })
    }

    private fun CosmeticImages?.bestImageUrl(): String? =
        this?.icon ?: this?.featured ?: this?.smallIcon ?: this?.large ?: this?.small ?: this?.lego ?: this?.bean

    private fun CosmeticImages?.bestShopImageUrl(displayImage: String?, bundleImage: String?): String? =
        displayImage ?: bundleImage ?: bestImageUrl()

    private fun ShopColors?.toTileHexes(): List<String> = listOfNotNull(
        this?.color1,
        this?.color2,
        this?.color3,
    ).ifEmpty { listOf("182033FF", "101521FF", "080B12FF") }

    private fun CosmeticSource.defaultTypeLabel(): String = when (this) {
        CosmeticSource.BattleRoyale -> "Outfit"
        CosmeticSource.Cars -> "Vehicle"
        CosmeticSource.Tracks -> "Jam Track"
        CosmeticSource.Lego -> "LEGO"
        CosmeticSource.LegoKits -> "LEGO Build"
        CosmeticSource.Kicks -> "Kicks"
        CosmeticSource.Instruments -> "Instrument"
    }

    private fun rarityPalette(rarity: String?): List<String> = when (rarity?.lowercase(Locale.getDefault())) {
        "common" -> listOf("6D7A8DFF", "455161FF", "1A1F29FF")
        "uncommon" -> listOf("6DD16EFF", "2B9348FF", "102516FF")
        "rare" -> listOf("54A8FFFF", "2176FFFF", "071B38FF")
        "epic" -> listOf("CC66FFFF", "8E44ADFF", "1D1029FF")
        "legendary" -> listOf("FFAF54FF", "FB8500FF", "2A1400FF")
        "mythic" -> listOf("FEEA7EFF", "E8C547FF", "2F2800FF")
        "marvel series" -> listOf("FF5A5FFF", "B80C09FF", "190103FF")
        "dark series" -> listOf("B18CFEFF", "6247AAFF", "120B1EFF")
        "dc series" -> listOf("60E3FFFF", "1677FFFF", "061229FF")
        "icon series" -> listOf("74F7F5FF", "00B4D8FF", "06253BFF")
        "frozen series" -> listOf("A5F3FCFF", "38BDF8FF", "05263BFF")
        "lava series" -> listOf("FF8C42FF", "D62828FF", "220601FF")
        "shadow series" -> listOf("AAB7C4FF", "5C677DFF", "0D1117FF")
        "star wars series" -> listOf("FFE066FF", "D9A404FF", "231700FF")
        "slurp series" -> listOf("62F0D1FF", "00A896FF", "042F2EFF")
        "gaming legends series" -> listOf("57C7FFFF", "0077B6FF", "051B2AFF")
        else -> listOf("2A5CAAFF", "1A2341FF", "090C17FF")
    }

    private fun JsonObject.objectAt(key: String): JsonObject? = this[key]?.jsonObject

    private fun JsonObject.stringAt(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrSafe()

    private fun JsonObject.numberAt(key: String): Double? {
        val primitive = (this[key] as? JsonPrimitive) ?: return null
        return primitive.doubleOrNull ?: primitive.intOrNull?.toDouble()
    }

    private fun JsonPrimitive.contentOrSafe(): String? = content.takeIf { it.isNotBlank() }

    private fun formatWholeNumber(value: Double?): String {
        if (value == null) return "0"
        return NumberFormat.getIntegerInstance(Locale.getDefault()).format(value.toInt())
    }

    private fun formatDecimal(value: Double?): String =
        if (value == null) "0.0" else String.format(Locale.getDefault(), "%.2f", value)

    private fun formatPercent(value: Double?): String =
        if (value == null) "0%" else String.format(Locale.getDefault(), "%.1f%%", value)
}
