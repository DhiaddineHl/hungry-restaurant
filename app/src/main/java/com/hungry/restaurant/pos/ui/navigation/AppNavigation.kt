package com.hungry.restaurant.pos.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.ui.screens.account.AccountScreen
import com.hungry.restaurant.pos.ui.screens.active.ActiveOrdersScreen
import com.hungry.restaurant.pos.ui.screens.details.OrderDetailsScreen
import com.hungry.restaurant.pos.ui.screens.history.OrderHistoryScreen
import com.hungry.restaurant.pos.ui.screens.incoming.IncomingOrderAlertScreen
import com.hungry.restaurant.pos.ui.screens.login.LoginScreen
import com.hungry.restaurant.pos.ui.screens.menu.MenuScreen
import com.hungry.restaurant.pos.ui.screens.settings.SettingsScreen
import com.hungry.restaurant.pos.ui.components.BottomNavItem
import com.hungry.restaurant.pos.ui.components.HungryBottomNav
import com.hungry.restaurant.pos.ui.components.HungryToast
import com.hungry.restaurant.pos.ui.screens.stats.StatsScreen
import com.hungry.restaurant.pos.ui.theme.Hungry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val app = LocalContext.current.applicationContext as HungryPosApp

    val onMessage: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevelDestination.entries.any { it.route == currentRoute }

    // The full-screen "New order" interrupt (screen 03) - queued so a second order
    // arriving while one is still on screen doesn't get lost, just counted. Harmless
    // to collect before sign-in: polling (and therefore this) never emits until
    // LoginViewModel starts it on a successful sign-in.
    val pendingAlerts = remember { mutableStateListOf<Order>() }
    val posSettings by app.container.posSettingsRepository.settings.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        app.container.orderRepository.newOrderEvents.collect { order ->
            pendingAlerts.add(order)
        }
    }

    // Rings the terminal's notification tone for a new order, repeating every few
    // seconds while Settings > Ring until accepted is on and the queue isn't empty.
    LaunchedEffect(pendingAlerts.size, posSettings?.ringUntilAccepted) {
        if (pendingAlerts.isEmpty()) return@LaunchedEffect
        app.container.alertSoundPlayer.playOnce()
        while (posSettings?.ringUntilAccepted == true && pendingAlerts.isNotEmpty()) {
            delay(4_000)
            if (pendingAlerts.isNotEmpty()) app.container.alertSoundPlayer.playOnce()
        }
    }

    Scaffold(
        containerColor = Hungry.colors.canvas,
        snackbarHost = {
            // Screen E4's Toast (§5) - a failure message with an optional "Retry" action,
            // sitting above the sticky action bar via the Scaffold's own snackbar slot.
            SnackbarHost(snackbarHostState) { data ->
                HungryToast(
                    message = data.visuals.message,
                    actionLabel = data.visuals.actionLabel,
                    onAction = if (data.visuals.actionLabel != null) { { data.performAction() } } else null,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    currentRoute = currentRoute,
                    onSelect = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.ACTIVE) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.ACTIVE) {
                ActiveOrdersScreen(
                    contentPadding = padding,
                    onOrderClick = { navController.navigate(Routes.details(it)) },
                    onMessage = onMessage,
                )
            }
            composable(Routes.HISTORY) {
                OrderHistoryScreen(
                    contentPadding = padding,
                    onOrderClick = { navController.navigate(Routes.details(it)) },
                )
            }
            composable(Routes.MENU) {
                MenuScreen(contentPadding = padding)
            }
            composable(Routes.STATS) {
                StatsScreen(contentPadding = padding)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    contentPadding = padding,
                    onOpenAccount = { navController.navigate(Routes.ACCOUNT) },
                    onMessage = onMessage,
                )
            }
            composable(Routes.ACCOUNT) {
                AccountScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = Routes.DETAILS_PATTERN,
                arguments = listOf(navArgument(Routes.DETAILS_ARG) { type = NavType.StringType }),
            ) {
                OrderDetailsScreen(
                    onBack = { navController.popBackStack() },
                    onMessage = onMessage,
                )
            }
        }
    }

    val currentAlert = pendingAlerts.firstOrNull()
    if (currentAlert != null) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false),
        ) {
            IncomingOrderAlertScreen(
                order = currentAlert,
                queuedCount = pendingAlerts.size - 1,
                defaultReadyInMinutes = posSettings?.defaultPrepTimeMinutes ?: 20,
                onAccept = { readyInMinutes ->
                    pendingAlerts.removeAt(0)
                    navController.navigate(Routes.details(currentAlert.id))
                    scope.launch {
                        app.container.orderRepository.accept(currentAlert.id)
                            .onSuccess {
                                if (posSettings?.autoPrintOnAccept != false) {
                                    app.container.sunmiPrinter.printReceipt(currentAlert, readyInMinutes)
                                }
                            }
                            .onFailure { onMessage(it.message ?: "Couldn't accept order #${currentAlert.code}") }
                    }
                },
                onReject = {
                    pendingAlerts.removeAt(0)
                    scope.launch {
                        app.container.orderRepository.reject(currentAlert.id)
                            .onFailure { onMessage(it.message ?: "Couldn't reject order #${currentAlert.code}") }
                    }
                },
            )
        }
    }
}

/** [TopLevelDestination] and [BottomNavItem] list the same 5 tabs in the same order. */
private fun TopLevelDestination.toNavItem(): BottomNavItem = BottomNavItem.entries[ordinal]
private fun BottomNavItem.toDestination(): TopLevelDestination = TopLevelDestination.entries[ordinal]

@Composable
private fun BottomBar(
    currentRoute: String?,
    onSelect: (TopLevelDestination) -> Unit,
) {
    val selected = TopLevelDestination.entries.firstOrNull { it.route == currentRoute } ?: TopLevelDestination.ACTIVE
    HungryBottomNav(
        selected = selected.toNavItem(),
        onSelect = { onSelect(it.toDestination()) },
    )
}
