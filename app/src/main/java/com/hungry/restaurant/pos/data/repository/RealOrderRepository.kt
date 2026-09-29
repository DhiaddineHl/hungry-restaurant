package com.hungry.restaurant.pos.data.repository

import android.util.Log
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderStatus
import com.hungry.restaurant.pos.data.network.RestaurantPosApi
import com.hungry.restaurant.pos.data.network.dto.CancelOrderRequestDto
import com.hungry.restaurant.pos.data.network.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Polls `GET /restaurants/me/orders` every [POLL_INTERVAL_MS] and exposes the
 * result reactively. Every mutating call re-fetches once it succeeds so
 * [orders] reflects the server's own transition rules immediately, rather than
 * the app guessing the next state itself.
 */
class RealOrderRepository(private val api: RestaurantPosApi) : OrderRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    override val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _connection = MutableStateFlow(ConnectionState())
    override val connection: StateFlow<ConnectionState> = _connection.asStateFlow()

    private val _newOrderEvents = MutableSharedFlow<Order>(extraBufferCapacity = 8)
    override val newOrderEvents = _newOrderEvents

    private val knownCreatedIds = mutableSetOf<String>()
    private var startedPolling = false

    /** Lazily starts the poll loop on first use - a repository built at app start shouldn't hit the network before anyone signs in. */
    fun ensurePolling() {
        if (startedPolling) return
        startedPolling = true
        scope.launch {
            while (true) {
                fetchOnce()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    override fun orderById(id: String): Order? = _orders.value.firstOrNull { it.id == id }

    override suspend fun fetchOrder(id: String): Order? =
        runCatching { api.getOrder(id).toDomain() }
            .onFailure { Log.e(TAG, "Failed to fetch order $id", it) }
            .getOrNull()

    override suspend fun refresh() = fetchOnce()

    private suspend fun fetchOnce() {
        try {
            // Active board only: history has its own paged, filterable query (OrderHistoryViewModel)
            // and would otherwise grow this single in-memory list without bound.
            val active = api.listOrders(status = null, page = 0, size = 100).content
                .map { it.toDomain() }
                .filter { it.status.isActive }
            _orders.value = active

            val nowCreated = active.filter { it.status == OrderStatus.CREATED }
            nowCreated.forEach { order ->
                if (knownCreatedIds.add(order.id)) {
                    _newOrderEvents.tryEmit(order)
                }
            }
            // Stop tracking ids that left the CREATED set (accepted/rejected elsewhere), so a
            // future *different* order reusing... it never will (server ids are UUIDs), but this
            // keeps the set from growing forever across a long-running terminal session.
            knownCreatedIds.retainAll(nowCreated.map { it.id }.toSet())
            _connection.value = ConnectionState(isOnline = true, lastSyncMillis = System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh orders", e)
            _connection.value = _connection.value.copy(isOnline = false)
        }
    }

    override suspend fun accept(orderId: String): Result<Unit> = runCatching {
        api.confirmOrder(orderId)
        // No distinct "start cooking" tap in this app's board - accepting an order moves it
        // straight into the kitchen, matching the design's New -> Preparing swipe gesture.
        api.prepareOrder(orderId)
        refresh()
    }

    override suspend fun reject(orderId: String): Result<Unit> = runCatching {
        api.rejectOrder(orderId)
        refresh()
    }

    override suspend fun markReady(orderId: String): Result<Unit> = runCatching {
        api.readyOrder(orderId)
        refresh()
    }

    override suspend fun cancel(orderId: String, reason: String?): Result<Unit> = runCatching {
        api.cancelOrder(orderId, CancelOrderRequestDto(reason))
        refresh()
    }

    private companion object {
        const val TAG = "RealOrderRepository"
        const val POLL_INTERVAL_MS = 4_000L
    }
}
