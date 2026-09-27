package com.hungry.restaurant.pos.auth

import com.hungry.restaurant.pos.BuildConfig

/** Static Keycloak/OIDC client configuration for the "hungry" realm. */
object KeycloakConfig {
    // BuildConfig.KEYCLOAK_BASE_URL is set per-build in app/build.gradle.kts (defaults to
    // the local docker-compose Keycloak on :8081 - override with -PkeycloakBaseUrl=... for
    // a deployed instance). Was hardcoded to a Railway URL that's since gone dark (404
    // "Application not found" - the deployment was torn down, not a bug in this app).
    val ISSUER: String = "${BuildConfig.KEYCLOAK_BASE_URL}/realms/hungry"
    const val CLIENT_ID = "hungry-om-app"
    const val REDIRECT_URI = "com.hungry.restaurant.pos:/oauth2redirect"
    const val SCOPE = "openid profile email"

    /** Keycloak's hosted, self-service Account Console — used to review/update profile info. */
    val ACCOUNT_CONSOLE_URI: String = "$ISSUER/account"

    /** Realm role required to use the POS app. */
    const val REQUIRED_ROLE = "RESTAURANT"
}
