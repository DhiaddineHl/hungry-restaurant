package com.hungry.restaurant.pos.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.hungry.restaurant.pos.BuildConfig
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.EndSessionRequest
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenResponse

/**
 * Owns the Keycloak OIDC session for the app: authorization-code + PKCE login via
 * Chrome Custom Tabs (AppAuth's default browser flow — never a WebView), refresh-token
 * renewal, role extraction from the ID token, and RP-initiated logout.
 *
 * All of AppAuth's callback APIs are wrapped with [suspendCancellableCoroutine] so
 * callers use plain suspend functions instead of nested callbacks.
 */
class AuthManager(
    context: Context,
    private val storage: EncryptedAuthStateStorage,
) {
    private val appContext = context.applicationContext

    // DevConnectionBuilder, not AppAuth's default: the default hard-refuses any non-https
    // issuer, which crashes (uncaught, off the coroutine call stack) against a plain-http
    // local/LAN Keycloak. See its kdoc. setSkipIssuerHttpsCheck is the second half of the
    // same relaxation - without it, AppAuth still rejects the ID token's "iss" claim
    // (net.openid.appauth.IdToken) for being http, after a successful token exchange.
    private val authService = AuthorizationService(
        appContext,
        AppAuthConfiguration.Builder()
            .setConnectionBuilder(DevConnectionBuilder)
            .setSkipIssuerHttpsCheck(BuildConfig.DEBUG)
            .build(),
    )

    private val discoveryMutex = Mutex()
    private var cachedServiceConfig: AuthorizationServiceConfiguration? = null

    private val _authUser = MutableStateFlow(deriveUser(storage.read()))

    /** Currently signed-in user, or null when signed out. Role membership is not implied. */
    val authUser: StateFlow<AuthUser?> = _authUser.asStateFlow()

    /** Builds the Custom Tabs intent for an authorization-code + PKCE (S256) request. */
    suspend fun buildAuthorizationIntent(): Intent {
        val serviceConfig = getServiceConfiguration()
        // AuthorizationRequest.Builder auto-generates an S256 PKCE code verifier/challenge
        // unless setCodeVerifier(null) is called, so this already performs PKCE.
        val request = AuthorizationRequest.Builder(
            serviceConfig,
            KeycloakConfig.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(KeycloakConfig.REDIRECT_URI),
        )
            .setScope(KeycloakConfig.SCOPE)
            .build()
        return authService.getAuthorizationRequestIntent(request)
    }

    /**
     * Completes the authorization-code flow from the Custom Tabs redirect: validates the
     * response, exchanges the code for tokens, persists the resulting [AuthState], and
     * derives the signed-in user from the ID token.
     */
    suspend fun handleAuthorizationResponse(intent: Intent): AuthUser {
        val response = AuthorizationResponse.fromIntent(intent)
        val authError = AuthorizationException.fromIntent(intent)

        val state = storage.read()
        state.update(response, authError)
        storage.write(state)

        if (response == null) throw mapAuthorizationException(authError)

        val tokenResponse = exchangeCode(response)
        state.update(tokenResponse, null)
        storage.write(state)

        val user = deriveUser(state) ?: throw AuthException.Unknown()
        _authUser.value = user
        return user
    }

    /** True if the signed-in user currently holds [role]. */
    fun hasRole(role: String): Boolean = _authUser.value?.hasRole(role) == true

    /**
     * Returns a valid access token, transparently refreshing it via AppAuth's
     * `performActionWithFreshTokens` when the current one is expired. Intended for an
     * OkHttp/Retrofit auth interceptor once the app has a network layer that needs it.
     */
    suspend fun getFreshAccessToken(): String {
        val state = storage.read()
        if (!state.isAuthorized) throw AuthException.NotAuthenticated()

        return suspendCancellableCoroutine { cont ->
            state.performActionWithFreshTokens(authService) { accessToken, _, ex ->
                storage.write(state)
                when {
                    accessToken != null -> cont.resume(accessToken)
                    else -> cont.resumeWithException(mapAuthorizationException(ex))
                }
            }
        }
    }

    /** Builds the Custom Tabs intent for Keycloak's RP-initiated (end-session) logout. */
    suspend fun buildEndSessionIntent(): Intent {
        val serviceConfig = getServiceConfiguration()
        val state = storage.read()
        val request = EndSessionRequest.Builder(serviceConfig)
            .setIdTokenHint(state.idToken)
            .setPostLogoutRedirectUri(Uri.parse(KeycloakConfig.REDIRECT_URI))
            .build()
        return authService.getEndSessionRequestIntent(request)
    }

    /**
     * Opens Keycloak's hosted Account Console in a Custom Tab so the user can review/update
     * their profile (name, email, password) using Keycloak's own self-service UI — the app
     * doesn't reimplement profile editing. No redirect back to the app is expected, so unlike
     * the auth/end-session intents this is launched directly, not through the result launcher.
     */
    fun buildAccountConsoleIntent(): Intent =
        CustomTabsIntent.Builder().build().intent.apply {
            data = Uri.parse(KeycloakConfig.ACCOUNT_CONSOLE_URI)
        }

    /** Clears the local session. Safe to call regardless of how the end-session redirect resolved. */
    fun clearSession() {
        storage.clear()
        _authUser.value = null
    }

    private suspend fun getServiceConfiguration(): AuthorizationServiceConfiguration {
        cachedServiceConfig?.let { return it }
        return discoveryMutex.withLock {
            cachedServiceConfig ?: fetchDiscovery().also { cachedServiceConfig = it }
        }
    }

    private suspend fun fetchDiscovery(): AuthorizationServiceConfiguration =
        suspendCancellableCoroutine { cont ->
            AuthorizationServiceConfiguration.fetchFromIssuer(
                Uri.parse(KeycloakConfig.ISSUER),
                { config, ex ->
                    when {
                        config != null -> cont.resume(config)
                        else -> cont.resumeWithException(AuthException.DiscoveryFailed(ex))
                    }
                },
                DevConnectionBuilder,
            )
        }

    private suspend fun exchangeCode(response: AuthorizationResponse): TokenResponse =
        suspendCancellableCoroutine { cont ->
            authService.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse, ex ->
                when {
                    tokenResponse != null -> cont.resume(tokenResponse)
                    else -> cont.resumeWithException(mapAuthorizationException(ex))
                }
            }
        }

    private fun mapAuthorizationException(ex: AuthorizationException?): AuthException = when {
        ex == null -> AuthException.Unknown()

        ex.type == AuthorizationException.TYPE_GENERAL_ERROR &&
            ex.code == AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW.code ->
            AuthException.UserCancelled()

        ex.type == AuthorizationException.TYPE_GENERAL_ERROR &&
            (ex.code == AuthorizationException.GeneralErrors.NETWORK_ERROR.code ||
                ex.code == AuthorizationException.GeneralErrors.SERVER_ERROR.code) ->
            AuthException.NetworkError(ex)

        ex.type == AuthorizationException.TYPE_OAUTH_TOKEN_ERROR &&
            ex.code == AuthorizationException.TokenRequestErrors.INVALID_GRANT.code ->
            AuthException.InvalidGrant(ex)

        else -> {
            Log.e(TAG, "Unmapped AppAuth error (type=${ex.type}, code=${ex.code})", ex)
            AuthException.Unknown(ex)
        }
    }

    private fun deriveUser(state: AuthState): AuthUser? {
        if (!state.isAuthorized) return null
        val idToken = state.idToken ?: return null
        val claims = try {
            JwtClaims.decodePayload(idToken)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode ID token claims", e)
            return null
        }
        // Keycloak's default "roles" client scope mapper adds realm_access.roles to the
        // access token but, depending on realm config, not always to the ID token — read
        // whichever one actually carries it.
        val roles = JwtClaims.realmRoles(claims).ifEmpty {
            state.accessToken?.let { token ->
                try {
                    JwtClaims.realmRoles(JwtClaims.decodePayload(token))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decode access token claims", e)
                    emptySet()
                }
            } ?: emptySet()
        }
        return AuthUser(
            subject = claims.optString("sub"),
            email = claims.optString("email", null),
            displayName = claims.optString("name", claims.optString("preferred_username", null)),
            roles = roles,
        )
    }

    private companion object {
        const val TAG = "AuthManager"
    }
}
