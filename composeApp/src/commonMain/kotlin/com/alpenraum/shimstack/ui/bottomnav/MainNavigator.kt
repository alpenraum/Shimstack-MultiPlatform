package com.alpenraum.shimstack.ui.bottomnav

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import org.koin.core.annotation.Single

@Single
class MainNavigator {
    fun navigateToBikeDetails(
        id: Int,
        navController: NavController
    ) {
        navController.navigate(MainRoute.BikeDetails(id))
    }

    fun navigateToHome(
        navController: NavController,
        navOptions: NavOptions? = null
    ) {
        navController.navigate(MainRoute.BottomNav(), navOptions = navOptions)
    }
}