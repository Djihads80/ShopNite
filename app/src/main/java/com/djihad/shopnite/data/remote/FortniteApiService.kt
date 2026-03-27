package com.djihad.shopnite.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FortniteApiService {
    @GET("v2/news/br")
    suspend fun getBattleRoyaleNews(
        @Query("language") language: String,
    ): FortniteApiResponse<BattleRoyaleNewsData>

    @GET("v2/shop")
    suspend fun getShop(
        @Query("language") language: String,
    ): FortniteApiResponse<ShopData>

    @GET("v2/cosmetics")
    suspend fun getAllCosmetics(
        @Query("language") language: String,
    ): FortniteApiResponse<AllCosmeticsData>

    @GET("v2/cosmetics/new")
    suspend fun getNewCosmetics(
        @Query("language") language: String,
    ): FortniteApiResponse<NewCosmeticsData>

    @GET("v2/stats/br/v2")
    suspend fun getBattleRoyaleStats(
        @Header("Authorization") apiKey: String,
        @Query("name") name: String,
        @Query("accountType") accountType: String,
        @Query("timeWindow") timeWindow: String = "lifetime",
        @Query("image") image: String = "none",
    ): JsonObject
}

fun createFortniteApiService(): FortniteApiService {
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    return Retrofit.Builder()
        .baseUrl("https://fortnite-api.com/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(FortniteApiService::class.java)
}

@Serializable
data class FortniteApiResponse<T>(
    val status: Int = 0,
    val data: T,
)

@Serializable
data class BattleRoyaleNewsData(
    val date: String? = null,
    val hash: String? = null,
    val image: String? = null,
    val motds: List<BattleRoyaleMotd> = emptyList(),
)

@Serializable
data class BattleRoyaleMotd(
    val id: String? = null,
    val title: String? = null,
    val tabTitle: String? = null,
    val body: String? = null,
    val image: String? = null,
    val tileImage: String? = null,
    val hidden: Boolean? = null,
    val sortingPriority: Int? = null,
)

@Serializable
data class ShopData(
    val date: String? = null,
    val hash: String? = null,
    val vbuckIcon: String? = null,
    val entries: List<ShopEntry> = emptyList(),
)

@Serializable
data class ShopEntry(
    val offerId: String,
    val devName: String? = null,
    val regularPrice: Int? = null,
    val finalPrice: Int? = null,
    val inDate: String? = null,
    val outDate: String? = null,
    val giftable: Boolean? = null,
    val refundable: Boolean? = null,
    val sortPriority: Int? = null,
    val layoutId: String? = null,
    val layout: ShopLayout? = null,
    val bundle: ShopBundle? = null,
    val banner: ShopBanner? = null,
    val offerTag: ShopTag? = null,
    val colors: ShopColors? = null,
    val tileSize: String? = null,
    val displayAssetPath: String? = null,
    val newDisplayAssetPath: String? = null,
    val newDisplayAsset: NewDisplayAsset? = null,
    val brItems: List<CosmeticItem> = emptyList(),
    val cars: List<CarCosmeticItem> = emptyList(),
    val tracks: List<TrackCosmeticItem> = emptyList(),
    val legoKits: List<CosmeticItem> = emptyList(),
    val instruments: List<CosmeticItem> = emptyList(),
    val beans: List<CosmeticItem> = emptyList(),
)

@Serializable
data class ShopLayout(
    val id: String? = null,
    val name: String? = null,
    val category: String? = null,
    val index: Int? = null,
    val rank: Int? = null,
    val showIneligibleOffers: String? = null,
    val useWidePreview: Boolean? = null,
    val displayType: String? = null,
)

@Serializable
data class ShopBundle(
    val name: String? = null,
    val info: String? = null,
    val image: String? = null,
)

@Serializable
data class ShopBanner(
    val value: String? = null,
    val intensity: String? = null,
    val backendValue: String? = null,
)

@Serializable
data class ShopTag(
    val id: String? = null,
    val text: String? = null,
)

@Serializable
data class ShopColors(
    val color1: String? = null,
    val color2: String? = null,
    val color3: String? = null,
    val textBackgroundColor: String? = null,
)

@Serializable
data class NewDisplayAsset(
    val id: String? = null,
    val materialInstances: List<JsonElement> = emptyList(),
    val renderImages: List<RenderImage> = emptyList(),
)

@Serializable
data class RenderImage(
    val productTag: String? = null,
    val fileName: String? = null,
    val image: String? = null,
)

@Serializable
data class AllCosmeticsData(
    val br: List<CosmeticItem> = emptyList(),
    val tracks: List<TrackCosmeticItem> = emptyList(),
    val cars: List<CarCosmeticItem> = emptyList(),
    val lego: List<CosmeticItem> = emptyList(),
    val legoKits: List<CosmeticItem> = emptyList(),
    val beans: List<CosmeticItem> = emptyList(),
    val instruments: List<CosmeticItem> = emptyList(),
)

@Serializable
data class NewCosmeticsData(
    val build: String? = null,
    val date: String? = null,
    val hashes: JsonElement? = null,
    val items: NewCosmeticsItems = NewCosmeticsItems(),
    val lastAdditions: JsonElement? = null,
    val previousBuild: String? = null,
)

@Serializable
data class NewCosmeticsItems(
    val br: List<CosmeticItem> = emptyList(),
    val tracks: List<TrackCosmeticItem> = emptyList(),
    val cars: List<CarCosmeticItem> = emptyList(),
    val lego: List<CosmeticItem> = emptyList(),
    val legoKits: List<CosmeticItem> = emptyList(),
    val beans: List<CosmeticItem> = emptyList(),
    val instruments: List<CosmeticItem> = emptyList(),
)

@Serializable
data class CosmeticItem(
    val id: String,
    val cosmeticId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val type: CosmeticType? = null,
    val rarity: CosmeticRarity? = null,
    val series: CosmeticSeries? = null,
    val set: CosmeticSet? = null,
    val introduction: CosmeticIntroduction? = null,
    val images: CosmeticImages? = null,
    val showcaseVideo: String? = null,
    val metaTags: List<String> = emptyList(),
    val added: String? = null,
    val shopHistory: List<String> = emptyList(),
    val lastAppearance: String? = null,
    val variants: List<JsonElement> = emptyList(),
)

@Serializable
data class CarCosmeticItem(
    val id: String,
    val vehicleId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val type: CosmeticType? = null,
    val rarity: CosmeticRarity? = null,
    val images: CosmeticImages? = null,
    val series: CosmeticSeries? = null,
    val added: String? = null,
    val shopHistory: List<String> = emptyList(),
    val lastAppearance: String? = null,
)

@Serializable
data class TrackCosmeticItem(
    val id: String,
    val devName: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val releaseYear: Int? = null,
    val bpm: Int? = null,
    val duration: Int? = null,
    val difficulty: TrackDifficulty? = null,
    val albumArt: String? = null,
    val added: String? = null,
    val shopHistory: List<String> = emptyList(),
    val lastAppearance: String? = null,
)

@Serializable
data class TrackDifficulty(
    val vocals: Int? = null,
    val guitar: Int? = null,
    val bass: Int? = null,
    val plasticBass: Int? = null,
    val drums: Int? = null,
    val plasticDrums: Int? = null,
)

@Serializable
data class CosmeticType(
    val value: String? = null,
    val displayValue: String? = null,
    val backendValue: String? = null,
)

@Serializable
data class CosmeticRarity(
    val value: String? = null,
    val displayValue: String? = null,
    val backendValue: String? = null,
)

@Serializable
data class CosmeticSeries(
    val value: String? = null,
    val image: String? = null,
    val colors: List<String> = emptyList(),
    val backendValue: String? = null,
)

@Serializable
data class CosmeticSet(
    val value: String? = null,
    val text: String? = null,
    val backendValue: String? = null,
)

@Serializable
data class CosmeticIntroduction(
    val chapter: String? = null,
    val season: String? = null,
    val text: String? = null,
    val backendValue: Int? = null,
)

@Serializable
data class CosmeticImages(
    @Serializable(with = FlexibleImageUrlSerializer::class)
    val smallIcon: String? = null,
    @Serializable(with = FlexibleImageUrlSerializer::class)
    val icon: String? = null,
    @Serializable(with = FlexibleImageUrlSerializer::class)
    val featured: String? = null,
    @Serializable(with = FlexibleImageUrlSerializer::class)
    val lego: String? = null,
    @Serializable(with = FlexibleImageUrlSerializer::class)
    val bean: String? = null,
    @Serializable(with = FlexibleImageUrlSerializer::class)
    val small: String? = null,
    @Serializable(with = FlexibleImageUrlSerializer::class)
    val large: String? = null,
)

object FlexibleImageUrlSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleImageUrl", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value)
        }
    }

    override fun deserialize(decoder: Decoder): String? {
        val element = (decoder as? kotlinx.serialization.json.JsonDecoder)?.decodeJsonElement()
            ?: return decoder.decodeString()

        return element.extractImageUrl()
    }

    private fun JsonElement.extractImageUrl(): String? = when (this) {
        is JsonPrimitive -> contentOrNull
        is JsonObject -> preferredImageKeys
            .asSequence()
            .mapNotNull { key -> jsonObject[key]?.extractImageUrl() }
            .firstOrNull()
        else -> null
    }

    private companion object {
        val preferredImageKeys = listOf(
            "icon",
            "featured",
            "smallIcon",
            "small",
            "large",
            "url",
            "background",
            "displayAsset",
        )
    }
}
