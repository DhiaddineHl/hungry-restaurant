package com.hungry.restaurant.pos.data.repository

import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.network.RestaurantPosApi
import com.hungry.restaurant.pos.data.network.toDomain

/**
 * A separate, one-shot query surface for the History screen - deliberately not
 * the same cache [OrderRepository] polls for the active board (that one only
 * ever keeps active orders in memory). The backend's `/restaurants/me/orders`
 * has no date-range filter today, so this fetches a generous recent page and
 * the ViewModel filters by date/status client-side - a single restaurant's
 * order volume over the last couple hundred orders is small enough that this
 * is simpler than adding a new filter parameter for one screen.
 */
interface OrderHistoryRepository {
    suspend fun recentOrders(limit: Int = 200): List<Order>
}

class RealOrderHistoryRepository(private val api: RestaurantPosApi) : OrderHistoryRepository {
    override suspend fun recentOrders(limit: Int): List<Order> =
        api.listOrders(status = null, page = 0, size = limit).content.map { it.toDomain() }
}
