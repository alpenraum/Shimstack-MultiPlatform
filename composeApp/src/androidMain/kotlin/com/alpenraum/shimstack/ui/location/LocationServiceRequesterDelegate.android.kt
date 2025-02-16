package com.alpenraum.shimstack.ui.location

import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import com.alpenraum.shimstack.base.di.LocationServiceDelegateName
import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.base.openIntent
import com.alpenraum.shimstack.ui.location.model.PermissionState
import org.koin.core.annotation.Single

@Single
@LocationServiceDelegateName
class LocationServiceRequesterDelegate(
    private val context: Context,
    private val locationManager: LocationManager,
    private val logger: ShimstackLogger
) : LocationRequesterDelegate {
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