package com.alpenraum.shimstack

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.alpenraum.shimstack.base.di.ShimstackGeneratedModule
import com.alpenraum.shimstack.base.di.databaseModule
import com.alpenraum.shimstack.base.di.navigationModule
import com.alpenraum.shimstack.base.di.platformModule
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class ShimstackApplication : Application() {
    companion object {
        lateinit var appContext: Context
        lateinit var activity: MainActivity
    }

    private val activityCallback =
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?
            ) {
                (activity as? MainActivity)?.let {
                    ShimstackApplication.activity = it
                }
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(
                activity: Activity,
                outState: Bundle
            ) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        }

    override fun onTerminate() {
        super.onTerminate()
        unregisterActivityLifecycleCallbacks(activityCallback)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        registerActivityLifecycleCallbacks(
            activityCallback
        )

        startKoin {
            modules(
                navigationModule(),
                ShimstackGeneratedModule().module,
                databaseModule(),
                platformModule()
            )
        }
    }
}