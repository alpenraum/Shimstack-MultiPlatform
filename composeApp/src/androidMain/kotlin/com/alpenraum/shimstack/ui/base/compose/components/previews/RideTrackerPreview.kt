package com.alpenraum.shimstack.ui.base.compose.components.previews

import androidx.compose.runtime.Composable
import com.alpenraum.shimstack.ui.base.compose.theme.AppTheme
import com.alpenraum.shimstack.ui.location.model.AppPermissions
import com.alpenraum.shimstack.ui.location.model.PermissionState
import com.alpenraum.shimstack.ui.ridetracker.ActiveRideContent
import com.alpenraum.shimstack.ui.ridetracker.DefaultContent
import com.alpenraum.shimstack.ui.ridetracker.PermissionsContent
import com.alpenraum.shimstack.ui.ridetracker.RideTrackerContract
import com.alpenraum.shimstack.ui.ridetracker.RideView
import kotlinx.collections.immutable.persistentListOf

@ShimstackPreviews
@Composable
private fun PermissionPreview() =
    AppTheme {
        val state =
            RideTrackerContract.State.Permissions(
                RideTrackerContract.Permission(
                    PermissionState.NOT_DETERMINED,
                    AppPermissions.LOCATION_FOREGROUND
                ),
                RideTrackerContract.Permission(
                    PermissionState.DENIED,
                    AppPermissions.LOCATION_SERVICE_ON
                ),
                RideTrackerContract.Permission(
                    PermissionState.GRANTED,
                    AppPermissions.LOCATION_BACKGROUND
                ),
                RideTrackerContract.Permission(
                    PermissionState.GRANTED,
                    AppPermissions.SHOW_NOTIFICATIONS
                )
            )

        PermissionsContent(state) { }
    }

@ShimstackPreviews
@Composable
private fun ActiveRidePreview() =
    AppTheme {
        val state =
            RideTrackerContract.State.ActiveRide(
                "55.0 kph",
                "23.4 km",
                "523m",
                "00:34:23",
                persistentListOf()
            )

        ActiveRideContent(state) { }
    }

@ShimstackPreviews
@Composable
private fun DefaultPreview() =
    AppTheme {
        val rideview = RideView("24.06.2026 13:55", "00:54:23", "24.3 km", "652 m", "54.2 km/h", "100 km/h")
        val state =
            RideTrackerContract.State.Default(
                persistentListOf(rideview,rideview,rideview,rideview)
            )

    DefaultContent (state) { }
}