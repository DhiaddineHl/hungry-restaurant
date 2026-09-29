package com.hungry.restaurant.pos.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hungry.restaurant.pos.R

// Add res/font/montserrat_{medium,semibold,bold,extrabold}.ttf (Google Fonts, OFL).
val Montserrat = FontFamily(
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_extrabold, FontWeight.ExtraBold),
)

// Tabular figures so prices/timers don't jitter.
private fun s(size: Int, line: Int, weight: FontWeight, trackingPx: Float = 0f) = TextStyle(
    fontFamily = Montserrat,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = (trackingPx / size).em,
    fontFeatureSettings = "tnum",
)

@Immutable
data class HungryType(
    val display: TextStyle = s(34, 38, FontWeight.ExtraBold, -1f),
    val hero: TextStyle = s(30, 36, FontWeight.ExtraBold, -0.9f),
    val stat: TextStyle = s(30, 36, FontWeight.ExtraBold, -1f),
    val headline: TextStyle = s(24, 30, FontWeight.Bold, -0.5f),
    val orderId: TextStyle = s(20, 24, FontWeight.ExtraBold, -0.4f),
    val title: TextStyle = s(18, 24, FontWeight.Bold, -0.3f),
    val bodyStrong: TextStyle = s(15, 21, FontWeight.Bold),
    val body: TextStyle = s(14, 21, FontWeight.SemiBold),
    val label: TextStyle = s(13, 19, FontWeight.SemiBold),
    val caption: TextStyle = s(12, 17, FontWeight.Medium),
    val overline: TextStyle = s(12, 16, FontWeight.Bold, 0.6f), // render uppercase
    val badge: TextStyle = s(11, 14, FontWeight.Bold),
    val button: TextStyle = s(16, 20, FontWeight.ExtraBold),
)

val LocalHungryType = staticCompositionLocalOf { HungryType() }
