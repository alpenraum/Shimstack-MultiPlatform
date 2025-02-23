package com.alpenraum.shimstack.ui.bottomnav

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.alpenraum.shimstack.ui.base.navigation.NavDestinationDefinition
import com.alpenraum.shimstack.ui.base.navigation.NavGraphDefinition
import com.alpenraum.shimstack.ui.base.navigation.NavigationTarget
import com.alpenraum.shimstack.ui.bikeDetails.BikeDetailsScreen
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single(binds = [NavGraphDefinition::class])
class MainNavigationGraph : NavGraphDefinition<MainRoute> {
    override val startDestinationRoute: MainRoute = MainRoute.BottomNav()

    override fun build(
        navGraphBuilder: NavGraphBuilder,
        navController: NavController
    ) {
        createNavigation(navGraphBuilder, navController)
    }

    private fun createNavigation(
        navGraphBuilder: NavGraphBuilder,
        navController: NavController
    ) {
        navGraphBuilder.navigation<MainRoute.MainRouteRoot>(
            startDestination = startDestinationRoute
        ) {
            composable<MainRoute.BottomNav>(
                deepLinks =
                    listOf(
                        navDeepLink<MainRoute.BottomNav>("hahaha.at/lol")
                    )
            ) {
                val subTarget = it.toRoute<MainRoute.BottomNav>().subTarget
                BottomNavFeature(navController, subTarget)
            }

            composable<MainRoute.BikeDetails> {
                BikeDetailsScreen(navController)
            }
        }
    }
}

@Serializable
sealed class MainRoute : NavDestinationDefinition {
    @Serializable
    data object MainRouteRoot : MainRoute()

    @Serializable
    data class BottomNav(
        val subTarget: NavigationTarget? = null
    ) : MainRoute()

    @Serializable
    data class BikeDetails(
        val id: Int
    ) : MainRoute()
}