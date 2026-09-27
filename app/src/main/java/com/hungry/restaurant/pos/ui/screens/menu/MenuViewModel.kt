package com.hungry.restaurant.pos.ui.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.data.model.MenuItem
import com.hungry.restaurant.pos.data.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MenuUiState(
    val items: List<MenuItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val query: String = "",
    val category: String? = null,
) {
    val visibleItems: List<MenuItem>
        get() = items
            .filter { category == null || it.category == category }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .sortedBy { it.name }

    val offTodayCount: Int get() = items.count { !it.available }
}

class MenuViewModel(private val menuRepository: MenuRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _category = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MenuUiState> = combine(
        menuRepository.items, _query, _category,
    ) { items, query, category ->
        MenuUiState(
            items = items,
            categories = items.mapNotNull { it.category }.distinct().sorted(),
            query = query,
            category = category,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { menuRepository.refresh() }
    }

    fun setQuery(query: String) {
        _query.value = query
    }

    fun setCategory(category: String?) {
        _category.value = category
    }

    fun setAvailable(item: MenuItem, available: Boolean) {
        viewModelScope.launch {
            menuRepository.setAvailability(item.id, available, if (!available) "Off for today" else null)
        }
    }

    fun setPrepTime(item: MenuItem, minutes: Int) {
        viewModelScope.launch { menuRepository.setPrepTime(item.id, minutes) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                MenuViewModel(app.container.menuRepository)
            }
        }
    }
}
