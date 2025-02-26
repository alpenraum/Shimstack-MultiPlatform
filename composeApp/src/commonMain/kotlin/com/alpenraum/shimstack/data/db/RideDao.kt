package com.alpenraum.shimstack.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alpenraum.shimstack.data.model.ridetracker.RideDto

@Dao
interface RideDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: RideDto): Long

    @Query("SELECT * FROM rides WHERE rideId =:rideId")
    suspend fun getRide(rideId: Long): RideDto?

    @Query("SELECT * FROM rides WHERE endTime IS NULL LIMIT 1")
    suspend fun getOngoingRide(): RideDto?

    @Query("UPDATE rides SET endTime = :endTime, totalDistance = :distance, averageSpeed = :speed WHERE rideId = :rideId")
    suspend fun finishRide(
        rideId: Long,
        endTime: String,
        distance: Float,
        speed: Float
    )
}