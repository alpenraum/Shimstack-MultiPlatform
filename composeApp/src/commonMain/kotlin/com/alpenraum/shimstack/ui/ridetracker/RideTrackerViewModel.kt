package com.alpenraum.shimstack.ui.ridetracker

import androidx.navigation.NavController
import com.alpenraum.shimstack.base.BaseViewModel
import com.alpenraum.shimstack.base.DispatchersProvider
import com.alpenraum.shimstack.base.UnidirectionalViewModel
import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.data.formatted
import com.alpenraum.shimstack.data.kmToMiles
import com.alpenraum.shimstack.data.kphToMph
import com.alpenraum.shimstack.data.mToFeet
import com.alpenraum.shimstack.data.roundToUiFormat
import com.alpenraum.shimstack.data.toDate
import com.alpenraum.shimstack.domain.model.measurementunit.MeasurementUnitType
import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import com.alpenraum.shimstack.domain.ridetracker.RideTrackerRepository
import com.alpenraum.shimstack.domain.ridetracker.RideTrackerService
import com.alpenraum.shimstack.domain.userSettings.GetUserSettingsUseCase
import com.alpenraum.shimstack.ui.bikeDetails.getLargeDistanceStringRes
import com.alpenraum.shimstack.ui.bikeDetails.getMediumDistanceStringRes
import com.alpenraum.shimstack.ui.bikeDetails.getSpeedStringRes
import com.alpenraum.shimstack.ui.location.LocationPermissionManager
import com.alpenraum.shimstack.ui.location.LocationService
import com.alpenraum.shimstack.ui.location.model.AppPermissions
import com.alpenraum.shimstack.ui.location.model.PermissionState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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
import org.jetbrains.compose.resources.getString
import org.koin.android.annotation.KoinViewModel

// TODO: VISUALISE RIDE DATA IF ACTIVE
@KoinViewModel
class RideTrackerViewModel(
    private val locationService: LocationService,
    private val locationPermissionManager: LocationPermissionManager,
    private val rideTrackerRepository: RideTrackerRepository,
    private val shimstackLogger: ShimstackLogger,
    private val rideTrackerService: RideTrackerService,
    private val userSettingsUseCase: GetUserSettingsUseCase,
    dispatchersProvider: DispatchersProvider
) : BaseViewModel(dispatchersProvider),
    RideTrackerContract {
    private var measurementUnitType: MeasurementUnitType = MeasurementUnitType.METRIC

    init {
        iOScope.launch {
            userSettingsUseCase().collectLatest {
                measurementUnitType = it.measurementUnitType
            }
        }
    }

    private val _event = MutableSharedFlow<RideTrackerContract.Event>()
    private val _state = MutableStateFlow<RideTrackerContract.State>(RideTrackerContract.State.Default(persistentListOf()))
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
            RideTrackerContract.Intent.OnStopRideClick -> finishRide()
        }
    }

    private var backgroundWorkJob: Job? = null

    override fun onStart() {
        backgroundWorkJob =
            iOScope.launch {
                shimstackLogger.d("starting permission job")
                if (locationService.isLocationServiceActive()) {
                    triggerActiveRideCollectionFromCache()
                } else {
                    combine(
                        locationPermissionManager
                            .checkPermissionFlow(AppPermissions.LOCATION_FOREGROUND),
                        locationPermissionManager.checkPermissionFlow(AppPermissions.LOCATION_BACKGROUND),
                        locationPermissionManager.checkPermissionFlow(AppPermissions.LOCATION_SERVICE_ON),
                        locationPermissionManager.checkPermissionFlow(AppPermissions.SHOW_NOTIFICATIONS)
                    ) { foreground, background, location, notification ->
                        shimstackLogger.d("getting update from locationPermissionManager!")
                        listOf(foreground, background, location, notification)
                    }.collectLatest {
                        updatePermissionState(it[0], it[1], it[2], it[3])
                    }
                }
            }
    }

    override fun onStop() {
        super.onStop()
        backgroundWorkJob?.cancel()
    }

    private fun finishRide() =
        iOScope.launch {
            rideTrackerService.finishRide()
            locationService.stopLocationService()
            emitDefaultState()
        }

    private suspend fun triggerActiveRideCollectionFromCache() {
        rideTrackerService
            .getRideDataFlow()
            .map {
                when {
                    it.ride.endTime == null ->
                        RideTrackerContract.State.ActiveRide(
                            formatSpeed(it.gpsPoints.lastOrNull()?.speedInKph ?: 0f),
                            formatDistance(it.totalDistance),
                            formatElevation(it.totalElevation),
                            (Clock.System.now() - it.ride.startTime).formatted(),
                            it.gpsPoints.toImmutableList()
                        )

                    else -> getDefaultState()
                }
            }.takeWhile { newState -> newState !is RideTrackerContract.State.Default }
            .onCompletion {
                shimstackLogger.d("on completion called!")
                _state.emit(getDefaultState())
            }.collectLatest { newState ->
                shimstackLogger.d("emitting new state: $newState")
                _state.emit(newState)
            }
    }

    @Deprecated("triggerActiveRideCollectionFromCache works better")
    private suspend fun triggerActiveRideCollectionFromDb(activeRideId: Long? = null) {
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

                                RideTrackerContract.State.ActiveRide(
                                    formatSpeed(pair.second.lastOrNull()?.speedInKph ?: 0f),
                                    formatDistance(ride.totalDistance),
                                    formatElevation(ride.totalElevation),
                                    (Clock.System.now() - ride.startTime).formatted(),
                                    pair.second.toImmutableList()
                                )

                            else -> getDefaultState()
                        }
                    } ?: getDefaultState()
                }.takeWhile { newState -> newState !is RideTrackerContract.State.Default }
                .onCompletion { emitDefaultState() }
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
                getDefaultState()
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

    private suspend fun getDefaultState() =
        RideTrackerContract.State.Default(
            rideTrackerRepository
                .getAllRides()
                .map {
                    val date = it.startTime.toDate()
                    val duration = it.endTime?.let { it1 -> (it1 - it.startTime).formatted() } ?: "-"
                    val distance = formatDistance(it.totalDistance)
                    val elevationSum = formatElevation(it.totalElevation)
                    val averageSpeed = formatSpeed(it.averageSpeed)
                    val topSpeed = formatSpeed(it.topSpeed)
                    RideView(date, duration, distance, elevationSum, averageSpeed, topSpeed)
                }.toImmutableList()
        )

    private suspend fun emitDefaultState() {
        val rides = getDefaultState()
        _state.emit(rides)
    }

    private suspend fun startNewRide() {
        val ride = rideTrackerRepository.createNewRide()

        ride.rideId?.let {
            rideTrackerService.ride = ride
            locationService.startLocationService(it)
        }
        triggerActiveRideCollectionFromCache()
    }

    private suspend fun continueExistingRide() {
        val ride = rideTrackerRepository.getActiveRide()

        ride?.let {
            rideTrackerService.ride = it
            locationService.startLocationService(it.rideId!!)
            triggerActiveRideCollectionFromCache()
        }
    }

    private suspend fun formatDistance(distance: Float): String {
        val amount = (if (measurementUnitType.isMetric()) distance else distance.kmToMiles()).roundToUiFormat()
        return "$amount ${getString(measurementUnitType.getLargeDistanceStringRes())}"
    }

    private suspend fun formatElevation(elevation: Float): String {
        val amount = (if (measurementUnitType.isMetric()) elevation else elevation.mToFeet()).roundToUiFormat()
        return "$amount ${getString(measurementUnitType.getMediumDistanceStringRes())}"
    }

    private suspend fun formatSpeed(speed: Float): String {
        val amount = (if (measurementUnitType.isMetric()) speed else speed.kphToMph()).roundToUiFormat()
        return "$amount ${getString(measurementUnitType.getSpeedStringRes())}"
    }
}

interface RideTrackerContract :
    UnidirectionalViewModel<RideTrackerContract.State, RideTrackerContract.Intent, RideTrackerContract.Event> {
    sealed class State {
        data class Default(
            val rides: ImmutableList<RideView>
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
            val gpsPoints: ImmutableList<GpsPoint>
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

        object OnStopRideClick : Intent
    }
}