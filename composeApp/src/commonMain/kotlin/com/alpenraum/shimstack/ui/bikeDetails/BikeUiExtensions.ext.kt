package com.alpenraum.shimstack.ui.bikeDetails

import androidx.compose.runtime.Composable
import com.alpenraum.shimstack.domain.model.bike.Bike
import com.alpenraum.shimstack.domain.model.measurementunit.MeasurementUnitType
import com.alpenraum.shimstack.domain.model.measurementunit.Pressure
import com.alpenraum.shimstack.domain.model.suspension.Damping
import com.alpenraum.shimstack.domain.model.suspension.Suspension
import com.alpenraum.shimstack.domain.model.tire.Tire
import com.alpenraum.shimstack.ui.homescreen.UIDataLabel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.bar
import shimstackmultiplatform.composeapp.generated.resources.comp
import shimstackmultiplatform.composeapp.generated.resources.feet
import shimstackmultiplatform.composeapp.generated.resources.front
import shimstackmultiplatform.composeapp.generated.resources.hsc
import shimstackmultiplatform.composeapp.generated.resources.hsr
import shimstackmultiplatform.composeapp.generated.resources.inch
import shimstackmultiplatform.composeapp.generated.resources.km
import shimstackmultiplatform.composeapp.generated.resources.lsc
import shimstackmultiplatform.composeapp.generated.resources.lsr
import shimstackmultiplatform.composeapp.generated.resources.meter
import shimstackmultiplatform.composeapp.generated.resources.miles
import shimstackmultiplatform.composeapp.generated.resources.mm
import shimstackmultiplatform.composeapp.generated.resources.pressure
import shimstackmultiplatform.composeapp.generated.resources.psi
import shimstackmultiplatform.composeapp.generated.resources.rear
import shimstackmultiplatform.composeapp.generated.resources.rebound
import shimstackmultiplatform.composeapp.generated.resources.tokens

@Composable
fun Bike.getTireUIData(isMetric: Boolean) =
    Pair(
        UIDataLabel.Simple(
            stringResource(Res.string.front),
            this.frontTire.getFormattedPressure(isMetric)
        ),
        UIDataLabel.Simple(
            stringResource(Res.string.rear),
            this.rearTire.getFormattedPressure(isMetric)
        )
    )

@Composable
fun Bike.getFrontSuspensionUIData(isMetric: Boolean): ImmutableList<UIDataLabel>? =
    frontSuspension?.let {
        return getSuspensionUIData(it, isMetric).toImmutableList()
    }

@Composable
fun Bike.getRearSuspensionUIData(isMetric: Boolean): ImmutableList<UIDataLabel>? =
    rearSuspension?.let {
        return getSuspensionUIData(it, isMetric).toImmutableList()
    }

@Composable
private fun Bike.getSuspensionUIData(
    suspension: Suspension,
    isMetric: Boolean
): ImmutableList<UIDataLabel> {
    with(suspension) {
        val uiData = ArrayList<UIDataLabel>()

        val compressionData = getDampingUIData(compression, false)
        uiData.add(compressionData)
        val reboundData = getDampingUIData(rebound, true)
        uiData.add(reboundData)

        uiData.add(
            UIDataLabel.Simple(
                stringResource(Res.string.pressure),
                pressure.toFormattedString(isMetric)
            )
        )
        uiData.add(
            UIDataLabel.Simple(stringResource(Res.string.tokens), tokens.toString())
        )

        return uiData.toImmutableList()
    }
}

@Composable
private fun Bike.getDampingUIData(
    damping: Damping,
    isRebound: Boolean
) = if (damping.highSpeedFromClosed != null) {
    UIDataLabel.Complex(
        mapOf(
            stringResource(if (isRebound) Res.string.lsr else Res.string.lsc) to damping.lowSpeedFromClosed.toString(),
            stringResource(if (isRebound) Res.string.hsr else Res.string.hsc) to damping.highSpeedFromClosed.toString()
        )
    )
} else {
    UIDataLabel.Simple(
        stringResource(if (isRebound) Res.string.rebound else Res.string.comp),
        damping.lowSpeedFromClosed.toString()
    )
}

fun MeasurementUnitType.getPressureStringRes() = if (this.isMetric()) Res.string.bar else Res.string.psi

@Composable
fun MeasurementUnitType.getPressureLabel(): String = stringResource(getPressureStringRes())

fun MeasurementUnitType.getSmallDistanceStringRes() = if (this.isMetric()) Res.string.mm else Res.string.inch

@Composable
fun MeasurementUnitType.getSmallDistanceLabel(): String = stringResource(getSmallDistanceStringRes())

fun MeasurementUnitType.getLargeDistanceStringRes() = if (this.isMetric()) Res.string.km else Res.string.miles

@Composable
fun MeasurementUnitType.getLargeDistanceLabel(): String = stringResource(getLargeDistanceStringRes())

fun MeasurementUnitType.getMediumDistanceStringRes() = if (this.isMetric()) Res.string.meter else Res.string.feet

@Composable
fun MeasurementUnitType.getMediumDistanceLabel(): String = stringResource(getMediumDistanceStringRes())

// region Tire
@Composable
fun Tire.getFormattedPressure(isMetric: Boolean) = pressure.toFormattedString(isMetric)

@Composable
fun Tire.getFormattedTireWidth(isMetric: Boolean) =
    if (isMetric) {
        "${width.asMetric()} ${stringResource(Res.string.mm)}"
    } else {
        "${width.asImperial()}${stringResource(Res.string.inch)}"
    }

@Composable
fun Tire.getFormattedInternalRimWidth() = "$internalRimWidthInMM ${stringResource(Res.string.mm)}"

// endregion
// region Suspension
@Composable
fun Suspension.getFormattedCompression() =
    "${compression.lowSpeedFromClosed} ${stringResource(Res.string.lsc)}${
        compression.highSpeedFromClosed?.let {
            " / $it ${
                stringResource(Res.string.hsc)
            }"
        } ?: ""
    }"

@Composable
fun Suspension.getFormattedRebound() =
    "${rebound.lowSpeedFromClosed} ${stringResource(Res.string.lsr)}${
        rebound.highSpeedFromClosed?.let {
            " / $it ${
                stringResource(Res.string.hsr)
            }"
        } ?: ""
    }"

@Composable
fun Suspension.getFormattedPressure(isMetric: Boolean) = pressure.toFormattedString(isMetric)

@Composable
fun Suspension.getFormattedTravel(isMetric: Boolean) =
    if (isMetric) {
        "$travel ${stringResource(Res.string.mm)}"
    } else {
        "$travel ${stringResource(Res.string.mm)}"
    }

// endRegion

// Region Pressure
@Composable
fun Pressure.toFormattedString(isMetric: Boolean): String =
    if (isMetric) {
        "${asMetric()} ${stringResource(Res.string.bar)}"
    } else {
        "${asImperial()} ${stringResource(Res.string.psi)}"
    }

// endRegion

// Region Bike