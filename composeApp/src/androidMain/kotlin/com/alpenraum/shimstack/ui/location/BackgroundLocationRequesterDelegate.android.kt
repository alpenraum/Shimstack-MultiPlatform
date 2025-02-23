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

actual class BackgroundLocationRequesterDelegate(
    private val context: Context,
    private val activity: Lazy<Activity>,
    private val locationForegroundPermissionDelegate: ForegroundLocationRequesterDelegate
) : LocationRequesterDelegate {
    override fun getPermissionState(): PermissionState =
        when (locationForegroundPermissionDelegate.getPermissionState()) {
            PermissionState.GRANTED ->
                checkPermissions(context, activity, backgroundLocationPermissions)

            PermissionState.DENIED,
            PermissionState.NOT_DETERMINED
            -> PermissionState.NOT_DETERMINED
        }

    override suspend fun providePermission() {
        activity.value.providePermissions(backgroundLocationPermissions) {
            throw Exception(
                "Failed to request background location permission"
            )
        }
        getPermissionState()
    }

    override fun openSettingPage() {
        context.openAppSettingsPage(AppPermissions.LOCATION_BACKGROUND) {}
    }
}

private val backgroundLocationPermissions: List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        emptyList()
    }