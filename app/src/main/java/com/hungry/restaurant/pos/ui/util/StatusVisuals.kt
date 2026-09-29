package com.hungry.restaurant.pos.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hungry.restaurant.pos.data.model.OrderStatus
import com.hungry.restaurant.pos.ui.theme.Hungry

/** Sentence-case label for an [OrderStatus], per the design system's copy rules. */
fun OrderStatus.label(): String = when (this) {
    OrderStatus.CREATED -> "New"
    OrderStatus.CONFIRMED -> "Preparing"
    OrderStatus.PREPARING -> "Preparing"
    OrderStatus.READY -> "Ready"
    OrderStatus.FINISHED -> "Completed"
    OrderStatus.REJECTED -> "Rejected"
    OrderStatus.CANCELLED -> "Cancelled"
}

/** Text for the primary action that advances this status, or null if terminal. */
fun OrderStatus.advanceLabel(): String? = when (this) {
    OrderStatus.CREATED -> "Accept"
    OrderStatus.CONFIRMED, OrderStatus.PREPARING -> "Mark as ready"
    else -> null
}

/** An amount is shown struck through for these terminal, unfulfilled statuses. */
val OrderStatus.isVoided: Boolean get() = this == OrderStatus.REJECTED || this == OrderStatus.CANCELLED

/** Tone pair (fg, bg) for an [OrderStatus] badge - `tokens.json`'s `orderStatus` map. */
@Composable
fun OrderStatus.statusTone(): Pair<Color, Color> {
    val c = Hungry.colors
    return when (this) {
        OrderStatus.CREATED -> c.onPrimary to c.primary
        OrderStatus.CONFIRMED, OrderStatus.PREPARING -> c.info to c.infoSoft
        OrderStatus.READY -> c.success to c.successSoft
        OrderStatus.FINISHED -> c.success to c.successSoft
        OrderStatus.CANCELLED -> c.danger to c.dangerSoft
        OrderStatus.REJECTED -> c.inkMuted to c.surfaceSunken
    }
}
