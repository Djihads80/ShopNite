package com.djihad.shopnite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.djihad.shopnite.ui.ShopNiteApp
import com.djihad.shopnite.ui.theme.ShopNiteTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val app = application as ShopNiteApplication
        applyLocale(app)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        lifecycleScope.launch {
            app.appContainer.userSettingsRepository.settings.collect { settings ->
                val locales = if (settings.appLanguageTag == "system") {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(settings.appLanguageTag)
                }
                AppCompatDelegate.setApplicationLocales(locales)
            }
        }

        setContent {
            ShopNiteTheme {
                ShopNiteApp()
            }
        }
    }

    private fun applyLocale(app: ShopNiteApplication) {
        val settings = runBlocking { app.appContainer.userSettingsRepository.settings.first() }
        val locales = if (settings.appLanguageTag == "system") {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(settings.appLanguageTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
