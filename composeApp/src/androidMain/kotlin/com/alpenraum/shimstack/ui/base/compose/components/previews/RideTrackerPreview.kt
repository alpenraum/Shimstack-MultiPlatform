package com.alpenraum.shimstack.ui.base.compose.components.previews

import androidx.compose.runtime.Composable
import com.alpenraum.shimstack.ui.base.compose.theme.AppTheme
import com.alpenraum.shimstack.ui.location.model.LocationPermission
import com.alpenraum.shimstack.ui.location.model.PermissionState
import com.alpenraum.shimstack.ui.ridetracker.PermissionsContent
import com.alpenraum.shimstack.ui.ridetracker.RideTrackerContract
import kotlinx.collections.immutable.persistentListOf

@ShimstackPreviews
@Composable
private fun PermissionPreview() = AppTheme(){
    val state =
        RideTrackerContract.State.Permissions(
            persistentListOf(
                RideTrackerContract.Permission(
                    PermissionState.NOT_DETERMINED,
                    LocationPermission.LOCATION_FOREGROUND
                ),
                RideTrackerContract.Permission(
                    PermissionState.DENIED,
                    LocationPermission.LOCATION_SERVICE_ON
                ),
                RideTrackerContract.Permission(
                    PermissionState.GRANTED,
                    LocationPermission.LOCATION_BACKGROUND
                )
            )
        )

    PermissionsContent(state) { }
}