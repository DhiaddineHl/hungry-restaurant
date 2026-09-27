package com.hungry.restaurant.pos.data.network.dto

import kotlinx.serialization.Serializable

/** Mirrors `RestaurantOutputData` — only the fields this app reads. */
@Serializable
data class RestaurantDto(
    val id: String,
    val code: String? = null,
    val name: String? = null,
    val brandName: String? = null,
    val logoUrl: String? = null,
    val enabled: Boolean = true,
    val acceptingOrders: Boolean = true,
)
