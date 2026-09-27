package com.hungry.restaurant.pos.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import com.hungry.restaurant.pos.ui.screens.staffpicker.StaffPickerScreen
import com.hungry.restaurant.pos.ui.screens.stats.StatsScreen
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
    // arriving while one is still on screen doesn't get lost, just counted.
    val pendingAlerts = remember { mutableStateListOf<Order>() }
    var staffSignedIn by remember { mutableStateOf(app.container.currentShift.currentStaff.value != null) }

    LaunchedEffect(Unit) {
        app.container.currentShift.currentStaff.collect { staffSignedIn = it != null }
    }
    LaunchedEffect(staffSignedIn) {
        if (staffSignedIn) {
            app.container.orderRepository.newOrderEvents.collect { order ->
                pendingAlerts.add(order)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        navController.navigate(Routes.STAFF_PICKER) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.STAFF_PICKER) {
                StaffPickerScreen(
                    onSignedIn = {
                        navController.navigate(Routes.ACTIVE) {
                            popUpTo(Routes.STAFF_PICKER) { inclusive = true }
                        }
                    },
                    onManagerLogin = {
                        app.container.authManager.clearSession()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
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
                    onSwitchStaff = {
                        app.container.currentShift.signOut()
                        navController.navigate(Routes.STAFF_PICKER) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
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
                onAccept = {
                    pendingAlerts.removeAt(0)
                    navController.navigate(Routes.details(currentAlert.id))
                    scope.launch {
                        app.container.orderRepository.accept(currentAlert.id)
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

@Composable
private fun BottomBar(
    currentRoute: String?,
    onSelect: (TopLevelDestination) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        TopLevelDestination.entries.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(destination) },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}
