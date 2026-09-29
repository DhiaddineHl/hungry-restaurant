package com.hungry.restaurant.pos.auth

import com.hungry.restaurant.pos.BuildConfig

/** Static Keycloak/OIDC client configuration for the "hungry" realm. */
object KeycloakConfig {
    // BuildConfig.KEYCLOAK_BASE_URL is set per-build in app/build.gradle.kts (defaults to
    // the staging deployment - override with -PkeycloakBaseUrl=... for a local docker-compose
    // Keycloak instead). trimEnd so a base URL with (staging, e.g.) or without (local dev) a
    // trailing slash both produce a single "/realms/hungry", not a double slash the server
    // 404s on - this is what broke sign-in the first time this pointed at a deployed URL.
    val ISSUER: String = "${BuildConfig.KEYCLOAK_BASE_URL.trimEnd('/')}/realms/hungry"
    const val CLIENT_ID = "hungry-om-app"
    const val REDIRECT_URI = "com.hungry.restaurant.pos:/oauth2redirect"
    const val SCOPE = "openid profile email"

    /** Keycloak's hosted, self-service Account Console — used to review/update profile info. */
    val ACCOUNT_CONSOLE_URI: String = "$ISSUER/account"

    /** Realm role required to use the POS app. */
    const val REQUIRED_ROLE = "RESTAURANT"
}
