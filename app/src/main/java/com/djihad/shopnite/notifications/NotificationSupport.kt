package com.djihad.shopnite.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.djihad.shopnite.MainActivity
import com.djihad.shopnite.R
import java.util.concurrent.atomic.AtomicInteger

object NotificationSupport {
    private val ephemeralNotificationIds = AtomicInteger(
        ((System.currentTimeMillis() and 0x7fffffffL).toInt()).coerceAtLeast(1),
    )

    fun showTextNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        body: String,
    ) {
        if (!hasNotificationPermission(context)) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body.lines().firstOrNull().orEmpty())
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent(context))
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun stableNotificationId(
        channelId: String,
        uniqueKey: String,
    ): Int = (("$channelId:$uniqueKey").hashCode().toLong() and 0x7fffffffL).toInt()

    fun nextEphemeralNotificationId(): Int =
        ephemeralNotificationIds.getAndUpdate { current ->
            if (current >= Int.MAX_VALUE - 1) 1 else current + 1
        }

    private fun openAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
