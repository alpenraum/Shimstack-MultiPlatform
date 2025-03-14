package com.alpenraum.shimstack.data

import com.alpenraum.shimstack.data.model.bike.BikeDTO
import com.alpenraum.shimstack.data.model.bike.BikeTemplateDTO
import com.alpenraum.shimstack.data.model.bike.DampingDTO
import com.alpenraum.shimstack.data.model.bike.SuspensionDTO
import com.alpenraum.shimstack.data.model.bike.TireDTO
import com.alpenraum.shimstack.domain.model.bike.Bike
import com.alpenraum.shimstack.domain.model.bike.BikeType
import com.alpenraum.shimstack.domain.model.biketemplate.BikeTemplate
import com.alpenraum.shimstack.domain.model.measurementunit.Distance
import com.alpenraum.shimstack.domain.model.measurementunit.Pressure
import com.alpenraum.shimstack.domain.model.suspension.Damping
import com.alpenraum.shimstack.domain.model.suspension.Suspension
import com.alpenraum.shimstack.domain.model.tire.Tire
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.math.round
import kotlin.time.Duration

fun BikeTemplateDTO.toDomain() =
    BikeTemplate(
        id,
        name,
        BikeType.fromId(type),
        isEBike,
        frontSuspensionTravelInMM,
        rearSuspensionTravelInMM,
        frontTireWidthInMM,
        frontRimWidthInMM,
        rearTireWidthInMM,
        rearRimWidthInMM
    )

fun BikeTemplate.toDTO() =
    BikeTemplateDTO(
        id,
        name,
        type.id,
        isEBike,
        frontSuspensionTravelInMM,
        rearSuspensionTravelInMM,
        frontTireWidthInMM,
        frontRimWidthInMM,
        rearTireWidthInMM
    )

fun SuspensionDTO.toDomain() =
    Suspension(
        Pressure(pressure),
        sag,
        compression.toDomain(),
        rebound.toDomain(),
        tokens,
        Distance(travel.toDouble())
    )

fun Suspension.toDTO() =
    SuspensionDTO(pressure.asMetric(), sag, compression.toDTO(), rebound.toDTO(), tokens, travel.asMetric().toInt())

fun DampingDTO.toDomain() = Damping(lowSpeedFromClosed, highSpeedFromClosed)

fun Damping.toDTO() = DampingDTO(lowSpeedFromClosed, highSpeedFromClosed)

fun TireDTO.toDomain() = Tire(Pressure(pressure), Distance(widthInMM), internalRimWidthInMM?.let { Distance(it) })

fun Tire.toDTO() = TireDTO(pressure.asMetric(), width.asMetric(), internalRimWidthInMM?.asMetric())

fun Bike.toDTO() =
    BikeDTO(
        id,
        name,
        type.id,
        frontSuspension?.toDTO(),
        rearSuspension?.toDTO(),
        this.frontTire.toDTO(),
        this.rearTire.toDTO(),
        isEBike
    )

fun BikeDTO.toDomain() =
    Bike(
        id ?: 0,
        name,
        BikeType.fromId(type),
        frontSuspension?.toDomain(),
        rearSuspension?.toDomain(),
        frontTire.toDomain(),
        rearTire.toDomain(),
        isEBike
    )

fun Float.kmToMiles() = this / 1.609f

fun Float.mToFeet() = this * 3.28084f

fun Float.kphToMph() = this / 1.609f

fun Float.roundToUiFormat() = round(this * 10) / 10

fun Duration.formatted(): String =
    this.toComponents { hours, minutes, seconds, _ ->
        "${hours.toInt().padStartForWidth()}:${minutes.padStartForWidth()}:${seconds.padStartForWidth()}"
    }

fun Int.padStartForWidth(width: Int = 2): String {
    val times = width - this.toString().length
    return if (times <= 0) this.toString() else "${"0".repeat(times)}$this"
}

fun Instant.toDate(): String {
    val timeZone = TimeZone.currentSystemDefault() // Gets the device's current time zone
    val localDateTime = this.toLocalDateTime(timeZone)
    return localDateTime.format(
        LocalDateTime.Format {
            dayOfMonth()
            char('.')
            monthNumber()
            char('.')
            year()
        }
    )
}