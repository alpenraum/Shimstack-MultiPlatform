package com.alpenraum.shimstack.base

import platform.Foundation.NSBundle
import platform.UIKit.UIDevice
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual object BuildInfo {
    actual fun isDebug() = Platform.isDebugBinary

    actual fun osName() = "iOS"

    actual fun osVersion() = UIDevice.currentDevice.systemVersion

    actual fun appVersion() = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: ""

    actual fun appBuild() = NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String ?: ""
}