package com.hungry.restaurant.pos.data.model

/** A dish on the restaurant's own menu, as the Menu availability screen shows it. */
data class MenuItem(
    val id: String,
    val name: String,
    val category: String?,
    val price: Double?,
    val currency: String?,
    val available: Boolean,
    val unavailableReason: String?,
    val prepTimeMinutes: Int?,
)
