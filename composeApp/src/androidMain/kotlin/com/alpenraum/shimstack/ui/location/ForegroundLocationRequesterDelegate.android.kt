package com.alpenraum.shimstack.ui.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import com.alpenraum.shimstack.base.checkPermissions
import com.alpenraum.shimstack.base.openAppSettingsPage
import com.alpenraum.shimstack.base.providePermissions
import com.alpenraum.shimstack.ui.location.model.AppPermissions
import com.alpenraum.shimstack.ui.location.model.PermissionState

actual class ForegroundLocationRequesterDelegate(
    private val context: Context,
    private val activity: Lazy<Activity>
) : LocationRequesterDelegate {
    override fun getPermissionState(): PermissionState = checkPermissions(activity, fineLocationPermissions)

    override suspend fun providePermission() {
        activity.value.providePermissions(fineLocationPermissions) {
            throw Exception(
                "Failed to request foreground location permission"
            )
        }
    }

    override fun openSettingPage() {
        context.openAppSettingsPage(AppPermissions.LOCATION_FOREGROUND) {}
    }
}

internal val fineLocationPermissions: List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }