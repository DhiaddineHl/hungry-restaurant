package com.hungry.restaurant.pos.ui.screens.menu

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hungry.restaurant.pos.data.model.MenuItem
import com.hungry.restaurant.pos.data.model.asCurrency
import com.hungry.restaurant.pos.ui.components.Badge
import com.hungry.restaurant.pos.ui.components.EmptyState
import com.hungry.restaurant.pos.ui.components.HungryBottomSheet
import com.hungry.restaurant.pos.ui.components.HungryFilterChip
import com.hungry.restaurant.pos.ui.components.ListCard
import com.hungry.restaurant.pos.ui.components.ListCardDivider
import com.hungry.restaurant.pos.ui.components.PrimaryButton
import com.hungry.restaurant.pos.ui.components.QuickPicks
import com.hungry.restaurant.pos.ui.components.SearchField
import com.hungry.restaurant.pos.ui.components.Stepper
import com.hungry.restaurant.pos.ui.components.Toggle
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius

@Composable
fun MenuScreen(
    contentPadding: PaddingValues,
    viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory),
) {
    val c = Hungry.colors
    val type = Hungry.type
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var sheetItem by remember { mutableStateOf<MenuItem?>(null) }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(contentPadding.calculateTopPadding() + 12.dp))
            Text("Menu", style = type.headline, color = c.ink)
            if (state.offTodayCount > 0) {
                Text(
                    "${state.offTodayCount} item${if (state.offTodayCount == 1) "" else "s"} off today",
                    style = type.body,
                    color = c.inkMuted,
                )
            }
            Spacer(Modifier.height(14.dp))
            SearchField(value = state.query, onValueChange = viewModel::setQuery, placeholder = "Search items")
            if (state.categories.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HungryFilterChip("All", selected = state.category == null, onClick = { viewModel.setCategory(null) })
                    state.categories.forEach { category ->
                        HungryFilterChip(category, selected = state.category == category, onClick = { viewModel.setCategory(category) })
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        if (state.visibleItems.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = if (state.query.isBlank()) "No items in this category" else "No items match \"${state.query}\"",
                body = "Items are added and priced from the Hungry partner portal.",
                actionLabel = if (state.query.isNotBlank()) "Clear search" else null,
                onAction = { viewModel.setQuery("") },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp),
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                ),
            ) {
                item {
                    ListCard {
                        state.visibleItems.forEachIndexed { index, menuItem ->
                            MenuItemRow(
                                item = menuItem,
                                onToggle = { viewModel.setAvailable(menuItem, it) },
                                onClick = { sheetItem = menuItem },
                            )
                            if (index != state.visibleItems.lastIndex) ListCardDivider()
                        }
                    }
                }
            }
        }
    }

    sheetItem?.let { item ->
        PrepTimeSheet(
            item = item,
            onDismiss = { sheetItem = null },
            onToggleAvailable = { viewModel.setAvailable(item, it) },
            onSave = { minutes, applyToCategory ->
                viewModel.setPrepTime(item, minutes, applyToCategory)
                sheetItem = null
            },
        )
    }
}

@Composable
private fun MenuItemRow(item: MenuItem, onToggle: (Boolean) -> Unit, onClick: () -> Unit) {
    val c = Hungry.colors
    val type = Hungry.type
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                item.name,
                style = type.bodyStrong,
                color = if (item.available) c.ink else c.inkMuted,
            )
            Spacer(Modifier.height(4.dp))
            if (item.available) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    item.price?.let {
                        Text(it.asCurrency(item.currency), style = type.caption, color = c.inkMuted)
                        Spacer(Modifier.width(8.dp))
                    }
                    item.prepTimeMinutes?.let { minutes ->
                        Row(
                            Modifier.clip(HungryRadius.pill).background(c.surfaceSunken).padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = c.ink, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("$minutes min", style = type.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = c.ink)
                        }
                    }
                }
            } else {
                Badge(item.unavailableReason ?: "Off today", fg = c.danger, bg = c.dangerSoft)
            }
        }
        Spacer(Modifier.width(12.dp))
        Toggle(checked = item.available, onCheckedChange = onToggle)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrepTimeSheet(
    item: MenuItem,
    onDismiss: () -> Unit,
    onToggleAvailable: (Boolean) -> Unit,
    onSave: (minutes: Int, applyToCategory: Boolean) -> Unit,
) {
    val c = Hungry.colors
    val type = Hungry.type
    var minutes by remember(item.id) { mutableStateOf(item.prepTimeMinutes ?: 10) }
    var applyToCategory by remember(item.id) { mutableStateOf(false) }
    var available by remember(item.id) { mutableStateOf(item.available) }

    HungryBottomSheet(onDismissRequest = onDismiss) {
        Text(item.name, style = type.headline, color = c.ink)
        Text(
            listOfNotNull(item.category, item.price?.asCurrency(item.currency)).joinToString(" · "),
            style = type.body,
            color = c.inkMuted,
        )
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(HungryRadius.field)
                .background(c.surfaceSunken)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Available", style = type.bodyStrong, color = c.ink)
            Toggle(checked = available, onCheckedChange = { available = it; onToggleAvailable(it) })
        }
        Spacer(Modifier.height(20.dp))
        Text("Estimated prep time", style = type.bodyStrong, color = c.ink)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Stepper(
                value = "$minutes",
                onDecrement = { if (minutes > 1) minutes-- },
                onIncrement = { minutes++ },
            )
            Spacer(Modifier.width(4.dp))
            Text("min", style = type.body, color = c.inkMuted, modifier = Modifier.align(Alignment.CenterVertically))
        }
        Spacer(Modifier.height(14.dp))
        QuickPicks(values = listOf(5, 8, 10, 12, 15), selected = minutes, onSelect = { minutes = it })
        Spacer(Modifier.height(12.dp))
        Text(
            "Used to suggest the ready time on new orders and the customer's ETA. Staff can still adjust it per order.",
            style = type.caption,
            color = c.inkMuted,
        )
        item.category?.let { category ->
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(HungryRadius.field)
                    .border(1.dp, c.outline, HungryRadius.field)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Apply to all $category", style = type.bodyStrong, color = c.ink)
                Toggle(checked = applyToCategory, onCheckedChange = { applyToCategory = it })
            }
        }
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Save", onClick = { onSave(minutes, applyToCategory) })
        Spacer(Modifier.height(4.dp))
    }
}
