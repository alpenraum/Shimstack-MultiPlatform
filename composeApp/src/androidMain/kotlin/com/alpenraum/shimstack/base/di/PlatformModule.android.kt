package com.alpenraum.shimstack.base.di

import com.alpenraum.shimstack.ShimstackApplication
import com.alpenraum.shimstack.data.datastore.createDataStore
import com.alpenraum.shimstack.data.db.DatabaseFactory
import org.koin.core.annotation.ComponentScan
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
    module {
        single {
            createDataStore(ShimstackApplication.appContext)
        }
        single {
            DatabaseFactory(ShimstackApplication.appContext).getDatabaseBuilder()
        }
    }