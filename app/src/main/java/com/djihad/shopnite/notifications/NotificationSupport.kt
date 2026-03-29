package com.djihad.shopnite.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.djihad.shopnite.MainActivity
import com.djihad.shopnite.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min
import kotlin.math.roundToInt

object NotificationSupport {
    private const val LargeIconSizeDp = 64f
    private const val LargeIconPaddingDp = 6f
    private const val EmoteOutlineBlurDp = 2f
    private const val EmoteOutlineAlpha = 41

    private val ephemeralNotificationIds = AtomicInteger(
        ((System.currentTimeMillis() and 0x7fffffffL).toInt()).coerceAtLeast(1),
    )
    private val notificationImageClient by lazy { OkHttpClient() }

    fun showTextNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        body: String,
        largeIcon: Bitmap? = null,
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
            .apply {
                if (largeIcon != null) {
                    setLargeIcon(largeIcon)
                }
            }
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    suspend fun loadCosmeticLargeIcon(
        context: Context,
        imageUrl: String?,
        addEmoteOutline: Boolean,
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (imageUrl.isNullOrBlank()) {
            return@withContext null
        }

        runCatching {
            val downloaded = downloadBitmap(imageUrl) ?: return@runCatching null
            val normalized = normalizeLargeIcon(context, downloaded)
            if (downloaded !== normalized) {
                downloaded.recycle()
            }

            if (!addEmoteOutline) {
                normalized
            } else {
                val outlined = addOutline(normalized, context)
                if (outlined !== normalized) {
                    normalized.recycle()
                }
                outlined
            }
        }.getOrNull()
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

    private fun downloadBitmap(imageUrl: String): Bitmap? {
        val request = Request.Builder()
            .url(imageUrl)
            .build()

        return notificationImageClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return null
            }
            val body = response.body ?: return null
            body.byteStream().use(BitmapFactory::decodeStream)
        }
    }

    private fun normalizeLargeIcon(
        context: Context,
        source: Bitmap,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (LargeIconSizeDp * density).roundToInt().coerceAtLeast(64)
        val paddingPx = (LargeIconPaddingDp * density).roundToInt().coerceAtLeast(4)
        val contentSize = (sizePx - (paddingPx * 2)).coerceAtLeast(1)

        val result = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val sourceWidth = source.width.toFloat().coerceAtLeast(1f)
        val sourceHeight = source.height.toFloat().coerceAtLeast(1f)
        val scale = min(contentSize / sourceWidth, contentSize / sourceHeight)
        val destWidth = sourceWidth * scale
        val destHeight = sourceHeight * scale
        val left = (sizePx - destWidth) / 2f
        val top = (sizePx - destHeight) / 2f

        canvas.drawBitmap(
            source,
            null,
            RectF(left, top, left + destWidth, top + destHeight),
            paint,
        )
        return result
    }

    private fun addOutline(
        bitmap: Bitmap,
        context: Context,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val blurRadius = (EmoteOutlineBlurDp * density).coerceAtLeast(1f)
        val outlineBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val outlineCanvas = Canvas(outlineBitmap)
        val alphaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.OUTER)
        }
        val offset = IntArray(2)
        val alphaMask = bitmap.extractAlpha(alphaPaint, offset)
        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(EmoteOutlineAlpha, 0, 0, 0)
        }

        outlineCanvas.drawBitmap(alphaMask, offset[0].toFloat(), offset[1].toFloat(), outlinePaint)
        outlineCanvas.drawBitmap(bitmap, 0f, 0f, null)
        alphaMask.recycle()

        return outlineBitmap
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
