package com.alpenraum.shimstack.ui.location

import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.ui.location.model.AppPermissions
import com.alpenraum.shimstack.ui.location.model.PermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class LocationPermissionManager(
    private val foregroundLocationRequesterDelegate: ForegroundLocationRequesterDelegate,
    private val backgroundLocationDelegate: BackgroundLocationRequesterDelegate,
    private val locationServiceDelegate: LocationServiceRequesterDelegate,
    private val notificationRequesterDelegate: NotificationRequesterDelegate,
    private val logger: ShimstackLogger
) {
    suspend fun checkPermission(permission: AppPermissions): PermissionState {
        return try {
            return getPermissionDelegate(permission).getPermissionState()
        } catch (e: Exception) {
            println("Failed to check permission $permission")
            e.printStackTrace()
            PermissionState.NOT_DETERMINED
        }
    }

    fun checkPermissionFlow(permission: AppPermissions): Flow<PermissionState> =
        flow {
            while (true) {
                val permissionState = checkPermission(permission)
                emit(permissionState)
                delay(PERMISSION_CHECK_FLOW_FREQUENCY)
            }
        }.distinctUntilChanged()

    suspend fun requestPermission(permission: AppPermissions) {
        try {
            getPermissionDelegate(permission).providePermission()
        } catch (e: Exception) {
            println("Failed to request permission $permission")
            e.printStackTrace()
        }
    }

    fun openSettingPage(permission: AppPermissions) {
        try {
            getPermissionDelegate(permission).openSettingPage()
        } catch (e: Exception) {
            println("Failed to open settings for permission $permission")
            e.printStackTrace()
        }
    }

    private fun getPermissionDelegate(permission: AppPermissions): PermissionRequesterDelegate =
        when (permission) {
            AppPermissions.LOCATION_SERVICE_ON -> locationServiceDelegate
            AppPermissions.LOCATION_FOREGROUND -> foregroundLocationRequesterDelegate
            AppPermissions.LOCATION_BACKGROUND -> backgroundLocationDelegate
            AppPermissions.SHOW_NOTIFICATIONS -> notificationRequesterDelegate
        }

    companion object {
        const val PERMISSION_CHECK_FLOW_FREQUENCY = 1000L
    }
}