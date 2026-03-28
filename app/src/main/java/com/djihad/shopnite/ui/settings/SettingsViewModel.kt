package com.djihad.shopnite.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djihad.shopnite.BuildConfig
import com.djihad.shopnite.ShopNiteApplication
import com.djihad.shopnite.data.local.UserSettings
import com.djihad.shopnite.data.local.UserSettingsRepository
import com.djihad.shopnite.data.repository.FortniteRepository
import com.djihad.shopnite.model.AccountType
import com.djihad.shopnite.notifications.DebugNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isValidatingProfile: Boolean = false,
    val profileValidationMessage: String? = null,
)

class SettingsViewModel(
    private val app: ShopNiteApplication,
    private val repository: FortniteRepository,
    private val settingsRepository: UserSettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun validateAndSaveProfile(
        apiKey: String,
        name: String,
        accountType: AccountType,
    ) {
        val trimmedName = name.trim()
        val effectiveApiKey = apiKey.trim().ifBlank { BuildConfig.FORTNITE_API_KEY.trim() }
        if (trimmedName.isBlank()) {
            viewModelScope.launch {
                settingsRepository.saveProfile("", accountType)
                _uiState.update {
                    it.copy(
                        isValidatingProfile = false,
                        profileValidationMessage = null,
                    )
                }
            }
            return
        }

        if (effectiveApiKey.isBlank()) {
            _uiState.update {
                it.copy(
                    isValidatingProfile = false,
                    profileValidationMessage = "Add a Fortnite-API key below to validate that player profile.",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isValidatingProfile = true,
                    profileValidationMessage = null,
                )
            }

            runCatching {
                repository.getBattleRoyaleSummary(
                    apiKey = effectiveApiKey,
                    playerName = trimmedName,
                    accountType = accountType,
                )
            }.onSuccess {
                settingsRepository.saveProfile(trimmedName, accountType)
                _uiState.update {
                    it.copy(
                        isValidatingProfile = false,
                        profileValidationMessage = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isValidatingProfile = false,
                        profileValidationMessage = friendlyProfileError(throwable),
                    )
                }
            }
        }
    }

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch { settingsRepository.saveApiKey(apiKey) }
    }

    fun saveProfile(name: String, accountType: AccountType) {
        viewModelScope.launch { settingsRepository.saveProfile(name, accountType) }
    }

    fun saveApiLanguage(tag: String) {
        viewModelScope.launch { settingsRepository.saveApiLanguage(tag) }
    }

    fun saveAppLanguage(tag: String) {
        viewModelScope.launch { settingsRepository.saveAppLanguage(tag) }
    }

    fun updateNotificationPreferences(
        notifyReturns: Boolean? = null,
        notifyLeavingSoon: Boolean? = null,
    ) {
        viewModelScope.launch {
            settingsRepository.saveNotificationPreferences(
                notifyReturns = notifyReturns,
                notifyLeavingSoon = notifyLeavingSoon,
            )
        }
    }

    fun unlockDebugMenu() {
        viewModelScope.launch { settingsRepository.unlockDebugMenu() }
    }

    fun setForceCosmeticNotificationButtonEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveDebugPreferences(
                forceCosmeticNotificationButtonEnabled = enabled,
            )
        }
    }

    fun forceSendWishlistNotification() {
        viewModelScope.launch {
            DebugNotificationManager.forceWishlistReturnNotifications(
                context = app.applicationContext,
                repository = repository,
                settings = _uiState.value.settings,
            )
        }
    }

    fun forceSendWishlistLeavingNotification() {
        viewModelScope.launch {
            DebugNotificationManager.forceWishlistLeavingNotifications(
                context = app.applicationContext,
                repository = repository,
                settings = _uiState.value.settings,
            )
        }
    }

    private fun friendlyProfileError(throwable: Throwable): String = when (throwable) {
        is HttpException -> when (throwable.code()) {
            403 -> "The current API key could not access that player profile."
            404 -> "That Fortnite username was not found for the selected platform."
            429 -> "Fortnite-API rate limited this profile lookup. Try again in a moment."
            else -> "Fortnite-API could not validate that player profile right now."
        }
        is IOException -> "Couldn't reach Fortnite-API to validate that player profile."
        else -> "Fortnite-API could not validate that player profile right now."
    }
}
