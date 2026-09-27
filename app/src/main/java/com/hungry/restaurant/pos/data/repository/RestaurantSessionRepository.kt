package com.hungry.restaurant.pos.data.repository

import android.util.Log
import com.hungry.restaurant.pos.data.model.RestaurantProfile
import com.hungry.restaurant.pos.data.network.RestaurantPosApi
import com.hungry.restaurant.pos.data.network.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The signed-in restaurant's own profile, resolved once via `GET /restaurants/me`
 * (the server maps the caller's token to their restaurant - see the backend's
 * `RestaurantSelfController`). Every other repository that needs "my restaurant"
 * reads this instead of resolving it again.
 */
interface RestaurantSessionRepository {
    val restaurant: StateFlow<RestaurantProfile?>

    /** Called once right after sign-in (and again on manual refresh). */
    suspend fun refresh(): Result<RestaurantProfile>

    suspend fun setAcceptingOrders(value: Boolean): Result<Unit>

    fun clear()
}

class RealRestaurantSessionRepository(private val api: RestaurantPosApi) : RestaurantSessionRepository {

    private val _restaurant = MutableStateFlow<RestaurantProfile?>(null)
    override val restaurant: StateFlow<RestaurantProfile?> = _restaurant.asStateFlow()

    override suspend fun refresh(): Result<RestaurantProfile> = runCatching {
        val profile = api.getMyRestaurant().toDomain()
        _restaurant.value = profile
        profile
    }.onFailure { Log.e(TAG, "Failed to resolve the signed-in restaurant", it) }

    override suspend fun setAcceptingOrders(value: Boolean): Result<Unit> = runCatching {
        val updated = api.setAcceptingOrders(value).toDomain()
        _restaurant.value = updated
    }

    override fun clear() {
        _restaurant.value = null
    }

    private companion object {
        const val TAG = "RestaurantSession"
    }
}
