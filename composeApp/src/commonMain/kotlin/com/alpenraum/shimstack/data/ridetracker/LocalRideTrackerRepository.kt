package com.alpenraum.shimstack.data.ridetracker

import com.alpenraum.shimstack.data.db.GpsPointDao
import com.alpenraum.shimstack.data.db.RideDao
import com.alpenraum.shimstack.data.model.ridetracker.GpsPointDto
import com.alpenraum.shimstack.data.model.ridetracker.RideDto
import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import com.alpenraum.shimstack.domain.model.ridetracker.Ride
import com.alpenraum.shimstack.domain.ridetracker.RideTrackerRepository
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class LocalRideTrackerRepository(
    private val rideDao: RideDao,
    private val gpsPointDao: GpsPointDao
) : RideTrackerRepository {
    override suspend fun createNewRide(): Ride {
        val rideDto = RideDto(startTime = Clock.System.now().toString(), endTime = null)
        val id = rideDao.insertRide(rideDto)
        return getRide(id) ?: throw IllegalStateException("inserted Ride does not exist in db! $id")
    }

    override suspend fun updateRide(ride: Ride) {
        rideDao.insertRide(RideDto.fromDomain(ride))
    }

    override suspend fun finishRide(ride: Ride) {
        if (ride.rideId == null) {
            throw IllegalArgumentException("RideId is null!")
        }
        if (ride.endTime == null) {
            throw IllegalArgumentException("EndTime is null!")
        }
        rideDao.finishRide(ride.rideId, ride.endTime.toString(), ride.totalDistance, ride.averageSpeed)
    }

    override suspend fun getRide(rideId: Long): Ride? = rideDao.getRide(rideId)?.toDomain()

    override suspend fun insertGpsPoints(list: List<GpsPoint>) {
        gpsPointDao.insertAll(list.map { GpsPointDto.fromDomain(it) })
    }

    override suspend fun getGpsPointsForRide(rideId: Long): List<GpsPoint> =
        gpsPointDao.getPointsForRide(rideId).map {
            it.toDomain()
        }

    override suspend fun getActiveRide(): Ride? = rideDao.getOngoingRide()?.toDomain()
}