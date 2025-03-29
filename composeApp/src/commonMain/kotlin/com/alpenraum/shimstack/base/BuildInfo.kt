package com.alpenraum.shimstack.base

expect object BuildInfo {
    fun isDebug(): Boolean

    fun osName(): String

    fun osVersion(): String

    fun appVersion(): String

    fun appBuild(): String
}