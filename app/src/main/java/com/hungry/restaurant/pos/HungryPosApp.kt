package com.hungry.restaurant.pos

import android.app.Application
import com.hungry.restaurant.pos.di.AppContainer

class HungryPosApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Eagerly bind the printer so it's ready by the time the first ticket prints.
        container.sunmiPrinter.connect()
    }
}
