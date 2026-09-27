package com.hungry.restaurant.pos.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.hungry.restaurant.pos.ui.components.StatusPill
import com.hungry.restaurant.pos.ui.theme.HungryOrange
import com.hungry.restaurant.pos.ui.util.clockTime
import com.hungry.restaurant.pos.ui.util.dateTime
import com.hungry.restaurant.pos.ui.util.visual

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: OrderDetailsViewModel = viewModel(factory = OrderDetailsViewModel.Factory),
) {
    val order by viewModel.order.collectAsStateWithLifecycle()
    val defaultPrepTimeMinutes by viewModel.defaultPrepTimeMinutes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.messages.collect { onMessage(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(order?.let { "Order #${it.code}" } ?: "Order") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            order?.let { ActionBar(it, viewModel) }
        },
    ) { padding ->
        val current = order
        if (current == null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Order not found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(2.dp))
            StatusHeader(current)
            if (current.status == OrderStatus.CONFIRMED || current.status == OrderStatus.PREPARING) {
                DueTimeCard(current, defaultPrepTimeMinutes)
            }
            CustomerCard(current)
            ItemsCard(current)
            TotalsCard(current)
            current.comment?.takeIf { it.isNotBlank() }?.let { NoteCard(it) }
            current.cancelReason?.takeIf { it.isNotBlank() }?.let { NoteCard("Cancelled: $it") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatusHeader(order: Order) {
    val vis = order.status.visual()
    Card {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "#${order.code}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    dateTime(order.placedAtMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusPill(vis.label, vis.color)
        }
    }
}

@Composable
private fun DueTimeCard(order: Order, defaultPrepTimeMinutes: Int) {
    val confirmedAt = order.confirmedAtMillis ?: return
    val dueAtMillis = confirmedAt + defaultPrepTimeMinutes * 60_000L
    val now = System.currentTimeMillis()
    val elapsedFraction = ((now - confirmedAt).toFloat() / (dueAtMillis - confirmedAt).toFloat()).coerceIn(0f, 1f)
    val minutesLeft = ((dueAtMillis - now) / 60_000L).toInt()
    val overdue = minutesLeft < 0

    Card {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Accepted ${clockTime(confirmedAt)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                if (overdue) "Due ${clockTime(dueAtMillis)} · ${-minutesLeft} min overdue" else "Due ${clockTime(dueAtMillis)} · $minutesLeft min left",
                style = MaterialTheme.typography.bodyMedium,
                color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { elapsedFraction },
            color = if (overdue) MaterialTheme.colorScheme.error else HungryOrange,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
        )
    }
}

@Composable
private fun CustomerCard(order: Order) {
    Card {
        InfoRow(Icons.Outlined.Person, "Customer", order.customerName)
        order.dropoffAddress?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(12.dp))
            InfoRow(Icons.Outlined.LocationOn, "Address", it)
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ItemsCard(order: Order) {
    Card {
        Text(
            "Items (${order.itemCount})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        order.items.forEachIndexed { index, item ->
            ItemRow(item, order.currency)
            if (index != order.items.lastIndex) {
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ItemRow(item: OrderItem, currency: String?) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.weight(1f)) {
                Text(
                    "${item.quantity}×",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                item.lineTotal.asCurrency(currency),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item.modifiers.forEach { mod ->
            Text(
                mod,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 26.dp, top = 2.dp),
            )
        }
    }
}

@Composable
private fun TotalsCard(order: Order) {
    Card {
        TotalRow("Subtotal", order.subtotal.asCurrency(order.currency))
        if (order.discountTotal > 0) TotalRow("Discount", "-${order.discountTotal.asCurrency(order.currency)}")
        if (order.deliveryFee > 0) TotalRow("Delivery fee", order.deliveryFee.asCurrency(order.currency))
        if (order.serviceFee > 0) TotalRow("Service fee", order.serviceFee.asCurrency(order.currency))
        if (order.additionalFees > 0) TotalRow("Additional fees", order.additionalFees.asCurrency(order.currency))
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                order.total.asCurrency(order.currency),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun NoteCard(note: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Note",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun ActionBar(order: Order, viewModel: OrderDetailsViewModel) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Column(Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { viewModel.print() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(52.dp),
                ) {
                    Icon(Icons.Outlined.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Print")
                }
                when (order.status) {
                    OrderStatus.CREATED -> Button(
                        onClick = { viewModel.accept() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(52.dp),
                    ) { Text("Accept", style = MaterialTheme.typography.titleMedium) }

                    OrderStatus.CONFIRMED, OrderStatus.PREPARING -> Button(
                        onClick = { viewModel.markReady() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(52.dp),
                    ) { Text("Mark as ready", style = MaterialTheme.typography.titleMedium) }

                    else -> {}
                }
            }
            if (order.status == OrderStatus.CREATED) {
                TextButton(onClick = { viewModel.reject() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Reject order", color = MaterialTheme.colorScheme.error)
                }
            } else if (order.status.isActive) {
                TextButton(onClick = { viewModel.cancel() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel order", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/** Flat, borderless surface card - the redesign has no card outlines, only white-on-cream contrast. */
@Composable
private fun Card(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        content = content,
    )
}
