package com.alpenraum.shimstack.ui.location

expect class LocationService {
    fun isLocationServiceActive(): Boolean

    fun startLocationService(rideId: Long)
}