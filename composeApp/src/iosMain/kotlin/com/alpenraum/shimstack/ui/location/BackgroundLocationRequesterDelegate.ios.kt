package com.alpenraum.shimstack.ui.location

import com.alpenraum.shimstack.base.openAppSettingsPage
import com.alpenraum.shimstack.ui.location.model.PermissionState
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusDenied

actual class BackgroundLocationRequesterDelegate(
    private val locationForegroundPermissionDelegate: PermissionRequesterDelegate
) : PermissionRequesterDelegate {
    override suspend fun getPermissionState(): PermissionState {
        val foregroundPermissionStatus =
            locationForegroundPermissionDelegate.getPermissionState()
        return when (foregroundPermissionStatus) {
            PermissionState.GRANTED -> checkBackgroundLocationPermission()

            PermissionState.DENIED,
            PermissionState.NOT_DETERMINED
            -> foregroundPermissionStatus
        }
    }

    override suspend fun providePermission() {
        CLLocationManager().requestAlwaysAuthorization()
    }

    override fun openSettingPage() {
        openAppSettingsPage()
    }

    private fun checkBackgroundLocationPermission(): PermissionState =
        when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways -> PermissionState.GRANTED
            kCLAuthorizationStatusDenied -> PermissionState.DENIED
            else -> PermissionState.NOT_DETERMINED
        }
}