package com.djihad.shopnite.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val ShopMonitorWorkName = "shopnite_shop_monitor"

    fun scheduleShopMonitor(context: Context) {
        val request = PeriodicWorkRequestBuilder<ShopNotificationWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ShopMonitorWorkName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
