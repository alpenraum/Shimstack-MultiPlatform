package com.alpenraum.shimstack.ui.location

import com.alpenraum.shimstack.base.openAppSettingsPage
import com.alpenraum.shimstack.ui.location.model.PermissionState
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class NotificationRequesterDelegate : PermissionRequesterDelegate {
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun getPermissionState(): PermissionState =
        suspendCoroutine { continuation ->
            notificationCenter.getNotificationSettingsWithCompletionHandler {
                continuation.resume(
                    when (it?.authorizationStatus) {
                        UNAuthorizationStatusAuthorized -> PermissionState.GRANTED
                        UNAuthorizationStatusDenied -> PermissionState.DENIED
                        else -> PermissionState.NOT_DETERMINED
                    }
                )
            }
        }

    override suspend fun providePermission() {
        println("Requesting notification pemrission")
        notificationCenter.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert
        ) { _, _ -> }
    }

    override fun openSettingPage() {
        println("Trying to open app settings page")
        openAppSettingsPage()
    }
}