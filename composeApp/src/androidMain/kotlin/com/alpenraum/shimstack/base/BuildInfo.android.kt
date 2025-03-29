package com.alpenraum.shimstack.base

import android.os.Build
import com.alpenraum.shimstack.BuildConfig

actual object BuildInfo {
    actual fun isDebug() = BuildConfig.DEBUG

    actual fun osName() = "Android"

    actual fun osVersion() = Build.VERSION.SDK_INT.toString()

    actual fun appVersion() = BuildConfig.VERSION_NAME

    actual fun appBuild() = BuildConfig.VERSION_CODE.toString()
}