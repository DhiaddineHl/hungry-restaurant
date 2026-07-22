package com.hungry.restaurant.pos.data.model

/** The authenticated restaurant partner / venue. */
data class Partner(
    val id: String,
    val restaurantName: String,
    val email: String,
    val addressLine: String,
)
