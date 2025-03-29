package com.alpenraum.shimstack.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.alpenraum.shimstack.base.DispatchersProvider
import com.alpenraum.shimstack.data.model.bike.BikeDTO
import com.alpenraum.shimstack.data.model.bike.BikeTemplateDTO
import com.alpenraum.shimstack.data.model.ridetracker.GpsPointDto
import com.alpenraum.shimstack.data.model.ridetracker.RideDto
import com.alpenraum.shimstack.data.model.setupRecommendation.SetupRecommendationDTO

@Database(
    entities = [BikeDTO::class, BikeTemplateDTO::class, SetupRecommendationDTO::class, RideDto::class, GpsPointDto::class],
    version = 7,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bikeDao(): BikeDAO

    abstract fun bikeTemplateDao(): BikeTemplateDAO

    abstract fun setupRecommendationsDao(): SetupRecommendationDAO

    abstract fun rideDao(): RideDao

    abstract fun gpsPointDao(): GpsPointDao

    companion object {
        const val TABLE_BIKE = "user_bikes"
        const val TABLE_BIKE_TEMPLATE = "template_bikes"
        const val TABLE_SETUP_RECOMMENDATION = "setup_recommendation"
        const val TABLE_RIDE = "rides"
        const val TABLE_GPS_POINTS = "gps_points"
    }
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun createDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    dispatchersProvider: DispatchersProvider
): AppDatabase =
    builder
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .fallbackToDestructiveMigration(true) // .addMigration
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(dispatchersProvider.io)
        .build()

internal const val dbFileName = "shimstack_db.db"