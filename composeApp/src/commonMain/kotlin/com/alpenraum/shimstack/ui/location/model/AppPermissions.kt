package com.alpenraum.shimstack.ui.location.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Smartphone
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.permission_background_explainer
import shimstackmultiplatform.composeapp.generated.resources.permission_background_name
import shimstackmultiplatform.composeapp.generated.resources.permission_foreground_explainer
import shimstackmultiplatform.composeapp.generated.resources.permission_foreground_name
import shimstackmultiplatform.composeapp.generated.resources.permission_location_explainer
import shimstackmultiplatform.composeapp.generated.resources.permission_location_name
import shimstackmultiplatform.composeapp.generated.resources.permission_notification_explainer
import shimstackmultiplatform.composeapp.generated.resources.permission_notification_name

enum class AppPermissions {
    /**
     * Indicates that the system setting location service is on.
     */
    LOCATION_SERVICE_ON,

    /**
     * App location fine permission.
     */
    LOCATION_FOREGROUND,

    /**
     * App location background permission.
     */
    LOCATION_BACKGROUND,

    SHOW_NOTIFICATIONS
}

fun AppPermissions.getNameResource() =
    when (this) {
        AppPermissions.LOCATION_SERVICE_ON -> Res.string.permission_location_name
        AppPermissions.LOCATION_FOREGROUND -> Res.string.permission_foreground_name
        AppPermissions.LOCATION_BACKGROUND -> Res.string.permission_background_name
        AppPermissions.SHOW_NOTIFICATIONS -> Res.string.permission_notification_name
    }

fun AppPermissions.getExplainerResource() =
    when (this) {
        AppPermissions.LOCATION_FOREGROUND -> Res.string.permission_foreground_explainer
        AppPermissions.LOCATION_BACKGROUND -> Res.string.permission_background_explainer
        AppPermissions.LOCATION_SERVICE_ON -> Res.string.permission_location_explainer
        AppPermissions.SHOW_NOTIFICATIONS -> Res.string.permission_notification_explainer
    }

fun AppPermissions.getDrawable() =
    when (this) {
        AppPermissions.LOCATION_SERVICE_ON -> Icons.Default.MyLocation
        AppPermissions.LOCATION_FOREGROUND -> Icons.Default.Smartphone
        AppPermissions.LOCATION_BACKGROUND -> Icons.AutoMirrored.Default.DirectionsBike
        AppPermissions.SHOW_NOTIFICATIONS -> Icons.Default.Notifications
    }