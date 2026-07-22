package com.hungry.restaurant.pos.ui.screens.active

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.PrintDisabled
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.printer.SunmiPrinter
import com.hungry.restaurant.pos.ui.components.OrderCard
import com.hungry.restaurant.pos.ui.theme.StatusNew
import com.hungry.restaurant.pos.ui.theme.StatusPreparing
import com.hungry.restaurant.pos.ui.theme.StatusReady
import com.hungry.restaurant.pos.ui.util.visual

@Composable
fun ActiveOrdersScreen(
    contentPadding: PaddingValues,
    onOrderClick: (String) -> Unit,
    onMessage: (String) -> Unit,
    viewModel: ActiveOrdersViewModel = viewModel(factory = ActiveOrdersViewModel.Factory),
) {
    val board by viewModel.board.collectAsStateWithLifecycle()
    val printerStatus by viewModel.printerStatus.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.messages.collect { onMessage(it) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Header(
                total = board.total,
                printerStatus = printerStatus,
                onReconnect = viewModel::reconnectPrinter,
            )
        }
        item {
            SummaryStrip(
                incoming = board.incoming.size,
                preparing = board.preparing.size,
                ready = board.ready.size,
            )
        }

        if (board.total == 0) {
            item { EmptyState() }
        }

        section("New orders", StatusNew, board.incoming, onOrderClick, viewModel)
        section("Preparing", StatusPreparing, board.preparing, onOrderClick, viewModel)
        section("Ready for handover", StatusReady, board.ready, onOrderClick, viewModel)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    title: String,
    accent: Color,
    orders: List<Order>,
    onOrderClick: (String) -> Unit,
    viewModel: ActiveOrdersViewModel,
) {
    if (orders.isEmpty()) return
    item(key = "header_$title") {
        Row(
            Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "${orders.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    items(orders, key = { it.id }) { order ->
        Column {
            OrderCard(order = order, onClick = { onOrderClick(order.id) })
            ActionRow(order = order, viewModel = viewModel)
        }
    }
}

@Composable
private fun ActionRow(order: Order, viewModel: ActiveOrdersViewModel) {
    val advanceLabel = order.status.visual().advanceLabel ?: return
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = { viewModel.print(order) },
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Outlined.Print, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Print")
        }
        androidx.compose.material3.Button(
            onClick = { viewModel.advance(order) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(advanceLabel)
        }
    }
}

@Composable
private fun Header(
    total: Int,
    printerStatus: SunmiPrinter.Status,
    onReconnect: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                "Active Orders",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "$total in progress",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        PrinterStatusChip(printerStatus, onReconnect)
    }
}

@Composable
private fun PrinterStatusChip(status: SunmiPrinter.Status, onReconnect: () -> Unit) {
    val connected = status == SunmiPrinter.Status.CONNECTED
    val (label, color) = when (status) {
        SunmiPrinter.Status.CONNECTED -> "Printer ready" to MaterialTheme.colorScheme.primary
        SunmiPrinter.Status.CONNECTING -> "Connecting…" to MaterialTheme.colorScheme.onSurfaceVariant
        SunmiPrinter.Status.IDLE -> "Printer idle" to MaterialTheme.colorScheme.onSurfaceVariant
        SunmiPrinter.Status.UNAVAILABLE -> "No printer" to MaterialTheme.colorScheme.error
    }
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(50))
            .background(color.copy(alpha = 0.08f))
            .then(if (!connected) Modifier.clickable(onClick = onReconnect) else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (connected) Icons.Outlined.Print else Icons.Outlined.PrintDisabled,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SummaryStrip(incoming: Int, preparing: Int, ready: Int) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatTile("New", incoming, StatusNew, Modifier.weight(1f))
        StatTile("Preparing", preparing, StatusPreparing, Modifier.weight(1f))
        StatTile("Ready", ready, StatusReady, Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(label: String, value: Int, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Text(
            "$value",
            style = MaterialTheme.typography.headlineSmall,
            color = accent,
            fontWeight = FontWeight.Bold,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "No active orders right now.\nNew tickets will appear here automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
