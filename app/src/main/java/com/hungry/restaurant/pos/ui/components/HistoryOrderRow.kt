package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.asCurrency
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.util.dateTime
import com.hungry.restaurant.pos.ui.util.isVoided
import com.hungry.restaurant.pos.ui.util.label
import com.hungry.restaurant.pos.ui.util.statusTone

/**
 * A single, compact list row for the History screen (§5 ListCard row) - past
 * orders are a plain list (code/customer/price, a subtitle line, a colored
 * status word), never a full card the way the active board is.
 */
@Composable
fun HistoryOrderRow(order: Order, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Hungry.colors
    val type = Hungry.type
    val (fg, _) = order.status.statusTone()
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "#${order.code} · ${order.customerName}",
                style = type.bodyStrong,
                color = c.ink,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Text(
                "${dateTime(order.placedAtMillis)} · ${order.itemCount} items",
                style = type.caption,
                color = c.inkMuted,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                order.total.asCurrency(order.currency),
                style = type.bodyStrong,
                color = c.ink,
                textDecoration = if (order.status.isVoided) TextDecoration.LineThrough else null,
            )
            Text(order.status.label(), style = type.label.copy(fontWeight = FontWeight.Bold), color = fg)
        }
    }
}
