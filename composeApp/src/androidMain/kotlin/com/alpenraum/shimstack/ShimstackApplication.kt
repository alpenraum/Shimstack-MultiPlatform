package com.alpenraum.shimstack

import android.app.Application
import android.content.Context
import com.alpenraum.shimstack.base.di.GeneratedPlatformModule
import com.alpenraum.shimstack.base.di.ShimstackGeneratedModule
import com.alpenraum.shimstack.base.di.databaseModule
import com.alpenraum.shimstack.base.di.navigationModule
import com.alpenraum.shimstack.base.di.platformModule
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class ShimstackApplication : Application() {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        startKoin {
            modules(
                navigationModule(),
                ShimstackGeneratedModule().module,
                databaseModule(),
                platformModule(),
                GeneratedPlatformModule().module
            )
        }
    }
}