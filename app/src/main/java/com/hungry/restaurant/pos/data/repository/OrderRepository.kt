package com.hungry.restaurant.pos.data.repository

import com.hungry.restaurant.pos.data.model.DashboardMetrics
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Source of truth for orders and dashboard metrics.
 *
 * The UI observes [orders] reactively; status changes mutate the same stream so
 * every screen stays in sync. Replace [MockOrderRepository] with a REST/Firebase
 * implementation later — the contract stays identical.
 */
interface OrderRepository {
    val orders: StateFlow<List<Order>>

    fun orderById(id: String): Order?

    /** Emits whenever the metrics recompute (e.g. after an order completes). */
    val metrics: Flow<DashboardMetrics>

    suspend fun updateStatus(orderId: String, status: OrderStatus)

    /** Advance an order to its next natural status (NEW → PREPARING → READY → COMPLETED). */
    suspend fun advance(orderId: String)

    suspend fun refresh()
}
