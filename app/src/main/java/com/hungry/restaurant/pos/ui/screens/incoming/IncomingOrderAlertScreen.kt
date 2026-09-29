package com.hungry.restaurant.pos.ui.screens.incoming

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderItem
import com.hungry.restaurant.pos.data.model.asCurrency
import com.hungry.restaurant.pos.ui.components.PrimaryButton
import com.hungry.restaurant.pos.ui.components.QuickPicks
import com.hungry.restaurant.pos.ui.components.Stepper
import com.hungry.restaurant.pos.ui.theme.AlertColors
import com.hungry.restaurant.pos.ui.theme.Hungry

/**
 * Screen 03 of the redesign - the full-screen "New order" interrupt, shown as a
 * dialog overlay from [com.hungry.restaurant.pos.ui.navigation.AppNavigation]
 * over whatever screen was active when [com.hungry.restaurant.pos.data.repository.OrderRepository.newOrderEvents]
 * fires. `queuedCount` is the mockup's "1 more waiting" indicator for a second
 * order that arrived while this one was still up. Deliberately no acceptance
 * countdown, per the design system.
 *
 * `defaultReadyInMinutes` seeds the "Ready in" stepper (from the restaurant's
 * own Settings default, or the order's own item prep times when known) - the
 * backend has no per-order prep-time field, so whatever the staff member picks
 * here is used only to print on the ticket, never sent anywhere.
 */
@Composable
fun IncomingOrderAlertScreen(
    order: Order,
    queuedCount: Int,
    defaultReadyInMinutes: Int,
    onAccept: (readyInMinutes: Int) -> Unit,
    onReject: () -> Unit,
) {
    val type = Hungry.type
    var readyInMinutes by remember(order.id) { mutableIntStateOf(defaultReadyInMinutes) }
    val isDelivery = !order.dropoffAddress.isNullOrBlank()

    Column(
        Modifier
            .fillMaxSize()
            .background(AlertColors.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Hungry.colors.primary)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Hungry.colors.onPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("New order", style = type.label.copy(fontWeight = FontWeight.Bold), color = Hungry.colors.onPrimary)
            }
            if (queuedCount > 0) {
                Text("$queuedCount more waiting", style = type.body, color = AlertColors.inkMuted)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("#${order.code}", style = type.display, color = AlertColors.ink)
        Spacer(Modifier.height(4.dp))
        Row {
            Icon(
                if (isDelivery) Icons.Outlined.DirectionsBike else Icons.Outlined.ShoppingBag,
                contentDescription = null,
                tint = AlertColors.inkMuted,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "${if (isDelivery) "Delivery" else "Pickup"} · ${order.customerName}",
                style = type.bodyStrong,
                color = AlertColors.inkMuted,
            )
        }

        Spacer(Modifier.height(20.dp))
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AlertColors.panel),
        ) {
            LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                item { Spacer(Modifier.height(16.dp)) }
                items(order.items) { item -> ItemRow(item, order.currency) }
                item { Spacer(Modifier.height(8.dp)) }
            }
            HorizontalDivider(color = AlertColors.panelBorder, thickness = 1.dp)
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${order.itemCount} items", style = type.body, color = AlertColors.inkMuted)
                Text(order.total.asCurrency(order.currency), style = type.title, color = AlertColors.ink)
            }
        }

        Spacer(Modifier.height(16.dp))
        ReadyInSection(minutes = readyInMinutes, onChange = { readyInMinutes = it })

        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            "Accept & print",
            onClick = { onAccept(readyInMinutes) },
            leadingIcon = Icons.Outlined.Check,
            height = 64.dp,
            radius = RoundedCornerShape(18.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Reject order",
            style = type.bodyStrong,
            color = AlertColors.inkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onReject)
                .padding(vertical = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun ReadyInSection(minutes: Int, onChange: (Int) -> Unit) {
    val type = Hungry.type
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text("Ready in", style = type.label, color = AlertColors.inkMuted)
                Text("From item prep times", style = type.caption, color = AlertColors.inkMuted)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Stepper(
                value = "$minutes min",
                onDecrement = { if (minutes > 5) onChange(minutes - 5) },
                onIncrement = { onChange(minutes + 5) },
                borderColor = AlertColors.controlBorder,
                valueColor = AlertColors.ink,
            )
        }
        Spacer(Modifier.height(12.dp))
        QuickPicks(
            values = listOf(10, 15, 20, 30, 45),
            selected = minutes,
            onSelect = onChange,
            selectedFill = Hungry.colors.primary,
            selectedContent = Hungry.colors.onPrimary,
            unselectedFill = androidx.compose.ui.graphics.Color.Transparent,
            unselectedContent = AlertColors.ink,
            unselectedBorder = AlertColors.controlBorder,
        )
    }
}

@Composable
private fun ItemRow(item: OrderItem, currency: String?) {
    val type = Hungry.type
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("${item.quantity}×", style = type.bodyStrong, color = Hungry.colors.primary)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, style = type.bodyStrong, color = AlertColors.ink)
            if (item.modifiers.isNotEmpty()) {
                Text(item.modifiers.joinToString(" · "), style = type.caption, color = AlertColors.inkMuted)
            }
        }
    }
}
