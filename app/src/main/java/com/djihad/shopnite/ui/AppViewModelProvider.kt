package com.djihad.shopnite.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.djihad.shopnite.ShopNiteApplication
import com.djihad.shopnite.ui.cosmetics.CosmeticsViewModel
import com.djihad.shopnite.ui.detail.CosmeticDetailViewModel
import com.djihad.shopnite.ui.home.HomeViewModel
import com.djihad.shopnite.ui.settings.SettingsViewModel
import com.djihad.shopnite.ui.shop.ShopViewModel

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                shopNiteApplication().appContainer.fortniteRepository,
                shopNiteApplication().appContainer.userSettingsRepository,
            )
        }
        initializer {
            ShopViewModel(
                shopNiteApplication().appContainer.fortniteRepository,
                shopNiteApplication().appContainer.userSettingsRepository,
            )
        }
        initializer {
            CosmeticsViewModel(
                shopNiteApplication().appContainer.fortniteRepository,
                shopNiteApplication().appContainer.userSettingsRepository,
            )
        }
        initializer {
            SettingsViewModel(
                shopNiteApplication(),
                shopNiteApplication().appContainer.fortniteRepository,
                shopNiteApplication().appContainer.userSettingsRepository,
            )
        }
        initializer {
            CosmeticDetailViewModel(
                savedStateHandle = createSavedStateHandle(),
                app = shopNiteApplication(),
                repository = shopNiteApplication().appContainer.fortniteRepository,
                settingsRepository = shopNiteApplication().appContainer.userSettingsRepository,
            )
        }
    }
}

private fun CreationExtras.shopNiteApplication(): ShopNiteApplication =
    this[APPLICATION_KEY] as ShopNiteApplication
