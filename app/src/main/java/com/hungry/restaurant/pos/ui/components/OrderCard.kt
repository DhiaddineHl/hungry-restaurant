package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderStatus
import com.hungry.restaurant.pos.data.model.asCurrency
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius
import com.hungry.restaurant.pos.ui.util.relativeTime

/**
 * §5 OrderCard (board) - surface, radius 18, padding 14, gap 10. Row 1: orderId
 * + age badge. Row 2: mode icon + customer. Items list. Footer divider, total
 * (no wrap), reject (48dp square outline) + accept (108×48 primary). NEW cards
 * get a 1.5px primary border; PREPARING cards show a `12 / 20 min` timer + bar.
 *
 * The backend has no per-order prep-time field (see [com.hungry.restaurant.pos.ui.screens.incoming.IncomingOrderAlertScreen]'s
 * doc comment) so [defaultPrepMinutes] - the restaurant's own Settings default -
 * stands in as the target for the PREPARING progress bar.
 */
@Composable
fun ActiveOrderCard(
    order: Order,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    defaultPrepMinutes: Int = 20,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onMarkReady: (() -> Unit)? = null,
    onPrint: (() -> Unit)? = null,
) {
    val c = Hungry.colors
    val type = Hungry.type
    val isNew = order.status == OrderStatus.CREATED
    val isPreparing = order.status == OrderStatus.CONFIRMED || order.status == OrderStatus.PREPARING
    val isDelivery = !order.dropoffAddress.isNullOrBlank()

    Column(
        modifier
            .fillMaxWidth()
            .clip(HungryRadius.card)
            .background(c.surface)
            .then(if (isNew) Modifier.border(1.5.dp, c.primary, HungryRadius.card) else Modifier)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        // Row 1: order id + age badge. The order code can run long (e.g. the cart
        // checkout flow's "ORD-20260929-F9R8NN", well past the mockup's short "#P-231"),
        // so the id truncates with an ellipsis instead of squeezing the badge into a
        // vertical sliver.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                "#${order.code}",
                style = type.orderId,
                color = c.ink,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.width(8.dp))
            Badge(relativeTime(order.placedAtMillis), fg = c.inkMuted, bg = c.surfaceSunken)
        }

        Spacer(Modifier.height(8.dp))

        // Row 2: mode icon + customer
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isDelivery) Icons.Outlined.DirectionsBike else Icons.Outlined.ShoppingBag,
                contentDescription = if (isDelivery) "Delivery" else "Pickup",
                tint = c.inkMuted,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(order.customerName, style = type.label, color = c.inkMuted)
        }

        Spacer(Modifier.height(10.dp))

        Column {
            order.items.forEach { item ->
                Text("${item.quantity}× ${item.name}", style = type.body, color = c.ink)
            }
        }

        if (isPreparing) {
            Spacer(Modifier.height(10.dp))
            val elapsedMin = ((System.currentTimeMillis() - order.placedAtMillis) / 60_000L).toInt().coerceAtLeast(0)
            PreparingTimer(elapsedMin, defaultPrepMinutes)
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = c.outline, thickness = 1.dp)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                order.total.asCurrency(order.currency),
                style = type.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                color = c.ink,
                maxLines = 1,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    isNew -> {
                        if (onReject != null) SquareIconButton(Icons.Outlined.Close, onReject, tint = c.danger, contentDescription = "Reject")
                        if (onAccept != null) CardActionButton("Accept", onAccept)
                    }
                    isPreparing -> {
                        if (onPrint != null) SquareIconButton(Icons.Outlined.Print, onPrint, tint = c.inkMuted, contentDescription = "Print")
                        if (onMarkReady != null) CardActionButton("Mark ready", onMarkReady, widthDp = 132)
                    }
                    order.status == OrderStatus.READY -> {
                        if (onPrint != null) CardActionButton("Print ticket", onPrint, widthDp = 140)
                    }
                }
            }
        }
    }
}

@Composable
private fun PreparingTimer(elapsedMin: Int, targetMin: Int) {
    val c = Hungry.colors
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Preparing", style = Hungry.type.caption, color = c.inkMuted)
            Text("$elapsedMin / $targetMin min", style = Hungry.type.caption.copy(fontWeight = FontWeight.Bold), color = c.info)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (elapsedMin.toFloat() / targetMin.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50)),
            color = c.info,
            trackColor = c.infoSoft,
        )
    }
}

@Composable
private fun CardActionButton(label: String, onClick: () -> Unit, widthDp: Int = 108) {
    val c = Hungry.colors
    androidx.compose.material3.Button(
        onClick = onClick,
        shape = HungryRadius.control,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
        modifier = Modifier.width(widthDp.dp).height(48.dp),
    ) {
        Text(label, style = Hungry.type.label.copy(fontWeight = FontWeight.Bold), maxLines = 1)
    }
}
