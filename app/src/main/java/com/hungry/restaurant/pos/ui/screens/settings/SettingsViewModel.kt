package com.hungry.restaurant.pos.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.auth.AuthManager
import com.hungry.restaurant.pos.auth.AuthUser
import com.hungry.restaurant.pos.data.model.PosSettings
import com.hungry.restaurant.pos.data.repository.PosSettingsRepository
import com.hungry.restaurant.pos.data.repository.ThemeModeRepository
import com.hungry.restaurant.pos.printer.SunmiPrinter
import com.hungry.restaurant.pos.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: PosSettingsRepository,
    private val themeModeRepository: ThemeModeRepository,
    authManager: AuthManager,
    val printer: SunmiPrinter,
) : ViewModel() {

    val settings: StateFlow<PosSettings?> = settingsRepository.settings
    val authUser: StateFlow<AuthUser?> = authManager.authUser
    val printerStatus: StateFlow<SunmiPrinter.Status> = printer.status
    val themeMode: StateFlow<ThemeMode> = themeModeRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.AUTO)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages

    init {
        viewModelScope.launch { settingsRepository.refresh() }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themeModeRepository.setThemeMode(mode) }
    }

    fun setAutoPrintOnAccept(value: Boolean) = update { settingsRepository.update(autoPrintOnAccept = value) }
    fun setTicketCopies(value: Int) = update { settingsRepository.update(ticketCopies = value) }
    fun setRingUntilAccepted(value: Boolean) = update { settingsRepository.update(ringUntilAccepted = value) }
    fun setDefaultPrepTime(value: Int) = update { settingsRepository.update(defaultPrepTimeMinutes = value) }
    fun setAutoAcceptOrders(value: Boolean) = update { settingsRepository.update(autoAcceptOrders = value) }

    fun reconnectPrinter() = printer.connect()

    fun printTestTicket() {
        viewModelScope.launch {
            printer.printTest()
                .onSuccess { _messages.tryEmit("Test ticket sent") }
                .onFailure { _messages.tryEmit(it.message ?: "Couldn't print a test ticket") }
        }
    }

    private fun update(action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            action().onFailure { _messages.tryEmit(it.message ?: "Couldn't save that setting") }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                SettingsViewModel(
                    app.container.posSettingsRepository,
                    app.container.themeModeRepository,
                    app.container.authManager,
                    app.container.sunmiPrinter,
                )
            }
        }
    }
}
