package com.djihad.shopnite.notifications

import android.content.Context
import com.djihad.shopnite.data.local.UserSettings
import com.djihad.shopnite.data.repository.FortniteRepository
import com.djihad.shopnite.util.Formatters

object DebugNotificationManager {
    suspend fun forceWishlistReturnNotifications(
        context: Context,
        repository: FortniteRepository,
        settings: UserSettings,
    ) {
        NotificationChannels.create(context)

        val matches = runCatching {
            repository.getWishlistMatches(settings.apiLanguageTag, settings.wishlist).items
        }.getOrDefault(emptyList())

        if (matches.isEmpty()) {
            ShopItemNotifications.showGenericDebugWishlistReturn(context)
            return
        }

        matches.forEach { item ->
            ShopItemNotifications.showDebugWishlistReturn(
                context = context,
                cosmeticName = item.name,
                cosmeticType = item.typeLabel,
                price = item.price,
                outDate = item.outDate,
            )
        }
    }

    suspend fun forceWishlistLeavingNotifications(
        context: Context,
        repository: FortniteRepository,
        settings: UserSettings,
    ) {
        NotificationChannels.create(context)

        val leavingSoon = runCatching {
            repository.getWishlistMatches(settings.apiLanguageTag, settings.wishlist).items
                .filter { Formatters.leavesWithinDay(it.outDate) }
        }.getOrDefault(emptyList())

        if (leavingSoon.isEmpty()) {
            ShopItemNotifications.showGenericDebugWishlistLeavingSoon(context)
            return
        }

        leavingSoon.forEach { item ->
            ShopItemNotifications.showDebugWishlistLeavingSoon(
                context = context,
                cosmeticName = item.name,
                cosmeticType = item.typeLabel,
                price = item.price,
                outDate = item.outDate,
            )
        }
    }
}
