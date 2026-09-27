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
    OrderStatus.CREATED -> StatusVisual("New", StatusNew, "Accept")
    OrderStatus.CONFIRMED -> StatusVisual("Preparing", StatusPreparing, "Mark ready")
    OrderStatus.PREPARING -> StatusVisual("Preparing", StatusPreparing, "Mark ready")
    OrderStatus.READY -> StatusVisual("Ready", StatusReady, null)
    OrderStatus.FINISHED -> StatusVisual("Completed", StatusCompleted, null)
    OrderStatus.REJECTED -> StatusVisual("Rejected", StatusCancelled, null)
    OrderStatus.CANCELLED -> StatusVisual("Cancelled", StatusCancelled, null)
}
