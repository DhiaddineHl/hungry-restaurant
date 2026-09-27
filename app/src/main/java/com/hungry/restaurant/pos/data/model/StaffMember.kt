package com.hungry.restaurant.pos.data.model

enum class StaffRole { OWNER, MANAGER, STAFF }

/** One entry in the "who's on shift?" PIN picker. Never carries a PIN - see `RestaurantStaff`'s backend javadoc. */
data class StaffMember(
    val id: String,
    val name: String,
    val initials: String,
    val role: StaffRole,
)

fun String?.toStaffRole(): StaffRole = when (this?.uppercase()) {
    "OWNER" -> StaffRole.OWNER
    "MANAGER" -> StaffRole.MANAGER
    else -> StaffRole.STAFF
}
