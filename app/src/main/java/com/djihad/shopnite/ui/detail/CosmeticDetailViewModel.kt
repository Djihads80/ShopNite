package com.djihad.shopnite.ui.detail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djihad.shopnite.ShopNiteApplication
import com.djihad.shopnite.data.local.UserSettingsRepository
import com.djihad.shopnite.data.repository.FortniteRepository
import com.djihad.shopnite.model.CosmeticDetail
import com.djihad.shopnite.notifications.NotificationChannels
import com.djihad.shopnite.notifications.ShopItemNotifications
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CosmeticDetailUiState(
    val detail: CosmeticDetail? = null,
    val isWishlisted: Boolean = false,
    val showForceNotificationDebugButton: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class CosmeticDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val app: ShopNiteApplication,
    private val repository: FortniteRepository,
    private val settingsRepository: UserSettingsRepository,
) : ViewModel() {
    private val cosmeticId: String = Uri.decode(checkNotNull(savedStateHandle["cosmeticId"]))
    private var loadedLanguage: String? = null

    private val _uiState = MutableStateFlow(CosmeticDetailUiState())
    val uiState: StateFlow<CosmeticDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update {
                    it.copy(
                        isWishlisted = cosmeticId in settings.wishlist,
                        showForceNotificationDebugButton = settings.debugForceCosmeticNotificationButtonEnabled,
                    )
                }
                if (loadedLanguage != settings.apiLanguageTag) {
                    loadedLanguage = settings.apiLanguageTag
                    refresh(settings.apiLanguageTag)
                }
            }
        }
    }

    fun toggleWishlist() {
        viewModelScope.launch {
            settingsRepository.toggleWishlist(cosmeticId)
        }
    }

    fun forceSendWishlistNotification() {
        val detail = _uiState.value.detail ?: return
        NotificationChannels.create(app.applicationContext)
        ShopItemNotifications.showDebugWishlistReturn(
            context = app.applicationContext,
            cosmeticName = detail.cosmetic.name,
            cosmeticType = detail.cosmetic.typeLabel,
            price = detail.currentShopItem?.price,
            outDate = detail.currentShopItem?.outDate,
        )
    }

    private fun refresh(language: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getCosmeticDetail(language, cosmeticId) }
                .onSuccess { detail ->
                    _uiState.update {
                        it.copy(
                            detail = detail,
                            isLoading = false,
                            errorMessage = if (detail == null) "That cosmetic couldn't be found." else null,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Couldn't load cosmetic details.")
                    }
                }
        }
    }
}
