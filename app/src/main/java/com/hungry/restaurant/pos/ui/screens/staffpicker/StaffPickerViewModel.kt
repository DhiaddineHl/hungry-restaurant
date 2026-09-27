package com.hungry.restaurant.pos.ui.screens.staffpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.data.model.RestaurantProfile
import com.hungry.restaurant.pos.data.model.StaffMember
import com.hungry.restaurant.pos.data.repository.CurrentShiftRepository
import com.hungry.restaurant.pos.data.repository.RestaurantSessionRepository
import com.hungry.restaurant.pos.data.repository.StaffRepository
import com.hungry.restaurant.pos.di.AppContainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val PIN_LENGTH = 4

data class StaffPickerUiState(
    val restaurant: RestaurantProfile? = null,
    val staff: List<StaffMember> = emptyList(),
    val selectedStaffId: String? = null,
    val pin: String = "",
    val loading: Boolean = true,
    val pinError: Boolean = false,
)

class StaffPickerViewModel(
    private val appContainer: AppContainer,
    private val sessionRepository: RestaurantSessionRepository,
    private val staffRepository: StaffRepository,
    private val currentShift: CurrentShiftRepository,
) : ViewModel() {

    private val _selectedStaffId = MutableStateFlow<String?>(null)
    private val _pin = MutableStateFlow("")
    private val _loading = MutableStateFlow(true)
    private val _pinError = MutableStateFlow(false)

    val uiState: StateFlow<StaffPickerUiState> = combine(
        sessionRepository.restaurant,
        staffRepository.staff,
        _selectedStaffId,
        _pin,
    ) { restaurant, staff, selected, pin ->
        StaffPickerUiState(
            restaurant = restaurant,
            staff = staff,
            selectedStaffId = selected ?: staff.firstOrNull()?.id,
            pin = pin,
            loading = _loading.value,
            pinError = _pinError.value,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StaffPickerUiState())

    /** Emits once a PIN check succeeds - the screen navigates to the orders board. */
    val signedIn = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * Emits when the cached Keycloak session turns out to be unusable against
     * this backend - e.g. a session cached from a previous Keycloak instance
     * (a different issuer/signing keys) that [com.hungry.restaurant.pos.auth.AuthManager]
     * still considers "authenticated" locally, so this screen was reached at
     * all, but every API call with it fails. Previously this just left the
     * picker permanently empty with no explanation; now it clears the stale
     * session and the screen bounces back to a real sign-in.
     */
    val sessionInvalid = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** One-shot user-facing messages (snackbar) - e.g. why "Add" silently did nothing before this existed. */
    val messages = MutableSharedFlow<String>(extraBufferCapacity = 4)

    init {
        viewModelScope.launch {
            val resolved = sessionRepository.refresh()
            if (resolved.isFailure) {
                appContainer.authManager.clearSession()
                sessionInvalid.tryEmit(Unit)
                return@launch
            }
            appContainer.startOrderPolling()
            staffRepository.refresh()
            appContainer.posSettingsRepository.refresh()
            _loading.value = false
        }
    }

    fun selectStaff(staffId: String) {
        _selectedStaffId.value = staffId
        clearPin()
    }

    fun onDigit(digit: Char) {
        if (_pin.value.length >= PIN_LENGTH) return
        _pinError.value = false
        _pin.value += digit
        if (_pin.value.length == PIN_LENGTH) checkPin()
    }

    fun onBackspace() {
        if (_pin.value.isEmpty()) return
        _pinError.value = false
        _pin.value = _pin.value.dropLast(1)
    }

    fun clearPin() {
        _pin.value = ""
        _pinError.value = false
    }

    private fun checkPin() {
        val staffId = uiState.value.selectedStaffId ?: return
        val pin = _pin.value
        viewModelScope.launch {
            val staff = staffRepository.verifyPin(staffId, pin)
            if (staff != null) {
                currentShift.signIn(staff)
                signedIn.tryEmit(Unit)
            } else {
                _pinError.value = true
                _pin.value = ""
            }
        }
    }

    /**
     * First-run setup: no staff exist yet, so the signed-in manager adds
     * themselves. A failure used to leave the dialog just sitting there with
     * no explanation - now it's reported so "nothing happened" is never
     * silent.
     */
    fun addStaffMember(name: String, pin: String, onDone: (StaffMember?) -> Unit) {
        viewModelScope.launch {
            staffRepository.addStaff(name, "MANAGER", pin)
                .onSuccess { onDone(it) }
                .onFailure {
                    messages.tryEmit(it.message ?: "Couldn't add that staff member - please try again.")
                    onDone(null)
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                StaffPickerViewModel(
                    app.container,
                    app.container.restaurantSession,
                    app.container.staffRepository,
                    app.container.currentShift,
                )
            }
        }
    }
}
