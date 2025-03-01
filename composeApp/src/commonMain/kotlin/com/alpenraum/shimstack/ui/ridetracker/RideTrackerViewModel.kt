package com.alpenraum.shimstack.ui.ridetracker

import androidx.navigation.NavController
import com.alpenraum.shimstack.base.BaseViewModel
import com.alpenraum.shimstack.base.DispatchersProvider
import com.alpenraum.shimstack.base.UnidirectionalViewModel
import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import com.alpenraum.shimstack.domain.ridetracker.RideTrackerRepository
import com.alpenraum.shimstack.ui.location.LocationPermissionManager
import com.alpenraum.shimstack.ui.location.LocationService
import com.alpenraum.shimstack.ui.location.model.AppPermissions
import com.alpenraum.shimstack.ui.location.model.PermissionState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.android.annotation.KoinViewModel

// TODO: VISUALISE RIDE DATA IF ACTIVE
@KoinViewModel
class RideTrackerViewModel(
    private val locationService: LocationService,
    private val locationPermissionManager: LocationPermissionManager,
    private val rideTrackerRepository: RideTrackerRepository,
    private val shimstackLogger: ShimstackLogger,
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
            RideTrackerContract.Intent.OnContinueExistingRide -> viewModelScope.launch { continueExistingRide() }
            RideTrackerContract.Intent.OnStartNewRide -> viewModelScope.launch { startNewRide() }
        }
    }

    private var permissionJob: Job? = null

    override fun onStart() {
        permissionJob =
            iOScope.launch {
                combine(
                    locationPermissionManager
                        .checkPermissionFlow(AppPermissions.LOCATION_FOREGROUND),
                    locationPermissionManager.checkPermissionFlow(AppPermissions.LOCATION_BACKGROUND),
                    locationPermissionManager.checkPermissionFlow(AppPermissions.LOCATION_SERVICE_ON),
                    locationPermissionManager.checkPermissionFlow(AppPermissions.SHOW_NOTIFICATIONS)
                ) { foreground, background, location, notification ->
                    listOf(foreground, background, location, notification)
                }.collectLatest {
                    updatePermissionState(it[0], it[1], it[2], it[3])
                }
            }

        iOScope.launch {
            if (locationService.isLocationServiceActive()) {
                triggerActiveRideCollection()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        permissionJob?.cancel()
    }

    private suspend fun triggerActiveRideCollection(activeRideId: Long? = null) {
        (activeRideId ?: rideTrackerRepository.getActiveRide()?.rideId)?.let {
            shimstackLogger.d("starting ride UI for id: $it")
            rideTrackerRepository
                .getRideFlow(it)
                .combine(rideTrackerRepository.getGpsPointsForRideFlow(it)) { ride, gpsPoints ->
                    ride to gpsPoints
                }.map { pair ->
                    pair.first?.let { ride ->
                        when {
                            ride.endTime == null ->
                                // TODO: PROPER FORMATTING
                                RideTrackerContract.State.ActiveRide(
                                    "${pair.second.lastOrNull()?.speed ?: 0} kmh",
                                    "${ride.totalDistance} m",
                                    "${ride.totalElevation} m",
                                    "${(Clock.System.now() - ride.startTime).inWholeSeconds}",
                                    pair.second
                                )

                            else -> RideTrackerContract.State.Default()
                        }
                    } ?: RideTrackerContract.State.Default()
                }.takeWhile { newState -> newState !is RideTrackerContract.State.Default }
                .onCompletion { _state.emit(RideTrackerContract.State.Default()) }
                .collectLatest { newState ->
                    _state.emit(newState)
                }
        }
    }

    private suspend fun updatePermissionState(
        foregroundPermissionState: PermissionState,
        backgroundPermissionState: PermissionState,
        locationServicePermissionState: PermissionState,
        notificationPermissionState: PermissionState
    ) {
        _state.emit(
            if (locationServicePermissionState.granted() &&
                foregroundPermissionState.granted() &&
                backgroundPermissionState.granted() &&
                notificationPermissionState.granted()
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
        )
    }

    private fun requestPermission(permission: AppPermissions) =
        viewModelScope.launch {
            val permissionState = locationPermissionManager.checkPermission(permission)
            when (permissionState) {
                PermissionState.NOT_DETERMINED ->
                    locationPermissionManager.requestPermission(
                        permission
                    )
                PermissionState.GRANTED -> {}
                PermissionState.DENIED -> locationPermissionManager.openSettingPage(permission)
            }
        }

    private fun startLocationTracking() =
        viewModelScope.launch {
            if (state.value is RideTrackerContract.State.Permissions
            ) {
                // TODO show error
                return@launch
            }
            if (!locationService.isLocationServiceActive()) {
                if (rideTrackerRepository.getActiveRide() != null) {
                    _event.emit(RideTrackerContract.Event.ShowContinueExistingRideDialog)
                } else {
                    startNewRide()
                }
            }
        }

    private suspend fun startNewRide() {
        val ride = rideTrackerRepository.createNewRide()

        ride.rideId?.let { locationService.startLocationService(it) }
        triggerActiveRideCollection(ride.rideId)
    }

    private suspend fun continueExistingRide() {
        val ride = rideTrackerRepository.getActiveRide()
        ride?.rideId?.let { locationService.startLocationService(it) }
        triggerActiveRideCollection(ride?.rideId)
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

        data class ActiveRide(
            val currentSpeed: String,
            val currentDistance: String,
            val currentElevationSum: String,
            val currentDuration: String,
            val gpsPoints: List<GpsPoint>
        ) : State()
    }

    data class Permission(
        val state: PermissionState,
        val permission: AppPermissions
    )

    sealed class Event {
        object ShowContinueExistingRideDialog : Event()
    }

    sealed interface Intent {
        object StartTracking : Intent

        class RequestPermission(
            val permission: AppPermissions
        ) : Intent

        object OnContinueExistingRide : Intent

        object OnStartNewRide : Intent
    }
}