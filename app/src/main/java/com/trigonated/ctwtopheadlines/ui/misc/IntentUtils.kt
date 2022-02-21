package com.trigonated.ctwtopheadlines.ui.misc

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Utility functions for dealing with intents.
 */
object IntentUtils {
    /** Launch a view intent, which usually opens the web-browser on the provided [url]. */
    fun openUrl(context: Context, url: String): Boolean {
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}