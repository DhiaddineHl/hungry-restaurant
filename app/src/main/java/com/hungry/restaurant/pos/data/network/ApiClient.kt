package com.hungry.restaurant.pos.data.network

import com.hungry.restaurant.pos.BuildConfig
import com.hungry.restaurant.pos.auth.AuthManager
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the single [RestaurantPosApi] instance the app talks to. [BuildConfig.API_BASE_URL]
 * is set per-build from `app/build.gradle.kts` (see its own comment for how to point this
 * at a different backend without touching source).
 */
object ApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun create(authManager: AuthManager): RestaurantPosApi {
        val logging = HttpLoggingInterceptor().apply {
            // BODY is fine for a POS terminal talking to a backend the operator controls -
            // no third-party payment/card data ever flows through this client.
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authManager))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(RestaurantPosApi::class.java)
    }
}
