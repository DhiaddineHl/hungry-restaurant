package com.hungry.restaurant.pos.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderStatus
import com.hungry.restaurant.pos.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class HistoryFilter(val label: String) {
    ALL("All"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
}

data class HistoryUiState(
    val filter: HistoryFilter = HistoryFilter.ALL,
    val orders: List<Order> = emptyList(),
    val completedCount: Int = 0,
    val cancelledCount: Int = 0,
    val totalRevenueCents: Int = 0,
)

class OrderHistoryViewModel(orders: OrderRepository) : ViewModel() {

    private val _filter = MutableStateFlow(HistoryFilter.ALL)

    val uiState: StateFlow<HistoryUiState> = combine(orders.orders, _filter) { list, filter ->
        val past = list
            .filter { it.status == OrderStatus.COMPLETED || it.status == OrderStatus.CANCELLED }
            .sortedByDescending { it.placedAtMillis }
        val visible = when (filter) {
            HistoryFilter.ALL -> past
            HistoryFilter.COMPLETED -> past.filter { it.status == OrderStatus.COMPLETED }
            HistoryFilter.CANCELLED -> past.filter { it.status == OrderStatus.CANCELLED }
        }
        HistoryUiState(
            filter = filter,
            orders = visible,
            completedCount = past.count { it.status == OrderStatus.COMPLETED },
            cancelledCount = past.count { it.status == OrderStatus.CANCELLED },
            totalRevenueCents = past.filter { it.status == OrderStatus.COMPLETED }.sumOf { it.totalCents },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun setFilter(filter: HistoryFilter) {
        _filter.value = filter
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                OrderHistoryViewModel(app.container.orderRepository)
            }
        }
    }
}
