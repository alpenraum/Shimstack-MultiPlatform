package com.alpenraum.shimstack.domain.ridetracker

import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import com.alpenraum.shimstack.domain.model.ridetracker.Ride
import kotlinx.coroutines.flow.Flow

interface RideTrackerCache {
    suspend fun addNewGpsPoint(
        gpsPoint: GpsPoint,
        ride: Ride
    )

    suspend fun finishRide(ride: Ride)

    suspend fun getLastGpsPoint(): GpsPoint?

    fun addToTotalDistance(distance: Float)

    fun getTotalDistance(): Float

    fun addToTotalElevation(elevation: Float)

    fun getTotalElevation(): Float

    fun getGpsPointFlow(): Flow<List<GpsPoint>>

    fun getGpsPoints(): List<GpsPoint>
}