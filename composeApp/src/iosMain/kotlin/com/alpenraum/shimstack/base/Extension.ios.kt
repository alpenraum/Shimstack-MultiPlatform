package com.alpenraum.shimstack.base

import com.alpenraum.shimstack.ui.location.LocationPermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

fun openNSUrl(string: String) {
    println("opening setting page for $string")
    val settingsUrl: NSURL = NSURL.URLWithString(string)!!
    UIApplication.sharedApplication.openURL(settingsUrl, emptyMap<Any?, Any>()) {}
}

fun openAppSettingsPage() {
    openNSUrl(UIApplicationOpenSettingsURLString)
}

fun CoroutineScope.observePermission(
    frequency: Long = LocationPermissionManager.PERMISSION_CHECK_FLOW_FREQUENCY,
    block: suspend () -> Unit
): Job =
    launch {
        while (true) {
            block()
            delay(frequency)
        }
    }