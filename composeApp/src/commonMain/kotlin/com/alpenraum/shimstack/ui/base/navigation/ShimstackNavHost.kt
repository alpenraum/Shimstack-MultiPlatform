package com.alpenraum.shimstack.ui.base.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.alpenraum.shimstack.base.di.KoinViewModel
import com.alpenraum.shimstack.ui.bottomnav.MainRoute
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

class NavViewModel(
    val startDestinationRoute: NavDestinationDefinition,
    val deeplinkManager: DeeplinkManager
) : KoinViewModel() {
    val destinationBuilders: List<NavGraphDefinition<NavDestinationDefinition>> = getKoin().getAll()
}

@Composable
fun ShimstackNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: NavViewModel = koinViewModel()
) {
    val destinationBuilders = viewModel.destinationBuilders
    NavHost(
        navController = navController,
        startDestination = viewModel.startDestinationRoute,
        modifier = modifier
    ) {
        destinationBuilders.forEach { navGraphDefinition ->
            navGraphDefinition.build(navGraphBuilder = this, navController)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.deeplinkManager.navigationAction.collectLatest {
            if (it == null) return@collectLatest
            navController.navigate(MainRoute.BottomNav(subTarget = it.name))
        }
    }
}