package com.alpenraum.shimstack.ui.setupTroubleshooting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.byValue
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alpenraum.shimstack.base.use
import com.alpenraum.shimstack.ui.base.compose.ClassKeyedCrossfade
import com.alpenraum.shimstack.ui.base.compose.components.AttachToLifeCycle
import com.alpenraum.shimstack.ui.base.compose.components.BikePager
import com.alpenraum.shimstack.ui.base.compose.components.ButtonText
import com.alpenraum.shimstack.ui.base.compose.components.LargeButton
import com.alpenraum.shimstack.ui.base.compose.components.LargeSecondaryButton
import com.alpenraum.shimstack.ui.base.compose.components.LoadingSpinner
import com.alpenraum.shimstack.ui.base.compose.components.ShimstackCard
import com.alpenraum.shimstack.ui.base.compose.components.getScreenHeight
import com.alpenraum.shimstack.ui.base.compose.decimal
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.btn_confirm
import shimstackmultiplatform.composeapp.generated.resources.err_technical
import shimstackmultiplatform.composeapp.generated.resources.fork
import shimstackmultiplatform.composeapp.generated.resources.il_support
import shimstackmultiplatform.composeapp.generated.resources.label_no
import shimstackmultiplatform.composeapp.generated.resources.label_yes
import shimstackmultiplatform.composeapp.generated.resources.pressure
import shimstackmultiplatform.composeapp.generated.resources.setup_wizard_button_see_history
import shimstackmultiplatform.composeapp.generated.resources.setup_wizard_button_select_symptom
import shimstackmultiplatform.composeapp.generated.resources.setup_wizard_label_no_bikes
import shimstackmultiplatform.composeapp.generated.resources.setup_wizard_label_recommendation_cta
import shimstackmultiplatform.composeapp.generated.resources.setup_wizard_label_recommendation_error_dismiss
import shimstackmultiplatform.composeapp.generated.resources.setup_wizard_label_recommendation_header
import shimstackmultiplatform.composeapp.generated.resources.setup_wizard_label_recommendation_is_front
import shimstackmultiplatform.composeapp.generated.resources.setup_wizard_label_recommendation_is_high_speed
import shimstackmultiplatform.composeapp.generated.resources.setup_wizard_label_updated_sus_pressure
import shimstackmultiplatform.composeapp.generated.resources.setup_wizard_primary_cta
import shimstackmultiplatform.composeapp.generated.resources.shock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupWizardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SetupWizardViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.initVm()
    }
    AttachToLifeCycle(viewModel = viewModel)
    val (state, intents, events) = use(viewModel = viewModel, navController)

    val snackState = remember { SnackbarHostState() }
    val errorMessage = stringResource(Res.string.err_technical)

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        events.collectLatest {
            when (it) {
                SetupWizardContract.Event.Error -> snackState.showSnackbar(errorMessage)
                SetupWizardContract.Event.HideSymptomBottomSheet -> bottomSheetState.hide()
                SetupWizardContract.Event.ShowSymptomBottomSheet -> bottomSheetState.show()
            }
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackState) }, modifier = modifier) { paddingValues ->
        Content(state, Modifier.padding(paddingValues), intents)

        (state as? SetupWizardContract.State.SelectSymptom)?.let {
            ModalBottomSheet(
                sheetState = bottomSheetState,
                onDismissRequest = { intents(SetupWizardContract.Intent.OnBottomSheetDismissed) }
            ) {
                SetupSymptomList(it.symptoms, Modifier, intents)
            }
        }
    }
}

@Composable
fun Content(
    state: SetupWizardContract.State,
    modifier: Modifier = Modifier,
    intents: (SetupWizardContract.Intent) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0) { state.bikes.size }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        BikePager(
            modifier =
                Modifier
                    .padding(top = 32.dp, bottom = 16.dp),
            showPlaceholder = false,
            bikes = state.bikes,
            pagerState = pagerState,
            onDetailsClick = null,
            onAddNewBikeClick = null,
            onSelectedBikeChanged = { intents(SetupWizardContract.Intent.OnSelectedBikeChanged(it)) }
        )

        ShimstackCard(
            Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 16.dp)
        ) { paddingValues ->
            if (state.bikes.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(paddingValues)) {
                    Text(
                        stringResource(Res.string.setup_wizard_label_no_bikes),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                ClassKeyedCrossfade(state, modifier = Modifier.padding(paddingValues)) {
                    when (it) {
                        is SetupWizardContract.State.Recommendation -> SetupRecommendationContent(it.setupRecommendation, intents)
                        is SetupWizardContract.State.Start -> StartContent(intents)
                        is SetupWizardContract.State.Success -> Text("Success!", style = MaterialTheme.typography.titleMedium)
                        is SetupWizardContract.State.SelectSymptom, SetupWizardContract.State.Initializing ->
                            Column(
                                Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LoadingSpinner()
                            }

                        is SetupWizardContract.State.UpdateSuspensionPressure -> UpdateSuspensionPressure(it, intents = intents)
                        is SetupWizardContract.State.NoFittingSolution -> NoFittingSolutionContent(intents, Modifier)
                    }
                }
            }
        }
    }
}

@Composable
private fun StartContent(
    intents: (SetupWizardContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.il_support),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentDescription = null
        )
        Spacer(Modifier.height(16.dp))
        LargeButton(onClick = { intents(SetupWizardContract.Intent.OnStartTroubleshootClick) }) {
            ButtonText(Res.string.setup_wizard_primary_cta)
        }
        Spacer(Modifier.height(8.dp))
        LargeSecondaryButton(onClick = { intents(SetupWizardContract.Intent.OnSeePreviousRecommendationsClick) }) {
            ButtonText(Res.string.setup_wizard_button_see_history)
        }
    }
}

@Composable
private fun NoFittingSolutionContent(
    intents: (SetupWizardContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = ":(",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Sorry, but there is no solution on your bike to your problem. Try another problem, maybe that will solve your issue!",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))
        LargeButton(onClick = { intents(SetupWizardContract.Intent.OnDismissError) }) {
            ButtonText(Res.string.setup_wizard_label_recommendation_error_dismiss)
        }
    }
}

@Composable
private fun SetupRecommendationContent(
    setupRecommendations: ImmutableList<SetupWizardContract.SetupRecommendationView>,
    intents: (SetupWizardContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Text(
            stringResource(Res.string.setup_wizard_label_recommendation_header),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(start = 8.dp, top = 16.dp)
        )
        if (setupRecommendations.size == 1) {
            SingleSetupRecommendation(setupRecommendations.first())
        } else {
            MultiSetupRecommendation(setupRecommendations)
        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(Res.string.setup_wizard_label_recommendation_cta),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Row(Modifier.fillMaxWidth().padding(top = 16.dp)) {
                FilledTonalButton(
                    onClick = { intents(SetupWizardContract.Intent.OnRecommendationDeclined) },
                    modifier = Modifier.weight(1.0f),
                    colors =
                        ButtonDefaults.filledTonalButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null)
                    ButtonText(Res.string.label_no)
                }
                Spacer(Modifier.width(20.dp))
                FilledTonalButton(
                    onClick = { intents(SetupWizardContract.Intent.OnRecommendationAccepted) },
                    modifier = Modifier.weight(1.0f),
                    colors =
                        ButtonDefaults.filledTonalButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    ButtonText(Res.string.label_yes)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.SingleSetupRecommendation(setupRecommendationView: SetupWizardContract.SetupRecommendationView) {
    Column(Modifier.align(Alignment.CenterHorizontally), horizontalAlignment = Alignment.CenterHorizontally) {
        with(setupRecommendationView) {
            Text(
                stringResource(sentence, stringResource(target1), stringResource(target2)),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                "$value " + unit?.let { stringResource(it) },
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
    }
}

@Composable
private fun ColumnScope.MultiSetupRecommendation(
    setupRecommendationViews: ImmutableList<SetupWizardContract.SetupRecommendationView>
) {
    Column(
        Modifier
            .align(Alignment.CenterHorizontally)
            .weight(1.0f)
            .padding(bottom = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        setupRecommendationViews.forEachIndexed { index, it ->
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    stringResource(it.sentence, stringResource(it.target1), stringResource(it.target2)),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(Modifier.weight(1.0f))
                Text(
                    "${it.value} " + it.unit?.let { it1 -> stringResource(it1) },
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            if (index != setupRecommendationViews.size) {
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun SetupSymptomList(
    symptomViews: ImmutableList<SetupSymptomView>,
    modifier: Modifier = Modifier,
    intents: (SetupWizardContract.Intent) -> Unit
) {
    val isFront = remember { mutableStateOf(false) }
    val isHighSpeed = remember { mutableStateOf(false) }
    val selectedSymptom = symptomViews.firstOrNull { it.selected }?.setupSymptom
    Column(modifier.padding(16.dp)) {
        LazyColumn(Modifier.heightIn(max = getScreenHeight() * 0.6f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(symptomViews) {
                SetupSymptomItem(
                    it,
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            intents(SetupWizardContract.Intent.OnSymptomSelected(it.setupSymptom))
                        }
                )
            }
        }
        Column(Modifier.padding(top = 16.dp)) {
            HorizontalDivider()
            AnimatedVisibility(selectedSymptom?.requiresLocation ?: false) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(Res.string.setup_wizard_label_recommendation_is_front),
                        modifier = Modifier.weight(1.0f)
                    ) // TODO
                    Switch(isFront.value, onCheckedChange = { isFront.value = !isFront.value })
                }
            }
            AnimatedVisibility(selectedSymptom?.requiresSpeed ?: false) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(Res.string.setup_wizard_label_recommendation_is_high_speed),
                        modifier = Modifier.weight(1.0f)
                    )
                    Switch(isHighSpeed.value, onCheckedChange = { isHighSpeed.value = !isHighSpeed.value })
                }
            }
            LargeButton(modifier = Modifier.padding(top = 4.dp), onClick = {
                intents(SetupWizardContract.Intent.OnSymptomConfirmed(isFront.value, isHighSpeed.value))
            }, enabled = symptomViews.any { it.selected }) {
                ButtonText(Res.string.setup_wizard_button_select_symptom)
            }
        }
    }
}

@Composable
private fun SetupSymptomItem(
    setupSymptomView: SetupSymptomView,
    modifier: Modifier = Modifier
) {
    ShimstackCard { paddingValues ->
        Row(modifier.padding(paddingValues)) {
            RadioButton(selected = setupSymptomView.selected, onClick = null)
            Column(Modifier.padding(start = 16.dp)) {
                Text(stringResource(setupSymptomView.name), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(setupSymptomView.description), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun UpdateSuspensionPressure(
    state: SetupWizardContract.State.UpdateSuspensionPressure,
    modifier: Modifier = Modifier,
    intents: (SetupWizardContract.Intent) -> Unit
) {
    Column(modifier) {
        val frontTextFieldValue = remember { mutableStateOf(TextFieldValue()) }
        val rearTextFieldValue = remember { mutableStateOf(TextFieldValue()) }
        Text(stringResource(Res.string.setup_wizard_label_updated_sus_pressure))
        Row {
            if (state.showFront) {
                SuspensionPressureInput(
                    frontTextFieldValue.value,
                    "${stringResource(Res.string.fork)} ${stringResource(Res.string.pressure)}",
                    Modifier.weight(1.0f)
                ) { frontTextFieldValue.value = it }
            }
            if (state.showRear) {
                SuspensionPressureInput(
                    rearTextFieldValue.value,
                    "${stringResource(Res.string.shock)} ${stringResource(Res.string.pressure)}",
                    Modifier.weight(1.0f)
                ) { rearTextFieldValue.value = it }
            }
        }
        Spacer(Modifier.weight(1.0f))
        LargeButton(onClick = {
            intents(
                SetupWizardContract.Intent.OnConfirmUpdatedSuspensionPressure(
                    frontTextFieldValue.value,
                    rearTextFieldValue.value
                )
            )
        }) {
            ButtonText(Res.string.btn_confirm)
        }
    }
}

@Composable
private fun SuspensionPressureInput(
    textFieldValue: TextFieldValue,
    label: String,
    modifier: Modifier,
    onTextChange: (TextFieldValue) -> Unit
) {
    Column(modifier) {
        Text(label)
        TextField(
            textFieldValue,
            onValueChange = {
                if ("^(0|[1-9]\\d*)([.,]\\d*)?\$".toRegex().matches(it.text)) {
                    onTextChange(it)
                }
            },
            keyboardOptions = KeyboardOptions.decimal()
        )
    }
}

/**
 * Returns [InputTransformation] that rejects input that is not allowed for amount input field.
 */
@Stable
fun InputTransformation.decimal() =
    byValue { current, proposed ->
        if ("^(0|[1-9]\\d*)([.,]\\d*)?\$".toRegex().matches(proposed)) proposed else current
    }