package com.dari.dermek

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = true,
    val items: List<RegulationItem> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: Int = 0,
    val selectedLanguage: Language = Language.RU,
    val selectedItem: RegulationItem? = null,
    val syncSource: String = "Local Cache (Offline)",
    val checkedItems: Set<String> = emptySet()
)

class MainScreenModel(
    private val repository: RegulationRepository = RegulationRepository()
) : ScreenModel {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadRegulations()
    }

    fun loadRegulations() {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val data = repository.getRegulations()
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    items = data,
                    syncSource = repository.lastSyncSource
                ) 
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex, selectedItem = null, checkedItems = emptySet()) }
    }

    fun onItemSelected(item: RegulationItem?) {
        _uiState.update { it.copy(selectedItem = item, checkedItems = emptySet()) }
    }

    fun toggleChecklistItem(itemText: String) {
        _uiState.update { state ->
            val updated = if (state.checkedItems.contains(itemText)) {
                state.checkedItems - itemText
            } else {
                state.checkedItems + itemText
            }
            state.copy(checkedItems = updated)
        }
    }

    fun toggleLanguage() {
        _uiState.update {
            val nextLang = if (it.selectedLanguage == Language.RU) Language.KK else Language.RU
            it.copy(selectedLanguage = nextLang)
        }
    }
}
