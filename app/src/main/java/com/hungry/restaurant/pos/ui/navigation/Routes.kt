package com.hungry.restaurant.pos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val LOGIN = "login"
    const val ACTIVE = "active"
    const val HISTORY = "history"
    const val METRICS = "metrics"
    const val DETAILS = "details"
    const val DETAILS_ARG = "orderId"
    const val DETAILS_PATTERN = "$DETAILS/{$DETAILS_ARG}"

    fun details(orderId: String) = "$DETAILS/$orderId"
}

/** Destinations shown in the bottom navigation bar. */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    ACTIVE(Routes.ACTIVE, "Active", Icons.Outlined.Restaurant),
    HISTORY(Routes.HISTORY, "History", Icons.AutoMirrored.Outlined.ReceiptLong),
    METRICS(Routes.METRICS, "Metrics", Icons.Outlined.BarChart),
}
