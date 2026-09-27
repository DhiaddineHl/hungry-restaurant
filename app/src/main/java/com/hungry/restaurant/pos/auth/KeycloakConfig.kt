package com.hungry.restaurant.pos.auth

/** Static Keycloak/OIDC client configuration for the "hungry" realm. */
object KeycloakConfig {
    const val ISSUER = "https://hungry-keycloak-production.up.railway.app/realms/hungry"
    const val CLIENT_ID = "hungry-om-app"
    const val REDIRECT_URI = "com.hungry.restaurant.pos:/oauth2redirect"
    const val SCOPE = "openid profile email"

    /** Keycloak's hosted, self-service Account Console — used to review/update profile info. */
    val ACCOUNT_CONSOLE_URI: String = "$ISSUER/account"

    /** Realm role required to use the POS app. */
    const val REQUIRED_ROLE = "RESTAURANT"
}
