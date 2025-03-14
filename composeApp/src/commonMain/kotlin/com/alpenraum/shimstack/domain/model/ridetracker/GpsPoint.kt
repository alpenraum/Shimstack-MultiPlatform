package com.alpenraum.shimstack.domain.model.ridetracker

import com.alpenraum.shimstack.ui.location.model.LocationResult
import kotlinx.datetime.Instant

data class GpsPoint(
    val id: Long? = null,
    val rideId: Long,
    val latitude: Double,
    val longitude: Double,
    val speedInKph: Float,
    val altitude: Double,
    val accuracy: Float,
    val timestamp: Instant
) {
    companion object {
        fun fromLocationResult(
            locationResult: LocationResult,
            rideId: Long
        ) = with(locationResult) {
            return@with GpsPoint(
                null,
                rideId,
                locationResult.latitude,
                locationResult.longitude,
                locationResult.speedInMs / 3.6f,
                locationResult.altitude,
                locationResult.accuracy,
                Instant.fromEpochMilliseconds(locationResult.timestampUnixMs)
            )
        }
    }
}