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
import com.djihad.shopnite.model.CosmeticImageOption
import com.djihad.shopnite.model.CosmeticSource
import com.djihad.shopnite.model.NewsCard
import com.djihad.shopnite.model.ShopItem
import com.djihad.shopnite.model.ShopSnapshot
import com.djihad.shopnite.model.SummaryStat
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import java.text.NumberFormat
import java.util.Locale

class FortniteRepository(
    private val apiService: FortniteApiService,
) {
    private val catalogCache = mutableMapOf<String, CatalogSnapshot>()

    suspend fun getBattleRoyaleNews(language: String): List<NewsCard> {
        val data = apiService.getBattleRoyaleNews(language).data
        return data.motds
            .filterNot { it.hidden == true }
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
            timeWindow = "season",
        )

        val data = response.objectAt("data")
        val account = data?.objectAt("account")
        val battlePass = data?.objectAt("battlePass")
        val overall = data?.objectAt("stats")?.objectAt("all")?.objectAt("overall")
        val matches = overall?.numberAt("matches")
        val kills = overall?.numberAt("kills")
        val minutesPlayed = overall?.numberAt("minutesPlayed")
        val killsPerMatch = overall?.numberAt("killsPerMatch")
            ?: safeDivide(kills, matches)
        val averageMatchMinutes = safeDivide(minutesPlayed, matches)

        val statTiles = buildList {
            add(SummaryStat("Season LVL", formatWholeNumber(battlePass?.numberAt("level"))))
            add(SummaryStat("Wins", formatWholeNumber(overall?.numberAt("wins"))))
            add(SummaryStat("Win Rate", formatPercent(overall?.numberAt("winRate"))))
            add(SummaryStat("Matches", formatWholeNumber(matches)))
            add(SummaryStat("K/D", formatDecimal(overall?.numberAt("kd"))))
            add(SummaryStat("Kills / Match", formatDecimal(killsPerMatch)))
            add(SummaryStat("Kills", formatWholeNumber(kills)))
            add(SummaryStat("Playtime", formatDuration(minutesPlayed)))
            add(SummaryStat("Avg Match", formatDuration(averageMatchMinutes)))
        }

        return BrSummary(
            playerName = account?.stringAt("name").orEmpty().ifBlank { playerName },
            accountType = accountType,
            battlePassLevel = battlePass?.numberAt("level")?.toInt(),
            statTiles = statTiles,
        )
    }

    suspend fun getShop(language: String): ShopSnapshot {
        val data = apiService.getShop(language).data
        val items = data.entries.mapNotNull { entry -> entry.toShopItem(data.vbuckIcon) }

        return ShopSnapshot(
            shopDate = data.date,
            hash = data.hash,
            vbuckIconUrl = data.vbuckIcon,
            items = items,
        )
    }

    suspend fun getCatalog(language: String): CatalogSnapshot {
        catalogCache[language]?.let { return it }

        val all = loadStage("all cosmetics") { apiService.getAllCosmetics(language).data }
        val newIds = loadStage("new cosmetics") { apiService.getNewCosmetics(language).data.items.allIds() }

        val items = buildList {
            addAll(all.br.map { it.toCatalogItem(CosmeticSource.BattleRoyale, it.id in newIds) })
            addAll(all.cars.map { it.toCatalogItem(CosmeticSource.Cars, it.id in newIds) })
            addAll(all.tracks.map { it.toCatalogItem(CosmeticSource.Tracks, it.id in newIds) })
            addAll(all.lego.map { it.toCatalogItem(CosmeticSource.Lego, it.id in newIds) })
            addAll(all.legoKits.map { it.toCatalogItem(CosmeticSource.LegoKits, it.id in newIds) })
            addAll(all.instruments.map { it.toCatalogItem(CosmeticSource.Instruments, it.id in newIds) })
        }.sortedBy { it.name.lowercase(Locale.getDefault()) }

        return CatalogSnapshot(items = items, newIds = newIds)
            .also { catalogCache[language] = it }
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
        return shop.copy(items = shop.items.filter { item ->
            item.cosmeticId in wishlist || item.bundledCosmeticIds.any { it in wishlist }
        })
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
            name = name.orEmpty().ifBlank { cosmeticId ?: id },
            subtitle = set?.text ?: introduction?.text,
            description = description,
            typeLabel = typeLabelFor(type?.value, type?.displayValue, source),
            typeValue = normalizeTypeValue(type?.value, source),
            filterLabel = filterLabelFor(type?.value, type?.displayValue, source),
            rarityLabel = rarityLabelFor(series?.value, rarity?.displayValue),
            rarityKey = series?.backendValue ?: rarity?.value.orEmpty(),
            seriesName = series?.value?.toDisplayRarityLabel(),
            seriesImage = series?.image,
            paletteHexes = series?.colors.takeUnless { it.isNullOrEmpty() } ?: rarityPalette(rarityLabelFor(null, rarity?.displayValue)),
            imageUrl = images.bestImageUrl(),
            addedDate = added,
            shopHistory = shopHistory,
            lastAppearance = lastAppearance,
            isNew = isNew,
            source = source,
            imageOptions = imageOptions(
                cosmeticName = name.orEmpty().ifBlank { cosmeticId ?: id },
                baseImageUrl = images.bestImageUrl(),
                legoImageUrl = images?.lego,
                variants = variants,
            ),
        ).takeUnless { it.name.startsWithHiddenPrefix() } ?: return null

    private fun CarCosmeticItem.toCatalogItem(source: CosmeticSource, isNew: Boolean): CosmeticCardItem =
        CosmeticCardItem(
            id = id,
            name = name.orEmpty(),
            subtitle = vehicleId,
            description = description,
            typeLabel = typeLabelFor(type?.value, type?.displayValue, source),
            typeValue = normalizeTypeValue(type?.value, source),
            filterLabel = filterLabelFor(type?.value, type?.displayValue, source),
            rarityLabel = rarityLabelFor(series?.value, rarity?.displayValue),
            rarityKey = series?.backendValue ?: rarity?.value.orEmpty(),
            seriesName = series?.value?.toDisplayRarityLabel(),
            seriesImage = series?.image,
            paletteHexes = series?.colors.takeUnless { it.isNullOrEmpty() } ?: rarityPalette(rarityLabelFor(null, rarity?.displayValue)),
            imageUrl = images.bestImageUrl(),
            addedDate = added,
            shopHistory = shopHistory,
            lastAppearance = lastAppearance,
            isNew = isNew,
            source = source,
        ).takeUnless { it.name.startsWithHiddenPrefix() } ?: return null

    private fun TrackCosmeticItem.toCatalogItem(source: CosmeticSource, isNew: Boolean): CosmeticCardItem =
        CosmeticCardItem(
            id = id,
            name = title.orEmpty().ifBlank { devName.orEmpty() },
            subtitle = artist,
            description = listOfNotNull(artist, releaseYear?.toString()).joinToString(" - ").ifBlank { null },
            typeLabel = "Jam Track",
            typeValue = normalizeTypeValue(null, source),
            filterLabel = filterLabelFor(null, "Jam Track", source),
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
        ).takeUnless { it.name.startsWithHiddenPrefix() } ?: return null

    private fun ShopEntry.toShopItem(vbuckIconUrl: String?): ShopItem? {
        val bundledIds = bundledCosmeticIds()
        if (bundle != null) {
            return toBundleShopItem(vbuckIconUrl, bundledIds)
        }

        return brItems.firstOrNull()?.let { toShopItem(it, vbuckIconUrl, CosmeticSource.BattleRoyale, bundledIds) }
            ?: cars.firstOrNull()?.let { toShopItem(it, vbuckIconUrl, CosmeticSource.Cars, bundledIds) }
            ?: tracks.firstOrNull()?.let { toShopItem(it, vbuckIconUrl, CosmeticSource.Tracks, bundledIds) }
            ?: legoKits.firstOrNull()?.let { toShopItem(it, vbuckIconUrl, CosmeticSource.LegoKits, bundledIds) }
            ?: instruments.firstOrNull()?.let { toShopItem(it, vbuckIconUrl, CosmeticSource.Instruments, bundledIds) }
    }

    private fun ShopEntry.toBundleShopItem(
        vbuckIconUrl: String?,
        bundledIds: Set<String>,
    ): ShopItem? {
        val representative = primaryOfferCosmetic() ?: return null
        return ShopItem(
            cosmeticId = representative.cosmeticId,
            bundledCosmeticIds = bundledIds,
            isBundle = true,
            offerId = offerId,
            name = bundle?.name.orEmpty().ifBlank { representative.name },
            subtitle = bundle?.info ?: layout?.name,
            description = representative.description,
            typeLabel = "Bundle",
            typeValue = "bundle",
            filterLabel = "Bundles",
            rarityLabel = representative.rarityLabel,
            rarityKey = representative.rarityKey,
            seriesName = representative.seriesName,
            seriesImage = representative.seriesImage,
            paletteHexes = representative.paletteHexes,
            tileHexes = colors.toTileHexes(),
            textBackgroundHex = colors?.textBackgroundColor,
            imageUrl = bundle?.image ?: newDisplayAsset?.renderImages?.firstOrNull()?.image ?: representative.imageUrl,
            price = finalPrice ?: regularPrice ?: 0,
            regularPrice = regularPrice,
            vbuckIconUrl = vbuckIconUrl,
            inDate = inDate,
            outDate = outDate,
            bannerText = banner?.value,
            sectionName = layout?.name ?: layout?.category,
            addedDate = representative.addedDate,
            source = representative.source,
        )
    }

    private fun ShopEntry.toShopItem(
        item: CosmeticItem,
        vbuckIconUrl: String?,
        source: CosmeticSource,
        bundledIds: Set<String>,
    ): ShopItem = ShopItem(
        cosmeticId = item.id,
        bundledCosmeticIds = bundledIds,
        offerId = offerId,
        name = item.name.orEmpty(),
        subtitle = bundle?.name ?: layout?.name,
        description = item.description,
        typeLabel = typeLabelFor(item.type?.value, item.type?.displayValue, source),
        typeValue = normalizeTypeValue(item.type?.value, source),
        filterLabel = filterLabelFor(item.type?.value, item.type?.displayValue, source),
        rarityLabel = rarityLabelFor(item.series?.value, item.rarity?.displayValue),
        rarityKey = item.series?.backendValue ?: item.rarity?.value.orEmpty(),
        seriesName = item.series?.value?.toDisplayRarityLabel(),
        seriesImage = item.series?.image,
        paletteHexes = item.series?.colors.takeUnless { it.isNullOrEmpty() } ?: rarityPalette(rarityLabelFor(null, item.rarity?.displayValue)),
        tileHexes = colors.toTileHexes(),
        textBackgroundHex = colors?.textBackgroundColor,
        imageUrl = item.images.bestShopImageUrl(newDisplayAsset?.renderImages?.firstOrNull()?.image, bundle?.image),
        price = finalPrice ?: regularPrice ?: 0,
        regularPrice = regularPrice,
        vbuckIconUrl = vbuckIconUrl,
        inDate = inDate,
        outDate = outDate,
        bannerText = banner?.value,
        sectionName = layout?.name ?: layout?.category,
        addedDate = item.added,
        source = source,
    )

    private fun ShopEntry.toShopItem(
        item: CarCosmeticItem,
        vbuckIconUrl: String?,
        source: CosmeticSource,
        bundledIds: Set<String>,
    ): ShopItem = ShopItem(
        cosmeticId = item.id,
        bundledCosmeticIds = bundledIds,
        offerId = offerId,
        name = item.name.orEmpty(),
        subtitle = bundle?.name ?: layout?.name,
        description = item.description,
        typeLabel = typeLabelFor(item.type?.value, item.type?.displayValue, source),
        typeValue = normalizeTypeValue(item.type?.value, source),
        filterLabel = filterLabelFor(item.type?.value, item.type?.displayValue, source),
        rarityLabel = rarityLabelFor(item.series?.value, item.rarity?.displayValue),
        rarityKey = item.series?.backendValue ?: item.rarity?.value.orEmpty(),
        seriesName = item.series?.value?.toDisplayRarityLabel(),
        seriesImage = item.series?.image,
        paletteHexes = item.series?.colors.takeUnless { it.isNullOrEmpty() } ?: rarityPalette(rarityLabelFor(null, item.rarity?.displayValue)),
        tileHexes = colors.toTileHexes(),
        textBackgroundHex = colors?.textBackgroundColor,
        imageUrl = item.images.bestShopImageUrl(newDisplayAsset?.renderImages?.firstOrNull()?.image, bundle?.image),
        price = finalPrice ?: regularPrice ?: 0,
        regularPrice = regularPrice,
        vbuckIconUrl = vbuckIconUrl,
        inDate = inDate,
        outDate = outDate,
        bannerText = banner?.value,
        sectionName = layout?.name ?: layout?.category,
        addedDate = item.added,
        source = source,
    )

    private fun ShopEntry.toShopItem(
        item: TrackCosmeticItem,
        vbuckIconUrl: String?,
        source: CosmeticSource,
        bundledIds: Set<String>,
    ): ShopItem = ShopItem(
        cosmeticId = item.id,
        bundledCosmeticIds = bundledIds,
        offerId = offerId,
        name = item.title.orEmpty().ifBlank { item.devName.orEmpty() },
        subtitle = item.artist ?: layout?.name,
        description = item.artist,
        typeLabel = source.defaultTypeLabel(),
        typeValue = normalizeTypeValue(null, source),
        filterLabel = filterLabelFor(null, source.defaultTypeLabel(), source),
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
        bannerText = banner?.value,
        sectionName = layout?.name ?: layout?.category,
        addedDate = item.added,
        source = source,
    )

    private fun ShopEntry.primaryOfferCosmetic(): ShopItem? =
        brItems.firstOrNull()?.let { toShopItem(it, null, CosmeticSource.BattleRoyale, bundledCosmeticIds()) }
            ?: cars.firstOrNull()?.let { toShopItem(it, null, CosmeticSource.Cars, bundledCosmeticIds()) }
            ?: tracks.firstOrNull()?.let { toShopItem(it, null, CosmeticSource.Tracks, bundledCosmeticIds()) }
            ?: legoKits.firstOrNull()?.let { toShopItem(it, null, CosmeticSource.LegoKits, bundledCosmeticIds()) }
            ?: instruments.firstOrNull()?.let { toShopItem(it, null, CosmeticSource.Instruments, bundledCosmeticIds()) }

    private fun ShopEntry.bundledCosmeticIds(): Set<String> =
        buildSet {
            addAll(brItems.map { it.id })
            addAll(cars.map { it.id })
            addAll(tracks.map { it.id })
            addAll(legoKits.map { it.id })
            addAll(instruments.map { it.id })
        }

    private fun NewCosmeticsItems.allIds(): Set<String> = buildSet {
        addAll(br.map { it.id })
        addAll(tracks.map { it.id })
        addAll(cars.map { it.id })
        addAll(lego.map { it.id })
        addAll(legoKits.map { it.id })
        addAll(instruments.map { it.id })
    }

    private fun CosmeticImages?.bestImageUrl(): String? =
        this?.icon ?: this?.featured ?: this?.smallIcon ?: this?.large ?: this?.small ?: this?.lego ?: this?.bean

    private fun CosmeticImages?.bestShopImageUrl(displayImage: String?, bundleImage: String?): String? =
        bestImageUrl() ?: displayImage ?: bundleImage

    private fun imageOptions(
        cosmeticName: String,
        baseImageUrl: String?,
        legoImageUrl: String?,
        variants: List<JsonElement>,
    ): List<CosmeticImageOption> = buildList {
        baseImageUrl?.takeIf { it.isNotBlank() }?.let {
            add(CosmeticImageOption("Default", it))
        }
        variants.flatMapIndexed { index, variant ->
            variant.extractVariantImages(fallbackLabel = "Style ${index + 1}")
        }.filterNot { option ->
            option.label.equals("Default", ignoreCase = true) ||
                option.label.equals(cosmeticName, ignoreCase = true)
        }.forEach { option ->
            if (none { it.imageUrl == option.imageUrl }) {
                add(option)
            }
        }
        legoImageUrl?.takeIf { it.isNotBlank() }?.let {
            if (none { option -> option.imageUrl == it }) {
                add(CosmeticImageOption("LEGO", it))
            }
        }
    }

    private fun JsonElement.extractVariantImages(fallbackLabel: String): List<CosmeticImageOption> = when (this) {
        is JsonArray -> flatMapIndexed { index, element ->
            element.extractVariantImages("$fallbackLabel ${index + 1}")
        }
        is JsonObject -> {
            val label = variantLabel() ?: fallbackLabel
            val directImage = variantImageUrl()
            val nested = values.flatMapIndexed { index, element ->
                element.extractVariantImages("$label ${index + 1}")
            }
            buildList {
                directImage?.let { add(CosmeticImageOption(label, it)) }
                addAll(nested)
            }
        }
        else -> emptyList()
    }

    private fun JsonObject.variantLabel(): String? =
        stringAt("name")
            ?: stringAt("displayName")
            ?: stringAt("tag")
            ?: stringAt("channel")
            ?: objectAt("variant")?.stringAt("name")

    private fun JsonObject.variantImageUrl(): String? {
        val imageKeys = listOf("image", "icon", "smallIcon", "featured", "url", "previewImage", "preview")
        return imageKeys.firstNotNullOfOrNull { key ->
            when (val element = this[key]) {
                is JsonPrimitive -> element.contentOrNull?.takeIf { it.startsWith("http") }
                is JsonObject -> element.variantImageUrl()
                else -> null
            }
        }
    }

    private fun ShopColors?.toTileHexes(): List<String> = listOfNotNull(
        this?.color1,
        this?.color2,
        this?.color3,
    )

    private fun CosmeticSource.defaultTypeLabel(): String = when (this) {
        CosmeticSource.BattleRoyale -> "Outfit"
        CosmeticSource.Cars -> "Car"
        CosmeticSource.Tracks -> "Jam Track"
        CosmeticSource.Lego -> "Lego Decor set"
        CosmeticSource.LegoKits -> "LEGO Build"
        CosmeticSource.Kicks -> "Kicks"
        CosmeticSource.Instruments -> "Instrument"
    }

    private fun normalizeTypeValue(typeValue: String?, source: CosmeticSource): String = when {
        !typeValue.isNullOrBlank() -> typeValue.lowercase(Locale.getDefault())
        source == CosmeticSource.Tracks -> "track"
        source == CosmeticSource.Kicks -> "shoe"
        source == CosmeticSource.LegoKits -> "legoset"
        source == CosmeticSource.Lego -> "legoprop"
        else -> ""
    }

    private fun typeLabelFor(
        typeValue: String?,
        typeDisplayValue: String?,
        source: CosmeticSource,
    ): String = when (normalizeTypeValue(typeValue, source)) {
        "shoe" -> "Kicks"
        else -> typeDisplayValue.orEmpty().ifBlank { source.defaultTypeLabel() }
    }

    private fun filterLabelFor(
        typeValue: String?,
        typeDisplayValue: String?,
        source: CosmeticSource,
    ): String {
        val normalized = normalizeTypeValue(typeValue, source)
        return when (normalized) {
            "outfit" -> "Outfits"
            "emote" -> "Emotes"
            "pickaxe" -> "Pickaxes"
            "backpack" -> "Backblings"
            "glider" -> "Gliders"
            "sidekick", "pet", "petcarrier" -> "Sidekicks"
            "shoe" -> "Kicks"
            "wrap" -> "Wraps"
            "loadingscreen" -> "Loadings"
            "music" -> "Music"
            "contrail" -> "Contrails"
            "spray" -> "Sprays"
            "banner" -> "Banners"
            "bundle" -> "Bundles"
            "body" -> "Cars"
            "skin" -> "Decals"
            "wheel" -> "Wheels"
            "drifttrail" -> "Trails"
            "booster" -> "Boost"
            "track" -> "Jam Tracks"
            "guitar" -> "Guitars"
            "bass" -> "Basses"
            "drum" -> "Drums"
            "keyboard" -> "Keytars"
            "mic" -> "Mics"
            "aura" -> "Auras"
            "legoset" -> "Lego Builds"
            "legoprop" -> "Lego Decor sets"
            else -> typeDisplayValue.orEmpty().ifBlank { source.defaultTypeLabel() }
        }
    }

    private fun rarityLabelFor(seriesValue: String?, rarityDisplayValue: String?): String =
        (seriesValue ?: rarityDisplayValue)
            .orEmpty()
            .ifBlank { "Unknown" }
            .toDisplayRarityLabel()

    private fun String.toDisplayRarityLabel(): String {
        val normalized = trim()
            .replace(Regex("\\s+"), " ")
            .lowercase(Locale.getDefault())
        when (normalized) {
            "gaming legends series" -> return "Gaming Legends Series"
            "dc series" -> return "DC Series"
            "bmw series" -> return "BMW Series"
        }
        return normalized
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                }
            }
    }

    private fun String?.startsWithHiddenPrefix(): Boolean =
        !this.isNullOrBlank() && (startsWith("Character_") || startsWith("CID_"))

    private suspend fun <T> loadStage(label: String, block: suspend () -> T): T = try {
        block()
    } catch (throwable: Throwable) {
        throw IllegalStateException("Failed while loading $label: ${throwable.message}", throwable)
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

    private fun JsonObject.objectAt(key: String): JsonObject? = this[key] as? JsonObject

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

    private fun formatDuration(value: Double?): String {
        if (value == null) return "0m"
        val totalMinutes = value.toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    private fun safeDivide(numerator: Double?, denominator: Double?): Double? {
        if (numerator == null || denominator == null || denominator == 0.0) return null
        return numerator / denominator
    }
}
