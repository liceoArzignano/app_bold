package it.liceoarzignano.bold;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;

public class Utils {
    private static SharedPreferences preferences;

    /**
     
     * animate fab with delay
     *
     * @param show: boolean show / hide state
     * @param fab:  the fab that will be animated
     */
    public static void animFab(final boolean show, final FloatingActionButton fab) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }
        }, 500);
    }

    /**
     
     * Force enable Google Analytics Tracker
     * if overlay requires it (used for test builds)
     *
     * @param context: used to access SharedPreferences
     * @param overlay: boolean xml overlay value
     */
    public static void enableTrackerIfOverlayRequests(Context context, boolean overlay) {
        if (overlay) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putBoolean("analytics_key", true).apply();
        }
    }

    /**
     * Convert calendar dialog results to a string that will be
     * saved in the events database.
     * Format: yyyy-mm-dd
     *
     * @param year:  year from the date picker dialog
     * @param month: month from the date picker dialog
     * @param day:   day of the month from the date picker dialog
     * @return string with formatted date
     */
    public static String rightDate(int year, int month, int day) {
        String ret;
        ret = year + "-";
        if (month < 10) {
            ret += "0";
        }
        ret = ret + month + "-";
        if (day < 10) {
            ret += "0";
        }
        ret += day;
        return ret;
    }

    /**
     
     * SharedPreferences getters
     *
     * @param context: used to access SharedPreferences
     * @return the value from SharedPreferences
     */

    public static boolean isTeacher(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("isTeacher_key", false);
    }

    public static boolean trackerEnabled(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("analytics_key", true);
    }

    public static boolean hasSuggestions(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("showSuggestions_key", true);
    }

    public static boolean hasSafe(Context context) {
        preferences = context.getSharedPreferences("SafePrefs", Context.MODE_PRIVATE);
        return preferences.getBoolean("doneSetup", false);
    }

    public static boolean hasNotification(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("notification_key", true);
    }

    public static String getAddress(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("address_key", "0");
    }

    public static String appVersionKey(Context context) {
        preferences = context.getSharedPreferences("HomePrefs", Context.MODE_PRIVATE);
        return preferences.getString("appVersionKey", "0");
    }

    public static String userNameKey(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("username_key", " ");
    }

    public static String getNotificationTime(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("notificationtime_key", "0");
    }

}
