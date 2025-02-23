package com.alpenraum.shimstack.ui.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.alpenraum.shimstack.MainActivity
import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.ui.location.model.LocationPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class LocationForegroundService(
    private val logger: ShimstackLogger
) : Service() {
    companion object {
        private const val CHANNEL_ID = "1"
        private const val LOCATION_UPDATE_INTERVAL = 1000L

        private var isActive: Boolean = false

        internal fun isActive(): Boolean = isActive
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var locationManager: LocationManager =
        getSystemService(Context.LOCATION_SERVICE) as
            LocationManager

    private var locationListener: LocationListener? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (!isActive) {
            createNotificationChannel()

            ServiceCompat
                .startForeground(
                    this,
                    1,
                    getNotification(),
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                    } else {
                        0
                    }
                )
            scope.launch {
                getLocationUpdates().collect {
                    logger.d("___ New location: $it")
                }
            }
            isActive = true
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        locationListener?.let { locationManager.removeUpdates(it) }
        scope.cancel()
        isActive = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    "TODO: Location Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
                )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun getLocationUpdates(): Flow<LocationResult> {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            throw PermissionNotGrantedException(LocationPermission.LOCATION_FOREGROUND)
        }
        return callbackFlow {
            locationListener =
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val locationResult =
                            LocationResult.Data(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                speed = location.speed,
                                altitude = location.altitude,
                                accuracy = location.accuracy
                            )
                        trySend(locationResult)
                    }

                    override fun onProviderEnabled(provider: String) {
                        logger.d("LocationListener: onProviderEnabled")
                    }

                    override fun onProviderDisabled(provider: String) {
                        logger.d("LocationListener: onProviderDisabled")
                    }
                }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_UPDATE_INTERVAL,
                0.5f,
                locationListener!!,
                Looper.getMainLooper()
            )
            awaitClose {
                locationManager.removeUpdates(locationListener!!)
            }
        }
    }

    private fun getNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        val builder =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setContentTitle("TODO: Location Service")
                .setContentText("TODO: Getting location updates")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        return builder.build()
    }
}