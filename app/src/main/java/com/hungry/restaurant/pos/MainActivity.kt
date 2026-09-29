package com.hungry.restaurant.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hungry.restaurant.pos.ui.navigation.AppNavigation
import com.hungry.restaurant.pos.ui.theme.HungryPosTheme
import com.hungry.restaurant.pos.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val themeModeRepository = (application as HungryPosApp).container.themeModeRepository
        setContent {
            val mode by themeModeRepository.themeMode.collectAsState(initial = ThemeMode.AUTO)
            HungryPosTheme(mode = mode) {
                AppNavigation()
            }
        }
    }
}
