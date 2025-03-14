package com.alpenraum.shimstack.ui.location.model

enum class PermissionState {
    /**
     * Indicates that the permission has not been requested yet
     */
    NOT_DETERMINED,

    /**
     * Indicates that the permission has been requested and accepted.
     */
    GRANTED,

    /**
     * Indicates that the permission has been requested but the user denied the permission
     */
    DENIED;

    /**
     * Extension function to check if the permission is not granted
     */
    fun notGranted(): Boolean = this != GRANTED

    fun granted(): Boolean = this == GRANTED
}