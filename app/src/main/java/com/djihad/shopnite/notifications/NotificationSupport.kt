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
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object NotificationSupport {
    private const val LargeIconSizeDp = 64f
    private const val LargeIconBadgeInsetDp = 2f
    private const val LargeIconContentPaddingDp = 6f
    private const val EmoteOutlineBlurDp = 2f
    private const val EmoteOutlineAlpha = 75
    private const val LargeIconBackgroundAlpha = 232

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
            val fittedArt = fitLargeIconArt(context, downloaded)
            downloaded.recycle()

            val preparedArt = if (!addEmoteOutline) {
                fittedArt
            } else {
                val outlined = addOutline(fittedArt, context)
                fittedArt.recycle()
                outlined
            }

            val composed = composeAvatarLargeIcon(
                context = context,
                art = preparedArt,
                emoteStyle = addEmoteOutline,
            )
            preparedArt.recycle()
            composed
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

    private fun fitLargeIconArt(
        context: Context,
        source: Bitmap,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (LargeIconSizeDp * density).roundToInt().coerceAtLeast(64)
        val badgeInsetPx = (LargeIconBadgeInsetDp * density).roundToInt().coerceAtLeast(1)
        val contentPaddingPx = (LargeIconContentPaddingDp * density).roundToInt().coerceAtLeast(4)
        val contentSize = (sizePx - (badgeInsetPx * 2) - (contentPaddingPx * 2)).coerceAtLeast(1)

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

    private fun composeAvatarLargeIcon(
        context: Context,
        art: Bitmap,
        emoteStyle: Boolean,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val badgeInsetPx = (LargeIconBadgeInsetDp * density).coerceAtLeast(1f)
        val avatar = Bitmap.createBitmap(art.width, art.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(avatar)
        val badgeBounds = RectF(
            badgeInsetPx,
            badgeInsetPx,
            art.width.toFloat() - badgeInsetPx,
            art.height.toFloat() - badgeInsetPx,
        )
        val badgeRadius = min(badgeBounds.width(), badgeBounds.height()) / 2f

        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = backgroundShader(art, badgeBounds, emoteStyle)
        }
        canvas.drawOval(badgeBounds, backgroundPaint)

        val clipPath = Path().apply {
            addOval(badgeBounds, Path.Direction.CW)
        }
        val saveCount = canvas.save()
        canvas.clipPath(clipPath)
        canvas.drawBitmap(art, 0f, 0f, null)
        canvas.restoreToCount(saveCount)

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = max(1f, density)
            color = if (emoteStyle) {
                Color.argb(48, 255, 255, 255)
            } else {
                Color.argb(36, 255, 255, 255)
            }
        }
        canvas.drawCircle(
            badgeBounds.centerX(),
            badgeBounds.centerY(),
            badgeRadius - (ringPaint.strokeWidth / 2f),
            ringPaint,
        )

        return avatar
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

    private fun backgroundShader(
        art: Bitmap,
        badgeBounds: RectF,
        emoteStyle: Boolean,
    ): Shader {
        val colors = if (emoteStyle) {
            intArrayOf(
                Color.argb(LargeIconBackgroundAlpha, 84, 89, 101),
                Color.argb(LargeIconBackgroundAlpha, 40, 44, 54),
            )
        } else {
            val baseColor = sampleBadgeColor(art)
            val lightColor = shiftColor(baseColor, 1.12f)
            val darkColor = shiftColor(baseColor, 0.74f)
            intArrayOf(
                Color.argb(LargeIconBackgroundAlpha, Color.red(lightColor), Color.green(lightColor), Color.blue(lightColor)),
                Color.argb(LargeIconBackgroundAlpha, Color.red(darkColor), Color.green(darkColor), Color.blue(darkColor)),
            )
        }

        return LinearGradient(
            badgeBounds.left,
            badgeBounds.top,
            badgeBounds.right,
            badgeBounds.bottom,
            colors,
            null,
            Shader.TileMode.CLAMP,
        )
    }

    private fun sampleBadgeColor(bitmap: Bitmap): Int {
        var redTotal = 0L
        var greenTotal = 0L
        var blueTotal = 0L
        var sampleCount = 0L
        val stepX = max(1, bitmap.width / 24)
        val stepY = max(1, bitmap.height / 24)

        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = Color.alpha(pixel)
                if (alpha > 32) {
                    redTotal += Color.red(pixel)
                    greenTotal += Color.green(pixel)
                    blueTotal += Color.blue(pixel)
                    sampleCount += 1
                }
                x += stepX
            }
            y += stepY
        }

        if (sampleCount == 0L) {
            return Color.rgb(76, 98, 132)
        }

        val averageColor = Color.rgb(
            (redTotal / sampleCount).toInt().coerceIn(0, 255),
            (greenTotal / sampleCount).toInt().coerceIn(0, 255),
            (blueTotal / sampleCount).toInt().coerceIn(0, 255),
        )
        val hsv = FloatArray(3)
        Color.colorToHSV(averageColor, hsv)
        hsv[1] = hsv[1].coerceIn(0.28f, 0.72f)
        hsv[2] = hsv[2].coerceIn(0.34f, 0.8f)
        return Color.HSVToColor(hsv)
    }

    private fun shiftColor(color: Int, factor: Float): Int = Color.rgb(
        (Color.red(color) * factor).roundToInt().coerceIn(0, 255),
        (Color.green(color) * factor).roundToInt().coerceIn(0, 255),
        (Color.blue(color) * factor).roundToInt().coerceIn(0, 255),
    )

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
