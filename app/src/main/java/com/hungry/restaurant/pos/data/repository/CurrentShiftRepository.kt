package com.hungry.restaurant.pos.data.repository

import com.hungry.restaurant.pos.data.model.StaffMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Which staff member is currently using the terminal - separate from the
 * restaurant's own Keycloak session ([com.hungry.restaurant.pos.auth.AuthManager]).
 * In-memory only: a cold app restart re-shows the PIN picker rather than
 * silently resuming as whoever tapped in last, matching how a shared POS
 * terminal is expected to behave.
 */
class CurrentShiftRepository {

    private val _currentStaff = MutableStateFlow<StaffMember?>(null)
    val currentStaff: StateFlow<StaffMember?> = _currentStaff.asStateFlow()

    fun signIn(staff: StaffMember) {
        _currentStaff.value = staff
    }

    /** "Switch staff" - clears who's on shift without touching the restaurant's own Keycloak session. */
    fun signOut() {
        _currentStaff.value = null
    }
}
