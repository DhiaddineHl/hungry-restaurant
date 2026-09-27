package com.hungry.restaurant.pos.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class TopItemDto(
    val name: String,
    val quantity: Int,
)

/** Mirrors `RestaurantStatsSummary`. */
@Serializable
data class StatsDto(
    val period: String,
    val currency: String? = null,
    val revenue: Double = 0.0,
    val revenueChangePct: Double? = null,
    val ordersCount: Int = 0,
    val avgPrepMinutes: Double? = null,
    val acceptedPct: Double = 0.0,
    val cancelledCount: Int = 0,
    val hourlyRevenue: List<Double> = emptyList(),
    val topItems: List<TopItemDto> = emptyList(),
)
