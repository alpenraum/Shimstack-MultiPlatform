package com.alpenraum.shimstack.ui.location

import com.alpenraum.shimstack.base.openNSUrl
import com.alpenraum.shimstack.ui.location.model.PermissionState
import platform.CoreLocation.CLLocationManager

actual class LocationServiceRequesterDelegate : LocationRequesterDelegate {
    private val locationManager = CLLocationManager()

    override fun getPermissionState(): PermissionState =
        if (locationManager.locationServicesEnabled()) {
            PermissionState.GRANTED
        } else {
            PermissionState.DENIED
        }

    override suspend fun providePermission() {
        openSettingPage()
    }

    override fun openSettingPage() {
        openNSUrl("App-Prefs:Privacy&path=LOCATION")
    }
}