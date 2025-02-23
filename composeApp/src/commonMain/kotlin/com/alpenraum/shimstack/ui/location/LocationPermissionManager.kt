package com.alpenraum.shimstack.ui.location

import com.alpenraum.shimstack.base.di.BackgroundLocationDelegateName
import com.alpenraum.shimstack.base.di.ForegroundLocationDelegateName
import com.alpenraum.shimstack.base.di.LocationServiceDelegateName
import com.alpenraum.shimstack.ui.location.model.LocationPermission
import com.alpenraum.shimstack.ui.location.model.PermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class LocationPermissionManager(
    @Named(type = ForegroundLocationDelegateName::class) private val foregroundLocationRequesterDelegate:
        LocationRequesterDelegate,
    @Named(type = BackgroundLocationDelegateName::class) private val backgroundLocationDelegate: LocationRequesterDelegate,
    @Named(type = LocationServiceDelegateName::class) private val locationServiceDelegate: LocationRequesterDelegate
) {
    fun checkPermission(permission: LocationPermission): PermissionState {
        return try {
            return getPermissionDelegate(permission).getPermissionState()
        } catch (e: Exception) {
            println("Failed to check permission $permission")
            e.printStackTrace()
            PermissionState.NOT_DETERMINED
        }
    }

    fun checkPermissionFlow(permission: LocationPermission): Flow<PermissionState> =
        flow {
            while (true) {
                val permissionState = checkPermission(permission)
                emit(permissionState)
                delay(PERMISSION_CHECK_FLOW_FREQUENCY)
            }
        }

    suspend fun requestPermission(permission: LocationPermission) {
        try {
            getPermissionDelegate(permission).providePermission()
        } catch (e: Exception) {
            println("Failed to request permission $permission")
            e.printStackTrace()
        }
    }

    fun openSettingPage(permission: LocationPermission) {
        println("Open settings for permission $permission")
        try {
            getPermissionDelegate(permission).openSettingPage()
        } catch (e: Exception) {
            println("Failed to open settings for permission $permission")
            e.printStackTrace()
        }
    }

    private fun getPermissionDelegate(permission: LocationPermission): LocationRequesterDelegate =
        when (permission) {
            LocationPermission.LOCATION_SERVICE_ON -> locationServiceDelegate
            LocationPermission.LOCATION_FOREGROUND -> foregroundLocationRequesterDelegate
            LocationPermission.LOCATION_BACKGROUND -> backgroundLocationDelegate
        }

    companion object {
        const val PERMISSION_CHECK_FLOW_FREQUENCY = 1000L
    }
}