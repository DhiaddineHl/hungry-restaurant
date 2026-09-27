package com.hungry.restaurant.pos.data.repository

import com.hungry.restaurant.pos.data.model.Order
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Source of truth for the restaurant's own orders - backed by
 * `GET /restaurants/me/orders` (polled) rather than push, since this app has
 * no STOMP client (see the backend's `/topic/restaurants/{id}/orders`, built
 * for a future client to consume - a deliberate scope cut for this pass, not
 * an oversight: it needs a hand-rolled SockJS-raw-WebSocket STOMP client with
 * nothing in this codebase or its sibling apps to verify the framing against,
 * and this app has no way to run/observe a build to catch a mistake there).
 */
interface OrderRepository {
    val orders: StateFlow<List<Order>>

    /** Emits a CREATED order the moment a poll first sees it - drives the full-screen incoming-order alert. */
    val newOrderEvents: SharedFlow<Order>

    /** From the in-memory active board only - null if the order isn't active (or hasn't loaded yet). */
    fun orderById(id: String): Order?

    /** Reads one order directly from the server, active or not - what the details screen uses when opened from History. */
    suspend fun fetchOrder(id: String): Order?

    /** Re-fetches immediately, outside the regular poll cadence (e.g. pull-to-refresh). */
    suspend fun refresh()

    /** Accepts a CREATED order and moves it straight into the kitchen (CONFIRMED, then PREPARING). */
    suspend fun accept(orderId: String): Result<Unit>

    /** Declines a CREATED order outright. */
    suspend fun reject(orderId: String): Result<Unit>

    /** Marks a PREPARING order ready for pickup/handover. */
    suspend fun markReady(orderId: String): Result<Unit>

    /** Calls off an order already accepted. */
    suspend fun cancel(orderId: String, reason: String? = null): Result<Unit>
}
