package com.alpenraum.shimstack.ui.base.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.alpenraum.shimstack.ui.base.compose.components.Coordinate
import com.alpenraum.shimstack.ui.base.compose.components.toCLLocation
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2D
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapView
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round

@OptIn(ExperimentalForeignApi::class)
internal class AppleMapState : NativeMapState {
    private var map: MKMapView? by mutableStateOf(null)

    override var zoomLevel: Float by mutableStateOf(MapConstants.DEFAULT_ZOOM)

    override suspend fun moveToCoordinate(coordinate: Coordinate) {
        val map = map ?: return

        map.moveToCoordinate(coordinate.toCLLocation(), map.getZoomLevel())
    }

    override suspend fun zoomIn() {
        val map = map ?: return

        val currentZoom = map.getZoomLevel()

        val zoomLevel = currentZoom + 1

        map.setZoomLevel(zoomLevel)
    }

    override suspend fun zoomOut() {
        val map = map ?: return

        val currentZoom = map.getZoomLevel()

        val zoomLevel = currentZoom - 1

        map.setZoomLevel(zoomLevel)
    }

    fun setMap(mapView: MKMapView?) {
        map = mapView
    }

    fun onMapUpdated() {
        val map = map ?: return

        zoomLevel = map.getZoomLevel()
    }
}

@Composable
actual fun rememberMapState(): NativeMapState = remember { AppleMapState() }

fun NativeMapState.setMap(mkMapView: MKMapView?) {
    (this as AppleMapState).setMap(mkMapView)
}

fun NativeMapState.onMapUpdated() {
    (this as AppleMapState).onMapUpdated()
}

// https://stackoverflow.com/questions/4189621/setting-the-zoom-level-for-a-mkmapview

@OptIn(ExperimentalForeignApi::class)
private fun MKMapView.getZoomLevel(): Float {
    val width = frame.useContents { size.width }
    val region = region.useContents { span.longitudeDelta }

    return round(log2(MapConstants.FULL_ROTATION_DEGREES * (width / MapConstants.TILE_SIZE) / region)).toFloat()
}

@OptIn(ExperimentalForeignApi::class)
private fun MKMapView.setZoomLevel(zoomLevel: Float) = moveToCoordinate(centerCoordinate, zoomLevel)

@OptIn(ExperimentalForeignApi::class)
private fun MKMapView.moveToCoordinate(
    coordinate: CValue<CLLocationCoordinate2D>,
    zoomLevel: Float
) {
    val width = frame.useContents { size.width }

    val span =
        MKCoordinateSpanMake(
            latitudeDelta = 0.0,
            longitudeDelta = MapConstants.FULL_ROTATION_DEGREES / 2.0.pow(zoomLevel.toDouble()) * (width / MapConstants.TILE_SIZE)
        )

    setRegion(MKCoordinateRegionMake(centerCoordinate = coordinate, span = span), animated = true)
}