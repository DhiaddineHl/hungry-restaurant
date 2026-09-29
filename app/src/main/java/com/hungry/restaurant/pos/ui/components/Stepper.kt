package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.ui.theme.Hungry

/**
 * §5 Stepper - 44–56dp round outline −/+ around a bold value.
 * [borderColor]/[valueColor] let the incoming-order alert render this in white on navy.
 */
@Composable
fun Stepper(
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    borderColor: Color? = null,
    valueColor: Color? = null,
    decrementEnabled: Boolean = true,
    incrementEnabled: Boolean = true,
) {
    val c = Hungry.colors
    val border = borderColor ?: c.outline
    val textColor = valueColor ?: c.ink
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(Icons.Outlined.Remove, onDecrement, size, border, textColor, decrementEnabled)
        Text(value, style = Hungry.type.hero, color = textColor)
        StepperButton(Icons.Outlined.Add, onIncrement, size, border, textColor, incrementEnabled)
    }
}

@Composable
private fun StepperButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, size: Dp, border: Color, tint: Color, enabled: Boolean) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(1.5.dp, border.copy(alpha = if (enabled) 1f else 0.4f), CircleShape),
    ) {
        Icon(icon, contentDescription = null, tint = tint.copy(alpha = if (enabled) 1f else 0.4f))
    }
}

/**
 * §5 QuickPicks - 5-col grid of 40dp radius-12 cells. [selectedFill] lets the
 * caller pass `primary` (alert screen) or `inverseSurface` (bottom sheet).
 */
@Composable
fun QuickPicks(
    values: List<Int>,
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedFill: Color? = null,
    selectedContent: Color? = null,
    unselectedFill: Color? = null,
    unselectedContent: Color? = null,
    unselectedBorder: Color? = null,
) {
    val c = Hungry.colors
    val fill = selectedFill ?: c.inverseSurface
    val content = selectedContent ?: c.onInverseSurface
    val offFill = unselectedFill ?: c.surface
    val offContent = unselectedContent ?: c.ink
    val offBorder = unselectedBorder ?: c.outline
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { v ->
            val isSelected = v == selected
            Row(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isSelected) Modifier.background(fill)
                        else Modifier.background(offFill).border(1.dp, offBorder, RoundedCornerShape(12.dp)),
                    )
                    .clickable { onSelect(v) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(v.toString(), style = Hungry.type.bodyStrong, color = if (isSelected) content else offContent)
            }
        }
    }
}
