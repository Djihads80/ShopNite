package com.djihad.shopnite.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djihad.shopnite.data.local.UserSettingsRepository
import com.djihad.shopnite.data.repository.FortniteRepository
import com.djihad.shopnite.model.ShopItem
import com.djihad.shopnite.model.ShopSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopUiState(
    val snapshot: ShopSnapshot = ShopSnapshot(null, null, null, emptyList()),
    val searchQuery: String = "",
    val selectedType: String = "All",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class ShopViewModel(
    private val repository: FortniteRepository,
    private val settingsRepository: UserSettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()
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

    fun refresh(language: String? = null) {
        viewModelScope.launch {
            val apiLanguage = language ?: settingsRepository.settings.first().apiLanguageTag
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getShop(apiLanguage) }
                .onSuccess { snapshot ->
                    _uiState.update { it.copy(snapshot = snapshot, isLoading = false) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Couldn't load the current item shop.")
                    }
                }
        }
    }

    fun filteredItems(): List<ShopItem> {
        val state = _uiState.value
        return state.snapshot.items.filter { item ->
            val matchesType = state.selectedType == "All" || item.typeLabel == state.selectedType
            val matchesQuery = state.searchQuery.isBlank() ||
                item.name.contains(state.searchQuery, ignoreCase = true) ||
                item.typeLabel.contains(state.searchQuery, ignoreCase = true)
            matchesType && matchesQuery
        }
    }
}
