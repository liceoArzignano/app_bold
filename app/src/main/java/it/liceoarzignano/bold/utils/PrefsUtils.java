package it.liceoarzignano.bold.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Calendar;
import java.util.Date;

import it.liceoarzignano.bold.R;

public class PrefsUtils {
    private static final String DEFAULT_DATE = "2000-01-01";
    public static final String IS_TEACHER = "isTeacher_key";
    public static final String SUGGESTIONS = "showSuggestions_key";
    public static final String NOTIF_NEWS = "notification_news_key";
    public static final String NOTIF_EVENT = "notification_events_key";
    public static final String ADDRESS = "address_key";
    public static final String SAFE_DONE = "doneSetup";
    public static final String EXTRA_PREFS = "extraPrefs";
    public static final String KEY_INTRO_SCREEN = "introScreen";
    public static final String KEY_SAFE_SETUP = "hasCompletedSetup";
    public static final String KEY_INTRO_DRAWER = "introDrawer";
    public static final String KEY_INITIAL_DAY = "introDay";
    public static final String KEY_VERSION = "introVersion";
    private static final String KEY_CURRENT_SCHOOL_YEAR = "currentSchoolYear";
    public static final String KEY_QUARTER_SELECTOR = "quarterSelector";
    private static final String SAFE_PREFS = "SafePrefs";
    private static final String KEY_SAFE_PASSED = "safetyNetPassed";
    private static final String ANALYTICS = "analytics_key";
    private static final String NOTIF_EVENT_TIME = "notification_events_time_key";
    private static final String USERNAME = "username_key";

    /**
     * Force enable Google Analytics Tracker
     * if overlay requires it (used for test builds)
     *
     * @param context: used to access SharedPreferences
     * @param overlay: boolean xml overlay value
     */
    public static void enableTrackerIfOverlayRequests(Context context, boolean overlay) {
        if (overlay) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putBoolean(ANALYTICS, true).apply();
        }
    }

    /**
     * Getter for initialDayKey
     *
     * @param context: used to get sharedprefs
     * @return the date of the day the first usage happened
     */
    public static Date getFirstUsageDate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(EXTRA_PREFS, Context.MODE_PRIVATE);
        return DateUtils.stringToDate(prefs.getString(KEY_INITIAL_DAY, DEFAULT_DATE));
    }

    /**
     * Check if device is running on lollipop or higher
     * (mostly for animations and vector drawable related stuffs)
     *
     * @return true if there's api21+
     */
    public static boolean isNotLegacy() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Determine if a mark has been assigned during the first or second quarter
     *
     * @param date given mark's date
     * @return true if first quarter, else false
     */
    public static boolean isFirstQuarter(Context context, Date date) {
        return DateUtils.stringToDate(context.getString(R.string.config_quarter_change))
                .after(date);
    }

    /**
     * Determine if given package is installed
     *
     * @param context to invoke pm
     * @return true if installed
     */
    public static boolean hasGDrive(Context context) {
        try {
            PackageInfo info = context.getPackageManager()
                    .getPackageInfo("com.google.android.apps.docs", 0);
            return info.applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            // Gotta catch 'em all
            return false;
        }

    }

    /**
     * Get notification topic
     *
     * @param context to read SharedPreferences
     * @return address-based topic
     */
    public static String getTopic(Context context) {
        if (isTeacher(context)) {
            return "addr_6";
        } else {
            switch (getAddress(context)) {
                case "1":
                    return "addr_1";
                case "2":
                    return "addr_2";
                case "3":
                    return "addr_3";
                case "4":
                    return "addr_4";
                case "5":
                    return "addr_5";
                default:
                    return "addr_6";
            }
        }
    }

    /**
     * Check if device is connected has an internet connection
     *
     * @param context to access ConnectivityManager
     * @return true if device is connected to the internet
     */
    public static boolean hasNoInternetConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() == null;
    }

    /**
     * Check if GoogleMobileServices are installed on the device
     *
     * @param context to check GoogleApi availability
     * @return true if GMS is not installed
     */
    public static boolean hasNoGMS(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) !=
                ConnectionResult.SUCCESS;
    }

    /**
     * Check if device passed SafetyNet test
     *
     * @param context to access the shared preferences
     * @return false if test failed
     */
    public static boolean hasPassedSafetyNetTest(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SAFE_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_SAFE_PASSED, false);
    }

    /**
     * Store SafetyNet test results
     *
     * @param context to access the shared preferences editor
     * @param hasPassed whether the device passed the test
     */
    public static void setSafetyNetResults(Context context, boolean hasPassed) {
        SharedPreferences prefs = context.getSharedPreferences(SAFE_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SAFE_PASSED, hasPassed).apply();
    }

    /*
     * SharedPreferences getters
     *
     * @param context: used to access SharedPreferences
     * @return the value from SharedPreferences
     */

    public static boolean isTeacher(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(IS_TEACHER, false);
    }

    public static boolean hasAnalytics(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(ANALYTICS, true);
    }

    public static boolean hasSuggestions(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(SUGGESTIONS, true);
    }

    public static boolean hasNewsNotification(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(NOTIF_NEWS, true);
    }

    public static boolean hasEventsNotification(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(NOTIF_EVENT, true);
    }

    static String getEventsNotificationTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(NOTIF_EVENT_TIME, "0");
    }

    public static String getAddress(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(ADDRESS, "0");
    }

    public static String getCurrentSchoolYear(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(EXTRA_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CURRENT_SCHOOL_YEAR, "2016");
    }

    public static void setCurrentSchoolYear(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(EXTRA_PREFS, Context.MODE_PRIVATE);
        Calendar cal = Calendar.getInstance();
        prefs.edit().putString(KEY_CURRENT_SCHOOL_YEAR, String.valueOf(cal.get(Calendar.YEAR)))
                .apply();
    }

    public static int getCurrentQuarter(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(EXTRA_PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_QUARTER_SELECTOR, 0);
    }

    public static void setCurrentQuarter(Context context, int value) {
        SharedPreferences prefs = context.getSharedPreferences(EXTRA_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_QUARTER_SELECTOR, value).apply();
    }

    public static String appVersionKey(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(EXTRA_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_VERSION, "0");
    }

    public static String userNameKey(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getString(USERNAME, "");
    }

    public static void setAddress(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(ADDRESS, value).putBoolean(IS_TEACHER, false)
                .apply();
    }

    public static void setTeacherMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(IS_TEACHER, true).putString(ADDRESS, "0")
                .apply();
    }

    public static boolean hasSafe(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SAFE_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(SAFE_DONE, false);
    }
}
