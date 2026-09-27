package com.hungry.restaurant.pos.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderStatus
import com.hungry.restaurant.pos.data.repository.OrderHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class DateFilter(val label: String) { TODAY("Today"), YESTERDAY("Yesterday"), WEEK("7 days") }

data class HistoryUiState(
    val dateFilter: DateFilter = DateFilter.TODAY,
    val query: String = "",
    val allOrders: List<Order> = emptyList(),
    val loading: Boolean = true,
) {
    val visibleOrders: List<Order>
        get() {
            val (start, end) = dateFilter.range()
            return allOrders
                .filter { it.status.isPast }
                .filter { it.placedAtMillis in start until end }
                .filter {
                    query.isBlank() ||
                        it.code.contains(query, ignoreCase = true) ||
                        it.customerName.contains(query, ignoreCase = true)
                }
                .sortedByDescending { it.placedAtMillis }
        }

    val summaryCount: Int get() = visibleOrders.size
    val summaryRevenue: Double get() = visibleOrders.filter { it.status == OrderStatus.FINISHED }.sumOf { it.total }
    val summaryCurrency: String? get() = visibleOrders.firstOrNull()?.currency
}

private fun DateFilter.range(): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val startOfToday = cal.timeInMillis
    return when (this) {
        DateFilter.TODAY -> startOfToday to Long.MAX_VALUE
        DateFilter.YESTERDAY -> (startOfToday - DAY_MS) to startOfToday
        DateFilter.WEEK -> (startOfToday - 7 * DAY_MS) to Long.MAX_VALUE
    }
}

private const val DAY_MS = 24 * 60 * 60 * 1000L

class OrderHistoryViewModel(private val historyRepository: OrderHistoryRepository) : ViewModel() {

    private val _dateFilter = MutableStateFlow(DateFilter.TODAY)
    private val _query = MutableStateFlow("")
    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _loading = MutableStateFlow(true)

    val uiState: StateFlow<HistoryUiState> = combine(
        _dateFilter, _query, _allOrders, _loading,
    ) { filter, query, orders, loading ->
        HistoryUiState(filter, query, orders, loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    init {
        refresh()
    }

    fun setDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
    }

    fun setQuery(query: String) {
        _query.value = query
    }

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            _allOrders.value = historyRepository.recentOrders()
            _loading.value = false
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                OrderHistoryViewModel(app.container.orderHistoryRepository)
            }
        }
    }
}
