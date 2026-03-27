package com.djihad.shopnite.ui.cosmetics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djihad.shopnite.data.local.UserSettingsRepository
import com.djihad.shopnite.data.repository.FortniteRepository
import com.djihad.shopnite.model.CatalogSnapshot
import com.djihad.shopnite.model.CosmeticCardItem
import com.djihad.shopnite.model.CosmeticFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CosmeticsUiState(
    val snapshot: CatalogSnapshot = CatalogSnapshot(emptyList(), emptySet()),
    val wishlist: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selectedType: String = CosmeticFilters.All,
    val selectedCollection: String = CosmeticFilters.All,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val debugDetails: String? = null,
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
                _uiState.update { it.copy(wishlist = settings.wishlist) }
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

    fun selectCollection(collection: String) {
        _uiState.update { it.copy(selectedCollection = collection) }
    }

    fun refresh(language: String? = null) {
        viewModelScope.launch {
            val apiLanguage = language ?: settingsRepository.settings.first().apiLanguageTag
            _uiState.update { it.copy(isLoading = true, errorMessage = null, debugDetails = null) }
            runCatching { repository.getCatalog(apiLanguage) }
                .onSuccess { snapshot ->
                    _uiState.update { it.copy(snapshot = snapshot, isLoading = false, debugDetails = null) }
                }
                .onFailure { throwable ->
                    Log.e("CosmeticsViewModel", "Catalog load failed", throwable)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Couldn't load cosmetics right now.",
                            debugDetails = buildDebugDetails(throwable),
                        )
                    }
                }
        }
    }

    fun filteredItems(): List<CosmeticCardItem> {
        val state = _uiState.value
        return state.snapshot.items.filter { item ->
            val matchesCollection = when (state.selectedCollection) {
                CosmeticFilters.New -> item.isNew
                CosmeticFilters.Wishlist -> item.id in state.wishlist
                else -> true
            }
            val matchesType = state.selectedType == CosmeticFilters.All || item.filterLabel == state.selectedType
            val matchesQuery = state.searchQuery.isBlank() ||
                item.name.contains(state.searchQuery, ignoreCase = true) ||
                item.typeLabel.contains(state.searchQuery, ignoreCase = true) ||
                item.filterLabel.contains(state.searchQuery, ignoreCase = true)
            matchesCollection && matchesType && matchesQuery
        }
    }

    private fun buildDebugDetails(throwable: Throwable): String {
        val cause = throwable.cause
        val primary = throwable.message?.takeIf { it.isNotBlank() }
        val secondary = cause?.message?.takeIf { it.isNotBlank() }
        val detail = listOfNotNull(primary, secondary)
            .distinct()
            .joinToString(" | ")
            .take(280)
        return listOfNotNull(
            throwable::class.simpleName,
            detail.takeIf { it.isNotBlank() },
        ).joinToString(": ")
    }
}
