package com.alpenraum.shimstack.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alpenraum.shimstack.data.model.ridetracker.RideDto
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: RideDto): Long

    @Update
    suspend fun updateRide(ride: RideDto): Int

    @Query("SELECT * FROM rides WHERE rideId =:rideId")
    suspend fun getRide(rideId: Long): RideDto?

    @Query("SELECT * FROM rides ORDER BY endTime DESC")
    suspend fun getAllRides(): List<RideDto>

    @Query("SELECT * FROM rides WHERE rideId =:rideId")
    fun getRideFlow(rideId: Long): Flow<RideDto?>

    @Query("SELECT * FROM rides ORDER BY rideId DESC LIMIT 1")
    suspend fun getOngoingRide(): RideDto?

    @Query("UPDATE rides SET endTime = :endTime, totalDistance = :distance, averageSpeed = :speed WHERE rideId = :rideId")
    suspend fun finishRide(
        rideId: Long,
        endTime: String,
        distance: Float,
        speed: Float
    )
}