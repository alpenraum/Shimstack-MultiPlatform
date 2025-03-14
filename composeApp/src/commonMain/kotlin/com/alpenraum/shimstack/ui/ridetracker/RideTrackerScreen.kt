package com.alpenraum.shimstack.ui.ridetracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alpenraum.shimstack.base.use
import com.alpenraum.shimstack.ui.base.compose.ClassKeyedCrossfade
import com.alpenraum.shimstack.ui.base.compose.components.AttachToLifeCycle
import com.alpenraum.shimstack.ui.base.compose.components.ButtonText
import com.alpenraum.shimstack.ui.base.compose.components.Coordinate
import com.alpenraum.shimstack.ui.base.compose.components.LargeButton
import com.alpenraum.shimstack.ui.base.compose.components.NativeMap
import com.alpenraum.shimstack.ui.base.compose.components.Polyline
import com.alpenraum.shimstack.ui.base.compose.components.ShimstackAlertDialog
import com.alpenraum.shimstack.ui.base.compose.components.ShimstackCard
import com.alpenraum.shimstack.ui.base.compose.state.rememberMapState
import com.alpenraum.shimstack.ui.bikeDetails.TextPair
import com.alpenraum.shimstack.ui.location.model.AppPermissions
import com.alpenraum.shimstack.ui.location.model.PermissionState
import com.alpenraum.shimstack.ui.location.model.getDrawable
import com.alpenraum.shimstack.ui.location.model.getExplainerResource
import com.alpenraum.shimstack.ui.location.model.getNameResource
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.average_speed_label
import shimstackmultiplatform.composeapp.generated.resources.distance_label
import shimstackmultiplatform.composeapp.generated.resources.duration_label
import shimstackmultiplatform.composeapp.generated.resources.elevation_label
import shimstackmultiplatform.composeapp.generated.resources.ride_tracker_last_ride_dialog
import shimstackmultiplatform.composeapp.generated.resources.ride_tracker_last_ride_dialog_accept
import shimstackmultiplatform.composeapp.generated.resources.ride_tracker_last_ride_dialog_decline
import shimstackmultiplatform.composeapp.generated.resources.speed_label
import shimstackmultiplatform.composeapp.generated.resources.start_ride_label
import shimstackmultiplatform.composeapp.generated.resources.stop_ride_label
import shimstackmultiplatform.composeapp.generated.resources.tire
import shimstackmultiplatform.composeapp.generated.resources.top_speed_label

@Composable
fun RideTrackerScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: RideTrackerViewModel = koinViewModel()
) {
    AttachToLifeCycle(viewModel = viewModel)
    val (state, intents, events) = use(viewModel = viewModel, navController)

    var showDialog by remember { mutableStateOf(false) }

    ShimstackAlertDialog(
        showDialog,
        text = Res.string.ride_tracker_last_ride_dialog,
        confirmLabel = Res.string.ride_tracker_last_ride_dialog_accept,
        dismissLabel = Res.string.ride_tracker_last_ride_dialog_decline,
        onConfirm = {
            showDialog = false
            intents(RideTrackerContract.Intent.OnContinueExistingRide)
        },
        onDismissButton = {
            showDialog = false
            intents(RideTrackerContract.Intent.OnStartNewRide)
        }
    )

    LaunchedEffect(Unit) {
        events.collectLatest {
            when (it) {
                RideTrackerContract.Event.ShowContinueExistingRideDialog -> showDialog = true
            }
        }
    }

    ClassKeyedCrossfade(state) {
        Column(
            modifier = modifier.fillMaxSize().padding(8.dp)
        ) {
            when (it) {
                is RideTrackerContract.State.Default -> DefaultContent(it, intents)

                is RideTrackerContract.State.Permissions -> PermissionsContent(it, intents)
                is RideTrackerContract.State.ActiveRide -> ActiveRideContent(it, intents)
            }
        }
    }
}

@Composable
fun DefaultContent(
    state: RideTrackerContract.State.Default,
    intents: (RideTrackerContract.Intent) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1.0f)) {
            items(state.rides) {
                RideViewCard(it, Modifier.fillMaxWidth())
            }
        }

        LargeButton({ intents(RideTrackerContract.Intent.OnStartNewRide) }, modifier = Modifier.padding(vertical = 16.dp)) {
            ButtonText(Res.string.start_ride_label)
        }
    }
}

@Composable
private fun RideViewCard(
    rideView: RideView,
    modifier: Modifier = Modifier
) {
    ShimstackCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
            Text(rideView.date, style = MaterialTheme.typography.titleLarge, textDecoration = TextDecoration.Underline)
            Row {
                Text(stringResource(Res.string.distance_label))
                Spacer(Modifier.width(8.dp))
                Text(rideView.distance)
            }
            Row {
                Text(stringResource(Res.string.elevation_label))
                Spacer(Modifier.width(8.dp))
                Text(rideView.elevationSum)
            }
            Row {
                Text(stringResource(Res.string.duration_label))
                Spacer(Modifier.width(8.dp))
                Text(rideView.duration)
            }
            Row {
                Text(stringResource(Res.string.average_speed_label))
                Spacer(Modifier.width(8.dp))
                Text(rideView.averageSpeed)
            }
            Row {
                Text(stringResource(Res.string.top_speed_label))
                Spacer(Modifier.width(8.dp))
                Text(rideView.topSpeed)
            }

        }
    }
}

@Composable
fun PermissionsContent(
    state: RideTrackerContract.State.Permissions,
    intents: (RideTrackerContract.Intent) -> Unit
) {
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Looks like you didn't grant all permissions.", // TODO LABELS
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        state.asList().forEach {
            Permission(
                it.state,
                it.permission,
                modifier = Modifier.clickable { intents(RideTrackerContract.Intent.RequestPermission(it.permission)) }
            )
        }
    }
}

@Composable
fun Permission(
    permissionState: PermissionState,
    permission: AppPermissions,
    modifier: Modifier
) {
    val (containerColor, contentColor) =
        when (permissionState) {
            PermissionState.NOT_DETERMINED ->
                MaterialTheme.colorScheme.surfaceVariant to
                    MaterialTheme.colorScheme.onSurfaceVariant

            PermissionState.GRANTED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
            PermissionState.DENIED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        }
    val (icon, description) = // TODO LABELS
        when (permissionState) {
            PermissionState.NOT_DETERMINED -> Icons.Default.QuestionMark to "Not set"
            PermissionState.GRANTED -> Icons.Default.Done to "Granted"
            PermissionState.DENIED -> Icons.Default.Block to "Denied"
        }
    ShimstackCard(containerColor = containerColor, contentColor = contentColor, modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp).semantics(true) {}) {
            Icon(
                permission.getDrawable(),
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp)
            )
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1.0f).padding(horizontal = 8.dp)) {
                Text(
                    stringResource(permission.getNameResource()),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(stringResource(permission.getExplainerResource()))
            }
            Icon(icon, contentDescription = description, modifier = Modifier.padding(end = 8.dp))
        }
    }
}

@Composable
fun ActiveRideContent(
    state: RideTrackerContract.State.ActiveRide,
    intents: (RideTrackerContract.Intent) -> Unit
) {
    val mapState = rememberMapState()

    val polyLines by derivedStateOf { state.gpsPoints.map { Coordinate(it.latitude, it.longitude) }.toPersistentList() }

    Column(Modifier.padding(8.dp)) {
        ShimstackCard(Modifier.fillMaxHeight(0.66f).fillMaxWidth().padding(bottom = 16.dp)) {
            NativeMap(mapState, modifier = Modifier.fillMaxSize()) {
                Polyline(polyLines)
            }
        }

        ShimstackCard(modifier = Modifier.weight(1.0f).fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Medium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    TextPair(Res.string.elevation_label, state.currentElevationSum, textStyle = style)
                    TextPair(Res.string.distance_label, state.currentDistance, textStyle = style)
                }
                Spacer(Modifier.weight(1.0f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    TextPair(Res.string.speed_label, state.currentSpeed, textStyle = style)
                    TextPair(Res.string.duration_label, state.currentDuration, textStyle = style)
                }
                Spacer(Modifier.weight(1.0f))
                LargeButton({ intents(RideTrackerContract.Intent.OnStopRideClick) }) {
                    ButtonText(Res.string.stop_ride_label)
                }
            }
        }

        LaunchedEffect(state.gpsPoints) {
            state.gpsPoints.lastOrNull()?.let {
                mapState.moveToCoordinate(Coordinate(it.latitude, it.longitude))
            }
        }
    }
}