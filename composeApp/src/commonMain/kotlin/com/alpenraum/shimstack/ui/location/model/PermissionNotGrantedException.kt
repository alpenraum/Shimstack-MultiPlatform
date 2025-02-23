package com.alpenraum.shimstack.ui.location

import com.alpenraum.shimstack.ui.location.model.AppPermissions

class PermissionNotGrantedException(
    val permission: AppPermissions
) : Exception()