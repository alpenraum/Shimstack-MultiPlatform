package com.alpenraum.shimstack.ui.location.model

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val speedInMs: Float,
    val altitude: Double,
    val accuracy: Float,
    val timestampUnixMs: Long
) {
    companion object {}
}