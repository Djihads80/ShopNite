package com.djihad.shopnite.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djihad.shopnite.data.local.UserSettings
import com.djihad.shopnite.data.local.UserSettingsRepository
import com.djihad.shopnite.data.repository.FortniteRepository
import com.djihad.shopnite.model.AccountType
import com.djihad.shopnite.model.BrSummary
import com.djihad.shopnite.model.NewsCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val settings: UserSettings = UserSettings(),
    val summary: BrSummary? = null,
    val news: List<NewsCard> = emptyList(),
    val isLoadingSummary: Boolean = false,
    val isLoadingNews: Boolean = false,
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val repository: FortniteRepository,
    private val settingsRepository: UserSettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var loadedNewsLanguage: String? = null
    private var loadedSummaryKey: String? = null

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
                if (loadedNewsLanguage != settings.apiLanguageTag) {
                    loadedNewsLanguage = settings.apiLanguageTag
                    refreshNews(settings.apiLanguageTag)
                }
                val summaryKey = "${settings.apiKey}|${settings.playerName}|${settings.accountType.apiValue}"
                if (loadedSummaryKey != summaryKey) {
                    loadedSummaryKey = summaryKey
                    refreshSummary()
                }
            }
        }
    }

    fun saveProfile(name: String, accountType: AccountType) {
        viewModelScope.launch {
            settingsRepository.saveProfile(name, accountType)
        }
    }

    fun refreshAll() {
        refreshNews(_uiState.value.settings.apiLanguageTag)
        refreshSummary()
    }

    private fun refreshNews(language: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingNews = true, errorMessage = null) }
            runCatching { repository.getBattleRoyaleNews(language) }
                .onSuccess { news ->
                    _uiState.update { it.copy(news = news, isLoadingNews = false) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoadingNews = false,
                            errorMessage = "Couldn't load Battle Royale news right now.",
                        )
                    }
                }
        }
    }

    private fun refreshSummary() {
        val settings = _uiState.value.settings
        if (settings.playerName.isBlank() || settings.apiKey.isBlank()) {
            _uiState.update { it.copy(summary = null, isLoadingSummary = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSummary = true, errorMessage = null) }
            runCatching {
                repository.getBattleRoyaleSummary(
                    apiKey = settings.apiKey,
                    playerName = settings.playerName,
                    accountType = settings.accountType,
                )
            }.onSuccess { summary ->
                _uiState.update { it.copy(summary = summary, isLoadingSummary = false) }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        summary = null,
                        isLoadingSummary = false,
                        errorMessage = "BR stats need a valid Fortnite API key and player profile.",
                    )
                }
            }
        }
    }
}
