package com.hungry.restaurant.pos.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderStatus
import com.hungry.restaurant.pos.data.repository.OrderRepository
import com.hungry.restaurant.pos.printer.SunmiPrinter
import com.hungry.restaurant.pos.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderDetailsViewModel(
    private val orderId: String,
    private val orders: OrderRepository,
    private val printer: SunmiPrinter,
) : ViewModel() {

    val order: StateFlow<Order?> = orders.orders
        .map { list -> list.firstOrNull { it.id == orderId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), orders.orderById(orderId))

    val printerStatus: StateFlow<SunmiPrinter.Status> = printer.status

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages

    fun advance() {
        viewModelScope.launch { orders.advance(orderId) }
    }

    fun cancel() {
        viewModelScope.launch { orders.updateStatus(orderId, OrderStatus.CANCELLED) }
    }

    fun print() {
        val current = order.value ?: return
        viewModelScope.launch {
            printer.printReceipt(current)
                .onSuccess { _messages.tryEmit("Printed ticket #${current.shortCode}") }
                .onFailure { _messages.tryEmit(it.message ?: "Print failed") }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                val handle = createSavedStateHandle()
                val orderId = handle.get<String>(Routes.DETAILS_ARG).orEmpty()
                OrderDetailsViewModel(orderId, app.container.orderRepository, app.container.sunmiPrinter)
            }
        }
    }
}
