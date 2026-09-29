package com.hungry.restaurant.pos.ui.screens.details

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderItem
import com.hungry.restaurant.pos.data.model.OrderStatus
import com.hungry.restaurant.pos.data.model.asCurrency
import com.hungry.restaurant.pos.ui.components.PrimaryButton
import com.hungry.restaurant.pos.ui.components.SquareIconButton
import com.hungry.restaurant.pos.ui.components.StatusPill
import com.hungry.restaurant.pos.ui.components.TextLink
import com.hungry.restaurant.pos.ui.components.HungryTopBar
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius
import com.hungry.restaurant.pos.ui.util.clockTime
import com.hungry.restaurant.pos.ui.util.dateTime
import com.hungry.restaurant.pos.ui.util.label
import com.hungry.restaurant.pos.ui.util.statusTone

/**
 * Screen 04. The mockup also shows a rider row ("Rider Hamza · arrives 12:36")
 * and a customer phone-call button - both dropped here because the backend's
 * `Order` has no courier-assignment or customer-phone field to back them
 * (matching this pass's "ask before changing data models" constraint).
 */
@Composable
fun OrderDetailsScreen(
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: OrderDetailsViewModel = viewModel(factory = OrderDetailsViewModel.Factory),
) {
    val c = Hungry.colors
    val order by viewModel.order.collectAsStateWithLifecycle()
    val defaultPrepTimeMinutes by viewModel.defaultPrepTimeMinutes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.messages.collect { onMessage(it) }
    }

    Scaffold(
        containerColor = c.canvas,
        topBar = {
            HungryTopBar(
                title = order?.let { "#${it.code}" } ?: "Order",
                onBack = onBack,
                orderIdStyle = true,
                trailing = {
                    order?.let {
                        val (fg, bg) = it.status.statusTone()
                        StatusPill(it.status.label(), fg = fg, bg = bg)
                    }
                },
            )
        },
        bottomBar = {
            order?.let { ActionBar(it, viewModel) }
        },
    ) { padding ->
        val current = order
        if (current == null) {
            Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Order not found.", style = Hungry.type.body, color = c.inkMuted)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(2.dp))
            if (current.status == OrderStatus.CONFIRMED || current.status == OrderStatus.PREPARING) {
                DueTimeCard(current, defaultPrepTimeMinutes)
            }
            CustomerCard(current)
            current.comment?.takeIf { it.isNotBlank() }?.let { NoteCard(it) }
            current.cancelReason?.takeIf { it.isNotBlank() }?.let { NoteCard("Cancelled: $it") }
            ItemsCard(current)
            TotalsCard(current)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DueTimeCard(order: Order, defaultPrepTimeMinutes: Int) {
    val c = Hungry.colors
    val type = Hungry.type
    val confirmedAt = order.confirmedAtMillis ?: return
    val dueAtMillis = confirmedAt + defaultPrepTimeMinutes * 60_000L
    val now = System.currentTimeMillis()
    val elapsedFraction = ((now - confirmedAt).toFloat() / (dueAtMillis - confirmedAt).toFloat()).coerceIn(0f, 1f)
    val minutesLeft = ((dueAtMillis - now) / 60_000L).toInt()
    val overdue = minutesLeft < 0

    Card {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Accepted ${clockTime(confirmedAt)}", style = type.body, color = c.inkMuted)
            Text(
                if (overdue) "Due ${clockTime(dueAtMillis)} · ${-minutesLeft} min overdue" else "Due ${clockTime(dueAtMillis)} · $minutesLeft min left",
                style = type.bodyStrong,
                color = if (overdue) c.danger else c.info,
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { elapsedFraction },
            color = if (overdue) c.danger else c.info,
            trackColor = c.infoSoft,
            modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50)),
        )
    }
}

@Composable
private fun CustomerCard(order: Order) {
    val c = Hungry.colors
    val type = Hungry.type
    Card {
        Text(order.customerName, style = type.title, color = c.ink)
        order.dropoffAddress?.takeIf { it.isNotBlank() }?.let { address ->
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = c.inkMuted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(address, style = type.body, color = c.inkMuted)
            }
        }
    }
}

@Composable
private fun ItemsCard(order: Order) {
    val c = Hungry.colors
    val type = Hungry.type
    Card {
        order.items.forEachIndexed { index, item ->
            ItemRow(item, order.currency)
            if (index != order.items.lastIndex) {
                Spacer(Modifier.height(10.dp))
                androidx.compose.material3.HorizontalDivider(color = c.outline, thickness = 1.dp)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ItemRow(item: OrderItem, currency: String?) {
    val c = Hungry.colors
    val type = Hungry.type
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(32.dp).clip(HungryRadius.control).background(c.surfaceSunken),
            contentAlignment = Alignment.Center,
        ) {
            Text(item.quantity.toString(), style = type.bodyStrong, color = c.ink)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, style = type.bodyStrong, color = c.ink)
            if (item.modifiers.isNotEmpty()) {
                Text(item.modifiers.joinToString(" · "), style = type.caption, color = c.inkMuted)
            }
        }
        Text(item.lineTotal.asCurrency(currency, withSuffix = false), style = type.bodyStrong, color = c.ink)
    }
}

@Composable
private fun TotalsCard(order: Order) {
    val c = Hungry.colors
    val type = Hungry.type
    Card {
        TotalRow("Subtotal", order.subtotal.asCurrency(order.currency, withSuffix = false))
        if (order.discountTotal > 0) TotalRow("Discount", "-${order.discountTotal.asCurrency(order.currency, withSuffix = false)}")
        if (order.deliveryFee > 0) TotalRow("Delivery", order.deliveryFee.asCurrency(order.currency, withSuffix = false))
        if (order.serviceFee > 0) TotalRow("Service fee", order.serviceFee.asCurrency(order.currency, withSuffix = false))
        if (order.additionalFees > 0) TotalRow("Additional fees", order.additionalFees.asCurrency(order.currency, withSuffix = false))
        Spacer(Modifier.height(6.dp))
        androidx.compose.material3.HorizontalDivider(color = c.outline, thickness = 1.dp)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", style = type.bodyStrong, color = c.ink)
            Text(order.total.asCurrency(order.currency), style = type.title, color = c.ink)
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String) {
    val c = Hungry.colors
    val type = Hungry.type
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = type.body, color = c.inkMuted)
        Text(value, style = type.body, color = c.ink)
    }
}

@Composable
private fun NoteCard(note: String) {
    val c = Hungry.colors
    val type = Hungry.type
    Row(
        Modifier
            .fillMaxWidth()
            .clip(HungryRadius.field)
            .background(c.primarySoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = c.onPrimarySoft, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text("“$note”", style = type.bodyStrong, color = c.onPrimarySoft)
    }
}

@Composable
private fun ActionBar(order: Order, viewModel: OrderDetailsViewModel) {
    val c = Hungry.colors
    Column(
        Modifier
            .fillMaxWidth()
            .background(c.surface)
            .padding(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            SquareIconButton(Icons.Outlined.Print, onClick = viewModel::print, tint = c.ink, contentDescription = "Print")
            when (order.status) {
                OrderStatus.CREATED -> PrimaryButton("Accept", onClick = viewModel::accept, modifier = Modifier.weight(1f), height = 48.dp)
                OrderStatus.CONFIRMED, OrderStatus.PREPARING -> PrimaryButton(
                    "Mark as ready",
                    onClick = viewModel::markReady,
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                )
                else -> {}
            }
        }
        if (order.status == OrderStatus.CREATED) {
            Spacer(Modifier.height(8.dp))
            TextLink("Reject order", onClick = viewModel::reject, color = c.danger, modifier = Modifier.fillMaxWidth())
        } else if (order.status.isActive) {
            Spacer(Modifier.height(8.dp))
            TextLink("Cancel order", onClick = viewModel::cancel, color = c.danger, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** Flat, borderless surface card - the redesign has no card outlines, only white-on-cream contrast. */
@Composable
private fun Card(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(HungryRadius.card)
            .background(Hungry.colors.surface)
            .padding(16.dp),
        content = content,
    )
}
