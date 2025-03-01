package com.alpenraum.shimstack.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpenraum.shimstack.data.model.ridetracker.GpsPointDto
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsPointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<GpsPointDto>): List<Long>

    @Query("SELECT * FROM gps_points WHERE rideId = :rideId ORDER BY timestamp ASC")
    suspend fun getPointsForRide(rideId: Long): List<GpsPointDto>

    @Query("SELECT * FROM gps_points")
    suspend fun getPointById(): List<GpsPointDto>

    @Query("SELECT * FROM gps_points WHERE rideId = :rideId ORDER BY timestamp ASC")
    fun getPointsForRideFlow(rideId: Long): Flow<List<GpsPointDto>>
}