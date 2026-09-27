package com.hungry.restaurant.pos.data.repository

import android.util.Log
import com.hungry.restaurant.pos.data.model.PosSettings
import com.hungry.restaurant.pos.data.network.RestaurantPosApi
import com.hungry.restaurant.pos.data.network.dto.UpdatePosSettingsRequestDto
import com.hungry.restaurant.pos.data.network.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PosSettingsRepository {
    val settings: StateFlow<PosSettings?>
    suspend fun refresh(): Result<Unit>
    suspend fun update(
        autoPrintOnAccept: Boolean? = null,
        ticketCopies: Int? = null,
        ringUntilAccepted: Boolean? = null,
        defaultPrepTimeMinutes: Int? = null,
        autoAcceptOrders: Boolean? = null,
    ): Result<Unit>
}

class RealPosSettingsRepository(private val api: RestaurantPosApi) : PosSettingsRepository {

    private val _settings = MutableStateFlow<PosSettings?>(null)
    override val settings: StateFlow<PosSettings?> = _settings.asStateFlow()

    override suspend fun refresh(): Result<Unit> = runCatching {
        _settings.value = api.getPosSettings().toDomain()
    }.onFailure { Log.e(TAG, "Failed to refresh POS settings", it) }

    override suspend fun update(
        autoPrintOnAccept: Boolean?,
        ticketCopies: Int?,
        ringUntilAccepted: Boolean?,
        defaultPrepTimeMinutes: Int?,
        autoAcceptOrders: Boolean?,
    ): Result<Unit> = runCatching {
        _settings.value = api.updatePosSettings(
            UpdatePosSettingsRequestDto(
                autoPrintOnAccept = autoPrintOnAccept,
                ticketCopies = ticketCopies,
                ringUntilAccepted = ringUntilAccepted,
                defaultPrepTimeMinutes = defaultPrepTimeMinutes,
                autoAcceptOrders = autoAcceptOrders,
            ),
        ).toDomain()
    }

    private companion object {
        const val TAG = "PosSettingsRepository"
    }
}
