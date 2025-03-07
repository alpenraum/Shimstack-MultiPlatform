@file:OptIn(ExperimentalMaterial3Api::class)

package com.alpenraum.shimstack.ui.bikeDetails

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alpenraum.shimstack.base.use
import com.alpenraum.shimstack.domain.bikeservice.ValidateBikeUseCase
import com.alpenraum.shimstack.domain.model.bike.Bike
import com.alpenraum.shimstack.domain.model.bike.BikeType
import com.alpenraum.shimstack.domain.model.measurementunit.Distance
import com.alpenraum.shimstack.domain.model.measurementunit.MeasurementUnitType
import com.alpenraum.shimstack.domain.model.measurementunit.Pressure
import com.alpenraum.shimstack.domain.model.suspension.Damping
import com.alpenraum.shimstack.domain.model.suspension.Suspension
import com.alpenraum.shimstack.ui.base.compose.ClassKeyedCrossfade
import com.alpenraum.shimstack.ui.base.compose.components.AttachToLifeCycle
import com.alpenraum.shimstack.ui.base.compose.components.BikeCard
import com.alpenraum.shimstack.ui.base.compose.components.BikeCardContent
import com.alpenraum.shimstack.ui.base.compose.components.ButtonText
import com.alpenraum.shimstack.ui.base.compose.components.InfoText
import com.alpenraum.shimstack.ui.base.compose.components.ShimstackCard
import com.alpenraum.shimstack.ui.base.compose.components.TextInput
import com.alpenraum.shimstack.ui.base.compose.number
import com.alpenraum.shimstack.ui.base.compose.theme.AppTheme
import com.alpenraum.shimstack.ui.bikeDetails.BikeDetailsContract.State.Edit
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.btn_close
import shimstackmultiplatform.composeapp.generated.resources.comp
import shimstackmultiplatform.composeapp.generated.resources.ic_save
import shimstackmultiplatform.composeapp.generated.resources.label_edit
import shimstackmultiplatform.composeapp.generated.resources.label_front_suspension
import shimstackmultiplatform.composeapp.generated.resources.label_front_tire
import shimstackmultiplatform.composeapp.generated.resources.label_internal_rim_width
import shimstackmultiplatform.composeapp.generated.resources.label_name
import shimstackmultiplatform.composeapp.generated.resources.label_rear_suspension
import shimstackmultiplatform.composeapp.generated.resources.label_rear_tire
import shimstackmultiplatform.composeapp.generated.resources.label_save
import shimstackmultiplatform.composeapp.generated.resources.label_tire_pressure
import shimstackmultiplatform.composeapp.generated.resources.label_tire_width
import shimstackmultiplatform.composeapp.generated.resources.label_travel
import shimstackmultiplatform.composeapp.generated.resources.label_type
import shimstackmultiplatform.composeapp.generated.resources.mm
import shimstackmultiplatform.composeapp.generated.resources.pressure
import shimstackmultiplatform.composeapp.generated.resources.rebound
import shimstackmultiplatform.composeapp.generated.resources.tokens

@Composable
fun BikeDetailsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BikeDetailsViewModel = koinViewModel()
) {
    AttachToLifeCycle(viewModel = viewModel)
    val (state, intent, event) = use(viewModel, navController)

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        event.collectLatest {
            when (it) {
                is BikeDetailsContract.Event.ShowSnackbar ->
                    snackbarHostState.showSnackbar(getString(it.message))
            }
        }
    }
    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Content(state = state, intents = intent, modifier = Modifier.padding(it))
    }
}

@Composable
private fun Content(
    state: BikeDetailsContract.State,
    modifier: Modifier = Modifier,
    intents: (BikeDetailsContract.Intent) -> Unit
) {
    Box(modifier.padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)) {
        IconButton(
            onClick = { intents(BikeDetailsContract.Intent.OnBackPressed) },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.btn_close))
        }
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BikeCard(showPlaceholder = false, modifier = Modifier.size(200.dp)) {
                BikeCardContent(state.bike, modifier = Modifier.padding(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
            BikeInfo(state = state, intents, Modifier)
        }
        AnimatedVisibility(
            visible = state is Edit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ExtendedFloatingActionButton(
                onClick = {
                    if ((state as? Edit)?.validationFailure == null) {
                        intents(BikeDetailsContract.Intent.OnSaveClicked)
                    }
                },
                containerColor =
                    if ((state as? Edit)?.validationFailure == null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_save),
                        contentDescription = null
                    )
                },
                text = {
                    ButtonText(textRes = Res.string.label_save)
                }
            )
        }
    }
}

@Composable
fun BikeInfo(
    state: BikeDetailsContract.State,
    intents: (BikeDetailsContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
    ) {
        ClassKeyedCrossfade(targetState = state, label = "") {
            if (it is Edit) {
                EditBikeHeading(state = state as Edit, intents = intents)
            } else {
                BikeHeading(state.bike.name, state.bike.type, intents)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            FrontTireBlock(state = state, intents = intents)
            RearTireBlock(state = state, intents = intents)
            FrontSuspensionBlock(state = state, intents)
            RearSuspensionBlock(state = state, intents)
            if (state is Edit) {
                Spacer(Modifier.height(64.dp))
            }
        }
    }
}

@Composable
private fun BikeHeading(
    bikeName: String,
    bikeType: BikeType,
    intents: (BikeDetailsContract.Intent) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                bikeName,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1.0f)
            )
            IconButton(onClick = { intents(BikeDetailsContract.Intent.OnEditClicked) }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(Res.string.label_edit)
                )
            }
        }
        InfoText(bikeType.labelRes)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBikeHeading(
    state: Edit,
    intents: (BikeDetailsContract.Intent) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextInput(
                value = state.bikeName,
                onValueChange = {
                    intents(
                        BikeDetailsContract.Intent.Input.BikeName(it)
                    )
                },
                modifier = Modifier.weight(1.0f),
                label = stringResource(Res.string.label_name)
            )
        }
        Spacer(Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            },
            modifier = Modifier
        ) {
            TextInput(
                readOnly = true,
                value = stringResource(state.bikeType.labelRes),
                onValueChange = {},
                label = stringResource(Res.string.label_type),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                isError = state.validationFailure?.type == false
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = {
                expanded = false
            }) {
                BikeType.entries.forEach { selectionOption ->
                    if (selectionOption != BikeType.UNKNOWN) {
                        DropdownMenuItem(text = {
                            Text(text = stringResource(selectionOption.labelRes))
                        }, onClick = {
                            intents(BikeDetailsContract.Intent.Input.BikeType(selectionOption))
                            expanded = false
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun FrontTireBlock(
    state: BikeDetailsContract.State,
    intents: (BikeDetailsContract.Intent) -> Unit
) {
    TireBlock(
        state = state,
        label = Res.string.label_front_tire,
        intents = intents,
        editMode = state is Edit,
        isFront = true,
        showError = (state as? Edit)?.validationFailure?.frontTire == false,
        measurementUnitType = state.measurementUnitType
    )
}

@Composable
private fun RearTireBlock(
    state: BikeDetailsContract.State,
    intents: (BikeDetailsContract.Intent) -> Unit
) {
    TireBlock(
        state = state,
        label = Res.string.label_rear_tire,
        intents = intents,
        editMode = state is Edit,
        isFront = false,
        showError = (state as? Edit)?.validationFailure?.rearTire == false,
        measurementUnitType = state.measurementUnitType
    )
}

@Composable
private fun TireBlock(
    state: BikeDetailsContract.State,
    label: StringResource,
    intents: (BikeDetailsContract.Intent) -> Unit,
    editMode: Boolean,
    isFront: Boolean,
    showError: Boolean,
    measurementUnitType: MeasurementUnitType
) {
    AnimatedContent(targetState = editMode, label = "") { edit ->
        val content: @Composable () -> Unit =
            if (edit) {
                val editState = state as Edit
                {
                    Column(Modifier.padding(8.dp)) {
                        InfoText(label)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.weight(1.0f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextInput(
                                value = if (isFront) editState.frontTirePressure else editState.rearTirePressure,
                                onValueChange = {
                                    intents(
                                        if (isFront) {
                                            BikeDetailsContract.Intent.Input.FrontTirePressure(
                                                it
                                            )
                                        } else {
                                            BikeDetailsContract.Intent.Input.RearTirePressure(it)
                                        }
                                    )
                                },
                                label =
                                    stringResource(
                                        Res.string.label_tire_pressure
                                    ),
                                suffix = measurementUnitType.getPressureLabel(),
                                modifier = Modifier.weight(1.0f),
                                keyboardOptions = KeyboardOptions.number(ImeAction.Next),
                                isError = showError
                            )

                            TextInput(
                                value = if (isFront) editState.frontTireWidth else editState.rearTireWidth,
                                onValueChange = {
                                    intents(
                                        if (isFront) {
                                            BikeDetailsContract.Intent.Input.FrontTireWidth(
                                                it
                                            )
                                        } else {
                                            BikeDetailsContract.Intent.Input.RearTireWidth(it)
                                        }
                                    )
                                },
                                label =
                                    stringResource(
                                        Res.string.label_tire_width
                                    ),
                                suffix = measurementUnitType.getSmallDistanceLabel(),
                                modifier = Modifier.weight(1.0f),
                                keyboardOptions = KeyboardOptions.number(ImeAction.Next),
                                isError = showError
                            )

                            TextInput(
                                value = (if (isFront) editState.frontInternalRimWidth else editState.rearInternalRimWidth) ?: "",
                                onValueChange = {
                                    intents(
                                        if (isFront) {
                                            BikeDetailsContract.Intent.Input.FrontTireInternalRimWidth(
                                                it
                                            )
                                        } else {
                                            BikeDetailsContract.Intent.Input.RearTireInternalRimWidth(
                                                it
                                            )
                                        }
                                    )
                                },
                                label =
                                    stringResource(
                                        Res.string.label_internal_rim_width
                                    ),
                                suffix = stringResource(Res.string.mm),
                                modifier = Modifier.weight(1.0f),
                                keyboardOptions = KeyboardOptions.number(ImeAction.Done),
                                isError = showError
                            )
                        }
                    }
                }
            } else {
                val tire = if (isFront) state.bike.frontTire else state.bike.rearTire
                {
                    Column(Modifier.padding(8.dp)) {
                        InfoText(label)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.weight(1.0f)) {
                            // rear tire
                            TextPair(
                                Res.string.label_tire_pressure,
                                tire.getFormattedPressure(measurementUnitType.isMetric()),
                                modifier = Modifier.weight(1.0f)
                            )
                            TextPair(
                                Res.string.label_tire_width,
                                tire.getFormattedTireWidth(measurementUnitType.isMetric()),
                                modifier = Modifier.weight(1.0f)
                            )
                            TextPair(
                                Res.string.label_internal_rim_width,
                                tire.getFormattedInternalRimWidth(),
                                modifier = Modifier.weight(1.0f)
                            )
                        }
                    }
                }
            }

        ShimstackCard(
            Modifier
                .height(IntrinsicSize.Min)
                .padding(top = 16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun FrontSuspensionBlock(
    state: BikeDetailsContract.State,
    intents: (BikeDetailsContract.Intent) -> Unit
) {
    state.bike.frontSuspension?.let { suspension ->
        ShimstackCard(
            Modifier
                .height(IntrinsicSize.Min)
                .padding(top = 16.dp)
        ) {
            Column(Modifier.padding(8.dp)) {
                SuspensionBlock(
                    label = Res.string.label_front_suspension,
                    state = state,
                    editMode = state is Edit,
                    intents = intents,
                    isFront = true,
                    measurementUnitType = state.measurementUnitType,
                    showError = (state as? Edit)?.validationFailure?.frontSuspension == false
                )
            }
        }
    }
}

@Composable
private fun RearSuspensionBlock(
    state: BikeDetailsContract.State,
    intents: (BikeDetailsContract.Intent) -> Unit
) {
    state.bike.rearSuspension?.let { suspension ->
        ShimstackCard(
            Modifier
                .height(IntrinsicSize.Min)
                .padding(top = 16.dp)
        ) {
            Column(Modifier.padding(8.dp)) {
                SuspensionBlock(
                    label = Res.string.label_rear_suspension,
                    state = state,
                    editMode = state is Edit,
                    intents = intents,
                    isFront = false,
                    measurementUnitType = state.measurementUnitType,
                    showError = (state as? Edit)?.validationFailure?.rearSuspension == false
                )
            }
        }
    }
}

@Composable
fun TextPair(
    label: StringResource,
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, style = textStyle)
        InfoText(textRes = label)
    }
}

@Composable
private fun SuspensionBlock(
    label: StringResource,
    state: BikeDetailsContract.State,
    editMode: Boolean,
    isFront: Boolean,
    showError: Boolean,
    measurementUnitType: MeasurementUnitType,
    modifier: Modifier = Modifier,
    intents: (BikeDetailsContract.Intent) -> Unit
) {
    AnimatedContent(targetState = editMode, modifier = modifier) {
        Column {
            InfoText(label)
            Spacer(modifier = Modifier.height(4.dp))
            if (it) {
                val editState = state as Edit
                Row(
                    modifier = Modifier.weight(1.0f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextInput(
                        value = (if (isFront) editState.frontSuspensionTravel else editState.rearSuspensionTravel) ?: "",
                        onValueChange = {
                            intents(
                                if (isFront) {
                                    BikeDetailsContract.Intent.Input.FrontSuspensionTravel(
                                        it
                                    )
                                } else {
                                    BikeDetailsContract.Intent.Input.RearSuspensionTravel(it)
                                }
                            )
                        },
                        label =
                            stringResource(
                                Res.string.label_travel
                            ),
                        suffix = measurementUnitType.getSmallDistanceLabel(),
                        modifier = Modifier.weight(1.0f),
                        keyboardOptions = KeyboardOptions.number(ImeAction.Next),
                        isError = showError
                    )

                    TextInput(
                        value = (if (isFront) editState.frontSuspensionPressure else editState.rearSuspensionPressure) ?: "",
                        onValueChange = {
                            intents(
                                if (isFront) {
                                    BikeDetailsContract.Intent.Input.FrontSuspensionPressure(
                                        it
                                    )
                                } else {
                                    BikeDetailsContract.Intent.Input.RearSuspensionPressure(it)
                                }
                            )
                        },
                        label =
                            stringResource(
                                Res.string.pressure
                            ),
                        suffix = measurementUnitType.getPressureLabel(),
                        modifier = Modifier.weight(1.0f),
                        keyboardOptions = KeyboardOptions.number(ImeAction.Next),
                        isError = showError
                    )

                    TextInput(
                        value = (if (isFront) editState.frontSuspensionTokens else editState.rearSuspensionTokens) ?: "",
                        onValueChange = {
                            intents(
                                if (isFront) {
                                    BikeDetailsContract.Intent.Input.FrontSuspensionTokens(
                                        it
                                    )
                                } else {
                                    BikeDetailsContract.Intent.Input.RearSuspensionTokens(
                                        it
                                    )
                                }
                            )
                        },
                        label =
                            stringResource(
                                Res.string.tokens
                            ),
                        modifier = Modifier.weight(1.0f),
                        keyboardOptions = KeyboardOptions.number(ImeAction.Done),
                        isError = showError
                    )
                }
            } else {
                val suspension = (if (isFront) state.bike.frontSuspension else state.bike.rearSuspension)!!
                Row(modifier = Modifier.weight(1.0f)) {
                    TextPair(
                        label = Res.string.label_travel,
                        text = suspension.getFormattedTravel(measurementUnitType.isMetric()),
                        modifier = Modifier.weight(1.0f)
                    )
                    TextPair(
                        label = Res.string.pressure,
                        text = suspension.getFormattedPressure(measurementUnitType.isMetric()),
                        modifier = Modifier.weight(1.0f)
                    )
                    TextPair(
                        label = Res.string.tokens,
                        text = suspension.tokens.toString(),
                        modifier = Modifier.weight(1.0f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.weight(1.0f), horizontalArrangement = Arrangement.Center) {
                    TextPair(
                        label = Res.string.rebound,
                        text = suspension.getFormattedRebound(),
                        modifier = Modifier.weight(1.0f)
                    )
                    TextPair(
                        label = Res.string.comp,
                        text = suspension.getFormattedCompression(),
                        modifier = Modifier.weight(1.0f)
                    )
                    Spacer(Modifier.weight(1.0f))
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        Content(
            state =
                BikeDetailsContract.State.Details(
                    Bike.empty().copy(
                        name = "Specialized Stumpjumper",
                        type = BikeType.ALL_MTN,
                        frontSuspension =
                            Suspension(
                                pressure = Pressure(10.0),
                                sag = 0.3,
                                compression = Damping(0, 1),
                                rebound = Damping(2, 3),
                                travel = Distance(150.0),
                                tokens = 5
                            ),
                        rearSuspension = Suspension(150)
                    )
                ),
            intents = {}
        )
    }
}

@Preview
@Composable
private fun EditPreview() {
    AppTheme {
        Content(
            state =
                Edit(
                    Bike.empty().copy(
                        name = "Specialized Stumpjumper",
                        type = BikeType.ALL_MTN,
                        frontSuspension =
                            Suspension(
                                pressure = Pressure(10.0),
                                sag = 0.3,
                                compression = Damping(0, 1),
                                rebound = Damping(2, 3),
                                travel = Distance(150.0),
                                tokens = 5
                            ),
                        rearSuspension = Suspension(150)
                    ),
                    validationFailure =
                        ValidateBikeUseCase.DetailsFailure(
                            false,
                            false,
                            false,
                            false,
                            false,
                            false
                        )
                ),
            intents = {}
        )
    }
}