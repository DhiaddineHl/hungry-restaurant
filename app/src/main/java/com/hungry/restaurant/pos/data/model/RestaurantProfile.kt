package com.hungry.restaurant.pos.data.model

/** The signed-in restaurant's own profile - resolved once from the account, not chosen by the app. */
data class RestaurantProfile(
    val id: String,
    val name: String,
    val brandName: String?,
    val logoUrl: String?,
    val acceptingOrders: Boolean,
)
