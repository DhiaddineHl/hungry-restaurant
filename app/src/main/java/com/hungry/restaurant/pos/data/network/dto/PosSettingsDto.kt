package com.hungry.restaurant.pos.data.network.dto

import kotlinx.serialization.Serializable

/** Mirrors `RestaurantPosSettingsController.SettingsResponse`. */
@Serializable
data class PosSettingsDto(
    val autoPrintOnAccept: Boolean = true,
    val ticketCopies: Int = 1,
    val ringUntilAccepted: Boolean = true,
    val defaultPrepTimeMinutes: Int = 20,
    val autoAcceptOrders: Boolean = false,
)

/** Body for `PUT /restaurants/me/pos-settings` — only non-null fields are applied. */
@Serializable
data class UpdatePosSettingsRequestDto(
    val autoPrintOnAccept: Boolean? = null,
    val ticketCopies: Int? = null,
    val ringUntilAccepted: Boolean? = null,
    val defaultPrepTimeMinutes: Int? = null,
    val autoAcceptOrders: Boolean? = null,
)
