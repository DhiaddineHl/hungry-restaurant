package com.hungry.restaurant.pos.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hungry.restaurant.pos.data.model.MenuItem
import com.hungry.restaurant.pos.data.model.asCurrency

@Composable
fun MenuScreen(
    contentPadding: PaddingValues,
    viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var sheetItem by remember { mutableStateOf<MenuItem?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(contentPadding.calculateTopPadding() + 8.dp))
            Text("Menu", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            if (state.offTodayCount > 0) {
                Text(
                    "${state.offTodayCount} item${if (state.offTodayCount == 1) "" else "s"} off today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Search menu") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.categories.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.category == null,
                        onClick = { viewModel.setCategory(null) },
                        label = { Text("All") },
                        shape = RoundedCornerShape(50),
                        colors = selectedChipColors(),
                    )
                    state.categories.forEach { category ->
                        FilterChip(
                            selected = state.category == category,
                            onClick = { viewModel.setCategory(category) },
                            label = { Text(category) },
                            shape = RoundedCornerShape(50),
                            colors = selectedChipColors(),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.visibleItems, key = { it.id }) { item ->
                MenuItemRow(
                    item = item,
                    onToggle = { viewModel.setAvailable(item, it) },
                    onClick = { sheetItem = item },
                )
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

/** No card/border - the redesign shows menu items as a flat list, separated by hairline dividers only. */
@Composable
private fun MenuItemRow(item: MenuItem, onToggle: (Boolean) -> Unit, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                val nameAlpha = if (item.available) 1f else 0.5f
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = nameAlpha),
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    item.price?.let {
                        Text(it.asCurrency(item.currency), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    item.prepTimeMinutes?.let { minutes ->
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("$minutes min", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (!item.available) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        item.unavailableReason ?: "Off today",
                        style = MaterialTheme.typography.labelSmall,
                        color = com.hungry.restaurant.pos.ui.theme.StatusPreparing,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Switch(checked = item.available, onCheckedChange = onToggle)
        }
        androidx.compose.material3.Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }
}

/** Selected = high-contrast onSurface fill (navy in light, near-white in dark) - matches the redesign's chip style, not the default tinted-orange Material one. */
@Composable
private fun selectedChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.onSurface,
    selectedLabelColor = MaterialTheme.colorScheme.surface,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrepTimeSheet(
    item: MenuItem,
    onDismiss: () -> Unit,
    onToggleAvailable: (Boolean) -> Unit,
    onSave: (minutes: Int, applyToCategory: Boolean) -> Unit,
) {
    var minutes by remember(item.id) { mutableStateOf(item.prepTimeMinutes ?: 10) }
    var applyToCategory by remember(item.id) { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.padding(24.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                listOfNotNull(item.category, item.price?.asCurrency(item.currency)).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Available", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Switch(checked = item.available, onCheckedChange = onToggleAvailable)
            }
            Spacer(Modifier.height(20.dp))
            Text("Estimated prep time", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.OutlinedButton(onClick = { if (minutes > 1) minutes-- }) { Text("−") }
                Text(
                    "$minutes min",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                androidx.compose.material3.OutlinedButton(onClick = { minutes++ }) { Text("+") }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 8, 10, 12, 15).forEach { preset ->
                    FilterChip(
                        selected = minutes == preset,
                        onClick = { minutes = preset },
                        label = { Text("$preset") },
                        shape = RoundedCornerShape(50),
                        colors = selectedChipColors(),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Used to suggest the ready time on new orders and the customer's ETA. Staff can still adjust it per order.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            item.category?.let { category ->
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Apply to all $category", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Switch(checked = applyToCategory, onCheckedChange = { applyToCategory = it })
                }
            }
            Spacer(Modifier.height(24.dp))
            androidx.compose.material3.Button(
                onClick = { onSave(minutes, applyToCategory) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) { Text("Save") }
        }
    }
}
