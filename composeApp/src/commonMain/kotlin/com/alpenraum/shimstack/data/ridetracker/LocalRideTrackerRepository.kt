package com.alpenraum.shimstack.data.ridetracker

import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.data.db.GpsPointDao
import com.alpenraum.shimstack.data.db.RideDao
import com.alpenraum.shimstack.data.model.ridetracker.GpsPointDto
import com.alpenraum.shimstack.data.model.ridetracker.RideDto
import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import com.alpenraum.shimstack.domain.model.ridetracker.Ride
import com.alpenraum.shimstack.domain.ridetracker.RideTrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class LocalRideTrackerRepository(
    private val rideDao: RideDao,
    private val gpsPointDao: GpsPointDao,
    private val logger: ShimstackLogger
) : RideTrackerRepository {
    override suspend fun createNewRide(): Ride {
        val rideDto = RideDto(startTime = Clock.System.now().toString(), endTime = null)
        val id = rideDao.insertRide(rideDto)
        return getRide(id) ?: throw IllegalStateException("inserted Ride does not exist in db! $id")
    }

    override suspend fun updateRide(ride: Ride) {
        rideDao.updateRide(RideDto.fromDomain(ride))
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

    override fun getRideFlow(rideId: Long): Flow<Ride?> =
        rideDao.getRideFlow(rideId).map { it?.toDomain() }.distinctUntilChanged()

    override suspend fun insertGpsPoints(list: List<GpsPoint>) {
        gpsPointDao.insertAll(list.map { GpsPointDto.fromDomain(it) })
    }

    override suspend fun getGpsPointsForRide(rideId: Long): List<GpsPoint> =
        gpsPointDao.getPointsForRide(rideId).map {
            it.toDomain()
        }

    override fun getGpsPointsForRideFlow(rideId: Long): Flow<List<GpsPoint>> =
        gpsPointDao
            .getPointsForRideFlow(rideId)
            .map { list ->
                list.map {
                    it.toDomain()
                }
            }

    override suspend fun getActiveRide(): Ride? = rideDao.getOngoingRide()?.takeIf { it.endTime == null }?.toDomain()

    override suspend fun getGpsById(id: Long): List<GpsPoint> = gpsPointDao.getPointById().map { it.toDomain() }
}