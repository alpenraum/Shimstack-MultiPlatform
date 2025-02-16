package com.alpenraum.shimstack.ui.newBike.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alpenraum.shimstack.domain.bikeservice.ValidateBikeUseCase
import com.alpenraum.shimstack.domain.model.bike.BikeType
import com.alpenraum.shimstack.ui.base.compose.PhonePreview
import com.alpenraum.shimstack.ui.base.compose.TabletPreview
import com.alpenraum.shimstack.ui.base.compose.components.ButtonText
import com.alpenraum.shimstack.ui.base.compose.components.InfoText
import com.alpenraum.shimstack.ui.base.compose.components.LargeButton
import com.alpenraum.shimstack.ui.base.compose.components.TextInput
import com.alpenraum.shimstack.ui.base.compose.compositionlocal.LocalWindowSizeClass
import com.alpenraum.shimstack.ui.base.compose.number
import com.alpenraum.shimstack.ui.newBike.NewBikeContract
import com.alpenraum.shimstack.ui.newBike.NewBikeDestinations
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.copy_new_bike_details
import shimstackmultiplatform.composeapp.generated.resources.copy_new_bike_ebike
import shimstackmultiplatform.composeapp.generated.resources.copy_new_bike_internal_width_inf
import shimstackmultiplatform.composeapp.generated.resources.inch
import shimstackmultiplatform.composeapp.generated.resources.label_front_suspension
import shimstackmultiplatform.composeapp.generated.resources.label_front_tire
import shimstackmultiplatform.composeapp.generated.resources.label_internal_rim_width
import shimstackmultiplatform.composeapp.generated.resources.label_name
import shimstackmultiplatform.composeapp.generated.resources.label_next_step
import shimstackmultiplatform.composeapp.generated.resources.label_rear_suspension
import shimstackmultiplatform.composeapp.generated.resources.label_rear_tire
import shimstackmultiplatform.composeapp.generated.resources.label_tire_width
import shimstackmultiplatform.composeapp.generated.resources.label_travel
import shimstackmultiplatform.composeapp.generated.resources.label_type
import shimstackmultiplatform.composeapp.generated.resources.mm
import shimstackmultiplatform.composeapp.generated.resources.new_bike_label_separate_high_low_comp
import shimstackmultiplatform.composeapp.generated.resources.new_bike_label_separate_high_low_rebound

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterDetailsScreen(
    navigator: NavController? = null,
    state: NewBikeContract.State,
    intent: (NewBikeContract.Intent) -> Unit,
    event: SharedFlow<NewBikeContract.Event>
) {
    LaunchedEffect(key1 = Unit) {
        event.collectLatest {
            when (it) {
                NewBikeContract.Event.NavigateToNextStep -> {
                    navigator?.navigate(NewBikeDestinations.SETUP_DECISION.route)
                }

                else -> {}
            }
        }
    }

    val isCompactScreen =
        LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = stringResource(Res.string.copy_new_bike_details),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        TextInput(
            value = state.detailsInput.name ?: "",
            onValueChange = {
                intent(NewBikeContract.Intent.BikeNameInput(it))
            },
            modifier =
                if (!isCompactScreen) {
                    Modifier.padding(
                        top = 16.dp
                    )
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                },
            label = stringResource(Res.string.label_name),
            isError = state.detailsValidationErrors?.name == false,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )

        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                },
                modifier =
                    if (isCompactScreen) {
                        Modifier.weight(1.0f)
                    } else {
                        Modifier.padding(
                            end = 32.dp
                        )
                    }
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
                    isError = state.detailsValidationErrors?.type == false
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = {
                    expanded = false
                }) {
                    BikeType.entries.forEach { selectionOption ->
                        if (selectionOption != BikeType.UNKNOWN) {
                            DropdownMenuItem(text = {
                                Text(text = stringResource(selectionOption.labelRes))
                            }, onClick = {
                                intent(NewBikeContract.Intent.BikeTypeInput(selectionOption))
                                expanded = false
                            })
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(text = stringResource(Res.string.copy_new_bike_ebike))
                Switch(
                    checked = state.isEbike,
                    onCheckedChange = { intent(NewBikeContract.Intent.EbikeInput(it)) }
                )
            }
        }
        val frontSuspensionTravel = state.detailsInput.frontTravel
        SuspensionInput(
            headline = stringResource(Res.string.label_front_suspension),
            travel = frontSuspensionTravel ?: "",
            isError = state.detailsValidationErrors?.frontSuspension == false,
            initialState = frontSuspensionTravel != null,
            hscSwitchState = state.hasHSCFork,
            hsrSwitchState = state.hasHSRFork,
            onSuspensionSwitchToggle = {
                if (!it) intent(NewBikeContract.Intent.FrontSuspensionInput(null))
            },
            onValueChange = {
                intent(NewBikeContract.Intent.FrontSuspensionInput(it))
            },
            onHSCSwitchToggle = { intent(NewBikeContract.Intent.HSCInput(it, true)) },
            onHSRSwitchToggle = { intent(NewBikeContract.Intent.HSRInput(it, true)) }
        )
        val rearSuspensionTravel = state.detailsInput.rearTravel
        SuspensionInput(
            headline = stringResource(Res.string.label_rear_suspension),
            travel = rearSuspensionTravel ?: "",
            isError = state.detailsValidationErrors?.rearSuspension == false,
            initialState = rearSuspensionTravel != null,
            hscSwitchState = state.hasHSCShock,
            hsrSwitchState = state.hasHSRShock,
            onSuspensionSwitchToggle = {
                if (!it) intent(NewBikeContract.Intent.RearSuspensionInput(null))
            },
            onValueChange = {
                intent(NewBikeContract.Intent.RearSuspensionInput(it))
            },
            onHSCSwitchToggle = { intent(NewBikeContract.Intent.HSCInput(it, false)) },
            onHSRSwitchToggle = { intent(NewBikeContract.Intent.HSRInput(it, false)) }
        )
        TireInput(
            headline = stringResource(Res.string.label_front_tire),
            tireWidth = state.detailsInput.frontTireWidth,
            internalRimWidth = state.detailsInput.frontInternalRimWidth,
            isError = state.detailsValidationErrors?.frontTire == false,
            isMetric = state.measurementUnitType.isMetric(),
            lastInputImeAction = ImeAction.Next,
            {
                intent(NewBikeContract.Intent.FrontTireWidthInput(it))
            },
            {
                intent(NewBikeContract.Intent.FrontInternalRimWidthInput(it))
            }
        )
        TireInput(
            headline = stringResource(Res.string.label_rear_tire),
            tireWidth = state.detailsInput.rearTireWidth,
            internalRimWidth = state.detailsInput.rearInternalRimWidth,
            isError = state.detailsValidationErrors?.rearTire == false,
            isMetric = state.measurementUnitType.isMetric(),
            lastInputImeAction = ImeAction.Done,
            {
                intent(NewBikeContract.Intent.RearTireWidthInput(it))
            },
            { intent(NewBikeContract.Intent.RearInternalRimWidthInput(it)) }
        )

        LargeButton(
            enabled = state.detailsValidationErrors == null,
            onClick = {
                intent(NewBikeContract.Intent.OnNextClicked())
            },
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            ButtonText(textRes = Res.string.label_next_step)
        }
    }
}

@Composable
private fun ColumnScope.SuspensionInput(
    headline: String,
    travel: String,
    isError: Boolean,
    initialState: Boolean,
    hscSwitchState: Boolean,
    hsrSwitchState: Boolean,
    onSuspensionSwitchToggle: (Boolean) -> Unit,
    onValueChange: (String?) -> Unit,
    onHSCSwitchToggle: (Boolean) -> Unit,
    onHSRSwitchToggle: (Boolean) -> Unit
) {
    var showSuspensionInput by remember {
        mutableStateOf(initialState)
    }

    Row(
        modifier = Modifier.padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = headline, style = MaterialTheme.typography.titleLarge)
        Switch(
            checked = showSuspensionInput,
            onCheckedChange = {
                showSuspensionInput = it
                onSuspensionSwitchToggle(it)
            },
            modifier = Modifier.padding(start = 16.dp)
        )
    }
    AnimatedVisibility(visible = showSuspensionInput) {
        Column {
            TextInput(
                value = travel,
                onValueChange = { value ->
                    onValueChange(value)
                },
                suffix = stringResource(Res.string.mm),
                modifier = Modifier.padding(top = 8.dp),
                label = stringResource(Res.string.label_travel),
                isError = isError,
                keyboardOptions = KeyboardOptions.number(ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.new_bike_label_separate_high_low_comp), modifier = Modifier.weight(1.0f))
                Switch(
                    checked = hscSwitchState,
                    onCheckedChange = onHSCSwitchToggle,
                    modifier = Modifier.weight(1.0f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.new_bike_label_separate_high_low_rebound), modifier = Modifier.weight(1.0f))
                Switch(
                    checked = hsrSwitchState,
                    onCheckedChange = onHSRSwitchToggle,
                    modifier = Modifier.weight(1.0f)
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.TireInput(
    headline: String,
    tireWidth: String?,
    internalRimWidth: String?,
    isError: Boolean,
    isMetric: Boolean,
    lastInputImeAction: ImeAction,
    onTireWidthChanged: (String) -> Unit,
    onRimWidthChanged: (String?) -> Unit
) {
    Text(
        text = headline,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 16.dp)
    )
    Row(modifier = Modifier.padding(top = 8.dp)) {
        TextInput(
            value = tireWidth ?: "",
            onValueChange = { value ->
                onTireWidthChanged(value)
            },
            suffix = stringResource(if (isMetric) Res.string.mm else Res.string.inch),
            modifier =
                Modifier
                    .weight(1.0f)
                    .padding(end = 16.dp),
            label = stringResource(Res.string.label_tire_width),
            isError = isError,
            keyboardOptions = KeyboardOptions.number(ImeAction.Next)
        )
        Column(
            modifier = Modifier.weight(1.0f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextInput(
                value = internalRimWidth ?: "",
                onValueChange = { value ->
                    onRimWidthChanged(value)
                },
                suffix = stringResource(Res.string.mm),
                label = stringResource(Res.string.label_internal_rim_width),
                isError = isError,
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = lastInputImeAction
                    )
            )
            InfoText(Res.string.copy_new_bike_internal_width_inf)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TabletPreview {
        EnterDetailsScreen(
            state = NewBikeContract.State(),
            intent = {},
            event = MutableSharedFlow()
        )
    }
}

@Preview
@Composable
private fun Preview1() {
    PhonePreview {
        EnterDetailsScreen(
            state = NewBikeContract.State(),
            intent = {},
            event = MutableSharedFlow()
        )
    }
}

@Preview
@Composable
private fun Error() {
    PhonePreview {
        EnterDetailsScreen(
            state =
                NewBikeContract.State(
                    detailsValidationErrors =
                        ValidateBikeUseCase.DetailsFailure(
                            name = false,
                            type = false,
                            frontTire = false,
                            rearTire = false,
                            frontSuspension = false,
                            rearSuspension = false
                        )
                ),
            intent = {},
            event = MutableSharedFlow()
        )
    }
}