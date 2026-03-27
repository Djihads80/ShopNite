package com.djihad.shopnite.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val WishlistReturns = "wishlist_returns"
    const val LeavingSoon = "leaving_soon"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    WishlistReturns,
                    "Wishlist returns",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Alerts when a wishlisted cosmetic returns to the shop."
                },
                NotificationChannel(
                    LeavingSoon,
                    "Leaving shop soon",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Alerts when a wishlisted cosmetic is about to leave the shop."
                },
            ),
        )
    }
}
