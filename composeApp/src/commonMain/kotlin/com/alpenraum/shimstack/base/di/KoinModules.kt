package com.alpenraum.shimstack.base.di

import org.koin.ksp.generated.module

fun koinModules() = listOf(databaseModule(), platformModule(), ShimstackGeneratedModule().module, navigationModule())