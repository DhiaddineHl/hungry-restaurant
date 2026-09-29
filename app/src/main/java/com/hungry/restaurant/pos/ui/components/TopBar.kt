package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.ui.tooling.preview.Preview
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryPosTheme
import com.hungry.restaurant.pos.ui.theme.ThemeMode

/**
 * §5 TopBar - 56dp. Left: 44dp round back button (surface + 1px outline) or a
 * 36dp restaurant logo. Title `title`/`orderId` style. Right: status pill
 * and/or a 40dp icon button, via [trailing].
 */
@Composable
fun HungryTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    orderIdStyle: Boolean = false,
    logo: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = Hungry.colors
    val type = Hungry.type
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            onBack != null -> IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.surface)
                    .border(1.dp, colors.outline, CircleShape),
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = colors.ink)
            }
            logo != null -> Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) { logo() }
        }
        Text(
            title,
            style = if (orderIdStyle) type.orderId else type.title,
            color = colors.ink,
            modifier = Modifier
                .weight(1f)
                .padding(start = if (onBack != null || logo != null) 12.dp else 0.dp),
        )
        if (trailing != null) {
            Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun HungryTopBarPreviewLight() = HungryPosTheme(ThemeMode.LIGHT) {
    HungryTopBar(title = "#P-228", onBack = {}, orderIdStyle = true, trailing = { Box(Modifier.width(1.dp)) })
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun HungryTopBarPreviewDark() = HungryPosTheme(ThemeMode.DARK) {
    HungryTopBar(title = "#P-228", onBack = {}, orderIdStyle = true, trailing = { Box(Modifier.width(1.dp)) })
}
