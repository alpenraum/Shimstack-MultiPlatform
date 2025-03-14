package com.alpenraum.shimstack.base.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(
            koinModules()
        )
    }
}