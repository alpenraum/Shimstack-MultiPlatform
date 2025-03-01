package com.alpenraum.shimstack.ui.base.compose.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableTargetMarker
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alpenraum.shimstack.ui.base.compose.state.NativeMapState
import kotlinx.collections.immutable.ImmutableList

@Composable
expect fun NativeMap(
    mapState: NativeMapState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content:
        @Composable @NativeMapComposable
        () -> Unit = {}
)

@Composable
@NativeMapComposable
expect fun Polyline(
    points: ImmutableList<Coordinate>,
    color: Color = MaterialTheme.colorScheme.primary,
    width: Dp = 4.dp
)

data class Coordinate(
    val latitude: Double,
    val longitude: Double
)

@Retention(AnnotationRetention.BINARY)
@ComposableTargetMarker(description = "Native Map Composable")
@Target(
    AnnotationTarget.FILE,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.TYPE,
    AnnotationTarget.TYPE_PARAMETER
)
annotation class NativeMapComposable