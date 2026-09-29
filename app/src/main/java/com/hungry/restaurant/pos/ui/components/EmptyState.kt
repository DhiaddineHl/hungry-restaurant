package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.ui.theme.Hungry

/**
 * §5 EmptyState - centered, padding 32. 72dp circle icon well (surface, or
 * dangerSoft/primarySoft for errors) with 30dp icon; title 18/700; body
 * 14/21 inkMuted; optional SecondaryButton.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    iconWellColor: Color? = null,
    iconColor: Color? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = Hungry.colors
    Column(
        modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(iconWellColor ?: c.surfaceSunken),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconColor ?: c.inkMuted, modifier = Modifier.size(30.dp))
        }
        Text(title, style = Hungry.type.title, color = c.ink, textAlign = TextAlign.Center)
        if (body != null) {
            Text(body, style = Hungry.type.body, color = c.inkMuted, textAlign = TextAlign.Center)
        }
        if (actionLabel != null && onAction != null) {
            androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
            SecondaryButton(actionLabel, onAction, modifier = Modifier.padding(horizontal = 24.dp), height = 48.dp)
        }
    }
}
