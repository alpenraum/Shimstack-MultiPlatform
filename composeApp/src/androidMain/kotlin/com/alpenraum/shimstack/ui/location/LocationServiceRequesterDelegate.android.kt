package com.alpenraum.shimstack.ui.location

import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.base.openIntent
import com.alpenraum.shimstack.ui.location.model.PermissionState

actual class LocationServiceRequesterDelegate(
    private val context: Context,
    private val logger: ShimstackLogger
) : LocationRequesterDelegate {
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as
            LocationManager

    override fun getPermissionState(): PermissionState {
        val granted =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return if (granted) {
            PermissionState.GRANTED
        } else {
            PermissionState.DENIED
        }
    }

    override suspend fun providePermission() {
        openSettingPage()
    }

    override fun openSettingPage() {
        context.openIntent(
            action = Settings.ACTION_LOCATION_SOURCE_SETTINGS,
            onError = { logger.e("Cannot open Location settings") }
        )
    }
}