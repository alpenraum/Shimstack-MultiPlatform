package com.alpenraum.shimstack.domain.model.ridetracker

import kotlinx.datetime.Instant

data class GpsPoint(
    val id: Long? = null,
    val rideId: Long,
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val altitude: Double,
    val accuracy: Float,
    val timestamp: Instant
)