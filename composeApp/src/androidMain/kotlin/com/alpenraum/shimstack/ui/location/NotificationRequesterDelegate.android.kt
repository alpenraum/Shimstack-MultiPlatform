package com.alpenraum.shimstack.ui.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.alpenraum.shimstack.base.checkPermissions
import com.alpenraum.shimstack.base.openAppSettingsPage
import com.alpenraum.shimstack.base.providePermissions
import com.alpenraum.shimstack.ui.location.model.AppPermissions
import com.alpenraum.shimstack.ui.location.model.PermissionState

actual class NotificationRequesterDelegate(
    private val context: Context,
    private val activity: Lazy<Activity>
) : PermissionRequesterDelegate {
    override suspend fun getPermissionState(): PermissionState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermissions(activity, listOf(Manifest.permission.POST_NOTIFICATIONS))
        } else {
            PermissionState.GRANTED
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun providePermission() {
        activity.value.providePermissions(listOf(Manifest.permission.POST_NOTIFICATIONS)) {
            throw Exception(
                "Failed to request Post notification permission"
            )
        }
    }

    override fun openSettingPage() {
        context.openAppSettingsPage(AppPermissions.SHOW_NOTIFICATIONS) {}
    }
}