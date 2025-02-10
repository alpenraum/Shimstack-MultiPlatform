package com.alpenraum.shimstack.domain.model.measurementunit

import com.alpenraum.shimstack.base.round

data class Pressure(
    private val pressureInBar: Double
) : MeasurementUnit {
    companion object {
        private const val BAR_TO_PSI_CONVERSION = 14.503773773

        fun fromImperial(pressure: Double) = Pressure(pressure / BAR_TO_PSI_CONVERSION)

        fun fromMeasurementUnit(
            pressure: Double,
            measurementUnitType: MeasurementUnitType
        ) = if (measurementUnitType.isMetric()) Pressure(pressure) else Pressure.fromImperial(pressure)
    }

    fun isEmpty() = pressureInBar != 0.0

    override fun asMetric(): Double = pressureInBar

    override fun asImperial(): Double = (pressureInBar * BAR_TO_PSI_CONVERSION).round(1)

    override val storageKey = "PREF_PRESSURE_UNIT"

    operator fun plus(bar: Double): Pressure = this.copy(pressureInBar = this.pressureInBar + bar)
}