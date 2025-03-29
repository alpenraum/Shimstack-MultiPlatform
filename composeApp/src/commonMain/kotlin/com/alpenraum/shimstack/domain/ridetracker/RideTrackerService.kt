package com.alpenraum.shimstack.domain.ridetracker

import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.base.logger.WithLogger
import com.alpenraum.shimstack.data.ridetracker.LocalRideTrackerCache
import com.alpenraum.shimstack.data.ridetracker.RideTrackerApiRepository
import com.alpenraum.shimstack.data.ridetracker.RideUpdateConsumer
import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import com.alpenraum.shimstack.domain.model.ridetracker.Ride
import com.alpenraum.shimstack.ui.location.model.LocationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class RideTrackerService(
    private val rideTrackerCache: RideTrackerCache,
    private val rideTrackerRepository: RideTrackerRepository,
    private val rideTrackerApiRepository: RideTrackerApiRepository,
    logger: ShimstackLogger
) : WithLogger(logger) {
    var ride: Ride? = null

    private var lastSavedDistance = 0f
    private var lastSavedTime = Clock.System.now().epochSeconds
    private var lastSaveIndex: Int = 0
    private var uploadToRemote: Boolean = false

    private var rideUpdateConsumers: MutableList<RideUpdateConsumer> = mutableListOf(rideTrackerRepository)

    suspend fun startNewRide(uploadToRemote: Boolean): Ride {
        val ride = rideTrackerRepository.createNewRide()

        if (uploadToRemote) {
            activateRemoteUpload()
        }

        return ride
    }

    fun activateRemoteUpload() {
        this.uploadToRemote = true
        rideUpdateConsumers.add(rideTrackerApiRepository)
    }

    suspend fun finishRide() {
        ride?.let {
            if (it.rideId == null) {
                logger.e("Set Ride has no valid id!")
                return@let
            }
            val gpsPoints = rideTrackerCache.getGpsPoints()

            var finalDistance = 0f
            var topSpeed = 0f
            for (i in 1 until gpsPoints.size) {
                val prevLatitude = gpsPoints[i - 1].latitude
                val prevLongitude = gpsPoints[i - 1].longitude
                val currLatitude = gpsPoints[i].latitude
                val currLongitude = gpsPoints[i].longitude

                finalDistance += calculateDistance(prevLatitude, prevLongitude, currLatitude, currLongitude)
                topSpeed =
                    if (gpsPoints[i].speedInKph > topSpeed) {
                        gpsPoints[i].speedInKph
                    } else {
                        topSpeed
                    }
            }

            val endTime = Clock.System.now()
            val rideDuration = (endTime - it.startTime)
            val averageSpeed = (if (rideDuration.isPositive()) finalDistance / rideDuration.inWholeSeconds else 0f) * 3.6f

            rideTrackerRepository.finishRide(
                it
                    .copy(
                        endTime = endTime,
                        averageSpeed = averageSpeed,
                        totalDistance = finalDistance,
                        topSpeed = topSpeed
                    ).also { newRide ->
                        ride = newRide
                    }
            )
            if (uploadToRemote) {
                rideTrackerApiRepository.finishRide()
                rideUpdateConsumers.remove(rideTrackerApiRepository)
                uploadToRemote = false
            }
            lastSavedDistance = 0f
            lastSaveIndex = 0
            lastSavedTime = Clock.System.now().epochSeconds
            ride = null
        }
    }

    suspend fun addNewGpsPoint(gpsPoint: LocationResult) =
        ride?.let {
            with(rideTrackerCache) {
                if (it.rideId == null) {
                    logger.e("Set Ride has no valid id!")
                    return@let
                }
                getLastGpsPoint()?.let { prev ->
                    val newDistance =
                        calculateDistance(prev.latitude, prev.longitude, gpsPoint.latitude, gpsPoint.longitude)
                    addToTotalDistance(newDistance)

                    addToTotalElevation(gpsPoint.altitude.toFloat() - prev.altitude.toFloat().coerceAtLeast(0f))
                }

                addNewGpsPoint(GpsPoint.fromLocationResult(gpsPoint, it.rideId), it)

                if (getTotalDistance() - lastSavedDistance >= 50 || Clock.System.now().epochSeconds - lastSavedTime >= 10) {
                    saveGpsData(it, getGpsPoints(), getTotalDistance())
                }
            }
        }

    private suspend fun saveGpsData(
        ride: Ride,
        gpsPoints: List<GpsPoint>,
        newTotalDistance: Float
    ) {
        logger.d("Saving GpsData to db!", tag = LocalRideTrackerCache::class.simpleName.toString())
        try {
            rideTrackerRepository.insertGpsPoints(gpsPoints.subList(fromIndex = lastSaveIndex, gpsPoints.size))
            lastSaveIndex = gpsPoints.size - 1

            rideTrackerRepository.updateRide(ride.copy(totalDistance = newTotalDistance))

            lastSavedDistance = newTotalDistance
            lastSavedTime = Clock.System.now().epochSeconds
        } catch (e: Exception) {
            logger.e("Something went wrong while storing gps data!", e, tag = LocalRideTrackerCache::class.simpleName.toString())
        }
    }

    fun getRideDataFlow(): Flow<RideData> =
        rideTrackerCache
            .getGpsPointFlow()
            .distinctUntilChanged()
            .map { RideData(ride!!, getTotalDistance(), getTotalElevation(), it) } // TODO: DELETE !!

    fun getTotalDistance() = rideTrackerCache.getTotalDistance()

    fun getTotalElevation() = rideTrackerCache.getTotalElevation()
}

data class RideData(
    val ride: Ride,
    val totalDistance: Float,
    val totalElevation: Float,
    val gpsPoints: List<GpsPoint>
)