package com.alpenraum.shimstack.data.model.ridetracker

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.alpenraum.shimstack.data.db.AppDatabase
import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import kotlinx.datetime.Instant

@Entity(
    tableName = AppDatabase.TABLE_GPS_POINTS,
    foreignKeys = [
        ForeignKey(
            entity = RideDto::class,
            parentColumns = ["rideId"],
            childColumns = ["rideId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("rideId")]
)
data class GpsPointDto(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val rideId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val accuracy: Float,
    val timestamp: String
) {
    fun toDomain() = GpsPoint(id, rideId, latitude, longitude, speed, altitude, accuracy, Instant.parse(timestamp))

    companion object {
        fun fromDomain(gpsPoint: GpsPoint) =
            GpsPointDto(
                gpsPoint.id,
                gpsPoint.rideId,
                gpsPoint.latitude,
                gpsPoint.longitude,
                gpsPoint.altitude,
                gpsPoint.speed,
                gpsPoint.accuracy,
                gpsPoint.timestamp.toString()
            )
    }
}