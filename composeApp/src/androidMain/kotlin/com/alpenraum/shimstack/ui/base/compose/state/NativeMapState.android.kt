package com.alpenraum.shimstack.ui.base.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.alpenraum.shimstack.ui.base.compose.components.Coordinate
import com.alpenraum.shimstack.ui.base.compose.components.toLatLong
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraPositionState

internal class GoogleMapState : NativeMapState {
    private var cameraPositionState: CameraPositionState? by mutableStateOf(null)

    override var zoomLevel: Float by mutableFloatStateOf(MapConstants.DEFAULT_ZOOM)

    override suspend fun moveToCoordinate(coordinate: Coordinate) {
        cameraPositionState?.let {
            val newCameraPosition =
                CameraPosition(
                    coordinate.toLatLong(),
                    it.position.zoom,
                    it.position.tilt,
                    it.position.bearing
                )
            it.animate(CameraUpdateFactory.newCameraPosition(newCameraPosition))
        }
    }

    override suspend fun zoomIn() {
        cameraPositionState?.let {
            val zoomUpdate = CameraUpdateFactory.zoomIn()
            it.animate(zoomUpdate)
            zoomLevel += 1
        }
    }

    override suspend fun zoomOut() {
        cameraPositionState?.let {
            val zoomUpdate = CameraUpdateFactory.zoomOut()
            it.animate(zoomUpdate)
            zoomLevel -= 1
        }
    }

    fun setMapController(cameraPositionState: CameraPositionState?) {
        this.cameraPositionState = cameraPositionState
    }
}

@Composable
actual fun rememberMapState(): NativeMapState = remember { GoogleMapState() }

fun NativeMapState.setMapController(cameraPositionState: CameraPositionState?) {
    (this as GoogleMapState).setMapController(cameraPositionState)
}