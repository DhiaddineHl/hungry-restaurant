package com.hungry.restaurant.pos.data.repository

import android.util.Log
import com.hungry.restaurant.pos.data.model.StatsPeriod
import com.hungry.restaurant.pos.data.model.StatsSummary
import com.hungry.restaurant.pos.data.network.RestaurantPosApi
import com.hungry.restaurant.pos.data.network.toDomain

interface StatsRepository {
    suspend fun fetch(period: StatsPeriod): StatsSummary?
}

class RealStatsRepository(private val api: RestaurantPosApi) : StatsRepository {
    override suspend fun fetch(period: StatsPeriod): StatsSummary? =
        runCatching { api.getStats(period.name).toDomain() }
            .onFailure { Log.e(TAG, "Failed to fetch stats for $period", it) }
            .getOrNull()

    private companion object {
        const val TAG = "StatsRepository"
    }
}
