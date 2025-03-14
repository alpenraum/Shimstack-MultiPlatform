package com.alpenraum.shimstack.base.di

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
        single { createDataStore() }

        single {
            DatabaseFactory().getDatabaseBuilder()
        }

        single {
            LocationService(get(), get())
        }

        single {
            BackgroundLocationRequesterDelegate(get<ForegroundLocationRequesterDelegate>())
        }
        single {
            ForegroundLocationRequesterDelegate()
        }
        single {
            LocationServiceRequesterDelegate()
        }
        single {
            NotificationRequesterDelegate()
        }
    }