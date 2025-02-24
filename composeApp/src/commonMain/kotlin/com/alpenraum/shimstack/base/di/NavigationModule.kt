package com.alpenraum.shimstack.base.di

import com.alpenraum.shimstack.data.datastore.ShimstackDatastore
import com.alpenraum.shimstack.ui.base.navigation.NavDestinationDefinition
import com.alpenraum.shimstack.ui.base.navigation.NavViewModel
import com.alpenraum.shimstack.ui.bottomnav.MainRoute
import com.alpenraum.shimstack.ui.onboarding.navigation.OnboardingRoute
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

fun navigationModule() =
    module {
        single(named("startDestinationRoute")) {
            runBlocking {
                val dataStore = get<ShimstackDatastore>()
                return@runBlocking if (dataStore.isOnboardingCompleted.firstOrNull() == true) {
                    MainRoute.MainRouteRoot
                } else {
                    OnboardingRoute.OnboardingRouteRoot
                }
            }
        } bind NavDestinationDefinition::class

        viewModel {
            NavViewModel(get(named("startDestinationRoute")), get())
        }
    }