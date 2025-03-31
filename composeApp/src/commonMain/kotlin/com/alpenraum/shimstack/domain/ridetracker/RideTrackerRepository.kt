package com.alpenraum.shimstack.domain.ridetracker

import com.alpenraum.shimstack.data.ridetracker.RideUpdateConsumer
import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import com.alpenraum.shimstack.domain.model.ridetracker.Ride
import kotlinx.coroutines.flow.Flow

interface RideTrackerRepository : RideUpdateConsumer {
    suspend fun createNewRide(): Ride

    suspend fun updateRide(ride: Ride)

    suspend fun finishRide(ride: Ride)

    suspend fun getRide(rideId: Long): Ride?

    fun getRideFlow(rideId: Long): Flow<Ride?>

    suspend fun getAllRides(): List<Ride>

    suspend fun insertGpsPoints(list: List<GpsPoint>)

    suspend fun getGpsPointsForRide(rideId: Long): List<GpsPoint>

    fun getGpsPointsForRideFlow(rideId: Long): Flow<List<GpsPoint>>

    suspend fun getActiveRide(): Ride?

    suspend fun getGpsById(id: Long): List<GpsPoint>
}