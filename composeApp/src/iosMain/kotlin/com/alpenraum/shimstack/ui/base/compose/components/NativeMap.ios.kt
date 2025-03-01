package com.alpenraum.shimstack.ui.base.compose.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.alpenraum.shimstack.ui.base.compose.state.NativeMapState
import com.alpenraum.shimstack.ui.base.compose.state.onMapUpdated
import com.alpenraum.shimstack.ui.base.compose.state.setMap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.interpretPointed
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.collections.immutable.ImmutableList
import platform.CoreLocation.CLLocationCoordinate2D
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKPolyline
import platform.MapKit.MKPolylineRenderer
import platform.MapKit.addOverlay
import platform.MapKit.removeOverlay
import platform.UIKit.NSDirectionalEdgeInsetsMake

@Composable
actual fun NativeMap(
    mapState: NativeMapState,
    modifier: Modifier,
    contentPadding: PaddingValues,
    content:
        @NativeMapComposable @Composable
        () -> Unit
) {
    val mkMapView = rememberMkMapView()
    val mkMapViewDelegate =
        rememberMKMapViewDelegate {
            mapState.onMapUpdated()
        }

    AppleMap(
        modifier = modifier,
        mkMapView = mkMapView,
        mkMapViewDelegate = mkMapViewDelegate,
        contentPadding = contentPadding,
        content = content
    )

    DisposableEffect(mkMapView, mapState) {
        mapState.setMap(mkMapView)

        onDispose {
            mapState.setMap(null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun AppleMap(
    modifier: Modifier = Modifier,
    mkMapView: MKMapView = rememberMkMapView(),
    mkMapViewDelegate: MKMapViewDelegateProtocol = rememberMKMapViewDelegate(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current

    val directionalEdgeInsets =
        remember(layoutDirection, contentPadding) {
            NSDirectionalEdgeInsetsMake(
                top = contentPadding.calculateTopPadding().value.toDouble(),
                leading = contentPadding.calculateStartPadding(layoutDirection).value.toDouble(),
                bottom = contentPadding.calculateBottomPadding().value.toDouble(),
                trailing = contentPadding.calculateEndPadding(layoutDirection).value.toDouble()
            )
        }

    UIKitView(
        modifier = modifier,
        properties =
            UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative
            ),
        factory = {
            mkMapView.apply {
                setZoomEnabled(true)
                setScrollEnabled(true)
                setRotateEnabled(true)
                setPitchEnabled(true)

                setShowsCompass(false)

                // customize map view further
            }
        },
        update = { mapView ->
            mapView.delegate = mkMapViewDelegate
            mapView.directionalLayoutMargins = directionalEdgeInsets
        }
    )

    CompositionLocalProvider(LocalMapView provides mkMapView) {
        content()
    }
}

@NativeMapComposable
@Composable
actual fun Polyline(
    points: ImmutableList<Coordinate>,
    color: Color,
    width: Dp
) {
    val mapView = LocalMapView.current
    val overlay = rememberPolylineOverlay(points, color, width)

    DisposableEffect(mapView, points, color, width) {
        mapView.addOverlay(overlay)

        onDispose {
            mapView.removeOverlay(overlay)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun rememberPolylineOverlay(
    points: ImmutableList<Coordinate>,
    color: Color,
    width: Dp
): MKOverlayProtocol {
    val polyline =
        remember(points) {
            val clLocations = points.map { it.toCLLocation() }

            memScoped {
                // allocate a C style array to hold the coordinates
                val coordinates = allocArray<CLLocationCoordinate2D>(clLocations.size)

                clLocations.forEachIndexed { index, clLocation ->
                    // calculate the offset of the current coordinate in the array
                    val offset = index * sizeOf<CLLocationCoordinate2D>()

                    // calculate the memory address of the current coordinate in the array
                    val pointer = coordinates.rawValue + offset

                    // copy the coordinate to the calculated memory address
                    clLocation.place(interpretPointed<CLLocationCoordinate2D>(pointer).ptr)
                }

                MKPolyline.polylineWithCoordinates(coordinates, clLocations.size.toULong())
            }
        }

    return remember(points, color, width) {
        CommonMapOverlay(
            delegate = polyline,
            rendererCreator = {
                MKPolylineRenderer(overlay = polyline).apply {
                    setStrokeColor(color.toUIColor())
                    setLineWidth(width.value.toDouble())
                    setLineCap(platform.CoreGraphics.CGLineCap.kCGLineCapRound)
                }
            }
        )
    }
}