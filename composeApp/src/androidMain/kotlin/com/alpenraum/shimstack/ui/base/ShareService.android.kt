package com.alpenraum.shimstack.ui.base

import android.content.Intent
import com.alpenraum.shimstack.ShimstackApplication

actual class ShareService {
    actual fun showTextChooser(
        text: String,
        title: String
    ) {
        val shareIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
        ShimstackApplication.appContext.startActivity(
            Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}