package com.alpenraum.shimstack.domain.ridetracker

import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import com.alpenraum.shimstack.domain.model.ridetracker.Ride
import com.alpenraum.shimstack.ui.location.model.LocationResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent

class RideTrackerCache(
    private val ride: Ride
) : KoinComponent {
    private val listMutex = Mutex()
    private var gpsPoints = mutableListOf<GpsPoint>()
        private set
    var totalDistance: Float = 0f
        private set
    var totalElevation: Float = 0f
        private set
    var previousGpsPoint: GpsPoint? = null
        private set

    private var lastSavedDistance = 0f
    private var lastSavedTime = Clock.System.now().epochSeconds

    private val rideTrackerRepository by getKoin().inject<RideTrackerRepository>()
    private val logger by getKoin().inject<ShimstackLogger>()

    suspend fun addNewGpsPoint(locationResult: LocationResult) {
        logger.d("Adding new Gps point!", tag = RideTrackerCache::class.simpleName.toString())

        if (ride.rideId == null) {
            throw IllegalArgumentException("Id of Ride is null! $ride")
        }
        listMutex.withLock {
            previousGpsPoint?.let { prev ->
                val newDistance =
                    calculateDistance(prev.latitude, prev.longitude, locationResult.latitude, locationResult.longitude)
                totalDistance += newDistance

                totalElevation += (locationResult.altitude.toFloat() - prev.altitude.toFloat().coerceAtLeast(0f))
            }

            val gpsPoint =
                GpsPoint(
                    null,
                    ride.rideId,
                    locationResult.latitude,
                    locationResult.longitude,
                    locationResult.speed,
                    locationResult.altitude,
                    locationResult.accuracy,
                    Instant.fromEpochMilliseconds(locationResult.timestampUnixMs)
                ).also {
                    previousGpsPoint = it
                }

            gpsPoints.add(gpsPoint)

            if (totalDistance - lastSavedDistance >= 50 || Clock.System.now().epochSeconds - lastSavedTime >= 10) {
                saveGpsData()
            }
        }
    }

    internal suspend fun saveGpsData() {
        logger.d("Saving GpsData to db!", tag = RideTrackerCache::class.simpleName.toString())
        try {
            rideTrackerRepository.insertGpsPoints(gpsPoints)
            gpsPoints.clear()

            rideTrackerRepository.updateRide(ride.copy(totalDistance = totalDistance))

            lastSavedDistance = totalDistance
            lastSavedTime = Clock.System.now().epochSeconds
        } catch (e: Exception) {
            logger.e("Something went wrong while storing gps data!", e, tag = RideTrackerCache::class.simpleName.toString())
        }
    }

    suspend fun finishRide() {
        logger.d("Finishing ride!", tag = RideTrackerCache::class.simpleName.toString())

        if (ride.rideId == null) {
            throw IllegalArgumentException("Id of Ride is null! $ride")
        }
        try {
            listMutex.withLock {
                saveGpsData()

                val gpsPoints = rideTrackerRepository.getGpsPointsForRide(ride.rideId)

                var finalDistance = 0f
                for (i in 1 until gpsPoints.size) {
                    val prevLatitude = gpsPoints[i - 1].latitude
                    val prevLongitude = gpsPoints[i - 1].longitude
                    val currLatitude = gpsPoints[i].latitude
                    val currLongitude = gpsPoints[i].longitude

                    finalDistance += calculateDistance(prevLatitude, prevLongitude, currLatitude, currLongitude)
                }

                val endTime = Clock.System.now()
                val rideDuration = (endTime - ride.startTime)
                val averageSpeed = (if (rideDuration.isPositive()) finalDistance / rideDuration.inWholeSeconds else 0f) * 3.6f

                rideTrackerRepository.finishRide(
                    ride.copy(
                        endTime = endTime,
                        averageSpeed = averageSpeed,
                        totalDistance = finalDistance
                    )
                )
            }
        } catch (e: Exception) {
            logger.e("Something went wrong while finishing ride!", e, tag = RideTrackerCache::class.simpleName.toString())
        }
    }
}