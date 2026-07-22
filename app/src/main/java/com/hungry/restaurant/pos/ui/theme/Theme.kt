package com.hungry.restaurant.pos.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = HungryOrange,
    onPrimary = Color.White,
    primaryContainer = HungryOrangeSoft,
    onPrimaryContainer = HungryOrangeDark,
    secondary = Ink,
    onSecondary = Color.White,
    background = Canvas,
    onBackground = Ink,
    surface = SurfaceLight,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF1EEE9),
    onSurfaceVariant = InkMuted,
    outline = OutlineLight,
    outlineVariant = OutlineLight,
    error = NegativeRed,
)

private val DarkColors = darkColorScheme(
    primary = HungryOrange,
    onPrimary = Color.White,
    primaryContainer = HungryOrangeDark,
    onPrimaryContainer = HungryOrangeSoft,
    secondary = InkDark,
    onSecondary = Ink,
    background = CanvasDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = Color(0xFF2A2824),
    onSurfaceVariant = InkMutedDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark,
    error = NegativeRed,
)

@Composable
fun HungryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Edge-to-edge draws content behind a transparent status bar; just
            // pick icon contrast. Light background -> dark (non-light-appearance
            // is only for dark backgrounds), so invert on theme.
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content,
    )
}
