package com.alpenraum.shimstack.ui.base

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual class ShareService {
    @OptIn(BetaInteropApi::class)
    actual fun showTextChooser(
        text: String,
        title: String
    ) {
        val activityItems =
            listOf(
                NSString.create(string = text)
            )
        val activityViewController = UIActivityViewController(activityItems = activityItems, applicationActivities = null)

        // Get the top-most view controller to present the activity view controller
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(activityViewController, animated = true, completion = null)
    }
}