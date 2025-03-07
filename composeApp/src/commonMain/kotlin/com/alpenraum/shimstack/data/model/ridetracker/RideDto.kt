package com.alpenraum.shimstack.data.model.ridetracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alpenraum.shimstack.data.db.AppDatabase
import com.alpenraum.shimstack.domain.model.ridetracker.Ride
import kotlinx.datetime.Instant

@Entity(tableName = AppDatabase.TABLE_RIDE)
data class RideDto(
    @PrimaryKey(autoGenerate = true) val rideId: Long? = null,
    val startTime: String,
    val endTime: String?, // Null if ongoing
    val totalDistance: Float = 0f, // Meters
    val totalElevation: Float = 0f,
    val averageSpeed: Float = 0f,
    val topSpeed: Float = 0f
) {
    fun toDomain() =
        Ride(
            rideId,
            Instant.parse(startTime),
            endTime?.let { Instant.parse(it) },
            totalDistance,
            totalElevation,
            averageSpeed,
            topSpeed
        )

    companion object {
        fun fromDomain(ride: Ride) =
            RideDto(
                ride.rideId,
                ride.startTime.toString(),
                ride.endTime?.toString(),
                ride.totalDistance,
                ride.totalElevation,
                ride.averageSpeed,
                ride.topSpeed
            )
    }
}