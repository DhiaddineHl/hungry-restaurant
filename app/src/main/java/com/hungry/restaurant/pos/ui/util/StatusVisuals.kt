package com.hungry.restaurant.pos.ui.util

import androidx.compose.ui.graphics.Color
import com.hungry.restaurant.pos.data.model.OrderStatus
import com.hungry.restaurant.pos.ui.theme.StatusCancelled
import com.hungry.restaurant.pos.ui.theme.StatusCompleted
import com.hungry.restaurant.pos.ui.theme.StatusNew
import com.hungry.restaurant.pos.ui.theme.StatusPreparing
import com.hungry.restaurant.pos.ui.theme.StatusReady

/** UI presentation for an [OrderStatus]: label, accent color, and CTA text. */
data class StatusVisual(
    val label: String,
    val color: Color,
    /** Text for the primary action that advances this status, or null if terminal. */
    val advanceLabel: String?,
)

fun OrderStatus.visual(): StatusVisual = when (this) {
    OrderStatus.NEW -> StatusVisual("New", StatusNew, "Accept")
    OrderStatus.PREPARING -> StatusVisual("Preparing", StatusPreparing, "Mark ready")
    OrderStatus.READY -> StatusVisual("Ready", StatusReady, "Complete")
    OrderStatus.COMPLETED -> StatusVisual("Completed", StatusCompleted, null)
    OrderStatus.CANCELLED -> StatusVisual("Cancelled", StatusCancelled, null)
}
