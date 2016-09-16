package it.liceoarzignano.bold;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import it.liceoarzignano.bold.marks.Mark;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static android.content.Context.MODE_PRIVATE;

public class Utils {
    private static final String HOME_PREFS = "HomePrefs";
    private static final String SAFE_PREFS = "SafePrefs";
    private static final String INITAL_DAY = "initialDayKey";
    private static final String ANALYTICS = "analytics_key";
    public static final String ISTEACHER = "isTeacher_key";
    public static final String SUGGESTIONS = "showSuggestions_key";
    public static final String NOTIFICATION = "notification_key";
    public static final String NOTIF_TIME = "notificationtime_key";
    public static final String ADDRESS = "address_key";
    private static final String USERNAME = "username_key";
    private static final String APP_VERSION = "appVersionKey";
    public static final String SAFE_DONE = "doneSetup";

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
     * @param fab:     fab that will be animated and exposed
     * @param title:   showcase title
     * @param message: showcase message
     * @param key:     showcase key to show it only the first time
     */
    public static void animFabIntro(final Activity context, final FloatingActionButton fab,
                                    final String title, final String message, final String key) {
        final SharedPreferences prefs = context.getSharedPreferences(HOME_PREFS, MODE_PRIVATE);
        final boolean firstUsage = prefs.getBoolean(key, true);
        if (hasApi21()) {
            fab.show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.setVisibility(View.VISIBLE);
                if (firstUsage) {
                    prefs.edit().putBoolean(key, false).apply();
                    new MaterialTapTargetPrompt.Builder(context)
                            .setTarget(fab)
                            .setPrimaryText(title)
                            .setSecondaryText(message)
                            .setBackgroundColourFromRes(R.color.colorAccentDark)
                            .show();
                }
            }
        }, 500);

    }

    /**
     * Get today date
     *
     * @return today formatted in Locale.ITALIAN (yyyy-mm-dd)
     */
    public static String getToday() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        StringBuilder builder = new StringBuilder();
        builder.append(calendar.get(Calendar.YEAR)).append("-");
        if (month < 10) {
            builder.append("0");
        }
        builder.append(month).append("-");
        if (day < 10) {
            builder.append("0");
        }
        builder.append(day);
        return builder.toString();
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
                    .edit().putBoolean(ANALYTICS, true).apply();
        }
    }

    /**
     * Getter for HomePrefs' initialDayKey
     *
     * @param context: used to get sharedprefs
     * @return the date of the day the first usage happened
     */
    private static String getFirstUsageDate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(HOME_PREFS, MODE_PRIVATE);
        return prefs.getString(INITAL_DAY, "2000-01-01");
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
     *
     * @param context: used to call getFirstUsageDate(Context)
     * @return true if user has been using this for more than one week
     */
    static boolean hasUsedForMoreThanOneWeek(Context context) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
        String first = getFirstUsageDate(context);

        if (first.equals("2000-01-01")) {
            return false;
        }

        try {
            Date date = format.parse(getToday());
            Calendar c = Calendar.getInstance();
            Calendar d = Calendar.getInstance();
            d.setTimeInMillis(date.getTime());
            date = format.parse(first);
            c.setTimeInMillis(date.getTime());

            int diff = d.get(Calendar.DAY_OF_YEAR) - c.get(Calendar.DAY_OF_YEAR);

            return c.get(Calendar.YEAR) == d.get(Calendar.YEAR) && diff > 7;
        } catch (ParseException e) {
            if (android.support.compat.BuildConfig.DEBUG) {
                Log.e("Backup", e.getMessage());
            }
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
    public static String[] getAverageElements(int filter) {
        int size = 0;
        Realm realm = Realm.getInstance(BoldApp.getAppRealmConfiguration());
        List<Mark> marks;
        switch (filter) {
            case 1:
                marks = realm.where(Mark.class).equalTo("isFirstQuarter", true).findAll();
                break;
            case 2:
                marks = realm.where(Mark.class).equalTo("isFirstQuarter", false).findAll();
                break;
            default:
                marks = realm.where(Mark.class).findAll();
        }

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
     * Get event category description from int
     *
     * @param category: event icon value
     * @return category name
     */
    static String eventCategoryToString(int category) {
        Context context = BoldApp.getBoldContext();
        switch (category) {
            case 0:
                return context.getString(R.string.event_spinner_test);
            case 1:
                return context.getString(R.string.event_spinner_school);
            case 2:
                return context.getString(R.string.event_spinner_bday);
        }
        return category == 3 && isTeacher(context) ?
                context.getString(R.string.event_spinner_hang_out) :
                context.getString(R.string.event_spinner_other);
    }

    /**
     * Convert string to date
     *
     * @param string yyyy-MM-dd date
     * @return java date
     */
    static Date stringToDate(String string) {
        if (string == null || string.length() != 10 || !string.contains("-")) {
            throw new IllegalArgumentException(string + ": invalid format. Must be yyyy-MM-dd");
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
            return format.parse(string);
        } catch (ParseException e) {
            if (android.support.compat.BuildConfig.DEBUG) {
                Log.e("Backup", e.getMessage());
            }
            return new Date();
        }
    }

    /**
     * Determine if a mark has been assigned during the first or second quarter
     *
     * @param markDate given mark's date
     * @return true if first quarter, else false
     */
    public static boolean isFirstQuarter(String markDate) {
        return stringToDate(BoldApp.getBoldContext().getString(R.string.config_quarter_change))
                .after(stringToDate(markDate));
    }

    /*
     * SharedPreferences getters
     *
     * @param context: used to access SharedPreferences
     * @return the value from SharedPreferences
     */

    public static boolean isTeacher(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(ISTEACHER, false);
    }

    public static boolean hasAnalytics(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(ANALYTICS, true);
    }

    public static boolean hasSuggestions(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(SUGGESTIONS, true);
    }

    public static boolean hasNotification(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(NOTIFICATION, true);
    }

    public static String getNotificationTime(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(NOTIF_TIME, "0");
    }

    public static String getAddress(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(ADDRESS, "0");
    }

    static String appVersionKey(Context context) {
        preferences = context.getSharedPreferences(HOME_PREFS, MODE_PRIVATE);
        return preferences.getString(APP_VERSION, "0");
    }

    public static String userNameKey(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(USERNAME, " ");
    }

    static void setAddress(Context context, String value) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(ADDRESS, value).putBoolean("isTeacher_key", false)
                .apply();
    }

    static void setTeacherMode(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(ISTEACHER, true).putString(ADDRESS, "0")
                .apply();
    }

    public static boolean hasSafe(Context context) {
        preferences = context.getSharedPreferences(SAFE_PREFS, MODE_PRIVATE);
        return preferences.getBoolean(SAFE_DONE, false);
    }

}
