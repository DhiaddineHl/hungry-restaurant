package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius

/**
 * §5 StatusPill - 36dp pill, soft bg + 8dp dot + label 13/700. Used for
 * board-level state such as "Open" (success) / "Offline" (danger).
 */
@Composable
fun StatusPill(
    label: String,
    fg: Color,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .height(36.dp)
            .clip(HungryRadius.pill)
            .background(bg)
            .padding(horizontal = 12.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(fg))
        Text(label, style = Hungry.type.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = fg)
    }
}

/** §5 Badge - 11/700, radius 6, padding 2×7, tone pair. */
@Composable
fun Badge(label: String, fg: Color, bg: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(HungryRadius.badge)
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 2.dp),
    ) {
        Text(label, style = Hungry.type.badge, color = fg)
    }
}

/** Status pill sized for a StatusPill(Open) using semantic success/danger tones. */
@Composable
fun ConnectionStatusPill(online: Boolean, modifier: Modifier = Modifier) {
    val c = Hungry.colors
    if (online) {
        StatusPill("Open", fg = c.success, bg = c.successSoft, modifier = modifier)
    } else {
        StatusPill("Offline", fg = c.danger, bg = c.dangerSoft, modifier = modifier)
    }
}
