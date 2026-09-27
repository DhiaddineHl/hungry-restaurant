package com.hungry.restaurant.pos.data.repository

import android.util.Log
import com.hungry.restaurant.pos.data.model.StaffMember
import com.hungry.restaurant.pos.data.network.RestaurantPosApi
import com.hungry.restaurant.pos.data.network.dto.UpsertStaffRequestDto
import com.hungry.restaurant.pos.data.network.dto.VerifyPinRequestDto
import com.hungry.restaurant.pos.data.network.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface StaffRepository {
    val staff: StateFlow<List<StaffMember>>
    suspend fun refresh(): Result<Unit>
    suspend fun addStaff(name: String, role: String, pin: String): Result<StaffMember>

    /** Null on a wrong PIN (server answers 401) - not an exception, the picker just shakes and clears. */
    suspend fun verifyPin(staffId: String, pin: String): StaffMember?

    suspend fun removeStaff(staffId: String): Result<Unit>
}

class RealStaffRepository(private val api: RestaurantPosApi) : StaffRepository {

    private val _staff = MutableStateFlow<List<StaffMember>>(emptyList())
    override val staff: StateFlow<List<StaffMember>> = _staff.asStateFlow()

    override suspend fun refresh(): Result<Unit> = runCatching {
        _staff.value = api.listStaff().map { it.toDomain() }
    }.onFailure { Log.e(TAG, "Failed to refresh staff", it) }

    override suspend fun addStaff(name: String, role: String, pin: String): Result<StaffMember> = runCatching {
        val created = api.createStaff(UpsertStaffRequestDto(name = name, role = role, pin = pin)).toDomain()
        _staff.value = _staff.value + created
        created
    }

    override suspend fun verifyPin(staffId: String, pin: String): StaffMember? {
        return try {
            val response = api.verifyPin(staffId, VerifyPinRequestDto(pin))
            if (response.isSuccessful) response.body()?.toDomain() else null
        } catch (e: Exception) {
            Log.e(TAG, "PIN verification failed", e)
            null
        }
    }

    override suspend fun removeStaff(staffId: String): Result<Unit> = runCatching {
        api.deactivateStaff(staffId)
        _staff.value = _staff.value.filterNot { it.id == staffId }
    }

    private companion object {
        const val TAG = "StaffRepository"
    }
}
