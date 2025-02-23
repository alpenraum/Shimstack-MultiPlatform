package com.alpenraum.shimstack.ui.base.compose.components.previews

import androidx.compose.runtime.Composable
import com.alpenraum.shimstack.ui.base.compose.theme.AppTheme
import com.alpenraum.shimstack.ui.location.model.AppPermissions
import com.alpenraum.shimstack.ui.location.model.PermissionState
import com.alpenraum.shimstack.ui.ridetracker.PermissionsContent
import com.alpenraum.shimstack.ui.ridetracker.RideTrackerContract

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
                ),
            )

        PermissionsContent(state) { }
    }