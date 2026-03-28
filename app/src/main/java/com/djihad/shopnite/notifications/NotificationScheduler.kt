package com.djihad.shopnite.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val LegacyShopMonitorWorkName = "shopnite_shop_monitor"
    private const val ShopResetWorkName = "shopnite_shop_reset_monitor"
    private const val ShopResetMinuteBuffer = 5L

    fun scheduleShopMonitor(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(LegacyShopMonitorWorkName)

        val request = PeriodicWorkRequestBuilder<ShopNotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntilNextShopReset())
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            ShopResetWorkName,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun delayUntilNextShopReset(
        nowUtc: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    ): Duration {
        val nextReset = nowUtc
            .withHour(0)
            .withMinute(ShopResetMinuteBuffer.toInt())
            .withSecond(0)
            .withNano(0)
            .let { reset ->
                if (nowUtc < reset) {
                    reset
                } else {
                    reset.plusDays(1)
                }
            }

        return Duration.between(nowUtc, nextReset)
    }
}
