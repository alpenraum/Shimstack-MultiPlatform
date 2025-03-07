package com.alpenraum.shimstack.domain.ridetracker

import com.alpenraum.shimstack.base.logger.ShimstackLogger
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
    private val logger: ShimstackLogger
) {
    var ride: Ride? = null

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
                    if (gpsPoints[i].speed > topSpeed) {
                        gpsPoints[i].speed
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
            rideTrackerCache.finishRide(it)
        }
    }

    suspend fun addNewGpsPoint(gpsPoint: LocationResult) =
        ride?.let {
            if (it.rideId == null) {
                logger.e("Set Ride has no valid id!")
                return@let
            }
            rideTrackerCache.getLastGpsPoint()?.let { prev ->
                val newDistance =
                    calculateDistance(prev.latitude, prev.longitude, gpsPoint.latitude, gpsPoint.longitude)
                rideTrackerCache.addToTotalDistance(newDistance)

                rideTrackerCache.addToTotalElevation(gpsPoint.altitude.toFloat() - prev.altitude.toFloat().coerceAtLeast(0f))
            }

            rideTrackerCache.addNewGpsPoint(GpsPoint.fromLocationResult(gpsPoint, it.rideId), it)
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