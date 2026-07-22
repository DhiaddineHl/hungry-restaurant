package com.hungry.restaurant.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hungry.restaurant.pos.ui.navigation.AppNavigation
import com.hungry.restaurant.pos.ui.theme.HungryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            HungryTheme {
                AppNavigation()
            }
        }
    }
}
