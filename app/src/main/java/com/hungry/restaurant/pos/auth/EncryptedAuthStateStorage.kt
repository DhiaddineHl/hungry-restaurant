package com.hungry.restaurant.pos.auth

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.openid.appauth.AuthState
import org.json.JSONException

/** Persists AppAuth's [AuthState] (tokens + OIDC session) in an encrypted preferences file. */
class EncryptedAuthStateStorage(context: Context) {

    private val appContext = context.applicationContext

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun read(): AuthState {
        val json = prefs.getString(KEY_AUTH_STATE, null) ?: return AuthState()
        return try {
            AuthState.jsonDeserialize(json)
        } catch (e: JSONException) {
            Log.w(TAG, "Discarding corrupt persisted AuthState", e)
            AuthState()
        }
    }

    fun write(state: AuthState) {
        prefs.edit { putString(KEY_AUTH_STATE, state.jsonSerializeString()) }
    }

    fun clear() {
        prefs.edit { remove(KEY_AUTH_STATE) }
    }

    private companion object {
        const val TAG = "EncryptedAuthState"
        const val PREFS_FILE_NAME = "auth_state_prefs"
        const val KEY_AUTH_STATE = "auth_state"
    }
}
