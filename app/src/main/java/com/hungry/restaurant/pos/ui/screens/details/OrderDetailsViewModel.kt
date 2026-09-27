package com.hungry.restaurant.pos.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.repository.OrderRepository
import com.hungry.restaurant.pos.data.repository.PosSettingsRepository
import com.hungry.restaurant.pos.printer.SunmiPrinter
import com.hungry.restaurant.pos.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderDetailsViewModel(
    private val orderId: String,
    private val orders: OrderRepository,
    private val printer: SunmiPrinter,
    posSettingsRepository: PosSettingsRepository,
) : ViewModel() {

    private val _order = MutableStateFlow(orders.orderById(orderId))
    val order: StateFlow<Order?> = _order.asStateFlow()

    val printerStatus: StateFlow<SunmiPrinter.Status> = printer.status

    /**
     * Estimated prep window for the "Due HH:mm · X min left" countdown - the
     * backend has no per-order prep-time field, so this borrows the
     * restaurant's own default (Settings screen) rather than showing nothing.
     * Falls back to 20 while settings haven't loaded yet.
     */
    val defaultPrepTimeMinutes: StateFlow<Int> = posSettingsRepository.settings
        .map { it?.defaultPrepTimeMinutes ?: 20 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 20)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages

    init {
        viewModelScope.launch {
            // Board cache has it (active order) -> just watch it for live updates; otherwise
            // (opened from History, or the app just started) read it straight from the server.
            if (_order.value == null) {
                _order.value = orders.fetchOrder(orderId)
            }
        }
        viewModelScope.launch {
            orders.orders.collect { list ->
                list.firstOrNull { it.id == orderId }?.let { _order.value = it }
            }
        }
    }

    fun accept() = act { orders.accept(orderId) }

    fun reject() = act { orders.reject(orderId) }

    fun markReady() = act { orders.markReady(orderId) }

    fun cancel() = act { orders.cancel(orderId) }

    private fun act(action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            action()
                .onSuccess { orders.fetchOrder(orderId)?.let { _order.value = it } }
                .onFailure { _messages.tryEmit(it.message ?: "That didn't work - please try again.") }
        }
    }

    fun print() {
        val current = order.value ?: return
        viewModelScope.launch {
            printer.printReceipt(current)
                .onSuccess { _messages.tryEmit("Printed ticket #${current.code}") }
                .onFailure { _messages.tryEmit(it.message ?: "Print failed") }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                val handle = createSavedStateHandle()
                val orderId = handle.get<String>(Routes.DETAILS_ARG).orEmpty()
                OrderDetailsViewModel(
                    orderId,
                    app.container.orderRepository,
                    app.container.sunmiPrinter,
                    app.container.posSettingsRepository,
                )
            }
        }
    }
}
