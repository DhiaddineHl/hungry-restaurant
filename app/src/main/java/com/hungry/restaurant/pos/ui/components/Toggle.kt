package com.hungry.restaurant.pos.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.ui.theme.Hungry

/** §5 Toggle - 48×28, thumb 22 white. On = primary, off = toggleOff. */
@Composable
fun Toggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = Hungry.colors
    val offset by animateDpAsState(if (checked) 17.dp else 3.dp, label = "toggle")
    Box(
        modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(CircleShape)
            .background(if (checked) c.primary else c.toggleOff)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = { onCheckedChange(!checked) },
            )
            .alpha(if (enabled) 1f else 0.5f),
    ) {
        Box(
            Modifier
                .padding(top = 3.dp)
                .offset(x = offset)
                .size(22.dp)
                .clip(CircleShape)
                .background(androidx.compose.ui.graphics.Color.White),
        )
    }
}
