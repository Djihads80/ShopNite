package com.djihad.shopnite.ui.cosmetics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djihad.shopnite.data.local.UserSettingsRepository
import com.djihad.shopnite.data.repository.FortniteRepository
import com.djihad.shopnite.model.CatalogSnapshot
import com.djihad.shopnite.model.CosmeticCardItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CosmeticsUiState(
    val snapshot: CatalogSnapshot = CatalogSnapshot(emptyList(), emptySet()),
    val searchQuery: String = "",
    val selectedType: String = "All",
    val showNewOnly: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class CosmeticsViewModel(
    private val repository: FortniteRepository,
    private val settingsRepository: UserSettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CosmeticsUiState())
    val uiState: StateFlow<CosmeticsUiState> = _uiState.asStateFlow()
    private var loadedLanguage: String? = null

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                if (loadedLanguage != settings.apiLanguageTag) {
                    loadedLanguage = settings.apiLanguageTag
                    refresh(settings.apiLanguageTag)
                }
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectType(type: String) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun setShowNewOnly(enabled: Boolean) {
        _uiState.update { it.copy(showNewOnly = enabled) }
    }

    fun refresh(language: String? = null) {
        viewModelScope.launch {
            val apiLanguage = language ?: settingsRepository.settings.first().apiLanguageTag
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getCatalog(apiLanguage) }
                .onSuccess { snapshot ->
                    _uiState.update { it.copy(snapshot = snapshot, isLoading = false) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Couldn't load cosmetics right now.")
                    }
                }
        }
    }

    fun filteredItems(): List<CosmeticCardItem> {
        val state = _uiState.value
        return state.snapshot.items.filter { item ->
            val matchesNew = !state.showNewOnly || item.isNew
            val matchesType = state.selectedType == "All" || item.typeLabel == state.selectedType
            val matchesQuery = state.searchQuery.isBlank() ||
                item.name.contains(state.searchQuery, ignoreCase = true) ||
                item.typeLabel.contains(state.searchQuery, ignoreCase = true)
            matchesNew && matchesType && matchesQuery
        }
    }
}
