package com.alpenraum.shimstack.ui.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.text.format.DateUtils
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.bundle.bundleOf
import androidx.core.content.ContextCompat
import com.alpenraum.shimstack.MainActivity
import com.alpenraum.shimstack.R
import com.alpenraum.shimstack.ShimstackApplication
import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.domain.ridetracker.RideTrackerService
import com.alpenraum.shimstack.ui.base.navigation.DeeplinkManager
import com.alpenraum.shimstack.ui.base.navigation.NavigationTarget
import com.alpenraum.shimstack.ui.location.model.AppPermissions
import com.alpenraum.shimstack.ui.location.model.LocationResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.roundToInt

class RideTrackerForegroundService :
    Service(),
    KoinComponent {
    private val logger: ShimstackLogger by inject()
    private val rideTrackerService: RideTrackerService by inject()

    companion object {
        private const val TAG = "LocationForegroundService"
        private const val CHANNEL_ID = "1"
        private const val NOTIFICATION_ID = 100
        private const val LOCATION_UPDATE_INTERVAL = 1000L
        const val EXTRA_RIDE_ID = "RIDE_ID"

        private var isActive: Boolean = false

        internal fun isActive(): Boolean = isActive

        const val ACTION_STOP = "STOP_FOREGROUND_SERVICE"
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var remoteView: RemoteViews? = null
    private var notification: Notification? = null

    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    private var fusedClient: FusedLocationProviderClient? = null
    private var locationUpdateListener: LocationCallback? = null

    override fun onCreate() {
        super.onCreate()
        remoteView =
            RemoteViews(packageName, R.layout.notification_layout)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            isActive = false
            return START_NOT_STICKY
        }
        scope.launch {
            if (rideTrackerService.ride?.rideId == null) {
                throw IllegalStateException("RideTrackerCache has no valid Ride! ${rideTrackerService.ride}")
            }
            if (!isActive) {
                createNotificationChannel()

                notification = getNotification()
                ServiceCompat
                    .startForeground(
                        this@RideTrackerForegroundService,
                        NOTIFICATION_ID,
                        notification!!,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                        } else {
                            0
                        }
                    )
                var gpsAcquired = false
                scope.launch {
                    getLocationUpdates().collect {
                        gpsAcquired = true
                        showTimer()

                        rideTrackerService.addNewGpsPoint(it)
                    }
                }
                scope.launch {
                    var seconds = 0L
                    while (true) {
                        if (gpsAcquired) {
                            val start = Clock.System.now().toEpochMilliseconds()
                            seconds++
                            updateTimer(seconds)
                            updateDistance(rideTrackerService.getTotalDistance())
                            updateElevation(rideTrackerService.getTotalElevation())

                            delay(1000L - (Clock.System.now().toEpochMilliseconds() - start))
                        }
                    }
                }

                isActive = true
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        locationUpdateListener?.let { fusedClient?.removeLocationUpdates(it) }
        val onDbFinished = {
            scope.cancel()
            isActive = false
            super.onDestroy()
        }
        scope.launch {
            rideTrackerService.finishRide()
            onDbFinished()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    private fun getLocationUpdates(): Flow<LocationResult> {
        if (ContextCompat.checkSelfPermission(
                this,
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

            fusedClient?.requestLocationUpdates(createLocationRequest(), locationUpdateListener!!, mainLooper)

            awaitClose {
                locationUpdateListener?.let { fusedClient?.removeLocationUpdates(it) }
            }
        }
    }

    private fun createLocationRequest() =
        LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
            .build()

    private fun getNotification(): Notification {
        val stopIntent =
            Intent(this, RideTrackerForegroundService::class.java).apply {
                action = ACTION_STOP
            }
        val stopPendingIntent =
            PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        val notificationIntent =
            Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtras(bundleOf(DeeplinkManager.NAV_ARG to NavigationTarget.RIDE_TRACKER.name))
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        // TODO: MAKE NOTIFICATION VIEW BETTER
        val builder =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCustomContentView(remoteView) // TODO: ADD SPECIFIC VIEW FOR COLLAPSED
                .setCustomBigContentView(remoteView)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_launcher_foreground, getString(R.string.stop), stopPendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        return builder.build()
    }

    private fun updateTimer(seconds: Long) {
        val timerText = DateUtils.formatElapsedTime(seconds)
        remoteView?.setTextViewText(R.id.timer_text, timerText)

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun updateDistance(distance: Float) {
        val distanceText = "${distance.roundToInt()} m" // TODO correct measurement
        remoteView?.setTextViewText(R.id.distance_text, distanceText)

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun updateElevation(elevation: Float) {
        val elevationText = "${elevation.roundToInt()} m" // TODO correct measurement
        remoteView?.setTextViewText(R.id.elevation_text, elevationText)

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showTimer() {
        remoteView?.setViewVisibility(R.id.progress_spinner, View.GONE)
        remoteView?.setViewVisibility(R.id.content, View.VISIBLE)

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

private fun LocationResult.Companion.fromLocation(location: Location) =
    LocationResult(
        latitude = location.latitude,
        longitude = location.longitude,
        speed = location.speed,
        altitude = location.altitude,
        accuracy = location.accuracy,
        timestampUnixMs = location.time
    )