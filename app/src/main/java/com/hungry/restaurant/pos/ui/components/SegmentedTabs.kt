package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.ui.theme.Hungry

/**
 * §5 SegmentedTabs - 44dp, track surfaceSunken + 1px outline, radius 14, inset 3.
 * Selected segment: surface, radius 11, shadow. Optional count badge 20dp.
 * Pass [small] for the 36dp/radius-12 variant (Stats period, Appearance).
 */
@Composable
fun <T> SegmentedTabs(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    count: ((T) -> Int?)? = null,
    small: Boolean = false,
) {
    val colors = Hungry.colors
    val type = Hungry.type
    val height = if (small) 36.dp else 44.dp
    val outerRadius = if (small) 12.dp else 14.dp
    val innerRadius = if (small) 9.dp else 11.dp
    Row(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(outerRadius))
            .background(colors.surfaceSunken)
            .border(1.dp, colors.outline, RoundedCornerShape(outerRadius))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(innerRadius))
                    .then(
                        if (isSelected) {
                            Modifier
                                .shadow(1.dp, RoundedCornerShape(innerRadius), clip = false)
                                .background(colors.surface, RoundedCornerShape(innerRadius))
                        } else Modifier,
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(option) },
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    label(option),
                    style = type.label,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) colors.ink else colors.inkMuted,
                )
                val c = count?.invoke(option)
                if (c != null && c > 0) {
                    Box(
                        Modifier
                            .padding(start = 6.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) colors.primarySoft else colors.outline)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            c.toString(),
                            style = type.badge,
                            color = if (isSelected) colors.onPrimarySoft else colors.inkMuted,
                        )
                    }
                }
            }
        }
    }
}
