package com.alpenraum.shimstack.data.ridetracker

import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import com.alpenraum.shimstack.domain.model.ridetracker.Ride

interface RideUpdateConsumer {
    suspend fun consumeUpdate(
        ride: Ride,
        gpsPoints: List<GpsPoint>
    )
}