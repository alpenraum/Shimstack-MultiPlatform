package com.alpenraum.shimstack.data.ridetracker

import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.base.logger.WithLogger
import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import com.alpenraum.shimstack.domain.model.ridetracker.Ride
import com.alpenraum.shimstack.domain.ridetracker.RideTrackerCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

@Single
class LocalRideTrackerCache(
    logger: ShimstackLogger
) : WithLogger(logger),
    RideTrackerCache {
    private val listMutex = Mutex()
    private var gpsPoints = mutableListOf<GpsPoint>()
    private var totalDistance: Float = 0f
    private var totalElevation: Float = 0f

    private val gpsPointsFlow = MutableStateFlow<List<GpsPoint>>(emptyList())

    override suspend fun addNewGpsPoint(
        gpsPoint: GpsPoint,
        ride: Ride
    ) = ride.let {
        logger.d("Adding new Gps point!", tag = LocalRideTrackerCache::class.simpleName.toString())

        if (it.rideId == null) {
            throw IllegalArgumentException("Id of Ride is null! $it")
        }
        listMutex.withLock {
            gpsPoints.add(gpsPoint)
            gpsPointsFlow.emit(gpsPoints)
        }
    }

//    override suspend fun finishRide(ride: Ride) =
//        ride.let {
//            logger.d("Finishing ride!", tag = LocalRideTrackerCache::class.simpleName.toString())
//            gpsPointsFlow.emit(gpsPoints)
//            try {
//                saveGpsData(ride)
//            } catch (e: Exception) {
//                logger.e(
//                    "Something went wrong while finishing ride!",
//                    e,
//                    tag = LocalRideTrackerCache::class.simpleName.toString()
//                )
//            }
//        }

    override suspend fun getLastGpsPoint(): GpsPoint? = gpsPoints.lastOrNull()

    override fun addToTotalDistance(distance: Float) {
        totalDistance += distance
    }

    override fun getTotalDistance(): Float = totalDistance

    override fun getTotalElevation(): Float = totalElevation

    override fun addToTotalElevation(elevation: Float) {
        totalElevation += totalElevation
    }

    override fun getGpsPointFlow(): Flow<List<GpsPoint>> = gpsPointsFlow.asStateFlow()

    override fun getGpsPoints(): List<GpsPoint> = gpsPoints

    override fun reset() {
        totalDistance = 0f
        totalElevation = 0f
        gpsPoints = mutableListOf()
    }
}