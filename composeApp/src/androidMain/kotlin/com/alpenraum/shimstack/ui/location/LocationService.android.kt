package com.alpenraum.shimstack.ui.location

import android.content.Intent
import android.os.Build
import com.alpenraum.shimstack.ShimstackApplication

actual class LocationService {
    private var intent: Intent? = null

    actual fun startLocationService(rideId: Long) {
        val context = ShimstackApplication.appContext
        intent =
            Intent(context, RideTrackerForegroundService::class.java).apply {
                putExtra(RideTrackerForegroundService.EXTRA_RIDE_ID, rideId)
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    actual fun isLocationServiceActive(): Boolean = RideTrackerForegroundService.isActive()

    actual fun stopLocationService() {
        val context = ShimstackApplication.appContext
        context.stopService(intent)
    }
}