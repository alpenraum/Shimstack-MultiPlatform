package com.alpenraum.shimstack.ui.location.model

sealed class LocationResult {
    data class Data(
        val latitude: Double,
        val longitude: Double,
        val speed: Float,
        val altitude: Double,
        val accuracy: Float
    ) : LocationResult() {
        companion object {
        }
    }

    class LocationDisabled : LocationResult()
}