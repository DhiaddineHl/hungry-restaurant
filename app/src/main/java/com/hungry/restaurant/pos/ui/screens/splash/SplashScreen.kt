package com.hungry.restaurant.pos.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hungry.restaurant.pos.BuildConfig
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius
import kotlinx.coroutines.delay

/** Screen 00 - navy `#003049` in both themes; the OS splash (androidx core-splashscreen,
 *  see MainActivity) covers the instant before Compose loads, this is what's actually on
 *  screen for the brief, deliberate hold while [onFinished] (a synchronous cached-session
 *  check - see AuthManager's own eagerly-read StateFlow) runs. */
private val SplashNavy = Color(0xFF003049)
private val SplashOrange = Color(0xFFEA8608)
private const val MIN_VISIBLE_MS = 900L

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val type = Hungry.type

    LaunchedEffect(Unit) {
        delay(MIN_VISIBLE_MS)
        onFinished()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(SplashNavy)
            .padding(horizontal = 24.dp),
    ) {
        Column(
            Modifier.weight(1f).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) { append("hungry") }
                    withStyle(SpanStyle(color = SplashOrange)) { append(".") }
                },
                style = type.hero.copy(fontSize = 44.sp, lineHeight = 48.sp, fontWeight = FontWeight.ExtraBold),
            )
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .clip(HungryRadius.pill)
                    .background(SplashOrange)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text("PARTNER", style = type.body.copy(fontWeight = FontWeight.ExtraBold), color = SplashNavy)
            }
        }

        Column(
            Modifier.fillMaxWidth().padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.height(24.dp),
                color = SplashOrange,
                trackColor = Color.White.copy(alpha = 0.15f),
                strokeWidth = 3.dp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "v${BuildConfig.VERSION_NAME}",
                style = type.caption,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}
