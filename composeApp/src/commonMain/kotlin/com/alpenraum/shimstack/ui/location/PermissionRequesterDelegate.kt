package com.alpenraum.shimstack.ui.location

import com.alpenraum.shimstack.ui.location.model.PermissionState

interface PermissionRequesterDelegate {
    suspend fun getPermissionState(): PermissionState

    suspend fun providePermission()

    fun openSettingPage()
}

expect class LocationServiceRequesterDelegate : PermissionRequesterDelegate

expect class BackgroundLocationRequesterDelegate : PermissionRequesterDelegate

expect class ForegroundLocationRequesterDelegate : PermissionRequesterDelegate

expect class NotificationRequesterDelegate : PermissionRequesterDelegate