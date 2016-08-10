package it.liceoarzignano.bold;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import it.liceoarzignano.bold.external.showcase.MaterialShowcaseView;
import it.liceoarzignano.bold.marks.Mark;

public class Utils {
    private static SharedPreferences preferences;

    /**
     * Animate fab with delay
     *
     * @param fab :  the fab that will be animated
     */
    static void animFab(final FloatingActionButton fab) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.hide();
            }
        }, 500);
    }

    /**
     * Animate fab and showcase it
     *
     * @param context: used to create materialshowcase
     * @param fab: fab that will be animated and exposed
     * @param text: showcase text
     * @param key: showcase key to show it only the first time
     */
    public static void animFabIntro(final Activity context,
                                    final FloatingActionButton fab,
                                    final String text, final String key) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (hasApi21()) {
                    fab.show();
                }
                fab.setVisibility(View.VISIBLE);
                new MaterialShowcaseView.Builder(context)
                        .setTarget(fab)
                        .setContentText(text)
                        .singleUse(key)
                        .show();
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
    static void enableTrackerIfOverlayRequests(Context context, boolean overlay) {
        if (overlay) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putBoolean("analytics_key", true).apply();
        }
    }

    /**
     * Getter for HomePrefs' initialDayKey
     *
     * @param context: used to get sharedprefs
     * @return the date of the day the first usage happened
     */
    private static String getFirstUsageDate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("HomePrefs", Context.MODE_PRIVATE);
        return prefs.getString("initialDayKey", "2000-01-01");
    }

    /**
     * Convert calendar dialog results to a string that will be
     * saved in the events database.
     * </br>
     * Format: yyyy-mm-dd (Locale.IT format)
     *
     * @param year:  year from the date picker dialog
     * @param month: month from the date picker dialog
     * @param day:   day of the month from the date picker dialog
     * @return string with formatted date
     */
    static String rightDate(int year, int month, int day) {
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
     * Use for adaptive feature discovery
     * TODO: implement ^^^^
     * @param context: used to call getFirstUsageDate(Context)
     * @param today: today date (will be confronted)
     * @return true if user has been using this for more than one week
     */
    @SuppressWarnings("unused")
    public static boolean hasUsedForMoreThanOneWeek(Context context, String today) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
        String first = getFirstUsageDate(context);

        if (first.equals("2000-01-01")) {
            return false;
        }

        try {
            Date date = format.parse(today);
            Calendar c = Calendar.getInstance();
            Calendar d = Calendar.getInstance();
            d.setTimeInMillis(date.getTime());
            date = format.parse(first);
            c.setTimeInMillis(date.getTime());

            int diff = d.get(Calendar.DAY_OF_YEAR) - c.get(Calendar.DAY_OF_YEAR);

            return c.get(Calendar.YEAR) == d.get(Calendar.YEAR) && diff > 7;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if device is running on lollipop or higher
     * (mostly for animations and vector drawable related stuffs)
     *
     * @return true if there's api21+
     */
    static boolean hasApi21() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Get array of subjects with at least one mark for averages list
     *
     * @return array of subjects
     */
    public static String[] getAverageElements() {
        int size = 0;
        Realm realm = Realm.getInstance(BoldApp.getAppRealmConfiguration());
        List<Mark> marks = realm.where(Mark.class).findAll();

        ArrayList<String> elements = new ArrayList<>();

        for (Mark mark : marks) {
            if (!elements.contains(mark.getTitle())) {
                elements.add(mark.getTitle());
                size++;
            }
        }

        return elements.toArray(new String[size]);
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

    static boolean trackerEnabled(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("analytics_key", true);
    }

    static boolean hasSuggestions(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("showSuggestions_key", true);
    }

    static boolean hasSafe(Context context) {
        preferences = context.getSharedPreferences("SafePrefs", Context.MODE_PRIVATE);
        return preferences.getBoolean("doneSetup", false);
    }

    static boolean hasNotification(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("notification_key", true);
    }

    public static String getAddress(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("address_key", "1");
    }

    static String appVersionKey(Context context) {
        preferences = context.getSharedPreferences("HomePrefs", Context.MODE_PRIVATE);
        return preferences.getString("appVersionKey", "0");
    }

    public static String userNameKey(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("username_key", " ");
    }

    static String getNotificationTime(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("notificationtime_key", "0");
    }

}
