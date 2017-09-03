package it.liceoarzignano.bold.utils

import android.content.Context
import android.support.annotation.StringRes
import android.widget.Toast

class HelpToast(context: Context, @StringRes title: Int, key: String) {

    init {
        val preferences = context.getSharedPreferences(HELP_TOAST_PREFS, Context.MODE_PRIVATE)

        if (!preferences.getBoolean(key, false)) {
            preferences.edit().putBoolean(key, true).apply()
            Toast.makeText(context, title, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private val HELP_TOAST_PREFS = "HelpToastPrefs"
        val KEY_EVENT_LONG_PRESS = "EventsLongPress"
        val KEY_NEWS_LONG_PRESS = "NewsLongPress"
    }
}
