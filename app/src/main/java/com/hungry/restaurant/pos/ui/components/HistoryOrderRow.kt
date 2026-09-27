package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.asCurrency
import com.hungry.restaurant.pos.ui.theme.NegativeRed
import com.hungry.restaurant.pos.ui.theme.PositiveGreen
import com.hungry.restaurant.pos.ui.theme.StatusPreparing
import com.hungry.restaurant.pos.ui.util.dateTime
import com.hungry.restaurant.pos.ui.util.visual

/**
 * A single, compact list row for the History screen - the redesign shows past
 * orders as a plain list (code/customer/price, a subtitle line, a colored
 * status word), never as a full card the way the active board does.
 */
@Composable
fun HistoryOrderRow(order: Order, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val vis = order.status.visual()
    val statusColor = when {
        order.status.name == "FINISHED" -> PositiveGreen
        order.status.name == "REJECTED" -> StatusPreparing
        order.status.name == "CANCELLED" -> NegativeRed
        else -> vis.color
    }
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            Row {
                Text(
                    "#${order.code}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    " · ${order.customerName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                "${dateTime(order.placedAtMillis)} · ${order.itemCount} items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                order.total.asCurrency(order.currency),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                vis.label,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
