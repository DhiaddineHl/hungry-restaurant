package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.data.model.Order
import com.hungry.restaurant.pos.data.model.asCurrency
import com.hungry.restaurant.pos.ui.util.relativeTime

/**
 * An order card for the active board - a flat, borderless surface (the redesign
 * has no card outlines anywhere, only the white-on-cream contrast) with the
 * total and its action(s) integrated into the same bottom row, matching the
 * mockup's compact layout rather than a separate action bar underneath.
 */
@Composable
fun ActiveOrderCard(
    order: Order,
    onClick: () -> Unit,
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryIcon: ImageVector? = null,
    onSecondary: (() -> Unit)? = null,
    secondaryTint: Color = MaterialTheme.colorScheme.error,
    primaryFillsWidth: Boolean = false,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "#${order.code}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TimeAgoPill(order.placedAtMillis)
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(15.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    order.customerName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            Column {
                order.items.forEach { item ->
                    Text(
                        "${item.quantity}× ${item.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!primaryFillsWidth) {
                    Text(
                        order.total.asCurrency(order.currency),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = if (primaryFillsWidth) Modifier.fillMaxWidth() else Modifier,
                ) {
                    if (secondaryIcon != null && onSecondary != null) {
                        IconButton(
                            onClick = onSecondary,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Icon(secondaryIcon, contentDescription = null, tint = secondaryTint, modifier = Modifier.size(18.dp))
                        }
                    }
                    PrimaryPillButton(
                        label = primaryLabel,
                        onClick = onPrimary,
                        modifier = if (primaryFillsWidth) Modifier.weight(1f) else Modifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeAgoPill(placedAtMillis: Long) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            relativeTime(placedAtMillis),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrimaryPillButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        modifier = modifier,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
