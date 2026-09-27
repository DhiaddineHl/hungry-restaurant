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
import com.hungry.restaurant.pos.auth.AuthUser
import com.hungry.restaurant.pos.auth.KeycloakConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object SignedOut : LoginUiState
    data object Authenticating : LoginUiState
    data object Authenticated : LoginUiState
    data class AccessDenied(val email: String?) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(private val authManager: AuthManager) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** A persisted session from a previous launch may already satisfy the role requirement. */
    private fun initialState(): LoginUiState {
        val user = authManager.authUser.value
        return if (user != null && user.hasRole(KeycloakConfig.REQUIRED_ROLE)) {
            LoginUiState.Authenticated
        } else {
            LoginUiState.SignedOut
        }
    }

    suspend fun buildAuthorizationIntent(): Intent = authManager.buildAuthorizationIntent()

    fun onAuthorizationResult(result: ActivityResult) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Authenticating
            try {
                val data = result.data
                if (result.resultCode != Activity.RESULT_OK || data == null) {
                    throw AuthException.UserCancelled()
                }
                val user = authManager.handleAuthorizationResponse(data)
                _uiState.value = evaluateRole(user)
            } catch (e: AuthException) {
                Log.e(TAG, "Sign-in failed", e)
                _uiState.value = LoginUiState.Error(e.message ?: "Sign-in failed")
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected sign-in failure", e)
                _uiState.value = LoginUiState.Error("Sign-in failed. Please try again.")
            }
        }
    }

    fun retry() {
        _uiState.value = LoginUiState.SignedOut
    }

    private fun evaluateRole(user: AuthUser): LoginUiState {
        if (!user.hasRole(KeycloakConfig.REQUIRED_ROLE)) {
            Log.w(TAG, "${user.email ?: user.subject} is missing required role ${KeycloakConfig.REQUIRED_ROLE}")
            authManager.clearSession()
            return LoginUiState.AccessDenied(user.email)
        }
        return LoginUiState.Authenticated
    }

    companion object {
        private const val TAG = "LoginViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                LoginViewModel(app.container.authManager)
            }
        }
    }
}
