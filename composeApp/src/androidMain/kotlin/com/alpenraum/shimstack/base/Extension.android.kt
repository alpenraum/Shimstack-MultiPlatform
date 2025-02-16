package com.alpenraum.shimstack.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.alpenraum.shimstack.ui.location.model.LocationPermission
import com.alpenraum.shimstack.ui.location.model.PermissionState

internal fun Context.openIntent(
    action: String,
    newData: Uri? = null,
    onError: (Exception) -> Unit
) {
    try {
        val intent =
            Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                newData?.let { data = it }
            }
        startActivity(intent)
    } catch (e: Exception) {
        onError(e)
    }
}

internal fun checkPermissions(
    context: Context,
    activity: Lazy<Activity>,
    permissions: List<String>
): PermissionState {
    permissions.ifEmpty { return PermissionState.GRANTED }
    val status: List<Int> =
        permissions.map {
            context.checkSelfPermission(it)
        }
    val isAllGranted: Boolean = status.all { it == PackageManager.PERMISSION_GRANTED }
    if (isAllGranted) return PermissionState.GRANTED

    val isAllRequestRationaleShown =
        try {
            permissions.all {
                !activity.value.shouldShowRequestPermissionRationale(it)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            true
        }
    return if (isAllRequestRationaleShown) {
        PermissionState.NOT_DETERMINED
    } else {
        PermissionState.DENIED
    }
}

internal fun Activity.providePermissions(
    permissions: List<String>,
    onError: (Throwable) -> Unit
) {
    try {
        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            100
        )
    } catch (t: Throwable) {
        onError(t)
    }
}

internal fun Context.openAppSettingsPage(
    permission: LocationPermission,
    onError: () -> Unit
) {
    openIntent(
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        newData = Uri.parse("package:$packageName"),
        onError = { onError() }
    )
}