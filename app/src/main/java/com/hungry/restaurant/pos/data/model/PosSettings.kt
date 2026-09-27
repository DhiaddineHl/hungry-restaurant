package com.hungry.restaurant.pos.data.model

/** The restaurant's own POS terminal preferences - see the Settings screen. */
data class PosSettings(
    val autoPrintOnAccept: Boolean,
    val ticketCopies: Int,
    val ringUntilAccepted: Boolean,
    val defaultPrepTimeMinutes: Int,
    val autoAcceptOrders: Boolean,
)
