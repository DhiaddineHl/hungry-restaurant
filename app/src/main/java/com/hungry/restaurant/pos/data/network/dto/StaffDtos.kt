package com.hungry.restaurant.pos.data.network.dto

import kotlinx.serialization.Serializable

/** Mirrors `RestaurantStaffController.StaffResponse` — never carries the PIN. */
@Serializable
data class StaffDto(
    val id: String,
    val name: String,
    val initials: String,
    val role: String,
    val lastSignedInAt: String? = null,
)

/** Body for create/update staff. */
@Serializable
data class UpsertStaffRequestDto(
    val name: String? = null,
    val initials: String? = null,
    val role: String? = null,
    val pin: String? = null,
)

/** Body for `POST /restaurants/me/staff/{id}/verify-pin`. */
@Serializable
data class VerifyPinRequestDto(val pin: String)
