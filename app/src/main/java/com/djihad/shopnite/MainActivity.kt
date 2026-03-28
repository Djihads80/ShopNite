package com.djihad.shopnite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.djihad.shopnite.ui.ShopNiteApp
import com.djihad.shopnite.ui.theme.ShopNiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ShopNiteTheme {
                ShopNiteApp()
            }
        }
    }
}
