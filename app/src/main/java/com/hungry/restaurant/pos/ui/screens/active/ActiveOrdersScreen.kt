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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.PrintDisabled
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.hungry.restaurant.pos.ui.theme.NegativeRed
import com.hungry.restaurant.pos.ui.theme.PositiveGreen
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
    val restaurant by viewModel.restaurant.collectAsStateWithLifecycle()
    val printerStatus by viewModel.printerStatus.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.messages.collect { onMessage(it) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(contentPadding.calculateTopPadding() + 8.dp))
            Header(
                restaurantName = restaurant?.name ?: "Orders",
                total = board.total,
                acceptingOrders = restaurant?.acceptingOrders ?: true,
                onToggleAcceptingOrders = viewModel::toggleAcceptingOrders,
                printerStatus = printerStatus,
                onReconnect = viewModel::reconnectPrinter,
            )
            Spacer(Modifier.height(14.dp))
            TabStrip(
                selected = selectedTab,
                counts = Triple(board.incoming.size, board.preparing.size, board.ready.size),
                onSelect = viewModel::selectTab,
            )
        }

        val visibleOrders = when (selectedTab) {
            BoardTab.NEW -> board.incoming
            BoardTab.PREPARING -> board.preparing
            BoardTab.READY -> board.ready
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 14.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (visibleOrders.isEmpty()) {
                item { EmptyState(selectedTab) }
            }
            items(visibleOrders, key = { it.id }) { order ->
                Column {
                    OrderCard(order = order, onClick = { onOrderClick(order.id) })
                    ActionRow(order = order, tab = selectedTab, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun ActionRow(order: Order, tab: BoardTab, viewModel: ActiveOrdersViewModel) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (tab) {
            BoardTab.NEW -> {
                OutlinedButton(
                    onClick = { viewModel.reject(order) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NegativeRed),
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "Reject", modifier = Modifier.size(18.dp))
                }
                Button(
                    onClick = { viewModel.accept(order) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Accept")
                }
            }

            BoardTab.PREPARING -> {
                OutlinedButton(onClick = { viewModel.print(order) }, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Outlined.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Print")
                }
                Button(
                    onClick = { viewModel.markReady(order) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Mark ready")
                }
            }

            BoardTab.READY -> {
                OutlinedButton(
                    onClick = { viewModel.print(order) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Print ticket")
                }
            }
        }
    }
}

@Composable
private fun Header(
    restaurantName: String,
    total: Int,
    acceptingOrders: Boolean,
    onToggleAcceptingOrders: () -> Unit,
    printerStatus: SunmiPrinter.Status,
    onReconnect: () -> Unit,
) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    restaurantName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "$total active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PrinterStatusChip(printerStatus, onReconnect)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (acceptingOrders) "Open for orders" else "Closed - not accepting orders",
                style = MaterialTheme.typography.bodyMedium,
                color = if (acceptingOrders) PositiveGreen else NegativeRed,
                fontWeight = FontWeight.SemiBold,
            )
            Switch(
                checked = acceptingOrders,
                onCheckedChange = { onToggleAcceptingOrders() },
                colors = SwitchDefaults.colors(checkedTrackColor = PositiveGreen),
            )
        }
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
private fun TabStrip(selected: BoardTab, counts: Triple<Int, Int, Int>, onSelect: (BoardTab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TabChip("New", counts.first, StatusNew, selected == BoardTab.NEW, Modifier.weight(1f)) { onSelect(BoardTab.NEW) }
        TabChip("Preparing", counts.second, StatusPreparing, selected == BoardTab.PREPARING, Modifier.weight(1f)) { onSelect(BoardTab.PREPARING) }
        TabChip("Ready", counts.third, StatusReady, selected == BoardTab.READY, Modifier.weight(1f)) { onSelect(BoardTab.READY) }
    }
}

@Composable
private fun TabChip(label: String, count: Int, accent: Color, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
        if (count > 0) {
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(accent.copy(alpha = if (selected) 1f else 0.5f))
                    .padding(horizontal = 7.dp, vertical = 1.dp),
            ) {
                Text("$count", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyState(tab: BoardTab) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            when (tab) {
                BoardTab.NEW -> "No new orders right now.\nIncoming tickets will appear here automatically."
                BoardTab.PREPARING -> "Nothing in the kitchen right now."
                BoardTab.READY -> "Nothing ready for pickup right now."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
