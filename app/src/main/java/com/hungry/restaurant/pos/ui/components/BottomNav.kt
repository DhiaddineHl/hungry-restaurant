package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.ui.theme.Hungry

enum class BottomNavItem(val label: String, val icon: ImageVector) {
    Orders("Orders", Icons.Outlined.ReceiptLong),
    History("History", Icons.Outlined.History),
    Menu("Menu", Icons.Outlined.Restaurant),
    Stats("Stats", Icons.Outlined.BarChart),
    Settings("Settings", Icons.Outlined.Tune),
}

/**
 * §5 BottomNav - 64dp, surface, 1px top outline. Icon 20dp in a 52×28 pill;
 * active pill `primarySoft`, icon `onPrimarySoft`, label `ink` 700; inactive
 * `inkMuted` 600.
 */
@Composable
fun HungryBottomNav(
    selected: BottomNavItem,
    onSelect: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Hungry.colors
    val type = Hungry.type
    Row(
        modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(colors.surface)
            .border(androidx.compose.foundation.BorderStroke(1.dp, colors.outline)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItem.entries.forEach { item ->
            val active = item == selected
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(item) },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    Modifier
                        .size(width = 52.dp, height = 28.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (active) colors.primarySoft else androidx.compose.ui.graphics.Color.Transparent),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (active) colors.onPrimarySoft else colors.inkMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    item.label,
                    style = type.badge,
                    color = if (active) colors.ink else colors.inkMuted,
                )
            }
        }
    }
}
