package com.hungry.restaurant.pos.di

import android.content.Context
import com.hungry.restaurant.pos.data.repository.AuthRepository
import com.hungry.restaurant.pos.data.repository.MockAuthRepository
import com.hungry.restaurant.pos.data.repository.MockOrderRepository
import com.hungry.restaurant.pos.data.repository.OrderRepository
import com.hungry.restaurant.pos.printer.SunmiPrinter

/**
 * Lightweight manual DI container. Holds app-scoped singletons and is created
 * once in [com.hungry.restaurant.pos.HungryPosApp]. Swapping mock repositories
 * for real ones is a one-line change here.
 */
class AppContainer(context: Context) {
    val authRepository: AuthRepository = MockAuthRepository()
    val orderRepository: OrderRepository = MockOrderRepository()
    val sunmiPrinter: SunmiPrinter = SunmiPrinter(context.applicationContext)
}
