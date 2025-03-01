package com.alpenraum.shimstack.ui.ridetracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.base.use
import com.alpenraum.shimstack.ui.base.compose.ClassKeyedCrossfade
import com.alpenraum.shimstack.ui.base.compose.components.AttachToLifeCycle
import com.alpenraum.shimstack.ui.base.compose.components.Coordinate
import com.alpenraum.shimstack.ui.base.compose.components.NativeMap
import com.alpenraum.shimstack.ui.base.compose.components.Polyline
import com.alpenraum.shimstack.ui.base.compose.components.ShimstackAlertDialog
import com.alpenraum.shimstack.ui.base.compose.components.ShimstackCard
import com.alpenraum.shimstack.ui.base.compose.state.rememberMapState
import com.alpenraum.shimstack.ui.location.model.AppPermissions
import com.alpenraum.shimstack.ui.location.model.PermissionState
import com.alpenraum.shimstack.ui.location.model.getDrawable
import com.alpenraum.shimstack.ui.location.model.getExplainerResource
import com.alpenraum.shimstack.ui.location.model.getNameResource
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.ride_tracker_last_ride_dialog
import shimstackmultiplatform.composeapp.generated.resources.ride_tracker_last_ride_dialog_accept
import shimstackmultiplatform.composeapp.generated.resources.ride_tracker_last_ride_dialog_decline

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
    Column(
        modifier = modifier.fillMaxSize().padding(8.dp)
    ) {
        ClassKeyedCrossfade(state) {
            when (it) {
                is RideTrackerContract.State.Default -> {
                    Text(it.x)
                    Button({ intents(RideTrackerContract.Intent.StartTracking) }) {
                        Text("start tracker")
                    }
                }

                is RideTrackerContract.State.Permissions -> PermissionsContent(state, intents)
                is RideTrackerContract.State.ActiveRide -> ActiveRideContent(state, intents)
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
            "Looks like you didn't grant all permissions.",
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
    val (icon, description) =
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
    val logger: ShimstackLogger = koinInject()
    logger.d("new gps points: ${state.gpsPoints.size}")
    val mapState = rememberMapState()

    val polyLines by derivedStateOf { state.gpsPoints.map { Coordinate(it.latitude, it.longitude) }.toPersistentList() }

    NativeMap(mapState, modifier = Modifier.fillMaxSize()) {
        Polyline(polyLines)
    }
    LaunchedEffect(state.gpsPoints) {
        state.gpsPoints.lastOrNull()?.let {
            mapState.moveToCoordinate(Coordinate(it.latitude, it.longitude))
        }
    }
}