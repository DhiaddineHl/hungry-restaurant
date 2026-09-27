package com.hungry.restaurant.pos.ui.screens.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderStatus
import com.hungry.restaurant.pos.data.model.RestaurantProfile
import com.hungry.restaurant.pos.data.repository.OrderRepository
import com.hungry.restaurant.pos.data.repository.PosSettingsRepository
import com.hungry.restaurant.pos.data.repository.RestaurantSessionRepository
import com.hungry.restaurant.pos.printer.SunmiPrinter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BoardTab { NEW, PREPARING, READY }

/** Active orders bucketed by kitchen stage for the board layout. */
data class ActiveBoard(
    val incoming: List<Order> = emptyList(),
    /** CONFIRMED and PREPARING both land here - this app calls prepare() right after
     *  confirm() (see [OrderRepository.accept]), so there's no distinct "start cooking"
     *  step for the board to show a separate column for. */
    val preparing: List<Order> = emptyList(),
    val ready: List<Order> = emptyList(),
) {
    val total: Int get() = incoming.size + preparing.size + ready.size
}

class ActiveOrdersViewModel(
    private val orderRepository: OrderRepository,
    private val sessionRepository: RestaurantSessionRepository,
    private val posSettingsRepository: PosSettingsRepository,
    val printer: SunmiPrinter,
) : ViewModel() {

    val board: StateFlow<ActiveBoard> = orderRepository.orders
        .map { list ->
            ActiveBoard(
                incoming = list.filter { it.status == OrderStatus.CREATED }.sortedBy { it.placedAtMillis },
                preparing = list.filter { it.status == OrderStatus.CONFIRMED || it.status == OrderStatus.PREPARING }
                    .sortedBy { it.placedAtMillis },
                ready = list.filter { it.status == OrderStatus.READY }.sortedBy { it.placedAtMillis },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActiveBoard())

    val restaurant: StateFlow<RestaurantProfile?> = sessionRepository.restaurant

    private val _selectedTab = MutableStateFlow(BoardTab.NEW)
    val selectedTab: StateFlow<BoardTab> = _selectedTab.asStateFlow()

    val printerStatus: StateFlow<SunmiPrinter.Status> = printer.status

    /** One-shot user-facing messages (snackbar). */
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages

    /** Emits the full order the moment it's accepted - the incoming-order alert uses this to trigger auto-print. */
    private val _accepted = MutableSharedFlow<Order>(extraBufferCapacity = 4)
    val accepted = _accepted

    init {
        viewModelScope.launch { posSettingsRepository.refresh() }
    }

    fun selectTab(tab: BoardTab) {
        _selectedTab.value = tab
    }

    fun toggleAcceptingOrders() {
        val current = sessionRepository.restaurant.value?.acceptingOrders ?: true
        viewModelScope.launch {
            sessionRepository.setAcceptingOrders(!current)
                .onFailure { _messages.tryEmit(it.message ?: "Couldn't update the open/closed status") }
        }
    }

    fun accept(order: Order) {
        viewModelScope.launch {
            orderRepository.accept(order.id)
                .onSuccess {
                    _accepted.tryEmit(order)
                    if (posSettingsRepository.settings.value?.autoPrintOnAccept != false) print(order)
                }
                .onFailure { _messages.tryEmit(it.message ?: "Couldn't accept order #${order.code}") }
        }
    }

    fun reject(order: Order) {
        viewModelScope.launch {
            orderRepository.reject(order.id)
                .onFailure { _messages.tryEmit(it.message ?: "Couldn't reject order #${order.code}") }
        }
    }

    fun markReady(order: Order) {
        viewModelScope.launch {
            orderRepository.markReady(order.id)
                .onFailure { _messages.tryEmit(it.message ?: "Couldn't update order #${order.code}") }
        }
    }

    fun reconnectPrinter() = printer.connect()

    fun print(order: Order) {
        viewModelScope.launch {
            printer.printReceipt(order)
                .onSuccess { _messages.tryEmit("Printed ticket #${order.code}") }
                .onFailure { _messages.tryEmit(it.message ?: "Print failed") }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                ActiveOrdersViewModel(
                    app.container.orderRepository,
                    app.container.restaurantSession,
                    app.container.posSettingsRepository,
                    app.container.sunmiPrinter,
                )
            }
        }
    }
}
