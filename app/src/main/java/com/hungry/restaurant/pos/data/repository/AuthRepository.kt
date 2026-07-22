package com.hungry.restaurant.pos.data.repository

import com.hungry.restaurant.pos.data.model.Partner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Partner authentication. Backed by a mock credential check for now; swap the
 * body of [login] for a real network/Firebase call without touching callers.
 */
interface AuthRepository {
    val currentPartner: StateFlow<Partner?>
    suspend fun login(email: String, password: String): Result<Partner>
    fun logout()
}

class MockAuthRepository : AuthRepository {

    private val _currentPartner = MutableStateFlow<Partner?>(null)
    override val currentPartner: StateFlow<Partner?> = _currentPartner.asStateFlow()

    override suspend fun login(email: String, password: String): Result<Partner> {
        delay(700) // simulate network latency
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required."))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("Incorrect email or password."))
        }
        val partner = Partner(
            id = "venue_001",
            restaurantName = "The Hungry Kitchen",
            email = email.trim(),
            addressLine = "128 Market St, Downtown",
        )
        _currentPartner.value = partner
        return Result.success(partner)
    }

    override fun logout() {
        _currentPartner.value = null
    }
}
