package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius

/**
 * §5 SearchField - 48dp, radius 14, surface + 1px outline, 18dp search icon,
 * placeholder inkMuted. Focused: 1.5px ink border + clear (x) icon.
 */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
) {
    val c = Hungry.colors
    var focused by remember { mutableStateOf(false) }
    val borderColor = if (focused) c.ink else c.outline
    val borderWidth = if (focused) 1.5.dp else 1.dp
    Row(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(HungryRadius.field)
            .background(c.surface)
            .border(borderWidth, borderColor, HungryRadius.field)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = c.inkMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, style = Hungry.type.body, color = c.inkMuted)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = Hungry.type.body.copy(color = c.ink),
                singleLine = true,
                cursorBrush = SolidColor(c.ink),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused },
            )
        }
        if (value.isNotEmpty()) {
            IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Outlined.Close, contentDescription = "Clear", tint = c.inkMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}
