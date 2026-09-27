package com.hungry.restaurant.pos.ui.screens.incoming

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.OrderItem
import com.hungry.restaurant.pos.data.model.asCurrency
import com.hungry.restaurant.pos.ui.theme.HungryOrange

/**
 * Screen 03 of the redesign - the full-screen "New order" interrupt, shown as a
 * dialog overlay from [com.hungry.restaurant.pos.ui.navigation.AppNavigation]
 * over whatever screen was active when [com.hungry.restaurant.pos.data.repository.OrderRepository.newOrderEvents]
 * fires. `queuedCount` is the mockup's "1 more waiting" indicator for a second
 * order that arrived while this one was still up.
 *
 * `defaultReadyInMinutes` seeds the "Ready in" stepper (from the restaurant's
 * own Settings default) - the backend has no per-order prep-time field, so
 * whatever the staff member picks here is used only to print on the ticket,
 * never sent anywhere.
 */
@Composable
fun IncomingOrderAlertScreen(
    order: Order,
    queuedCount: Int,
    defaultReadyInMinutes: Int,
    onAccept: (readyInMinutes: Int) -> Unit,
    onReject: () -> Unit,
) {
    var readyInMinutes by remember(order.id) { mutableIntStateOf(defaultReadyInMinutes) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF06283D)),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(HungryOrange)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text("New order", color = Color.White, fontWeight = FontWeight.Bold)
                }
                if (queuedCount > 0) {
                    Text(
                        "$queuedCount more waiting",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("#${order.code}", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(order.customerName, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.85f))

            Spacer(Modifier.height(20.dp))
            LazyColumn(Modifier.weight(1f)) {
                items(order.items) { item -> ItemRow(item, order.currency) }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(16.dp),
            ) {
                order.comment?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        "Note: $it",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${order.itemCount} items", color = Color.White.copy(alpha = 0.7f))
                    Text(
                        order.total.asCurrency(order.currency),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            ReadyInStepper(
                minutes = readyInMinutes,
                onChange = { readyInMinutes = it },
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onAccept(readyInMinutes) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HungryOrange),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text("Accept & print", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onReject,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Reject order")
            }
        }
    }
}

@Composable
private fun ReadyInStepper(minutes: Int, onChange: (Int) -> Unit) {
    Column {
        Text("Ready in", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.7f))
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { if (minutes > 5) onChange(minutes - 5) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            ) { Text("−") }
            Text(
                "$minutes min",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            OutlinedButton(
                onClick = { onChange(minutes + 5) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            ) { Text("+") }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(10, 15, 20, 30, 45).forEach { preset ->
                FilterChip(
                    selected = minutes == preset,
                    onClick = { onChange(preset) },
                    label = { Text("$preset") },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        labelColor = Color.White.copy(alpha = 0.8f),
                        selectedContainerColor = HungryOrange,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ItemRow(item: OrderItem, currency: String?) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${item.quantity}× ${item.name}",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(item.lineTotal.asCurrency(currency), color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
        if (item.modifiers.isNotEmpty()) {
            Text(
                item.modifiers.joinToString(" · "),
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
