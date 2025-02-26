package com.alpenraum.shimstack.domain.ridetracker

import platform.CoreLocation.CLLocation

actual fun calculateDistance(
    startLatitude: Double,
    startLongitude: Double,
    endLatitude: Double,
    endLongitude: Double
): Float {
    val startLocation = CLLocation(startLatitude, startLongitude)
    val endLocation = CLLocation(endLatitude, endLongitude)

    return startLocation.distanceFromLocation(endLocation).toFloat()
}