package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius

/**
 * §5 Toast - inverseSurface, radius 16, padding 12×14, shadow, 20dp icon +
 * message 13/600 + optional action in primary 800. Caller places this above
 * the sticky action bar (E4: with a "Retry" action).
 */
@Composable
fun HungryToast(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = Hungry.colors
    Row(
        modifier
            .fillMaxWidth()
            .shadow(12.dp, HungryRadius.button, clip = false)
            .clip(HungryRadius.button)
            .background(c.inverseSurface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = c.onInverseSurface, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(message, style = Hungry.type.label, color = c.onInverseSurface, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.width(10.dp))
            Text(
                actionLabel,
                style = Hungry.type.label.copy(fontWeight = FontWeight.ExtraBold),
                color = c.primary,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}
