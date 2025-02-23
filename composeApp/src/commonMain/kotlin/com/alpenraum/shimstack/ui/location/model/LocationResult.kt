package com.alpenraum.shimstack.ui.location

sealed class LocationResult {
    data class Data(
        val latitude: Double,
        val longitude: Double,
        val speed: Float,
        val altitude: Double,
        val accuracy: Float
    ) : LocationResult()

    class locationDisabled : LocationResult()
}