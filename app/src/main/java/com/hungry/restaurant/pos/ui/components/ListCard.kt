package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius

/** §5 ListCard - surface radius 18, horizontal padding 14, rows separated by 1px outline. */
@Composable
fun ListCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val c = Hungry.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(HungryRadius.card)
            .background(c.surface),
    ) {
        content()
    }
}

/** One row inside a [ListCard]: text column (bodyStrong + caption) + trailing content. */
@Composable
fun ListCardRow(
    title: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val c = Hungry.colors
    Row(
        modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f, fill = false)) {
            Text(title, style = Hungry.type.bodyStrong, color = c.ink)
            if (caption != null) {
                Text(caption, style = Hungry.type.caption, color = c.inkMuted)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.padding(start = 8.dp))
            trailing()
        }
    }
}

/** A chevron for a navigable [ListCardRow]. */
@Composable
fun ListCardChevron() {
    Icon(
        Icons.Outlined.ChevronRight,
        contentDescription = null,
        tint = Hungry.colors.inkMuted,
        modifier = Modifier.size(18.dp),
    )
}

/** 1px outline divider matching the card's own horizontal inset. */
@Composable
fun ListCardDivider() {
    HorizontalDivider(color = Hungry.colors.outline, thickness = 1.dp, modifier = Modifier.padding(horizontal = 14.dp))
}
