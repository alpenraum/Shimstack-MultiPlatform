package com.alpenraum.shimstack.ui.base.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import platform.UIKit.UIColor
import platform.UIKit.UIScreen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenWidth(): Dp =
    LocalWindowInfo.current.containerSize.width
        .pxToPoint()
        .dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenHeight(): Dp =
    LocalWindowInfo.current.containerSize.height
        .pxToPoint()
        .dp

fun Int.pxToPoint(): Double = this.toDouble() / UIScreen.mainScreen.scale

fun Color.toUIColor(): UIColor {
    val argb = this.toArgb()

    val blue = argb and 0xff
    val green = argb shr 8 and 0xff
    val red = argb shr 16 and 0xff
    val alpha = argb shr 24 and 0xff

    return UIColor(
        red = red / 255.0,
        green = green / 255.0,
        blue = blue / 255.0,
        alpha = alpha / 255.0
    )
}