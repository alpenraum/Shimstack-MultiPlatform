package com.alpenraum.shimstack.ui.base.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.alpenraum.shimstack.ui.base.compose.components.Coordinate

@Stable
interface NativeMapState {
    val zoomLevel: Float

    val isCloseZoomLevel: Boolean
        get() = zoomLevel >= MapConstants.MAX_ZOOM_LEVEL

    suspend fun moveToCoordinate(coordinate: Coordinate)

    // TOOD: CHECK REALISTIC MIN AND MAX ZOOM LEVELS
    suspend fun zoomIn()

    suspend fun zoomOut()
}

@Composable
expect fun rememberMapState(): NativeMapState