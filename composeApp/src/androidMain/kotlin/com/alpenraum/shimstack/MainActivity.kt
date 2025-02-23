package com.alpenraum.shimstack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer
import com.alpenraum.shimstack.ui.base.navigation.DeeplinkManager
import com.alpenraum.shimstack.ui.base.navigation.NavigationTarget
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity :
    ComponentActivity(),
    KoinComponent {
    private val deeplinkManager by inject<DeeplinkManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            App()

            DisposableEffect(Unit) {
                val listener =
                    Consumer<Intent> {
                        handleDeeplinkIntent(it)
                    }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }
        }
    }

    private fun handleDeeplinkIntent(intent: Intent) =
        intent.extras?.getString(DeeplinkManager.NAV_ARG)?.let {
            NavigationTarget.valueOfIgnoreCase(it)?.let { target ->
                deeplinkManager.onNewNavigationAction(target)
            }
        }
}

@Preview
@Composable
private fun AppAndroidPreview() {
    App()
}