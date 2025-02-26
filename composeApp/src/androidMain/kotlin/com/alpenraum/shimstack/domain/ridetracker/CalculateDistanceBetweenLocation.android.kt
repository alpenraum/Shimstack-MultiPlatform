package com.alpenraum.shimstack.domain.ridetracker

import android.location.Location

actual fun calculateDistance(
    startLatitude: Double,
    startLongitude: Double,
    endLatitude: Double,
    endLongitude: Double
): Float {
    val results = FloatArray(1)
    Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results)

    return results[0]
}