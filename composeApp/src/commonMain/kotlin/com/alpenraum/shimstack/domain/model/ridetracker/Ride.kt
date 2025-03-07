package com.alpenraum.shimstack.domain.model.ridetracker

import kotlinx.datetime.Instant

data class Ride(
    val rideId: Long? = null,
    val startTime: Instant,
    val endTime: Instant?,
    val totalDistance: Float = 0f,
    val totalElevation: Float = 0f,
    val averageSpeed: Float = 0f,
    val topSpeed: Float = 0f
)