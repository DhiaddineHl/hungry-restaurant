package com.hungry.restaurant.pos.data.network

import com.hungry.restaurant.pos.auth.AuthManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches the restaurant's bearer token to every request, refreshing it first
 * if needed via [AuthManager.getFreshAccessToken].
 *
 * OkHttp interceptors are synchronous and always run off the main thread (on
 * OkHttp's own dispatcher), so bridging the suspend token call with
 * [runBlocking] here is safe - it blocks that background thread, not the UI.
 * A request made before any sign-in (no [AuthManager.AuthException.NotAuthenticated]
 * token) is sent unauthenticated rather than crashing the call site; the
 * backend then answers 401, which callers already have to handle anyway.
 */
class AuthInterceptor(private val authManager: AuthManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runCatching { runBlocking { authManager.getFreshAccessToken() } }.getOrNull()
        val request = if (token != null) {
            original.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
