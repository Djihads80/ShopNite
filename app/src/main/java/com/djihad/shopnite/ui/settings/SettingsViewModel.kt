package com.djihad.shopnite.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djihad.shopnite.data.local.UserSettings
import com.djihad.shopnite.data.local.UserSettingsRepository
import com.djihad.shopnite.model.AccountType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
)

class SettingsViewModel(
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
}
