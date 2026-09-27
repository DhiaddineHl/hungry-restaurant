package com.hungry.restaurant.pos.auth

/** Typed failures surfaced by [AuthManager], so the UI can react appropriately. */
sealed class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class DiscoveryFailed(cause: Throwable? = null) :
        AuthException("Could not load the identity provider configuration.", cause)

    class NetworkError(cause: Throwable? = null) :
        AuthException("Network error while contacting the identity provider.", cause)

    class UserCancelled :
        AuthException("Sign-in was cancelled.")

    class InvalidGrant(cause: Throwable? = null) :
        AuthException("The authorization code or refresh token was rejected by the identity provider.", cause)

    class MissingRole(val role: String) :
        AuthException("Signed-in user is missing the required role: $role")

    class NotAuthenticated :
        AuthException("No signed-in session.")

    class Unknown(cause: Throwable? = null) :
        AuthException(cause?.message ?: "Authentication failed.", cause)
}
