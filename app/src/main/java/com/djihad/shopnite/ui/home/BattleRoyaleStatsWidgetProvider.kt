package com.djihad.shopnite.ui.home

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
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
                bindViews(settings, summary)
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

    private fun RemoteViews.bindViews(settings: UserSettings, summary: BrSummary?) {
        val titleText = summary?.playerName?.takeIf { it.isNotBlank() }
            ?: settings.playerName.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.widget_battle_royale_setup)

        setTextViewText(R.id.widget_title, context.getString(R.string.widget_battle_royale_title))
        setTextViewText(R.id.widget_player_name, titleText)
        setTextViewText(R.id.widget_subtitle, summary?.let { context.getString(R.string.widget_battle_royale_subtitle) } ?: context.getString(R.string.widget_battle_royale_setup))

        val statRows = summary?.statTiles?.take(3).orEmpty()
        when (statRows.size) {
            0 -> {
                setTextViewText(R.id.widget_stat_1_label, "")
                setTextViewText(R.id.widget_stat_1_value, "")
                setTextViewText(R.id.widget_stat_2_label, "")
                setTextViewText(R.id.widget_stat_2_value, "")
                setTextViewText(R.id.widget_stat_3_label, "")
                setTextViewText(R.id.widget_stat_3_value, "")
            }
            1 -> bindStatRow(R.id.widget_stat_1_label, R.id.widget_stat_1_value, statRows[0])
            2 -> {
                bindStatRow(R.id.widget_stat_1_label, R.id.widget_stat_1_value, statRows[0])
                bindStatRow(R.id.widget_stat_2_label, R.id.widget_stat_2_value, statRows[1])
            }
            else -> {
                bindStatRow(R.id.widget_stat_1_label, R.id.widget_stat_1_value, statRows[0])
                bindStatRow(R.id.widget_stat_2_label, R.id.widget_stat_2_value, statRows[1])
                bindStatRow(R.id.widget_stat_3_label, R.id.widget_stat_3_value, statRows[2])
            }
        }
    }

    private fun RemoteViews.bindStatRow(labelId: Int, valueId: Int, stat: SummaryStat) {
        setTextViewText(labelId, stat.label)
        setTextViewText(valueId, stat.value)
    }
}
