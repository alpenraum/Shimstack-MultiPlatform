package com.alpenraum.shimstack.ui.base.compose.components

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class CommonMapAnnotation(
    val identifier: String,
    private val position: Coordinate,
    private val viewCreator: (MKAnnotationProtocol) -> MKAnnotationView,
    private val viewBinder: (MKAnnotationView) -> Unit
) : NSObject(),
    MKAnnotationProtocol {
    override fun coordinate(): CValue<CLLocationCoordinate2D> = position.toCLLocation()

    fun createView(): MKAnnotationView = viewCreator(this)

    fun bindView(view: MKAnnotationView) = viewBinder(view)
}

@OptIn(ExperimentalForeignApi::class)
internal fun Coordinate.toCLLocation() = CLLocationCoordinate2DMake(latitude, longitude)