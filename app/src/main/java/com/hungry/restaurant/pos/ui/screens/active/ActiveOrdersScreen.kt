package com.hungry.restaurant.pos.ui.screens.active

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.hungry.restaurant.pos.printer.SunmiPrinter
import com.hungry.restaurant.pos.ui.components.ActiveOrderCard
import com.hungry.restaurant.pos.ui.theme.NegativeRed
import com.hungry.restaurant.pos.ui.theme.PositiveGreen

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
                when (selectedTab) {
                    BoardTab.NEW -> ActiveOrderCard(
                        order = order,
                        onClick = { onOrderClick(order.id) },
                        primaryLabel = "Accept",
                        onPrimary = { viewModel.accept(order) },
                        secondaryIcon = Icons.Outlined.Close,
                        onSecondary = { viewModel.reject(order) },
                        secondaryTint = NegativeRed,
                    )
                    BoardTab.PREPARING -> ActiveOrderCard(
                        order = order,
                        onClick = { onOrderClick(order.id) },
                        primaryLabel = "Mark ready",
                        onPrimary = { viewModel.markReady(order) },
                        secondaryIcon = Icons.Outlined.Print,
                        onSecondary = { viewModel.print(order) },
                        secondaryTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    BoardTab.READY -> ActiveOrderCard(
                        order = order,
                        onClick = { onOrderClick(order.id) },
                        primaryLabel = "Print ticket",
                        onPrimary = { viewModel.print(order) },
                        primaryFillsWidth = true,
                    )
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OpenClosedPill(acceptingOrders, onToggleAcceptingOrders)
            PrinterStatusDot(printerStatus, onReconnect)
        }
    }
}

@Composable
private fun OpenClosedPill(acceptingOrders: Boolean, onClick: () -> Unit) {
    val color = if (acceptingOrders) PositiveGreen else NegativeRed
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(
            if (acceptingOrders) "Open" else "Closed",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PrinterStatusDot(status: SunmiPrinter.Status, onReconnect: () -> Unit) {
    val connected = status == SunmiPrinter.Status.CONNECTED
    val color = if (connected) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onReconnect),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (connected) Icons.Outlined.Print else Icons.Outlined.PrintDisabled,
            contentDescription = "Printer",
            tint = color,
            modifier = Modifier.size(16.dp),
        )
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
        TabChip("New", counts.first, selected == BoardTab.NEW, Modifier.weight(1f)) { onSelect(BoardTab.NEW) }
        TabChip("Preparing", counts.second, selected == BoardTab.PREPARING, Modifier.weight(1f)) { onSelect(BoardTab.PREPARING) }
        TabChip("Ready", counts.third, selected == BoardTab.READY, Modifier.weight(1f)) { onSelect(BoardTab.READY) }
    }
}

@Composable
private fun TabChip(label: String, count: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
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
            if (count > 0) "$label $count" else label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
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
