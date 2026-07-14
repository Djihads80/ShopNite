package com.djihad.shopnite.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djihad.shopnite.data.local.UserSettingsRepository
import com.djihad.shopnite.data.repository.FortniteRepository
import com.djihad.shopnite.model.CosmeticFilters
import com.djihad.shopnite.model.CosmeticSort
import com.djihad.shopnite.model.CosmeticSource
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
    val selectedType: String = CosmeticFilters.All,
    val selectedRarity: String = CosmeticFilters.All,
    val selectedSort: CosmeticSort = CosmeticSort.NewestFirst,
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

    fun selectRarity(rarity: String) {
        _uiState.update { it.copy(selectedRarity = rarity) }
    }

    fun selectSort(sort: CosmeticSort) {
        _uiState.update { it.copy(selectedSort = sort) }
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
            val matchesType = state.selectedType == CosmeticFilters.All || item.filterLabel == state.selectedType
            val matchesRarity = state.selectedRarity == CosmeticFilters.All || item.rarityLabel == state.selectedRarity
            val matchesQuery = state.searchQuery.isBlank() ||
                item.name.contains(state.searchQuery, ignoreCase = true) ||
                item.typeLabel.contains(state.searchQuery, ignoreCase = true) ||
                item.filterLabel.contains(state.searchQuery, ignoreCase = true)
            matchesType && matchesRarity && matchesQuery
        }.sortedWith(state.selectedSort.comparator())
    }

    private fun CosmeticSort.comparator(): Comparator<ShopItem> = when (this) {
        CosmeticSort.NewestFirst -> compareByDescending<ShopItem> { it.addedDate.orEmpty() }
            .thenBy { it.name.lowercase() }
        CosmeticSort.OldestFirst -> compareBy<ShopItem> { it.addedDate.orEmpty() }
            .thenBy { it.name.lowercase() }
        CosmeticSort.Series -> compareBy<ShopItem> { if (it.source == CosmeticSource.Cars) 1 else 0 }
            .thenBy { (it.seriesName ?: it.rarityLabel).lowercase() }
            .thenBy { it.name.lowercase() }
        CosmeticSort.AToZ -> compareBy { it.name.lowercase() }
        CosmeticSort.ZToA -> compareByDescending { it.name.lowercase() }
    }
}
