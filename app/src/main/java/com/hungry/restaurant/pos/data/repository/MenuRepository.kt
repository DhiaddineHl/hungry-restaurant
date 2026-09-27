package com.hungry.restaurant.pos.data.repository

import android.util.Log
import com.hungry.restaurant.pos.data.model.MenuItem
import com.hungry.restaurant.pos.data.network.RestaurantPosApi
import com.hungry.restaurant.pos.data.network.dto.AvailabilityRequestDto
import com.hungry.restaurant.pos.data.network.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface MenuRepository {
    val items: StateFlow<List<MenuItem>>
    suspend fun refresh(): Result<Unit>
    suspend fun setAvailability(itemId: String, available: Boolean, unavailableReason: String?): Result<Unit>
    suspend fun setPrepTime(itemId: String, prepTimeMinutes: Int): Result<Unit>
}

class RealMenuRepository(private val api: RestaurantPosApi) : MenuRepository {

    private val _items = MutableStateFlow<List<MenuItem>>(emptyList())
    override val items: StateFlow<List<MenuItem>> = _items.asStateFlow()

    override suspend fun refresh(): Result<Unit> = runCatching {
        _items.value = api.listMenu(page = 0, size = 200).content.map { it.toDomain() }
    }.onFailure { Log.e(TAG, "Failed to refresh the menu", it) }

    override suspend fun setAvailability(itemId: String, available: Boolean, unavailableReason: String?): Result<Unit> =
        runCatching {
            val updated = api.setAvailability(
                itemId,
                AvailabilityRequestDto(available = available, unavailableReason = unavailableReason),
            ).toDomain()
            _items.value = _items.value.map { if (it.id == itemId) updated else it }
        }

    override suspend fun setPrepTime(itemId: String, prepTimeMinutes: Int): Result<Unit> = runCatching {
        val updated = api.setAvailability(itemId, AvailabilityRequestDto(prepTimeMinutes = prepTimeMinutes)).toDomain()
        _items.value = _items.value.map { if (it.id == itemId) updated else it }
    }

    private companion object {
        const val TAG = "MenuRepository"
    }
}
