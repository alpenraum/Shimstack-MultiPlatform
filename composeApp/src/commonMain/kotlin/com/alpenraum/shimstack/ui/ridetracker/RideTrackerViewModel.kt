package com.alpenraum.shimstack.ui.ridetracker

import androidx.navigation.NavController
import com.alpenraum.shimstack.base.BaseViewModel
import com.alpenraum.shimstack.base.DispatchersProvider
import com.alpenraum.shimstack.base.UnidirectionalViewModel
import com.alpenraum.shimstack.ui.location.LocationPermissionManager
import com.alpenraum.shimstack.ui.location.LocationService
import com.alpenraum.shimstack.ui.location.model.LocationPermission
import com.alpenraum.shimstack.ui.location.model.PermissionState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOf
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
    override val state: StateFlow<RideTrackerContract.State> =
        flowOf(getInitialState()).inViewModelScope(RideTrackerContract.State.Default())

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

    private fun getInitialState(): RideTrackerContract.State {
        val permissions = getPermissions()
        val isNotGranted = permissions.any { it.state != PermissionState.GRANTED }

        return if (isNotGranted) {
            RideTrackerContract.State.Permissions(permissions)
        } else {
            RideTrackerContract.State.Default()
        }
    }

    private fun requestPermission(permission: LocationPermission) =
        viewModelScope.launch {
            val permissionState = locationPermissionManager.checkPermission(permission)
            when (permissionState) {
                PermissionState.NOT_DETERMINED -> locationPermissionManager.requestPermission(permission)
                PermissionState.GRANTED -> {}
                PermissionState.DENIED -> locationPermissionManager.openSettingPage(permission)
            }
        }

    private fun getPermissions(): ImmutableList<RideTrackerContract.Permission> {
        val foregroundPermissionState = locationPermissionManager.checkPermission(LocationPermission.LOCATION_FOREGROUND)
        val backgroundPermissionState = locationPermissionManager.checkPermission(LocationPermission.LOCATION_BACKGROUND)
        val locationServicePermissionState = locationPermissionManager.checkPermission(LocationPermission.LOCATION_SERVICE_ON)

        return buildList {
            add(RideTrackerContract.Permission(locationServicePermissionState, LocationPermission.LOCATION_SERVICE_ON))
            add(RideTrackerContract.Permission(foregroundPermissionState, LocationPermission.LOCATION_FOREGROUND))
            add(RideTrackerContract.Permission(backgroundPermissionState, LocationPermission.LOCATION_BACKGROUND))
        }.toImmutableList()
    }

    // TODO - Explainer screen that shows missing permissions and updates them in real time using locationManager.checkPermissionFlow()
    private fun startLocationTracking() =
        viewModelScope.launch {
            requestPermission(LocationPermission.LOCATION_FOREGROUND)
            requestPermission(LocationPermission.LOCATION_BACKGROUND)
            requestPermission(LocationPermission.LOCATION_SERVICE_ON)

            if (getPermissions().any {
                    it.state !=
                        PermissionState.GRANTED
                }
            ) {
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
            val permissions: ImmutableList<Permission>
        ) : State()
    }

    data class Permission(
        val state: PermissionState,
        val permission: LocationPermission
    )

    sealed class Event

    sealed interface Intent {
        object StartTracking : Intent

        class RequestPermission(
            val permission: LocationPermission
        ) : Intent
    }
}