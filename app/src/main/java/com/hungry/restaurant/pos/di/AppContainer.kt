package com.hungry.restaurant.pos.di

import android.content.Context
import com.hungry.restaurant.pos.auth.AuthManager
import com.hungry.restaurant.pos.auth.EncryptedAuthStateStorage
import com.hungry.restaurant.pos.data.network.ApiClient
import com.hungry.restaurant.pos.data.network.RestaurantPosApi
import com.hungry.restaurant.pos.data.repository.MenuRepository
import com.hungry.restaurant.pos.data.repository.OrderHistoryRepository
import com.hungry.restaurant.pos.data.repository.OrderRepository
import com.hungry.restaurant.pos.data.repository.PosSettingsRepository
import com.hungry.restaurant.pos.data.repository.RealMenuRepository
import com.hungry.restaurant.pos.data.repository.RealOrderHistoryRepository
import com.hungry.restaurant.pos.data.repository.RealOrderRepository
import com.hungry.restaurant.pos.data.repository.RealPosSettingsRepository
import com.hungry.restaurant.pos.data.repository.RealRestaurantSessionRepository
import com.hungry.restaurant.pos.data.repository.RealStatsRepository
import com.hungry.restaurant.pos.data.repository.RestaurantSessionRepository
import com.hungry.restaurant.pos.data.repository.StatsRepository
import com.hungry.restaurant.pos.data.repository.ThemeModeRepository
import com.hungry.restaurant.pos.notification.AlertSoundPlayer
import com.hungry.restaurant.pos.printer.SunmiPrinter

/**
 * Lightweight manual DI container. Holds app-scoped singletons, created once
 * in [com.hungry.restaurant.pos.HungryPosApp]. Every repository here talks to
 * the real backend through the single [RestaurantPosApi] instance - nothing in
 * this container is a mock any more.
 *
 * <p>No staff/shift concept - one Keycloak account is the restaurant's only
 * identity (see [AuthManager]); a successful sign-in goes straight to the
 * main pages.
 */
class AppContainer(context: Context) {
    val authManager: AuthManager = AuthManager(context, EncryptedAuthStateStorage(context))

    private val api: RestaurantPosApi = ApiClient.create(authManager)

    val restaurantSession: RestaurantSessionRepository = RealRestaurantSessionRepository(api)

    private val realOrderRepository = RealOrderRepository(api)
    val orderRepository: OrderRepository = realOrderRepository
    val orderHistoryRepository: OrderHistoryRepository = RealOrderHistoryRepository(api)
    val menuRepository: MenuRepository = RealMenuRepository(api)
    val posSettingsRepository: PosSettingsRepository = RealPosSettingsRepository(api)
    val statsRepository: StatsRepository = RealStatsRepository(api)

    val sunmiPrinter: SunmiPrinter = SunmiPrinter(context.applicationContext)
    val themeModeRepository: ThemeModeRepository = ThemeModeRepository(context.applicationContext)
    val alertSoundPlayer: AlertSoundPlayer = AlertSoundPlayer(context.applicationContext)

    /** Starts the order-board poll loop - called once the restaurant session resolves after sign-in. */
    fun startOrderPolling() = realOrderRepository.ensurePolling()
}
