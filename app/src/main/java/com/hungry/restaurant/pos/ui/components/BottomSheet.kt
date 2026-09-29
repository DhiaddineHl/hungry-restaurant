package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.ui.theme.Hungry

/**
 * §5 BottomSheet - surface, top radius 24, 40×4 handle, padding 20,
 * scrim rgba(0,0,0,.45).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HungryBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = androidx.compose.material3.rememberModalBottomSheetState(),
    content: @Composable () -> Unit,
) {
    val c = Hungry.colors
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = c.surface,
        scrimColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f),
        dragHandle = {
            Box(Modifier.padding(top = 12.dp, bottom = 4.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(c.outline),
                )
            }
        },
    ) {
        // ModalBottomSheet hosts itself in its own window, which doesn't inherit the
        // Activity's system-bar visibility - on hardware with a persistent 3-button nav
        // bar (as opposed to gesture nav) it reappears just for this sheet and would
        // otherwise sit on top of whatever's at the bottom of `content` (e.g. Save).
        Column(modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)) {
            content()
        }
    }
}
