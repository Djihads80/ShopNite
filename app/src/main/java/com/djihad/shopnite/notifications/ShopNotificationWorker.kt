package com.djihad.shopnite.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.djihad.shopnite.ShopNiteApplication
import com.djihad.shopnite.model.ShopItem
import com.djihad.shopnite.util.Formatters
import kotlinx.coroutines.flow.first

class ShopNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val app = applicationContext as ShopNiteApplication
        val settingsRepository = app.appContainer.userSettingsRepository
        val repository = app.appContainer.fortniteRepository
        val settings = settingsRepository.settings.first()

        if (settings.wishlist.isEmpty()) return Result.success()
        if (!settings.notifyWishlistReturns && !settings.notifyWishlistLeavingSoon) return Result.success()
        if (!hasNotificationPermission()) return Result.success()

        return runCatching {
            NotificationChannels.create(applicationContext)
            val shop = repository.getWishlistMatches(settings.apiLanguageTag, settings.wishlist)
            val currentIds = shop.items.map { it.cosmeticId }.toSet()

            if (settings.notifyWishlistReturns) {
                val freshReturns = shop.items.filter { it.cosmeticId !in settings.notifiedReturnIds }
                if (freshReturns.isNotEmpty()) {
                    notifyItems(
                        channelId = NotificationChannels.WishlistReturns,
                        notificationId = 1001,
                        title = "Wishlist return${if (freshReturns.size == 1) "" else "s"}",
                        items = freshReturns,
                        bodyMapper = { item -> "${item.name} is back for ${Formatters.formatPrice(item.price)} V-Bucks." },
                    )
                }
                settingsRepository.setNotifiedReturnIds(currentIds)
            }

            if (settings.notifyWishlistLeavingSoon) {
                val leavingSoon = shop.items.filter { Formatters.leavesWithinDay(it.outDate) }
                val leavingTokens = leavingSoon.mapNotNull { item ->
                    shop.hash?.let { hash -> "$hash:${item.cosmeticId}" }
                }.toSet()
                val freshLeaving = leavingSoon.filter { item ->
                    val token = shop.hash?.let { "$it:${item.cosmeticId}" }
                    token != null && token !in settings.notifiedLeavingTokens
                }
                if (freshLeaving.isNotEmpty()) {
                    notifyItems(
                        channelId = NotificationChannels.LeavingSoon,
                        notificationId = 1002,
                        title = "Leaving the shop soon",
                        items = freshLeaving,
                        bodyMapper = { item ->
                            "${item.name} ${Formatters.formatTimeLeft(item.outDate)?.lowercase().orEmpty()}."
                        },
                    )
                }
                settingsRepository.setNotifiedLeavingTokens(leavingTokens)
            }

            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    private fun notifyItems(
        channelId: String,
        notificationId: Int,
        title: String,
        items: List<ShopItem>,
        bodyMapper: (ShopItem) -> String,
    ) {
        val body = if (items.size == 1) {
            bodyMapper(items.first())
        } else {
            items.take(3).joinToString(separator = "\n") { bodyMapper(it) }
        }

        NotificationSupport.showTextNotification(
            context = applicationContext,
            channelId = channelId,
            notificationId = notificationId,
            title = title,
            body = body,
        )
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
