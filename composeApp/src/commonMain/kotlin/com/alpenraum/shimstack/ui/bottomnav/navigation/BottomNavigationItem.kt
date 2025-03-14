package com.alpenraum.shimstack.ui.bottomnav.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.ride_tracking_name
import shimstackmultiplatform.composeapp.generated.resources.settings
import shimstackmultiplatform.composeapp.generated.resources.title_home
import shimstackmultiplatform.composeapp.generated.resources.title_setup_troubleshoot

sealed class BottomNavigationItem(
    val route: String,
    val titleRes: StringResource,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Home : BottomNavigationItem(
        "home",
        Res.string.title_home,
        Icons.Outlined.Home,
        Icons.Filled.Home
    )

    data object SetupTroubleshoot : BottomNavigationItem(
        "setupTroubleshoot",
        Res.string.title_setup_troubleshoot,
        Icons.Outlined.Bolt,
        Icons.Filled.Bolt
    )

    data object Settings :
        BottomNavigationItem("settings", Res.string.settings, Icons.Outlined.Settings, Icons.Filled.Settings)

    data object RideTracking :
        BottomNavigationItem("ride-tracking", Res.string.ride_tracking_name, Icons.Outlined.NearMe, Icons.Filled.NearMe)

    companion object {
        fun asList() = listOf(Home, SetupTroubleshoot, RideTracking, Settings)
    }
}