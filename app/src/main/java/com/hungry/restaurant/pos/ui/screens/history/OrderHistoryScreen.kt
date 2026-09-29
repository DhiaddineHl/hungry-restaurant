package com.hungry.restaurant.pos.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hungry.restaurant.pos.data.model.asCurrency
import com.hungry.restaurant.pos.ui.components.EmptyState
import com.hungry.restaurant.pos.ui.components.HistoryOrderRow
import com.hungry.restaurant.pos.ui.components.HungryFilterChip
import com.hungry.restaurant.pos.ui.components.ListCard
import com.hungry.restaurant.pos.ui.components.ListCardDivider
import com.hungry.restaurant.pos.ui.components.SearchField
import com.hungry.restaurant.pos.ui.theme.Hungry

@Composable
fun OrderHistoryScreen(
    contentPadding: PaddingValues,
    onOrderClick: (String) -> Unit,
    viewModel: OrderHistoryViewModel = viewModel(factory = OrderHistoryViewModel.Factory),
) {
    val c = Hungry.colors
    val type = Hungry.type
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(c.canvas),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Text("History", style = type.headline, color = c.ink) }

        item {
            SearchField(value = state.query, onValueChange = viewModel::setQuery, placeholder = "Order # or customer")
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateFilter.entries.forEach { filter ->
                    HungryFilterChip(
                        label = filter.label,
                        selected = state.dateFilter == filter,
                        onClick = { viewModel.setDateFilter(filter) },
                    )
                }
            }
        }

        if (state.visibleOrders.isNotEmpty() || state.loading) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${state.summaryCount} orders", style = type.body, color = c.inkMuted)
                    Text(state.summaryRevenue.asCurrency(state.summaryCurrency), style = type.bodyStrong, color = c.ink)
                }
            }
        }

        if (!state.loading && state.visibleOrders.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = if (state.query.isBlank()) "No orders yet" else "No orders match \"${state.query}\"",
                    body = "Try another order number or customer name, or widen the date range.",
                    actionLabel = if (state.dateFilter != DateFilter.WEEK) "Search last 7 days" else null,
                    onAction = { viewModel.setDateFilter(DateFilter.WEEK) },
                    modifier = Modifier.padding(top = 40.dp),
                )
            }
        }

        if (state.visibleOrders.isNotEmpty()) {
            item {
                ListCard {
                    state.visibleOrders.forEachIndexed { index, order ->
                        HistoryOrderRow(
                            order = order,
                            onClick = { onOrderClick(order.id) },
                            modifier = Modifier.padding(horizontal = 14.dp),
                        )
                        if (index != state.visibleOrders.lastIndex) ListCardDivider()
                    }
                }
            }
        }
    }
}
