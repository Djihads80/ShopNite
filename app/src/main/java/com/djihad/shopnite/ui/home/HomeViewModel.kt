package com.djihad.shopnite.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djihad.shopnite.data.local.UserSettings
import com.djihad.shopnite.data.local.UserSettingsRepository
import com.djihad.shopnite.data.repository.FortniteRepository
import com.djihad.shopnite.model.BrSummary
import com.djihad.shopnite.model.NewsCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class HomeUiState(
    val settings: UserSettings = UserSettings(),
    val summary: BrSummary? = null,
    val news: List<NewsCard> = emptyList(),
    val isLoadingSummary: Boolean = false,
    val isLoadingNews: Boolean = false,
    val summaryErrorMessage: String? = null,
    val newsErrorMessage: String? = null,
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

    fun refreshAll() {
        refreshNews(_uiState.value.settings.apiLanguageTag)
        refreshSummary()
    }

    private fun refreshNews(language: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingNews = true, newsErrorMessage = null) }
            runCatching { repository.getBattleRoyaleNews(language) }
                .onSuccess { news ->
                    _uiState.update { it.copy(news = news, isLoadingNews = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingNews = false,
                            newsErrorMessage = friendlyErrorMessage(throwable, fallback = "Couldn't load Battle Royale news right now."),
                        )
                    }
                }
        }
    }

    private fun refreshSummary() {
        val settings = _uiState.value.settings
        if (settings.playerName.isBlank() || settings.apiKey.isBlank()) {
            _uiState.update { it.copy(summary = null, isLoadingSummary = false, summaryErrorMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSummary = true, summaryErrorMessage = null) }
            runCatching {
                repository.getBattleRoyaleSummary(
                    apiKey = settings.apiKey,
                    playerName = settings.playerName,
                    accountType = settings.accountType,
                )
            }.onSuccess { summary ->
                _uiState.update { it.copy(summary = summary, isLoadingSummary = false, summaryErrorMessage = null) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        summary = null,
                        isLoadingSummary = false,
                        summaryErrorMessage = friendlyErrorMessage(
                            throwable,
                            fallback = "Couldn't load the current season BR summary.",
                        ),
                    )
                }
            }
        }
    }

    private fun friendlyErrorMessage(
        throwable: Throwable,
        fallback: String,
    ): String = when (throwable) {
        is HttpException -> when (throwable.code()) {
            403 -> "The current API key could not access that Battle Royale profile."
            404 -> "That Fortnite username was not found for the selected platform."
            429 -> "Fortnite-API rate limited this request. Please try again in a moment."
            else -> fallback
        }
        is IOException -> "Couldn't reach Fortnite-API right now. Check your connection and try again."
        else -> fallback
    }
}
