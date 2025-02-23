package com.alpenraum.shimstack.ui.location

import android.content.Intent
import android.os.Build
import com.alpenraum.shimstack.ShimstackApplication
import org.koin.core.annotation.Single

@Single
actual class LocationService {
    actual fun startLocationService() {
        val context = ShimstackApplication.appContext
        val intent = Intent(context, LocationForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    actual fun isLocationServiceActive(): Boolean = LocationForegroundService.isActive()
}