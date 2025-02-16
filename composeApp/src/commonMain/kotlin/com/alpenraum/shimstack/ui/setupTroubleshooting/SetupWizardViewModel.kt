package com.alpenraum.shimstack.ui.setupTroubleshooting

import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavController
import androidx.sqlite.SQLiteException
import com.alpenraum.shimstack.base.BaseViewModel
import com.alpenraum.shimstack.base.DispatchersProvider
import com.alpenraum.shimstack.base.UnidirectionalViewModel
import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.domain.SetupRecommendationRepository
import com.alpenraum.shimstack.domain.bikeservice.BikeRepository
import com.alpenraum.shimstack.domain.model.bike.Bike
import com.alpenraum.shimstack.domain.model.measurementunit.MeasurementUnitType
import com.alpenraum.shimstack.domain.model.measurementunit.Pressure
import com.alpenraum.shimstack.domain.setupwizard.GetSetupSolutionUseCase
import com.alpenraum.shimstack.domain.setupwizard.NoFittingSolutionException
import com.alpenraum.shimstack.domain.setupwizard.SetupRecommendation
import com.alpenraum.shimstack.domain.setupwizard.SetupSymptom
import com.alpenraum.shimstack.domain.userSettings.GetUserSettingsUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SetupWizardViewModel(
    private val bikeRepository: BikeRepository,
    private val setupRecommendationRepository: SetupRecommendationRepository,
    private val setupRecommendationViewMapper: SetupRecommendationViewMapper,
    private val setupSymptomViewMapper: SetupSymptomViewMapper,
    private val getSetupSolutionUseCase: GetSetupSolutionUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val shimstackLogger: ShimstackLogger,
    dispatchersProvider: DispatchersProvider
) : BaseViewModel(dispatchersProvider),
    SetupWizardContract {
    private val bikes = MutableStateFlow<ImmutableList<Bike>>(persistentListOf())
    private var selectedBike: MutableStateFlow<Bike?> = MutableStateFlow(null)
    private var setupRecommendation: SetupRecommendation? = null

    fun initVm() {
        fetchBikes()
            .map { it.toImmutableList() }
            .onEach {
                bikes.emit(it)
                selectedBike.emit(it.getOrNull(0))
                emitDefaultState()
            }.launchIn(iOScope)
    }

    private fun fetchBikes() = bikeRepository.getAllBikes()

    private val _event = MutableSharedFlow<SetupWizardContract.Event>()

    private val _state: MutableStateFlow<SetupWizardContract.State> =
        MutableStateFlow(SetupWizardContract.State.Initializing)

    override val state: StateFlow<SetupWizardContract.State> = _state.asStateFlow()

    override val event: SharedFlow<SetupWizardContract.Event>
        get() = _event.asSharedFlow()

    override fun intent(
        intent: SetupWizardContract.Intent,
        navController: NavController
    ) {
        when (intent) {
            is SetupWizardContract.Intent.OnSelectedBikeChanged -> {
                updateSelectedBike(intent.index)
            }

            SetupWizardContract.Intent.OnSeePreviousRecommendationsClick -> {} // TODO: IMPLEMENT THIS FEATURE
            SetupWizardContract.Intent.OnStartTroubleshootClick -> emitSelectSymptomState()
            is SetupWizardContract.Intent.OnSymptomSelected -> toggleSelectedSymptom(intent.symptom)
            SetupWizardContract.Intent.OnBottomSheetDismissed ->
                viewModelScope.launch {
                    emitDefaultState()
                }

            SetupWizardContract.Intent.OnRecommendationAccepted -> onRecommendationAccepted()

            SetupWizardContract.Intent.OnRecommendationDeclined -> onRecommendationDeclined()
            is SetupWizardContract.Intent.OnSymptomConfirmed -> processSetupSymptom(intent.isFront, intent.isHighSpeed)
            is SetupWizardContract.Intent.OnConfirmUpdatedSuspensionPressure ->
                onConfirmSuspensionPressure(
                    intent.front,
                    intent.rear
                )

            SetupWizardContract.Intent.OnDismissError -> viewModelScope.launch { emitDefaultState() }
        }
    }

    private fun onRecommendationDeclined() =
        iOScope.launch {
            setupRecommendation?.let {
                try {
                    setupRecommendationRepository.updateAcceptanceState(it.id ?: -1, false)
                    emitDefaultState()
                } catch (e: SQLiteException) {
                    shimstackLogger.e("error during updating bike based on recommendation", e)
                    _event.emit(SetupWizardContract.Event.Error)
                }
            }
        }

    private fun updateSelectedBike(bikeId: Int) =
        viewModelScope.launch {
            bikes.value.getOrNull(bikeId)?.let {
                selectedBike.emit(it)
                emitDefaultState()
            }
        }

    private fun onRecommendationAccepted() =
        iOScope.launch {
            setupRecommendation?.let {
                try {
                    setupRecommendationRepository.updateAcceptanceState(it.id ?: -1, true)

                    if (it.rearSagDelta != null || it.frontSagDelta != null) {
                        emitUpdateSuspensionPressure(it.frontSagDelta != null, it.rearSagDelta != null)
                    } else {
                        selectedBike.value?.let { bike ->
                            shimstackLogger.d("oldBike: $bike")
                            val newBike = bike.copyWithSetupRecommendation(it)
                            shimstackLogger.d("newBike: $newBike")
                            bikeRepository.updateBike(newBike)

                            emitSuccessState()
                            delay(3000L)
                        }
                        emitDefaultState()
                    }
                } catch (e: SQLiteException) {
                    shimstackLogger.e("error during updating bike based on recommendation", e)
                    _event.emit(SetupWizardContract.Event.Error)
                }
            }
        }

    private fun onConfirmSuspensionPressure(
        front: TextFieldValue,
        rear: TextFieldValue
    ) = iOScope.launch {
        setupRecommendation?.let {
            selectedBike.value?.let { bike ->
                val measurementUnit = getUserSettingsUseCase().first().measurementUnitType
                val frontPressure = convertStringToPressure(front.text, measurementUnit)
                val rearPressure = convertStringToPressure(rear.text, measurementUnit)

                val frontSuspension = frontPressure?.let { bike.frontSuspension?.copy(pressure = frontPressure) }
                val rearSuspension = rearPressure?.let { bike.rearSuspension?.copy(pressure = rearPressure) }

                val bikeWithNewPressure = bike.copy(frontSuspension = frontSuspension, rearSuspension = rearSuspension)
                bikeRepository.updateBike(bikeWithNewPressure.copyWithSetupRecommendation(it))

                emitSuccessState()
                delay(3000L)
            } ?: run { _event.emit(SetupWizardContract.Event.Error) }
        } ?: run { _event.emit(SetupWizardContract.Event.Error) }

        emitDefaultState()
    }

    private fun convertStringToPressure(
        value: String,
        measurementUnitType: MeasurementUnitType
    ): Pressure? {
        val doubleValue = value.replace(",", ".").toDoubleOrNull() ?: return null
        return if (measurementUnitType.isMetric()) Pressure(doubleValue) else Pressure.fromImperial(doubleValue)
    }

    private fun emitSuccessState() =
        viewModelScope.launch {
            _state.emit(SetupWizardContract.State.Success(bikes.value))
        }

    private suspend fun emitUpdateSuspensionPressure(
        showFront: Boolean,
        showRear: Boolean
    ) {
        _state.emit(
            SetupWizardContract.State.UpdateSuspensionPressure(
                showFront,
                showRear,
                bikes = bikes.value
            )
        )
    }

    private fun toggleSelectedSymptom(symptom: SetupSymptom) {
        (_state.value as? SetupWizardContract.State.SelectSymptom)?.let {
            viewModelScope.launch {
                _state.emit(
                    SetupWizardContract.State.SelectSymptom(
                        it.symptoms
                            .map {
                                if (it.setupSymptom == symptom) {
                                    it.copy(
                                        selected = !it.selected
                                    )
                                } else {
                                    if (it.selected) {
                                        it.copy(selected = false)
                                    } else {
                                        it
                                    }
                                }
                            }.toImmutableList(),
                        it.bikes
                    )
                )
            }
        }
    }

    private fun processSetupSymptom(
        isFront: Boolean,
        isHighSpeed: Boolean
    ) = iOScope.launch {
        _event.emit(SetupWizardContract.Event.HideSymptomBottomSheet)

        val selectedSymptom =
            (_state.value as? SetupWizardContract.State.SelectSymptom)?.symptoms?.firstOrNull { it.selected } ?: return@launch
        val bike = selectedBike.value ?: return@launch
        val front = if (selectedSymptom.setupSymptom.requiresLocation) isFront else false
        val highSpeed = if (selectedSymptom.setupSymptom.requiresSpeed) isHighSpeed else false

        try {
            getSetupSolutionUseCase(selectedSymptom.setupSymptom, bike, front, highSpeed)
            emitDefaultState()
        } catch (e: NoFittingSolutionException) {
            _state.emit(SetupWizardContract.State.NoFittingSolution(bikes.value))
        }
    }

    private fun emitSelectSymptomState() =
        viewModelScope.launch {
            SetupSymptom.entries.map { setupSymptomViewMapper.map(it) }.also {
                _state.emit(SetupWizardContract.State.SelectSymptom(it.toImmutableList(), bikes.value))
            }
            _event.emit(SetupWizardContract.Event.ShowSymptomBottomSheet)
        }

    private suspend fun emitDefaultState() {
        val selectedBike = selectedBike.value ?: return
        setupRecommendationRepository
            .getSetupRecommendations(selectedBike.id ?: -1)
            .first()
            .filter {
                it.isAccepted == null
            }.also {
                setupRecommendation = it.firstOrNull()
            }
        _state.emit(
            if (setupRecommendation != null) {
                SetupWizardContract.State.Recommendation(
                    setupRecommendationViewMapper.map(setupRecommendation!!).toImmutableList(),
                    bikes.value
                )
            } else {
                SetupWizardContract.State.Start(bikes.value)
            }
        )
    }
}

interface SetupWizardContract :
    UnidirectionalViewModel<SetupWizardContract.State, SetupWizardContract.Intent, SetupWizardContract.Event> {
    sealed class State(
        val bikes: ImmutableList<Bike?> = persistentListOf()
    ) {
        object Initializing : State()

        class Start(
            bikes: ImmutableList<Bike?>
        ) : State(bikes)

        class SelectSymptom(
            val symptoms: ImmutableList<SetupSymptomView>,
            bikes: ImmutableList<Bike?>
        ) : State(bikes)

        class Recommendation(
            val setupRecommendation: ImmutableList<SetupRecommendationView>,
            bikes: ImmutableList<Bike?>
        ) : State(bikes)

        class Success(
            bikes: ImmutableList<Bike?>
        ) : State(bikes)

        class NoFittingSolution(
            bikes: ImmutableList<Bike?>
        ) : State(bikes)

        class UpdateSuspensionPressure(
            val showFront: Boolean,
            val showRear: Boolean,
            bikes: ImmutableList<Bike?>
        ) : State(bikes)
    }

    data class SetupRecommendationView(
        val value: Double,
        val target1: StringResource,
        val target2: StringResource,
        val sentence: StringResource,
        val unit: StringResource?
    )

    sealed class Event {
        object Error : Event()

        object ShowSymptomBottomSheet : Event()

        object HideSymptomBottomSheet : Event()
    }

    sealed class Intent {
        class OnSelectedBikeChanged(
            val index: Int
        ) : Intent()

        object OnStartTroubleshootClick : Intent()

        object OnSeePreviousRecommendationsClick : Intent()

        class OnSymptomSelected(
            val symptom: SetupSymptom
        ) : Intent()

        class OnSymptomConfirmed(
            val isFront: Boolean,
            val isHighSpeed: Boolean
        ) : Intent()

        object OnBottomSheetDismissed : Intent()

        object OnRecommendationAccepted : Intent()

        object OnRecommendationDeclined : Intent()

        class OnConfirmUpdatedSuspensionPressure(
            val front: TextFieldValue,
            val rear: TextFieldValue
        ) : Intent()

        object OnDismissError : Intent()
    }
}