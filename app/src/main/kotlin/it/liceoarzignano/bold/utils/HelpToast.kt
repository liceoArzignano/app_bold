package it.liceoarzignano.bold.utils

import android.content.Context
import android.widget.Toast
import it.liceoarzignano.bold.R

class HelpToast(context: Context, key: String) {

    init {
        val preferences = context.getSharedPreferences(HELP_TOAST_PREFS, Context.MODE_PRIVATE)

        if (!preferences.getBoolean(key, false)) {
            preferences.edit().putBoolean(key, true).apply()
            Toast.makeText(context, R.string.intro_toast_long_press, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private val HELP_TOAST_PREFS = "HelpToastPrefs"
        val KEY_MARK_LONG_PRESS = "MarksLongPress"
        val KEY_EVENT_LONG_PRESS = "EventsLongPress"
        val KEY_NEWS_LONG_PRESS = "NewsLongPress"
    }
}
