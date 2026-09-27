package com.hungry.restaurant.pos.ui.screens.account

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.auth.AuthManager
import com.hungry.restaurant.pos.auth.AuthUser
import kotlinx.coroutines.flow.StateFlow

class AccountViewModel(private val authManager: AuthManager) : ViewModel() {

    val authUser: StateFlow<AuthUser?> = authManager.authUser

    /** Custom Tabs intent for Keycloak's hosted Account Console (view/update profile). */
    fun buildManageAccountIntent(): Intent = authManager.buildAccountConsoleIntent()

    /** Custom Tabs intent for Keycloak's RP-initiated logout (ends the IdP SSO session too). */
    suspend fun buildLogoutIntent(): Intent = authManager.buildEndSessionIntent()

    /** Drops the local session once the logout redirect returns, however it resolved. */
    fun onLogoutResult() = authManager.clearSession()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                AccountViewModel(app.container.authManager)
            }
        }
    }
}
