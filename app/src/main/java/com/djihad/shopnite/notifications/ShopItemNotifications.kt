package com.djihad.shopnite.notifications

import android.content.Context
import com.djihad.shopnite.model.ShopItem
import com.djihad.shopnite.util.Formatters

object ShopItemNotifications {
    fun showWishlistReturn(
        context: Context,
        item: ShopItem,
    ) {
        NotificationSupport.showTextNotification(
            context = context,
            channelId = NotificationChannels.WishlistReturns,
            notificationId = NotificationSupport.stableNotificationId(
                channelId = NotificationChannels.WishlistReturns,
                uniqueKey = item.cosmeticId,
            ),
            title = "Wishlist return",
            body = wishlistReturnBody(
                cosmeticName = item.name,
                price = item.price,
            ),
        )
    }

    fun showWishlistLeavingSoon(
        context: Context,
        item: ShopItem,
    ) {
        NotificationSupport.showTextNotification(
            context = context,
            channelId = NotificationChannels.LeavingSoon,
            notificationId = NotificationSupport.stableNotificationId(
                channelId = NotificationChannels.LeavingSoon,
                uniqueKey = item.cosmeticId,
            ),
            title = "Leaving the shop soon",
            body = wishlistLeavingSoonBody(
                cosmeticName = item.name,
                outDate = item.outDate,
            ),
        )
    }

    fun showDebugWishlistReturn(
        context: Context,
        cosmeticId: String,
        cosmeticName: String,
        price: Int? = null,
    ) {
        NotificationSupport.showTextNotification(
            context = context,
            channelId = NotificationChannels.WishlistReturns,
            notificationId = NotificationSupport.nextEphemeralNotificationId(),
            title = "Wishlist return",
            body = wishlistReturnBody(
                cosmeticName = cosmeticName,
                price = price,
            ),
        )
    }

    fun showDebugWishlistLeavingSoon(
        context: Context,
        cosmeticId: String,
        cosmeticName: String,
        outDate: String? = null,
    ) {
        NotificationSupport.showTextNotification(
            context = context,
            channelId = NotificationChannels.LeavingSoon,
            notificationId = NotificationSupport.nextEphemeralNotificationId(),
            title = "Leaving the shop soon",
            body = wishlistLeavingSoonBody(
                cosmeticName = cosmeticName,
                outDate = outDate,
            ),
        )
    }

    fun showGenericDebugWishlistReturn(context: Context) {
        NotificationSupport.showTextNotification(
            context = context,
            channelId = NotificationChannels.WishlistReturns,
            notificationId = NotificationSupport.nextEphemeralNotificationId(),
            title = "Wishlist return",
            body = "A wishlist item is back in the shop.",
        )
    }

    fun showGenericDebugWishlistLeavingSoon(context: Context) {
        NotificationSupport.showTextNotification(
            context = context,
            channelId = NotificationChannels.LeavingSoon,
            notificationId = NotificationSupport.nextEphemeralNotificationId(),
            title = "Leaving the shop soon",
            body = "A wishlist item is leaving the shop soon.",
        )
    }

    private fun wishlistReturnBody(
        cosmeticName: String,
        price: Int?,
    ): String = if (price != null) {
        "$cosmeticName is back for ${Formatters.formatPrice(price)} V-Bucks."
    } else {
        "$cosmeticName is back in the shop."
    }

    private fun wishlistLeavingSoonBody(
        cosmeticName: String,
        outDate: String?,
    ): String = Formatters.formatTimeLeft(outDate)
        ?.lowercase()
        ?.takeIf { it.isNotBlank() }
        ?.let { timeLeft -> "$cosmeticName $timeLeft." }
        ?: "$cosmeticName is leaving the shop soon."
}
