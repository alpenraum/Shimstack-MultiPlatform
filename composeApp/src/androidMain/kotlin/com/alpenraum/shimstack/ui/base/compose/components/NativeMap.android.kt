package com.alpenraum.shimstack.ui.base.compose.components

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.alpenraum.shimstack.R
import com.alpenraum.shimstack.ui.base.compose.state.MapConstants
import com.alpenraum.shimstack.ui.base.compose.state.NativeMapState
import com.alpenraum.shimstack.ui.base.compose.state.setMapController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.collections.immutable.ImmutableList

@Composable
actual fun NativeMap(
    mapState: NativeMapState,
    modifier: Modifier,
    contentPadding: PaddingValues,
    content:
        @NativeMapComposable @Composable
        () -> Unit
) {
    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                mapToolbarEnabled = false,
                indoorLevelPickerEnabled = false,
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        )
    }
    val context = LocalContext.current
    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                maxZoomPreference = MapConstants.MAX_ZOOM_LEVEL,
                minZoomPreference = MapConstants.MIN_ZOOM_LEVEL,
                isBuildingEnabled = false,
                isTrafficEnabled = false,
                isIndoorEnabled = false,
                isMyLocationEnabled = true,
                mapStyleOptions = loadMapStyle(context)
            )
        )
    }

    val cameraPositionState: CameraPositionState =
        rememberCameraPositionState {}

    DisposableEffect(cameraPositionState, mapState) {
        mapState.setMapController(cameraPositionState)
        onDispose {
            mapState.setMapController(null)
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        uiSettings = mapUiSettings,
        properties = mapProperties,
        cameraPositionState = cameraPositionState
    ) {
        content()
    }
    LaunchedEffect(Unit) {
        cameraPositionState.move(CameraUpdateFactory.zoomTo(MapConstants.DEFAULT_ZOOM))
    }
}

private fun loadMapStyle(context: Context): MapStyleOptions? =
    try {
        val rawStyle =
            context.resources
                .openRawResource(R.raw.native_map_style)
                .bufferedReader()
                .use { it.readText() }
        MapStyleOptions(rawStyle)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

internal fun Coordinate.toLatLong() = LatLng(latitude, longitude)

@NativeMapComposable
@Composable
actual fun Polyline(
    points: ImmutableList<Coordinate>,
    color: Color,
    width: Dp
) {
    val polylinePoints = remember(points) { points.map { it.toLatLong() } }

    com.google.maps.android.compose.Polyline(
        points = polylinePoints,
        width = with(LocalDensity.current) { width.toPx() },
        color = color
    )
}