package com.alpenraum.shimstack.data.model.ridetracker

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class RideUpdateDto(
    val startTime: Instant,
    val endTime: Instant? = null,
    val totalDistance: Float,
    val totalElevation: Float,
    val averageSpeed: Float,
    val topSpeed: Float,
    val gpsPoints: List<GpsPointDto>
)