package it.liceoarzignano.bold;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.liceoarzignano.bold.events.AlarmService;
import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventsController;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.MarksController;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

@SuppressWarnings("SameParameterValue")
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    private static final String DEFAULT_DATE = "2000-01-01";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String IS_TEACHER = "isTeacher_key";
    public static final String SUGGESTIONS = "showSuggestions_key";
    public static final String NOTIF_NEWS = "notification_news_key";
    public static final String NOTIF_EVENT = "notification_events_key";
    public static final String ADDRESS = "address_key";
    public static final String SAFE_DONE = "doneSetup";
    public static final String EXTRA_PREFS = "extraPrefs";
    public static final String KEY_INTRO_SCREEN = "introScreen";
    public static final String KEY_SAFE_SETUP = "hasCompletedSetup";
    static final String KEY_INTRO_DRAWER = "introDrawer";
    static final String KEY_INITIAL_DAY = "introDay";
    static final String KEY_VERSION = "introVersion";
    private static final String SAFE_PREFS = "SafePrefs";
    private static final String KEY_SAFE_PASSED = "safetyNetPassed";
    private static final String ANALYTICS = "analytics_key";
    private static final String NOTIF_EVENT_TIME = "notification_events_time_key";
    private static final String USERNAME = "username_key";

    /**
     * Animate fab with delay
     *
     * @param fab        :  the fab that will be animated
     * @param shouldShow: whether to show the fab
     */
    static void animFab(final FloatingActionButton fab, final boolean shouldShow) {
        new Handler().postDelayed(() -> {
            if (shouldShow) {
                fab.show();
            } else {
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
    @SuppressWarnings("SameParameterValue")
    public static void animFabIntro(final Activity context, final FloatingActionButton fab,
                                    final String title, final String message, final String key) {
        final SharedPreferences prefs = context.getSharedPreferences(EXTRA_PREFS,
                Context.MODE_PRIVATE);
        final boolean isFirstTime = prefs.getBoolean(key, true);
        if (isNotLegacy()) {
            fab.show();
        }
        new Handler().postDelayed(() -> {
            fab.setVisibility(View.VISIBLE);
            if (isFirstTime) {
                prefs.edit().putBoolean(key, false).apply();
                new MaterialTapTargetPrompt.Builder(context)
                        .setTarget(fab)
                        .setPrimaryText(title)
                        .setSecondaryText(message)
                        .setBackgroundColourFromRes(R.color.colorAccentDark)
                        .show();
            }
        }, 500);

    }

    /**
     * Get today date
     *
     * @return today
     */
    public static Date getToday() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    /**
     * Get today date
     *
     * @return today
     */
    public static String getTodayStr() {
        return dateToStr(getToday());
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
     * Getter for initialDayKey
     *
     * @param context: used to get sharedprefs
     * @return the date of the day the first usage happened
     */
    private static String getFirstUsageDate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(EXTRA_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(KEY_INITIAL_DAY, DEFAULT_DATE);
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
    static Date rightDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    /**
     * Convert date to string for UI elements
     *
     * @param date given date
     * @return yyyy-MM-dd string
     */
    public static String dateToStr(Date date) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date);
    }

    /**
     * Use for adaptive feature discovery
     *
     * @param context: used to call getFirstUsageDate(Context)
     * @return true if user has been using this for more than one week
     */
    static boolean hasUsedForMoreThanOneWeek(Context context) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ITALIAN);
        String firstDay = getFirstUsageDate(context);

        if (DEFAULT_DATE.equals(firstDay)) {
            return false;
        }

        try {
            Date date = getToday();
            Calendar firstCal = Calendar.getInstance();
            Calendar secondCal = Calendar.getInstance();
            secondCal.setTimeInMillis(date.getTime());
            date = format.parse(firstDay);
            firstCal.setTimeInMillis(date.getTime());

            int diff = secondCal.get(Calendar.DAY_OF_YEAR) - firstCal.get(Calendar.DAY_OF_YEAR);

            return firstCal.get(Calendar.YEAR) == secondCal.get(Calendar.YEAR) && diff > 7;
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
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
     * Get array of subjects with at least one mark for averages list
     *
     * @return array of subjects
     */
    public static String[] getAverageElements(Context context, int filter) {
        int size = 0;
        MarksController controller = new MarksController(
                ((BoldApp) context.getApplicationContext()).getConfig());

        List<Mark> marks = controller.getFilteredMarks(null, filter);
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
    public static String eventCategoryToString(Context context, int category) {
        switch (category) {
            case 0:
                return context.getString(R.string.event_spinner_test);
            case 1:
                return context.getString(R.string.event_spinner_school);
            case 2:
                return context.getString(R.string.event_spinner_bday);
            case 3:
                return context.getString(R.string.event_spinner_homework);
            case 4:
                return context.getString(R.string.event_spinner_reminder);
            case 5:
                return context.getString(R.string.event_spinner_hang_out);
            default:
                return context.getString(R.string.event_spinner_other);
        }
    }

    /**
     * Convert string to date
     *
     * @param string yyyy-MM-dd date
     * @return java date
     */
    private static Date stringToDate(String string) {
        if (string.length() != 10 || !string.contains("-")) {
            throw new IllegalArgumentException(string
                    + ": invalid format. Must be yyyy-MM-dd");
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ITALIAN);
            return format.parse(string);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
            return new Date();
        }
    }

    /**
     * Determine if a mark has been assigned during the first or second quarter
     *
     * @param date given mark's date
     * @return true if first quarter, else false
     */
    public static boolean isFirstQuarter(Context context, Date date) {
        return stringToDate(context.getString(R.string.config_quarter_change)).after(date);
    }

    /**
     * Determine if given package is installed
     *
     * @param context to invoke pm
     * @param pkg     package name
     * @return true if installed
     */
    public static boolean hasPackage(Context context, String pkg) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(pkg, 0);
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
    static String getTopic(Context context) {
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
     * Fetch all the upcoming events and create a description
     *
     * @return content for events notification
     */
    public static String getTomorrowInfo(Context context) {
        Resources res = context.getResources();
        String content = "";

        int categories[] = new int[] {
                0 /* test */, 0 /* atSchool */, 0 /* bday */,
                0 /* homeworks */, 0 /* reminder */, 0 /* meeting */,
                0 /* others */
        };

        int messages[] = new int[] {
                R.plurals.notification_test, R.plurals.notification_school,
                R.plurals.notification_birthday, R.plurals.notification_homework,
                R.plurals.notification_reminder, R.plurals.notification_meeting,
                R.plurals.notification_other
        };

        EventsController controller = new EventsController(
                ((BoldApp) context.getApplicationContext()).getConfig());
        List<Event> events = controller.getTomorrow();

        if (events.isEmpty()) {
            return null;
        }

        // Get data
        for (Event event : events) {
            categories[event.getIcon()]++;
        }

        // Build message
        for (int counter = 0; counter < categories.length; counter++) {
            if (categories[counter] > 0) {
                content = eventInfoBuilder(res, content, categories[counter], messages[counter]);
            }
        }
        content += " " + res.getString(R.string.notification_message_end);

        return content;
    }

    /**
     * Build part of summary for notification (only the given category)
     *
     * @param res to fetch strings
     * @param orig original message
     * @param size quantity of events
     * @param id string id
     * @return updated message
     */
    private static String eventInfoBuilder(Resources res, String orig, int size, int id) {
        String val = orig.isEmpty() ?
                res.getQuantityString(R.plurals.notification_message_first, size, size) :
                orig + res.getQuantityString(R.plurals.notification_message_half, size, size);

        return val + " " + res.getQuantityString(id, size, size);
    }

    /**
     * Create an event notification that will be fired later
     */
    public static void makeEventNotification(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        switch (getEventsNotificationTime(context)) {
            case "0":
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 6) {
                    // If it's too late for today's notification, plan one for tomorrow
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                }
                calendar.set(Calendar.HOUR_OF_DAY, 6);
                break;
            case "1":
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 15) {
                    // If it's too late for today's notification, plan one for tomorrow
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                }
                calendar.set(Calendar.HOUR_OF_DAY, 15);
                break;
            case "2":
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 21) {
                    // If it's too late for today's notification, plan one for tomorrow
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                }
                calendar.set(Calendar.HOUR_OF_DAY, 21);
                break;
        }

        // Set alarm
        AlarmManager manager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pIntent = PendingIntent.getService(context, 0,
                new Intent(context, AlarmService.class), 0);
        manager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pIntent);
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

    /**
     * Start an avd animation using proper class basing on api level
     *
     * @param drawable animated drawable
     */
    public static void animateAVD(Drawable drawable) {
        if (!Utils.isNotLegacy()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((AnimatedVectorDrawable) drawable).start();
        } else {
            ((AnimatedVectorDrawableCompat) drawable).start();
        }
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

    static boolean hasAnalytics(Context context) {
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

    private static String getEventsNotificationTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(NOTIF_EVENT_TIME, "0");
    }

    public static String getAddress(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(ADDRESS, "0");
    }

    static String appVersionKey(Context mContext) {
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
