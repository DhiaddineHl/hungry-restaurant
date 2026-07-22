package com.hungry.restaurant.pos.ui.screens.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderStatus
import com.hungry.restaurant.pos.data.repository.OrderRepository
import com.hungry.restaurant.pos.printer.SunmiPrinter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Active orders bucketed by kitchen stage for the board layout. */
data class ActiveBoard(
    val incoming: List<Order> = emptyList(),
    val preparing: List<Order> = emptyList(),
    val ready: List<Order> = emptyList(),
) {
    val total: Int get() = incoming.size + preparing.size + ready.size
}

class ActiveOrdersViewModel(
    private val orders: OrderRepository,
    val printer: SunmiPrinter,
) : ViewModel() {

    val board: StateFlow<ActiveBoard> = orders.orders
        .map { list ->
            ActiveBoard(
                incoming = list.filter { it.status == OrderStatus.NEW }.sortedBy { it.placedAtMillis },
                preparing = list.filter { it.status == OrderStatus.PREPARING }.sortedBy { it.placedAtMillis },
                ready = list.filter { it.status == OrderStatus.READY }.sortedBy { it.placedAtMillis },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActiveBoard())

    val printerStatus: StateFlow<SunmiPrinter.Status> = printer.status

    /** One-shot user-facing messages (snackbar). */
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages

    fun advance(order: Order) {
        viewModelScope.launch { orders.advance(order.id) }
    }

    fun reconnectPrinter() = printer.connect()

    fun print(order: Order) {
        viewModelScope.launch {
            printer.printReceipt(order)
                .onSuccess { _messages.tryEmit("Printed ticket #${order.shortCode}") }
                .onFailure { _messages.tryEmit(it.message ?: "Print failed") }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                ActiveOrdersViewModel(app.container.orderRepository, app.container.sunmiPrinter)
            }
        }
    }
}
