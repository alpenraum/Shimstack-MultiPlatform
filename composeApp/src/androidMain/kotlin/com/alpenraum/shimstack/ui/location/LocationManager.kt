package com.alpenraum.shimstack.ui.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.alpenraum.shimstack.ShimstackApplication
import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.ui.location.model.AppPermissions
import com.alpenraum.shimstack.ui.location.model.LocationResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class LocationManager : KoinComponent {
    companion object {
        private const val TAG = "LocationManager"
    }

    private val logger: ShimstackLogger = get<ShimstackLogger>().apply { setTag(this@LocationManager::class.simpleName) }

    private var fusedClient: FusedLocationProviderClient? = null
    private var locationUpdateListener: LocationCallback? = null

    private var lastLocation: Location? = null

    fun getLocationUpdates(context: Context): Flow<LocationResult> {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            throw PermissionNotGrantedException(AppPermissions.LOCATION_FOREGROUND)
        }
        return callbackFlow {
            locationUpdateListener =
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                        logger.d("received " + locationResult.locations.size + " locations", tag = TAG)

                        for (loc in locationResult.locations) {
                            trySend(LocationResult.fromLocation(loc))
                        }
                    }
                }

            fusedClient = LocationServices.getFusedLocationProviderClient(ShimstackApplication.activity)

            fusedClient?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    trySend(LocationResult.fromLocation(location))
                }
            }

            fusedClient?.requestLocationUpdates(createLocationRequest(), locationUpdateListener!!, context.mainLooper)

            awaitClose {
                locationUpdateListener?.let { fusedClient?.removeLocationUpdates(it) }
            }
        }
    }

    private fun createLocationRequest() =
        LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, LocationServiceConfig.LOCATION_UPDATE_INTERVAL)
            .setMinUpdateDistanceMeters(LocationServiceConfig.LOCATION_MIN_DISTANCE_METERS.toFloat())
            .build()

    fun onStop() {
        locationUpdateListener?.let { fusedClient?.removeLocationUpdates(it) }
    }

    private fun LocationResult.Companion.fromLocation(location: Location): LocationResult {
        val speed =
            if (location.hasSpeed()) {
                location.speed
            } else {
                lastLocation?.let {
                    val elapsedTimeInSeconds = (location.time - it.time) / 1_000
                    val distanceInMeters = it.distanceTo(location)
                    return@let distanceInMeters / elapsedTimeInSeconds
                } ?: 0.0f
            }
        return LocationResult(
            latitude = location.latitude,
            longitude = location.longitude,
            speedInMs = speed,
            altitude = location.altitude,
            accuracy = location.accuracy,
            timestampUnixMs = location.time
        )
    }
}