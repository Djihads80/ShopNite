package com.djihad.shopnite.ui.home

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.widget.RemoteViews
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.toArgb
import com.djihad.shopnite.MainActivity
import com.djihad.shopnite.R
import com.djihad.shopnite.ShopNiteApplication
import com.djihad.shopnite.data.local.UserSettings
import com.djihad.shopnite.model.BrSummary
import com.djihad.shopnite.model.SummaryStat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BattleRoyaleStatsWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, BattleRoyaleStatsWidgetProvider::class.java)
        appWidgetManager.getAppWidgetIds(componentName).forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val appContext = context.applicationContext
        val app = appContext as? ShopNiteApplication ?: return
        val container = app.appContainer

        CoroutineScope(Dispatchers.IO).launch {
            val settings = container.userSettingsRepository.settings.first()
            val summary = loadSummary(container, settings)

            val views = RemoteViews(appContext.packageName, R.layout.widget_battle_royale_stats).apply {
                bindViews(appContext, settings, summary)
                val launchIntent = Intent(appContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    appContext,
                    appWidgetId,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            withContext(Dispatchers.Main) {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private suspend fun loadSummary(
        container: com.djihad.shopnite.data.AppContainer,
        settings: UserSettings,
    ): BrSummary? {
        if (settings.playerName.isBlank() || settings.apiKey.isBlank()) {
            return null
        }

        return runCatching {
            container.fortniteRepository.getBattleRoyaleSummary(
                apiKey = settings.apiKey,
                playerName = settings.playerName,
                accountType = settings.accountType,
            )
        }.getOrNull()
    }

    private fun RemoteViews.bindViews(context: Context, settings: UserSettings, summary: BrSummary?) {
        val titleText = summary?.playerName?.takeIf { it.isNotBlank() }
            ?: settings.playerName.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.widget_battle_royale_setup)

        setTextViewText(R.id.widget_title, context.getString(R.string.widget_battle_royale_title))
        setTextViewText(R.id.widget_player_name, titleText)
        setTextViewText(R.id.widget_subtitle, summary?.let { context.getString(R.string.widget_battle_royale_subtitle) } ?: context.getString(R.string.widget_battle_royale_setup))

        applyWidgetThemeColors(context)

        val statTiles = summary?.statTiles.orEmpty()
        val visibleStats = statTiles.take(if (statTiles.size >= 9) 9 else statTiles.size)

        clearStatRows()
        visibleStats.forEachIndexed { index, stat ->
            bindStatRow(index, stat)
        }
    }

    private fun RemoteViews.clearStatRows() {
        for (index in 0 until 9) {
            val labelId = when (index) {
                0 -> R.id.widget_stat_1_label
                1 -> R.id.widget_stat_2_label
                2 -> R.id.widget_stat_3_label
                3 -> R.id.widget_stat_4_label
                4 -> R.id.widget_stat_5_label
                5 -> R.id.widget_stat_6_label
                6 -> R.id.widget_stat_7_label
                7 -> R.id.widget_stat_8_label
                else -> R.id.widget_stat_9_label
            }
            val valueId = when (index) {
                0 -> R.id.widget_stat_1_value
                1 -> R.id.widget_stat_2_value
                2 -> R.id.widget_stat_3_value
                3 -> R.id.widget_stat_4_value
                4 -> R.id.widget_stat_5_value
                5 -> R.id.widget_stat_6_value
                6 -> R.id.widget_stat_7_value
                7 -> R.id.widget_stat_8_value
                else -> R.id.widget_stat_9_value
            }
            setTextViewText(labelId, "")
            setTextViewText(valueId, "")
        }
    }

    private fun RemoteViews.bindStatRow(index: Int, stat: SummaryStat) {
        val labelId = when (index) {
            0 -> R.id.widget_stat_1_label
            1 -> R.id.widget_stat_2_label
            2 -> R.id.widget_stat_3_label
            3 -> R.id.widget_stat_4_label
            4 -> R.id.widget_stat_5_label
            5 -> R.id.widget_stat_6_label
            6 -> R.id.widget_stat_7_label
            7 -> R.id.widget_stat_8_label
            else -> R.id.widget_stat_9_label
        }
        val valueId = when (index) {
            0 -> R.id.widget_stat_1_value
            1 -> R.id.widget_stat_2_value
            2 -> R.id.widget_stat_3_value
            3 -> R.id.widget_stat_4_value
            4 -> R.id.widget_stat_5_value
            5 -> R.id.widget_stat_6_value
            6 -> R.id.widget_stat_7_value
            7 -> R.id.widget_stat_8_value
            else -> R.id.widget_stat_9_value
        }
        setTextViewText(labelId, stat.label)
        setTextViewText(valueId, stat.value)
    }

    private fun RemoteViews.applyWidgetThemeColors(context: Context) {
        val colorScheme = context.resolveWidgetColorScheme()
        val rootBackgroundColor = colorScheme.surfaceVariant.toArgb()
        val headerBackgroundColor = colorScheme.primary.toArgb()
        val statCardBackgroundColor = colorScheme.secondaryContainer.toArgb()
        val headerTextColor = colorScheme.onPrimary.toArgb()
        val statLabelTextColor = colorScheme.onSurfaceVariant.toArgb()
        val statValueTextColor = colorScheme.onSurface.toArgb()

        setInt(R.id.widget_root_bg, "setColorFilter", rootBackgroundColor)
        setInt(R.id.widget_header_bg_img, "setColorFilter", headerBackgroundColor)

        listOf(
            R.id.widget_stat_card_bg_1,
            R.id.widget_stat_card_bg_2,
            R.id.widget_stat_card_bg_3,
            R.id.widget_stat_card_bg_4,
            R.id.widget_stat_card_bg_5,
            R.id.widget_stat_card_bg_6,
            R.id.widget_stat_card_bg_7,
            R.id.widget_stat_card_bg_8,
            R.id.widget_stat_card_bg_9,
        ).forEach { cardBgId ->
            setInt(cardBgId, "setColorFilter", statCardBackgroundColor)
        }

        listOf(
            R.id.widget_title,
            R.id.widget_player_name,
            R.id.widget_subtitle,
        ).forEach { textId ->
            setTextColor(textId, headerTextColor)
        }

        listOf(
            R.id.widget_stat_1_label,
            R.id.widget_stat_2_label,
            R.id.widget_stat_3_label,
            R.id.widget_stat_4_label,
            R.id.widget_stat_5_label,
            R.id.widget_stat_6_label,
            R.id.widget_stat_7_label,
            R.id.widget_stat_8_label,
            R.id.widget_stat_9_label,
        ).forEach { textId ->
            setTextColor(textId, statLabelTextColor)
        }

        listOf(
            R.id.widget_stat_1_value,
            R.id.widget_stat_2_value,
            R.id.widget_stat_3_value,
            R.id.widget_stat_4_value,
            R.id.widget_stat_5_value,
            R.id.widget_stat_6_value,
            R.id.widget_stat_7_value,
            R.id.widget_stat_8_value,
            R.id.widget_stat_9_value,
        ).forEach { textId ->
            setTextColor(textId, statValueTextColor)
        }
    }

    private fun Context.resolveWidgetColorScheme(): ColorScheme {
        val isDarkTheme = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDarkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
        } else {
            if (isDarkTheme) darkColorScheme(
                primary = androidx.compose.ui.graphics.Color(0xFF0091FF),
                onPrimary = androidx.compose.ui.graphics.Color(0xFF001E3C),
                secondary = androidx.compose.ui.graphics.Color(0xFFFFD166),
                onSecondary = androidx.compose.ui.graphics.Color(0xFF001E3C),
                tertiary = androidx.compose.ui.graphics.Color(0xFF5D99FF),
                background = androidx.compose.ui.graphics.Color(0xFF0A1221),
                onBackground = androidx.compose.ui.graphics.Color(0xFFF3F4F8),
                surface = androidx.compose.ui.graphics.Color(0xFF121B2B),
                onSurface = androidx.compose.ui.graphics.Color(0xFFF3F4F8),
                surfaceVariant = androidx.compose.ui.graphics.Color(0xFF384969),
                onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFBFC7DB),
                outline = androidx.compose.ui.graphics.Color(0xFF5D99FF),
            ) else lightColorScheme(
                primary = androidx.compose.ui.graphics.Color(0xFF0091FF),
                onPrimary = androidx.compose.ui.graphics.Color(0xFF001E3C),
                secondary = androidx.compose.ui.graphics.Color(0xFFFFD166),
                onSecondary = androidx.compose.ui.graphics.Color(0xFF001E3C),
                tertiary = androidx.compose.ui.graphics.Color(0xFF0E2D52),
                background = androidx.compose.ui.graphics.Color(0xFFF8FAFF),
                onBackground = androidx.compose.ui.graphics.Color(0xFF001E3C),
                surface = androidx.compose.ui.graphics.Color(0xFFF8FAFF),
                onSurface = androidx.compose.ui.graphics.Color(0xFF001E3C),
                surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE7EEFF),
                onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF4B5563),
                outline = androidx.compose.ui.graphics.Color(0xFF7B92C9),
            )
        }
    }
}
