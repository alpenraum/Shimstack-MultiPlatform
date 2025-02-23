package com.alpenraum.shimstack.ui.location.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Smartphone
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.ic_save
import shimstackmultiplatform.composeapp.generated.resources.imperial
import shimstackmultiplatform.composeapp.generated.resources.permission_background_explainer
import shimstackmultiplatform.composeapp.generated.resources.permission_background_name
import shimstackmultiplatform.composeapp.generated.resources.permission_foreground_explainer
import shimstackmultiplatform.composeapp.generated.resources.permission_foreground_name
import shimstackmultiplatform.composeapp.generated.resources.permission_location_explainer
import shimstackmultiplatform.composeapp.generated.resources.permission_location_name

enum class LocationPermission {
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
    LOCATION_BACKGROUND
}

fun LocationPermission.getNameResource() =
    when (this) {
        LocationPermission.LOCATION_SERVICE_ON -> Res.string.permission_foreground_name
        LocationPermission.LOCATION_FOREGROUND -> Res.string.permission_background_name
        LocationPermission.LOCATION_BACKGROUND -> Res.string.permission_location_name
    }

fun LocationPermission.getExplainerResource() =
    when (this) {
        LocationPermission.LOCATION_SERVICE_ON -> Res.string.permission_foreground_explainer
        LocationPermission.LOCATION_FOREGROUND -> Res.string.permission_background_explainer
        LocationPermission.LOCATION_BACKGROUND -> Res.string.permission_location_explainer
    }

fun LocationPermission.getDrawable() =
    when (this) {
        LocationPermission.LOCATION_SERVICE_ON -> Icons.Default.MyLocation
        LocationPermission.LOCATION_FOREGROUND -> Icons.Default.Smartphone
        LocationPermission.LOCATION_BACKGROUND -> Icons.AutoMirrored.Default.DirectionsBike
    }