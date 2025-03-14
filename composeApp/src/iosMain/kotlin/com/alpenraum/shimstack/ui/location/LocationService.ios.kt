package com.alpenraum.shimstack.ui.location

import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.domain.ridetracker.RideTrackerService
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual class LocationService(
    private val logger: ShimstackLogger,
    private val rideTrackerService: RideTrackerService
) : KoinComponent {
    private var locationManager: LocationManager? = null

    actual fun isLocationServiceActive(): Boolean = locationManager != null

    actual fun startLocationService(rideId: Long) {
        locationManager = LocationManager(logger, rideTrackerService).also { it.startTracking() }
    }

    actual fun stopLocationService() {
        locationManager?.stopTracking().also { locationManager = null }
    }
}