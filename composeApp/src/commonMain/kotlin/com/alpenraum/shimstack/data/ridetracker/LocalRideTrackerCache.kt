package com.alpenraum.shimstack.data.ridetracker

import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import com.alpenraum.shimstack.domain.model.ridetracker.Ride
import com.alpenraum.shimstack.domain.ridetracker.RideTrackerCache
import com.alpenraum.shimstack.domain.ridetracker.RideTrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class LocalRideTrackerCache(
    private val rideTrackerRepository: RideTrackerRepository,
    private val logger: ShimstackLogger
) : RideTrackerCache {
    private val listMutex = Mutex()
    private var gpsPoints = mutableListOf<GpsPoint>()
    private var totalDistance: Float = 0f
    private var totalElevation: Float = 0f

    private var lastSavedDistance = 0f
    private var lastSavedTime = Clock.System.now().epochSeconds
    private var lastSaveIndex: Int = 0

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

            if (totalDistance - lastSavedDistance >= 50 || Clock.System.now().epochSeconds - lastSavedTime >= 10) {
                saveGpsData(ride)
            }
        }
    }

    private suspend fun saveGpsData(ride: Ride) {
        logger.d("Saving GpsData to db!", tag = LocalRideTrackerCache::class.simpleName.toString())
        try {
            rideTrackerRepository.insertGpsPoints(gpsPoints.subList(fromIndex = lastSaveIndex, gpsPoints.size))
            lastSaveIndex = gpsPoints.size - 1

            rideTrackerRepository.updateRide(ride.copy(totalDistance = totalDistance))

            lastSavedDistance = totalDistance
            lastSavedTime = Clock.System.now().epochSeconds
        } catch (e: Exception) {
            logger.e("Something went wrong while storing gps data!", e, tag = LocalRideTrackerCache::class.simpleName.toString())
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
}