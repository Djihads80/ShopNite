package com.djihad.shopnite.notifications

import android.content.Context
import com.djihad.shopnite.model.ShopItem
import com.djihad.shopnite.util.Formatters

object ShopItemNotifications {
    suspend fun showWishlistReturn(
        context: Context,
        item: ShopItem,
    ) {
        val largeIcon = NotificationSupport.loadCosmeticLargeIcon(
            context = context,
            imageUrl = item.imageUrl,
            addEmoteOutline = isEmote(item.typeValue),
        )
        NotificationSupport.showTextNotification(
            context = context,
            channelId = NotificationChannels.WishlistReturns,
            notificationId = NotificationSupport.stableNotificationId(
                channelId = NotificationChannels.WishlistReturns,
                uniqueKey = item.cosmeticId,
            ),
            title = wishlistReturnTitle(
                cosmeticName = item.name,
                cosmeticType = item.typeLabel,
            ),
            body = wishlistNotificationBody(
                price = item.price,
                outDate = item.outDate,
            ),
            largeIcon = largeIcon,
        )
    }

    suspend fun showWishlistLeavingSoon(
        context: Context,
        item: ShopItem,
    ) {
        val largeIcon = NotificationSupport.loadCosmeticLargeIcon(
            context = context,
            imageUrl = item.imageUrl,
            addEmoteOutline = isEmote(item.typeValue),
        )
        NotificationSupport.showTextNotification(
            context = context,
            channelId = NotificationChannels.LeavingSoon,
            notificationId = NotificationSupport.stableNotificationId(
                channelId = NotificationChannels.LeavingSoon,
                uniqueKey = item.cosmeticId,
            ),
            title = wishlistLeavingSoonTitle(
                cosmeticName = item.name,
                cosmeticType = item.typeLabel,
            ),
            body = wishlistNotificationBody(
                price = item.price,
                outDate = item.outDate,
            ),
            largeIcon = largeIcon,
        )
    }

    suspend fun showDebugWishlistReturn(
        context: Context,
        cosmeticName: String,
        cosmeticType: String,
        cosmeticTypeValue: String,
        imageUrl: String? = null,
        price: Int? = null,
        outDate: String? = null,
    ) {
        val largeIcon = NotificationSupport.loadCosmeticLargeIcon(
            context = context,
            imageUrl = imageUrl,
            addEmoteOutline = isEmote(cosmeticTypeValue),
        )
        NotificationSupport.showTextNotification(
            context = context,
            channelId = NotificationChannels.WishlistReturns,
            notificationId = NotificationSupport.nextEphemeralNotificationId(),
            title = wishlistReturnTitle(
                cosmeticName = cosmeticName,
                cosmeticType = cosmeticType,
            ),
            body = wishlistNotificationBody(
                price = price,
                outDate = outDate,
            ),
            largeIcon = largeIcon,
        )
    }

    suspend fun showDebugWishlistLeavingSoon(
        context: Context,
        cosmeticName: String,
        cosmeticType: String,
        cosmeticTypeValue: String,
        imageUrl: String? = null,
        price: Int? = null,
        outDate: String? = null,
    ) {
        val largeIcon = NotificationSupport.loadCosmeticLargeIcon(
            context = context,
            imageUrl = imageUrl,
            addEmoteOutline = isEmote(cosmeticTypeValue),
        )
        NotificationSupport.showTextNotification(
            context = context,
            channelId = NotificationChannels.LeavingSoon,
            notificationId = NotificationSupport.nextEphemeralNotificationId(),
            title = wishlistLeavingSoonTitle(
                cosmeticName = cosmeticName,
                cosmeticType = cosmeticType,
            ),
            body = wishlistNotificationBody(
                price = price,
                outDate = outDate,
            ),
            largeIcon = largeIcon,
        )
    }

    fun showGenericDebugWishlistReturn(context: Context) {
        NotificationSupport.showTextNotification(
            context = context,
            channelId = NotificationChannels.WishlistReturns,
            notificationId = NotificationSupport.nextEphemeralNotificationId(),
            title = "Wishlist item returned",
            body = wishlistNotificationBody(
                price = null,
                outDate = null,
            ),
        )
    }

    fun showGenericDebugWishlistLeavingSoon(context: Context) {
        NotificationSupport.showTextNotification(
            context = context,
            channelId = NotificationChannels.LeavingSoon,
            notificationId = NotificationSupport.nextEphemeralNotificationId(),
            title = "Wishlist item is leaving soon!",
            body = wishlistNotificationBody(
                price = null,
                outDate = null,
            ),
        )
    }

    private fun wishlistReturnTitle(
        cosmeticName: String,
        cosmeticType: String,
    ): String = "$cosmeticName (${cosmeticType.ifBlank { "Cosmetic" }}) returned"

    private fun wishlistLeavingSoonTitle(
        cosmeticName: String,
        cosmeticType: String,
    ): String = "$cosmeticName (${cosmeticType.ifBlank { "Cosmetic" }}) is leaving soon!"

    private fun wishlistNotificationBody(
        price: Int?,
        outDate: String?,
    ): String {
        val priceText = price?.let { "${Formatters.formatPrice(it)} V-Bucks" } ?: "Price unavailable"
        val leaveDateText = Formatters.formatDate(outDate) ?: "Unknown"
        return "$priceText | Leaves $leaveDateText"
    }

    private fun isEmote(typeValue: String): Boolean =
        typeValue.equals("emote", ignoreCase = true)
}
