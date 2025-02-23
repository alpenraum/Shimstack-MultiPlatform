package com.alpenraum.shimstack.ui.location

import com.alpenraum.shimstack.ui.location.model.LocationPermission

class PermissionNotGrantedException(
    val permission: LocationPermission
) : Exception()