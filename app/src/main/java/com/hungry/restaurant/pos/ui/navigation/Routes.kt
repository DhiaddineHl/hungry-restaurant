package com.hungry.restaurant.pos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    /** Screen 00 - brand moment shown just long enough to be legible, then always → [LOGIN]
     *  (which itself already shows 01b/skips straight to [ACTIVE] for a cached session). */
    const val SPLASH = "splash"

    /** Keycloak sign-in - the only entry point after splash. Success goes straight to [ACTIVE]. */
    const val LOGIN = "login"

    const val ACTIVE = "active"
    const val HISTORY = "history"
    const val MENU = "menu"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val ACCOUNT = "account"
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
    ACTIVE(Routes.ACTIVE, "Orders", Icons.Outlined.Storefront),
    HISTORY(Routes.HISTORY, "History", Icons.AutoMirrored.Outlined.ReceiptLong),
    MENU(Routes.MENU, "Menu", Icons.Outlined.RestaurantMenu),
    STATS(Routes.STATS, "Stats", Icons.Outlined.BarChart),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Outlined.Settings),
}
