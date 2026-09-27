package com.hungry.restaurant.pos.auth

import android.net.Uri
import com.hungry.restaurant.pos.BuildConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import net.openid.appauth.connectivity.ConnectionBuilder

/**
 * AppAuth's own [net.openid.appauth.connectivity.DefaultConnectionBuilder] hard-refuses any
 * non-`https://` issuer with `IllegalArgumentException("only https connections are permitted")`
 * — thrown on its internal discovery `AsyncTask`, outside any coroutine try/catch in this app,
 * so it surfaces as an unhandled crash rather than our normal error UI.
 *
 * A local docker-compose Keycloak (or a LAN one during development) is plain `http://` — same
 * reasoning as the debug-only `usesCleartextTraffic` manifest override. This builder is
 * otherwise identical to AppAuth's default (timeouts, no auto-redirect) but only enforces
 * https in release builds, where the configured Keycloak is expected to be a real deployment.
 */
object DevConnectionBuilder : ConnectionBuilder {

    private const val CONNECTION_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 10_000

    override fun openConnection(uri: Uri): HttpURLConnection {
        val scheme = uri.scheme
        val allowed = scheme == "https" || (BuildConfig.DEBUG && scheme == "http")
        if (!allowed) {
            throw IOException("Only https connections are permitted (got scheme=$scheme)")
        }
        val connection = URL(uri.toString()).openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECTION_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        connection.instanceFollowRedirects = false
        return connection
    }
}
