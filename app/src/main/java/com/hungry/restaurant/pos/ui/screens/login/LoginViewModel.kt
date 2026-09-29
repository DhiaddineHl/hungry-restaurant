package com.hungry.restaurant.pos.ui.screens.login

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.auth.AuthException
import com.hungry.restaurant.pos.auth.AuthManager
import com.hungry.restaurant.pos.auth.KeycloakConfig
import com.hungry.restaurant.pos.data.repository.RestaurantSessionRepository
import com.hungry.restaurant.pos.di.AppContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Screen 01b's 3-step reveal. */
enum class AuthStep { ACCOUNT_VERIFIED, CONNECTING, LOADING_ORDERS }

sealed interface LoginUiState {
    data object SignedOut : LoginUiState
    data class Authenticating(val step: AuthStep, val restaurantName: String? = null) : LoginUiState
    data object Authenticated : LoginUiState
    data class AccessDenied(val email: String?) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

/**
 * No staff/PIN picker any more - a successful Keycloak sign-in (real or a
 * still-valid cached session from a previous launch) resolves the caller's
 * restaurant ([RestaurantSessionRepository.refresh]) and, once that succeeds,
 * goes straight to [LoginUiState.Authenticated] - the main pages. A session
 * that authenticates locally but can't actually reach the backend (e.g. one
 * cached from a since-changed Keycloak instance) is treated as unusable and
 * cleared, the same way the removed staff-picker screen used to bounce back.
 *
 * [LoginUiState.Authenticating]'s 3 steps (screen 01b) map onto this actual
 * work rather than being decorative: ACCOUNT_VERIFIED is the Keycloak token
 * already in hand, CONNECTING is [RestaurantSessionRepository.refresh], and
 * LOADING_ORDERS is [AppContainer.startOrderPolling].
 */
class LoginViewModel(
    private val authManager: AuthManager,
    private val sessionRepository: RestaurantSessionRepository,
    private val appContainer: AppContainer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        if (_uiState.value is LoginUiState.Authenticating) {
            viewModelScope.launch { resolveSessionThenAuthenticate() }
        }
    }

    /** A persisted session from a previous launch may already satisfy the role requirement. */
    private fun initialState(): LoginUiState {
        val user = authManager.authUser.value
        return if (user != null && user.hasRole(KeycloakConfig.REQUIRED_ROLE)) {
            LoginUiState.Authenticating(AuthStep.ACCOUNT_VERIFIED, sessionRepository.restaurant.value?.name)
        } else {
            LoginUiState.SignedOut
        }
    }

    suspend fun buildAuthorizationIntent(): Intent = authManager.buildAuthorizationIntent()

    fun onAuthorizationResult(result: ActivityResult) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Authenticating(AuthStep.ACCOUNT_VERIFIED)
            try {
                val data = result.data
                if (result.resultCode != Activity.RESULT_OK || data == null) {
                    throw AuthException.UserCancelled()
                }
                val user = authManager.handleAuthorizationResponse(data)
                if (!user.hasRole(KeycloakConfig.REQUIRED_ROLE)) {
                    Log.w(TAG, "${user.email ?: user.subject} is missing required role ${KeycloakConfig.REQUIRED_ROLE}")
                    authManager.clearSession()
                    _uiState.value = LoginUiState.AccessDenied(user.email)
                    return@launch
                }
                resolveSessionThenAuthenticate()
            } catch (e: AuthException) {
                Log.e(TAG, "Sign-in failed", e)
                _uiState.value = LoginUiState.Error(e.message ?: "Sign-in failed")
            } catch (e: Throwable) {
                // Deliberately Throwable, not Exception: on some devices a missing/odd
                // Custom Tabs or WebView provider surfaces as a Error subtype
                // (e.g. NoClassDefFoundError), which `catch (e: Exception)` would let
                // through and crash the app instead of showing this screen's error state.
                Log.e(TAG, "Unexpected sign-in failure", e)
                _uiState.value = LoginUiState.Error(e.message ?: "Sign-in failed. Please try again.")
            }
        }
    }

    fun retry() {
        _uiState.value = LoginUiState.SignedOut
    }

    /**
     * The step that used to be the staff picker's job: prove the session
     * actually works against this backend before calling it "authenticated",
     * and start the order-board polling that used to kick off there too.
     */
    private suspend fun resolveSessionThenAuthenticate() {
        delay(STEP_REVEAL_MS) // let "Account verified" register before advancing
        _uiState.value = LoginUiState.Authenticating(AuthStep.CONNECTING, sessionRepository.restaurant.value?.name)
        val resolved = sessionRepository.refresh()
        if (resolved.isFailure) {
            Log.e(TAG, "Signed in with Keycloak but couldn't resolve the restaurant", resolved.exceptionOrNull())
            authManager.clearSession()
            _uiState.value = LoginUiState.Error("Couldn't reach Hungry. You're signed out. Check the terminal's connection and try again.")
            return
        }
        _uiState.value = LoginUiState.Authenticating(AuthStep.LOADING_ORDERS, sessionRepository.restaurant.value?.name)
        appContainer.startOrderPolling()
        delay(STEP_REVEAL_MS)
        _uiState.value = LoginUiState.Authenticated
    }

    companion object {
        private const val TAG = "LoginViewModel"
        private const val STEP_REVEAL_MS = 350L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                LoginViewModel(app.container.authManager, app.container.restaurantSession, app.container)
            }
        }
    }
}
