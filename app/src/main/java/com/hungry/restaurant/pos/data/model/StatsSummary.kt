package com.hungry.restaurant.pos.data.model

enum class StatsPeriod { TODAY, WEEK, MONTH }

data class TopSellingItem(val name: String, val quantity: Int)

/** The Stats screen's numbers for one period - computed live server-side, never cached on-device. */
data class StatsSummary(
    val period: StatsPeriod,
    val currency: String?,
    val revenue: Double,
    val revenueChangePct: Double?,
    val ordersCount: Int,
    val avgPrepMinutes: Double?,
    val acceptedPct: Double,
    val cancelledCount: Int,
    /** Revenue per hour of day, index 0-23. */
    val hourlyRevenue: List<Double>,
    val topItems: List<TopSellingItem>,
)
