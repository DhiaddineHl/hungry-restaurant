package com.hungry.restaurant.pos.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/** Settings > Appearance. Persist in DataStore; AUTO follows the system. */
enum class ThemeMode { LIGHT, DARK, AUTO }

object HungryRadius {
    val badge = RoundedCornerShape(6.dp)
    val chipSm = RoundedCornerShape(8.dp)
    val control = RoundedCornerShape(12.dp)
    val field = RoundedCornerShape(14.dp)
    val button = RoundedCornerShape(16.dp)
    val card = RoundedCornerShape(18.dp)
    val sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val pill = RoundedCornerShape(percent = 50)
}

object HungrySize {
    val screenPadding = 16.dp
    val minTouch = 44.dp
    val topBar = 56.dp
    val bottomNav = 64.dp
    val buttonPrimary = 56.dp
    val buttonCard = 48.dp
    val field = 48.dp
    val segment = 44.dp
    val chip = 36.dp
}

object Hungry {
    val colors: HungryColors @Composable get() = LocalHungryColors.current
    val type: HungryType @Composable get() = LocalHungryType.current
}

@Composable
fun HungryPosTheme(mode: ThemeMode = ThemeMode.AUTO, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AUTO -> isSystemInDarkTheme()
    }
    val c = if (dark) DarkHungryColors else LightHungryColors

    // Edge-to-edge draws content behind a transparent status bar; just pick icon contrast.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
        }
    }

    // Material scheme mapped from tokens so stock M3 components match.
    val scheme = if (dark) darkColorScheme(
        primary = c.primary, onPrimary = c.onPrimary,
        primaryContainer = c.primarySoft, onPrimaryContainer = c.onPrimarySoft,
        secondary = c.info, tertiary = c.success,
        background = c.canvas, onBackground = c.ink,
        surface = c.surface, onSurface = c.ink,
        surfaceVariant = c.surfaceSunken, onSurfaceVariant = c.inkMuted,
        outline = c.outline, outlineVariant = c.outline,
        error = c.danger, errorContainer = c.dangerSoft,
        inverseSurface = c.inverseSurface, inverseOnSurface = c.onInverseSurface,
    ) else lightColorScheme(
        primary = c.primary, onPrimary = c.onPrimary,
        primaryContainer = c.primarySoft, onPrimaryContainer = c.onPrimarySoft,
        secondary = c.info, tertiary = c.success,
        background = c.canvas, onBackground = c.ink,
        surface = c.surface, onSurface = c.ink,
        surfaceVariant = c.surfaceSunken, onSurfaceVariant = c.inkMuted,
        outline = c.outline, outlineVariant = c.outline,
        error = c.danger, errorContainer = c.dangerSoft,
        inverseSurface = c.inverseSurface, inverseOnSurface = c.onInverseSurface,
    )
    CompositionLocalProvider(
        LocalHungryColors provides c,
        LocalHungryType provides HungryType(),
    ) {
        MaterialTheme(
            colorScheme = scheme,
            shapes = Shapes(
                small = HungryRadius.control,
                medium = HungryRadius.card,
                large = HungryRadius.sheet,
            ),
            content = content,
        )
    }
}
