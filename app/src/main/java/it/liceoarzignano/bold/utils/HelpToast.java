package it.liceoarzignano.bold.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.widget.Toast;

public class HelpToast {
    private static final String HELP_TOAST_PREFS = "HelpToastPrefs";
    public static final String KEY_EVENT_LONG_PRESS = "EventsLongPress";
    public static final String KEY_NEWS_LONG_PRESS = "NewsLongPress";

    public HelpToast(Context context, @StringRes int title, String key) {
        SharedPreferences preferences = context.getSharedPreferences(HELP_TOAST_PREFS,
                Context.MODE_PRIVATE);

        if (preferences.getBoolean(key, false)) {
            return;
        }

        preferences.edit().putBoolean(key, true).apply();
        Toast.makeText(context, title, Toast.LENGTH_LONG).show();

    }
}
