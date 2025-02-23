package com.alpenraum.shimstack.ui.ridetracker

import androidx.navigation.NavController
import com.alpenraum.shimstack.base.BaseViewModel
import com.alpenraum.shimstack.base.DispatchersProvider
import com.alpenraum.shimstack.base.UnidirectionalViewModel
import com.alpenraum.shimstack.ui.location.LocationPermissionManager
import com.alpenraum.shimstack.ui.location.LocationService
import com.alpenraum.shimstack.ui.location.model.AppPermissions
import com.alpenraum.shimstack.ui.location.model.PermissionState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class RideTrackerViewModel(
    private val locationService: LocationService,
    private val locationPermissionManager: LocationPermissionManager,
    dispatchersProvider: DispatchersProvider
) : BaseViewModel(dispatchersProvider),
    RideTrackerContract {
    private val _event = MutableSharedFlow<RideTrackerContract.Event>()
    private val _state = MutableStateFlow<RideTrackerContract.State>(RideTrackerContract.State.Default())
    override val state: StateFlow<RideTrackerContract.State> = _state.asStateFlow()

    override val event: SharedFlow<RideTrackerContract.Event>
        get() = _event.asSharedFlow()

    override fun intent(
        intent: RideTrackerContract.Intent,
        navController: NavController
    ) {
        when (intent) {
            RideTrackerContract.Intent.StartTracking -> startLocationTracking()
            is RideTrackerContract.Intent.RequestPermission -> requestPermission(intent.permission)
        }
    }

    override fun onStart() {
        iOScope.launch {
            collectPermissionState(AppPermissions.LOCATION_FOREGROUND)
        }
        iOScope.launch {
            collectPermissionState(AppPermissions.LOCATION_BACKGROUND)
        }
        iOScope.launch {
            collectPermissionState(AppPermissions.LOCATION_SERVICE_ON)
        }
    }

    private suspend fun collectPermissionState(appPermissions: AppPermissions) =
        locationPermissionManager.checkPermissionFlow(appPermissions).collectLatest {
            updatePermissionState(it, appPermissions)
        }

    private inline fun updatePermissionState(
        permissionState: PermissionState,
        appPermissions: AppPermissions
    ) {
        val currentState = state.value as? RideTrackerContract.State.Permissions

        var foregroundPermissionState: PermissionState = currentState?.foregroundPermission?.state ?: PermissionState.GRANTED
        var backgroundPermissionState: PermissionState = currentState?.backgroundPermission?.state ?: PermissionState.GRANTED
        var locationServicePermissionState: PermissionState =
            currentState?.locationServicePermission?.state ?: PermissionState.GRANTED
        var notificationPermissionState: PermissionState =
            currentState?.locationServicePermission?.state ?: PermissionState.GRANTED

        when (appPermissions) {
            AppPermissions.LOCATION_SERVICE_ON -> {
                locationServicePermissionState = permissionState
            }

            AppPermissions.LOCATION_FOREGROUND -> {
                foregroundPermissionState = permissionState
            }

            AppPermissions.LOCATION_BACKGROUND -> {
                backgroundPermissionState = permissionState
            }

            AppPermissions.SHOW_NOTIFICATIONS -> notificationPermissionState = permissionState
        }

        _state.update {
            if (locationServicePermissionState.granted() &&
                foregroundPermissionState.granted() &&
                backgroundPermissionState.granted()
            ) {
                RideTrackerContract.State.Default()
            } else {
                RideTrackerContract.State.Permissions(
                    locationServicePermission =
                        RideTrackerContract.Permission(
                            locationServicePermissionState,
                            AppPermissions.LOCATION_SERVICE_ON
                        ),
                    foregroundPermission =
                        RideTrackerContract.Permission(
                            foregroundPermissionState,
                            AppPermissions.LOCATION_FOREGROUND
                        ),
                    backgroundPermission =
                        RideTrackerContract.Permission(
                            backgroundPermissionState,
                            AppPermissions.LOCATION_BACKGROUND
                        ),
                    notificationPermissionState =
                        RideTrackerContract.Permission(
                            notificationPermissionState,
                            AppPermissions.SHOW_NOTIFICATIONS
                        )
                )
            }
        }
    }

    private fun requestPermission(permission: AppPermissions) =
        viewModelScope.launch {
            val permissionState = locationPermissionManager.checkPermission(permission)
            when (permissionState) {
                PermissionState.NOT_DETERMINED -> locationPermissionManager.requestPermission(permission)
                PermissionState.GRANTED -> {}
                PermissionState.DENIED -> locationPermissionManager.openSettingPage(permission)
            }
        }

    // TODO - Explainer screen that shows missing permissions and updates them in real time using locationManager.checkPermissionFlow()
    private fun startLocationTracking() =
        viewModelScope.launch {
//            requestPermission(LocationPermission.LOCATION_FOREGROUND)
//            requestPermission(LocationPermission.LOCATION_BACKGROUND)
//            requestPermission(LocationPermission.LOCATION_SERVICE_ON)

            if (state.value is RideTrackerContract.State.Permissions
            ) {
                // TODO show error
                return@launch
            }
            if (!locationService.isLocationServiceActive()) {
                locationService.startLocationService()
            }
        }
}

interface RideTrackerContract :
    UnidirectionalViewModel<RideTrackerContract.State, RideTrackerContract.Intent, RideTrackerContract.Event> {
    sealed class State {
        data class Default(
            val x: String = "lol"
        ) : State()

        data class Permissions(
            val foregroundPermission: Permission,
            val backgroundPermission: Permission,
            val locationServicePermission: Permission,
            val notificationPermissionState: Permission
        ) : State() {
            fun asList() =
                persistentListOf(
                    locationServicePermission,
                    foregroundPermission,
                    backgroundPermission,
                    notificationPermissionState
                )
        }
    }

    data class Permission(
        val state: PermissionState,
        val permission: AppPermissions
    )

    sealed class Event

    sealed interface Intent {
        object StartTracking : Intent

        class RequestPermission(
            val permission: AppPermissions
        ) : Intent
    }
}