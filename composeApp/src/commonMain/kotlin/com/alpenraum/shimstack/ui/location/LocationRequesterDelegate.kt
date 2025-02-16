package com.alpenraum.shimstack.ui.location

import com.alpenraum.shimstack.ui.location.model.PermissionState

interface LocationRequesterDelegate {
    fun getPermissionState(): PermissionState

    suspend fun providePermission()

    fun openSettingPage()
}