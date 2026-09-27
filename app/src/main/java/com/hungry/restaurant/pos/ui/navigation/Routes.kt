package com.hungry.restaurant.pos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    /** Manager sign-in (Keycloak) - the device-pairing step, not the everyday one. */
    const val LOGIN = "login"

    /** The "who's on shift?" PIN picker - the everyday entry point once a device is paired. */
    const val STAFF_PICKER = "staff_picker"

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
