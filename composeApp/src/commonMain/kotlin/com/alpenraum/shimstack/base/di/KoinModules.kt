package com.alpenraum.shimstack.base.di

import com.alpenraum.shimstack.base.network.HttpClientProvider
import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.koin.ksp.generated.module

fun koinModules() =
    listOf(
        databaseModule(),
        platformModule(),
        ShimstackGeneratedModule().module,
        navigationModule(),
        module {
            single<HttpClient> { HttpClientProvider.client }
    })