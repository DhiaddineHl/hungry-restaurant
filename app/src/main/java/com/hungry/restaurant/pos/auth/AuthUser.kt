package com.hungry.restaurant.pos.auth

/** Identity of the signed-in Keycloak user, derived from the ID token claims. */
data class AuthUser(
    val subject: String,
    val email: String?,
    val displayName: String?,
    val roles: Set<String>,
) {
    fun hasRole(role: String): Boolean = roles.contains(role)
}
