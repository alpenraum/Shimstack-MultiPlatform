package com.alpenraum.shimstack.base.di

import com.alpenraum.shimstack.ShimstackApplication
import com.alpenraum.shimstack.data.datastore.createDataStore
import com.alpenraum.shimstack.data.db.DatabaseFactory
import com.alpenraum.shimstack.ui.location.BackgroundLocationRequesterDelegate
import com.alpenraum.shimstack.ui.location.ForegroundLocationRequesterDelegate
import com.alpenraum.shimstack.ui.location.LocationService
import com.alpenraum.shimstack.ui.location.LocationServiceRequesterDelegate
import com.alpenraum.shimstack.ui.location.NotificationRequesterDelegate
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

        single {
            LocationService()
        }

        single {
            BackgroundLocationRequesterDelegate(ShimstackApplication.appContext, lazy { ShimstackApplication.activity }, get())
        }
        single {
            ForegroundLocationRequesterDelegate(ShimstackApplication.appContext, lazy { ShimstackApplication.activity })
        }
        single {
            LocationServiceRequesterDelegate(ShimstackApplication.appContext, get())
        }
        single {
            NotificationRequesterDelegate(ShimstackApplication.appContext, lazy { ShimstackApplication.activity })
        }
    }