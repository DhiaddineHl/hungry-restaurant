package com.hungry.restaurant.pos.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Generated from design-system/tokens.json. Keep in sync.

@Immutable
data class HungryColors(
    val canvas: Color,
    val surface: Color,
    val surfaceSunken: Color,
    val outline: Color,
    val toggleOff: Color,
    val ink: Color,
    val inkMuted: Color,
    val primary: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val onPrimarySoft: Color,
    val info: Color,
    val infoSoft: Color,
    val success: Color,
    val successSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val inverseSurface: Color,
    val onInverseSurface: Color,
    val isDark: Boolean,
)

val LightHungryColors = HungryColors(
    canvas = Color(0xFFF4F1EC),
    surface = Color(0xFFFFFFFF),
    surfaceSunken = Color(0xFFF2EFE9),
    outline = Color(0xFFE6E2DA),
    toggleOff = Color(0xFFD6D1C8),
    ink = Color(0xFF003049),
    inkMuted = Color(0xFF5B6B75),
    primary = Color(0xFFEA8608),
    onPrimary = Color(0xFF002A40),
    primarySoft = Color(0xFFFDF0DE),
    onPrimarySoft = Color(0xFF9A5A00),
    info = Color(0xFF2466A8),
    infoSoft = Color(0xFFE4EEF8),
    success = Color(0xFF13875A),
    successSoft = Color(0xFFE1F3EA),
    danger = Color(0xFFC23B3B),
    dangerSoft = Color(0xFFFBE7E5),
    inverseSurface = Color(0xFF003049),
    onInverseSurface = Color(0xFFF4F1EC),
    isDark = false,
)

val DarkHungryColors = HungryColors(
    canvas = Color(0xFF0A1822),
    surface = Color(0xFF11242F),
    surfaceSunken = Color(0xFF182C38),
    outline = Color(0xFF223A48),
    toggleOff = Color(0xFF2E4655),
    ink = Color(0xFFEEF1F2),
    inkMuted = Color(0xFF93A6B1),
    primary = Color(0xFFEA8608),
    onPrimary = Color(0xFF002A40),
    primarySoft = Color(0xFF3A2A12),
    onPrimarySoft = Color(0xFFF5A640),
    info = Color(0xFF7FB2E5),
    infoSoft = Color(0xFF16304A),
    success = Color(0xFF5BCB98),
    successSoft = Color(0xFF123A2C),
    danger = Color(0xFFF07B7B),
    dangerSoft = Color(0xFF3D1B1D),
    inverseSurface = Color(0xFFEEF1F2),
    onInverseSurface = Color(0xFF0A1822),
    isDark = true,
)

/** Incoming-order takeover. Identical in both themes. */
object AlertColors {
    val background = Color(0xFF06283D)
    val ink = Color.White
    val inkMuted = Color.White.copy(alpha = 0.72f)
    val panel = Color.White.copy(alpha = 0.07f)
    val panelBorder = Color.White.copy(alpha = 0.10f)
    val controlBorder = Color.White.copy(alpha = 0.25f)
}

val LocalHungryColors = staticCompositionLocalOf { LightHungryColors }
