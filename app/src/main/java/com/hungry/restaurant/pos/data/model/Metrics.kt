package com.hungry.restaurant.pos.data.model

/** Aggregated performance figures for the Metrics Overview screen. */
data class DashboardMetrics(
    val revenueTodayCents: Int,
    val ordersToday: Int,
    val avgPrepMinutes: Int,
    val activeOrders: Int,
    val completedToday: Int,
    val cancelledToday: Int,
    /** Percentage change vs. yesterday for the headline revenue tile, e.g. +12.5. */
    val revenueChangePct: Double,
    val ordersChangePct: Double,
    /** Revenue per weekday for the trend chart, oldest → newest (7 points). */
    val revenueTrendCents: List<Int>,
    val trendLabels: List<String>,
    val platformBreakdown: List<PlatformShare>,
    val topItems: List<TopItem>,
)

data class PlatformShare(
    val platform: DeliveryPlatform,
    val orders: Int,
    val revenueCents: Int,
) {
    fun sharePct(totalOrders: Int): Double =
        if (totalOrders == 0) 0.0 else orders * 100.0 / totalOrders
}

data class TopItem(
    val name: String,
    val soldCount: Int,
    val revenueCents: Int,
)
