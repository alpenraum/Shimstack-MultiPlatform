package com.alpenraum.shimstack.ui.location

import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.domain.ridetracker.RideTrackerService
import com.alpenraum.shimstack.ui.location.model.LocationResult
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyNearestTenMeters
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
class LocationManager(
    private val logger: ShimstackLogger,
    private val rideTrackerService: RideTrackerService
) : NSObject(),
    CLLocationManagerDelegateProtocol {
    init {
        logger.setTag(this::class.simpleName)
    }

    private val NOTIFICATION_ID = "location_update"
    private val locationManager = CLLocationManager()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    init {
        if (rideTrackerService.ride?.rideId == null) {
            throw IllegalStateException("RideTrackerService has no valid Ride! ${rideTrackerService.ride}")
        }
        locationManager.delegate = this
        locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false
        locationManager.distanceFilter = LocationServiceConfig.LOCATION_MIN_DISTANCE_METERS
    }

    fun startTracking() {
        locationManager.requestAlwaysAuthorization()
        locationManager.startUpdatingLocation()

        scope.launch {
            var seconds = 0L
            while (true) {
                val start = Clock.System.now().toEpochMilliseconds()
                seconds++
                sendLocationNotification(rideTrackerService.getTotalDistance(), seconds)

                delay(1000L - (Clock.System.now().toEpochMilliseconds() - start))
            }
        }
    }

    fun stopTracking() {
        locationManager.stopUpdatingLocation()
        scope.launch {
            rideTrackerService.finishRide()
            scope.cancel()
        }
    }

    private fun isUserStationary(location: CLLocation): Boolean = location.speed < LocationServiceConfig.MIN_SPEED_THRESHOLD_MS

    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>
    ) {
        val lastLocation = didUpdateLocations.lastOrNull() as? CLLocation ?: return
        logger.d("new Location! $lastLocation", tag = "LocationManager")
//        if (isUserStationary(lastLocation)) {
//            logger.d("user is stationary, reducing update interval", tag = "LocationManager")
//            locationManager.desiredAccuracy = kCLLocationAccuracyKilometer
//            locationManager.distanceFilter = 100.0 // meters
//        } else {
//            logger.d("user is moving, increasing update interval", tag = "LocationManager")
//
//            locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
//            locationManager.distanceFilter = LocationServiceConfig.LOCATION_MIN_DISTANCE_METERS
//        }

        scope.launch {
            val (latitude, longitude) = lastLocation.coordinate.useContents { this.latitude to this.longitude }
            rideTrackerService.addNewGpsPoint(
                LocationResult(
                    latitude,
                    longitude,
                    lastLocation.speed.toFloat(),
                    lastLocation.altitude,
                    lastLocation.horizontalAccuracy.toFloat(),
                    (lastLocation.timestamp.timeIntervalSince1970 * 1000).toLong()
                )
            )
        }
    }

    private fun sendLocationNotification(
        distance: Float?,
        duration: Long
    ) {
        val content =
            UNMutableNotificationContent().apply {
                setTitle("TODO: Tracking Update")
                setBody("Distance: ${distance ?: 0} km/h, Duration: ${duration / 60} min")
                setSound(null)
            }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, false)
        val request =
            UNNotificationRequest.requestWithIdentifier(
                NOTIFICATION_ID,
                content,
                trigger
            )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request, null)
    }
}