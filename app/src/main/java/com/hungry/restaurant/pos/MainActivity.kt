package com.hungry.restaurant.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hungry.restaurant.pos.ui.navigation.AppNavigation
import com.hungry.restaurant.pos.ui.theme.HungryPosTheme
import com.hungry.restaurant.pos.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Screen 00 (Splash): covers the instant before Compose loads (navy background, no
        // white flash) and swaps to Theme.HungryPOS as soon as it's shown - our own Compose
        // SplashScreen route (with the "hungry." wordmark, PARTNER pill and spinner the OS
        // splash can't render) takes over immediately after, so this never needs to be held.
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Dedicated kiosk terminal, not a general-purpose phone screen: none of the design
        // system's mockups show a system nav bar, only our own BottomNav flush to the bottom
        // edge. On hardware with a 3-button (non-gesture) nav bar this also fixes a real
        // layout bug - the window only grants the app its "stable" height, so BottomNav's
        // 64dp got squeezed against the visible edge with the bar's own ~48dp eating into it.
        // The status bar stays (every mockup shows the clock/battery/signal row).
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val themeModeRepository = (application as HungryPosApp).container.themeModeRepository
        setContent {
            val mode by themeModeRepository.themeMode.collectAsState(initial = ThemeMode.AUTO)
            HungryPosTheme(mode = mode) {
                AppNavigation()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Re-hide after the user swipes it back up transiently, or after returning from the
        // Keycloak Custom Tab (which restores the system bars for its own window).
        if (hasFocus) {
            WindowCompat.getInsetsController(window, window.decorView)
                .hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
        }
    }
}
