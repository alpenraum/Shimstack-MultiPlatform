package com.alpenraum.shimstack.ui.location

import com.alpenraum.shimstack.base.di.ForegroundLocationDelegateName
import com.alpenraum.shimstack.base.openAppSettingsPage
import com.alpenraum.shimstack.ui.location.model.PermissionState
import org.koin.core.annotation.Single
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted

@Single
@ForegroundLocationDelegateName
class ForegroundLocationRequesterDelegate : LocationRequesterDelegate {
    private var locationManager = CLLocationManager()

    override fun getPermissionState(): PermissionState =
        when (locationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusRestricted -> PermissionState.GRANTED

            kCLAuthorizationStatusNotDetermined -> PermissionState.NOT_DETERMINED
            kCLAuthorizationStatusDenied -> PermissionState.DENIED
            else -> PermissionState.NOT_DETERMINED
        }

    override suspend fun providePermission() {
        locationManager.requestWhenInUseAuthorization()
    }

    override fun openSettingPage() {
        openAppSettingsPage()
    }
}