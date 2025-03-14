package com.alpenraum.shimstack.ui.location

expect class LocationService {
    fun isLocationServiceActive(): Boolean

    fun startLocationService(rideId: Long)

    fun stopLocationService()
}

abstract class LocationServiceConfig {
    companion object {
        const val LOCATION_UPDATE_INTERVAL = 1000L
        const val LOCATION_MIN_DISTANCE_METERS = 5.0
        const val MIN_SPEED_THRESHOLD_MS = 0.5
    }
}