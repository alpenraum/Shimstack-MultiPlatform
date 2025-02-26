package com.alpenraum.shimstack.domain.ridetracker

expect fun calculateDistance(
    startLatitude: Double,
    startLongitude: Double,
    endLatitude: Double,
    endLongitude: Double
): Float