package com.hungry.restaurant.pos.auth

import android.util.Base64
import org.json.JSONObject

/**
 * Minimal, unverified decoding of a JWT's payload segment. Signature verification is
 * Keycloak's/AppAuth's job during the token exchange over HTTPS; this is only used to
 * read display claims and `realm_access.roles` for client-side role gating.
 */
object JwtClaims {

    fun decodePayload(jwt: String): JSONObject {
        val segments = jwt.split(".")
        require(segments.size >= 2) { "Malformed JWT: expected header.payload.signature" }
        return JSONObject(String(decodeBase64Url(segments[1]), Charsets.UTF_8))
    }

    fun realmRoles(claims: JSONObject): Set<String> {
        val realmAccess = claims.optJSONObject("realm_access") ?: return emptySet()
        val roles = realmAccess.optJSONArray("roles") ?: return emptySet()
        return (0 until roles.length()).mapTo(mutableSetOf()) { roles.getString(it) }
    }

    private fun decodeBase64Url(segment: String): ByteArray {
        var normalized = segment.replace('-', '+').replace('_', '/')
        val padding = normalized.length % 4
        if (padding != 0) normalized += "=".repeat(4 - padding)
        return Base64.decode(normalized, Base64.DEFAULT)
    }
}
