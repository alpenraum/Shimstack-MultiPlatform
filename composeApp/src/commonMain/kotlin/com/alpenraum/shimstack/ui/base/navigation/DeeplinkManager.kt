package com.alpenraum.shimstack.ui.base.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Single

@Single
class DeeplinkManager {
    private val _navigationAction = MutableSharedFlow<NavigationTarget?>()

    val navigationAction = _navigationAction.asSharedFlow()

    suspend fun onNewNavigationAction(target: NavigationTarget) {
        _navigationAction.emit(target)
    }

    companion object {
        const val NAV_ARG = "nav_arg"
    }
}

enum class NavigationTarget {
    RIDE_TRACKER;

    companion object {
        fun valueOfIgnoreCase(value: String): NavigationTarget? = entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}