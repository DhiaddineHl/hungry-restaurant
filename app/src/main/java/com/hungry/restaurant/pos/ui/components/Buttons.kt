package com.hungry.restaurant.pos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.hungry.restaurant.pos.ui.theme.Hungry
import com.hungry.restaurant.pos.ui.theme.HungryRadius

/** §5 PrimaryButton - 56dp, radius 16, primary/onPrimary, `button` type. */
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    height: androidx.compose.ui.unit.Dp = 56.dp,
    radius: androidx.compose.foundation.shape.RoundedCornerShape = HungryRadius.button,
) {
    val c = Hungry.colors
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(height),
        shape = radius,
        colors = ButtonDefaults.buttonColors(
            containerColor = c.primary,
            contentColor = c.onPrimary,
            disabledContainerColor = c.primary.copy(alpha = 0.5f),
            disabledContentColor = c.onPrimary.copy(alpha = 0.7f),
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = c.onPrimary, strokeWidth = 2.dp)
        } else {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            }
            Text(label, style = Hungry.type.button)
            if (trailingIcon != null) {
                androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
                Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** §5 SecondaryButton - 48–52dp, radius 14–16, 1.5px outline, 14–15/700 ink. */
@Composable
fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
    leadingIcon: ImageVector? = null,
    height: androidx.compose.ui.unit.Dp = 52.dp,
) {
    val c = Hungry.colors
    val contentColor = if (destructive) c.danger else c.ink
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(height),
        shape = HungryRadius.field,
        border = BorderStroke(1.5.dp, c.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        }
        Text(label, style = Hungry.type.bodyStrong, color = contentColor)
    }
}

/** §5 TextLink - 13/700 underline, min 44dp hit area. */
@Composable
fun TextLink(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: androidx.compose.ui.graphics.Color? = null) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 44.dp, minHeight = 44.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        Text(
            label,
            style = Hungry.type.label.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
            ),
            color = color ?: Hungry.colors.ink,
        )
    }
}

/** A 48dp square outline icon button, used for the board card's Reject action. */
@Composable
fun SquareIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color? = null,
    contentDescription: String? = null,
) {
    val c = Hungry.colors
    androidx.compose.material3.OutlinedIconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = HungryRadius.control,
        border = BorderStroke(1.dp, c.outline),
        colors = androidx.compose.material3.IconButtonDefaults.outlinedIconButtonColors(contentColor = tint ?: c.danger),
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(18.dp))
    }
}
