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
import android.net.ConnectivityManager;
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
import it.liceoarzignano.bold.events.AlarmService;
import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventsController;
import it.liceoarzignano.bold.marks.Mark;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static android.content.Context.MODE_PRIVATE;

public class Utils {
    public static final String IS_TEACHER = "isTeacher_key";
    public static final String SUGGESTIONS = "showSuggestions_key";
    public static final String NOTIF_NEWS = "notification_news_key";
    public static final String NOTIF_EVENT = "notification_events_key";
    public static final String ADDRESS = "address_key";
    public static final String SAFE_DONE = "doneSetup";
    public static final String EXTRA_PREFS = "extraPrefs";
    public static final String KEY_INTRO_SCREEN = "introScreen";
    public static final String KEY_INTRO_DRAWER = "introDrawer";
    public static final String KEY_INITIAL_DAY = "introDay";
    public static final String KEY_VERSION = "introVersion";
    private static final String SAFE_PREFS = "SafePrefs";
    private static final String KEY_SAFE_PASSED = "safetyNetPassed";
    private static final String ANALYTICS = "analytics_key";
    private static final String NOTIF_EVENT_TIME = "notification_events_time_key";
    private static final String USERNAME = "username_key";

    private static SharedPreferences mPrefs;

    /**
     * Animate fab with delay
     *
     * @param mFab        :  the fab that will be animated
     * @param shouldShow: whether to show the fab
     */
    public static void animFab(final FloatingActionButton mFab, final boolean shouldShow) {
        new Handler().postDelayed(() -> {
            if (shouldShow) {
                mFab.show();
            } else {
                mFab.hide();
            }
        }, 500);
    }

    /**
     * Animate fab and showcase it
     *
     * @param mContext: used to create materialshowcase
     * @param mFab:     fab that will be animated and exposed
     * @param mTitle:   showcase title
     * @param mMessage: showcase message
     * @param mKey:     showcase key to show it only the first time
     */
    @SuppressWarnings("SameParameterValue")
    public static void animFabIntro(final Activity mContext, final FloatingActionButton mFab,
                                    final String mTitle, final String mMessage, final String mKey) {
        final SharedPreferences mPrefs = mContext.getSharedPreferences(EXTRA_PREFS, MODE_PRIVATE);
        final boolean isFirstTime = mPrefs.getBoolean(mKey, true);
        if (isNotLegacy()) {
            mFab.show();
        }
        new Handler().postDelayed(() -> {
            mFab.setVisibility(View.VISIBLE);
            if (isFirstTime) {
                mPrefs.edit().putBoolean(mKey, false).apply();
                new MaterialTapTargetPrompt.Builder(mContext)
                        .setTarget(mFab)
                        .setPrimaryText(mTitle)
                        .setSecondaryText(mMessage)
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
        Calendar mCal = Calendar.getInstance();
        return mCal.getTime();
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
     * @param mContext: used to access SharedPreferences
     * @param mOverlay: boolean xml overlay value
     */
    static void enableTrackerIfOverlayRequests(Context mContext, boolean mOverlay) {
        if (mOverlay) {
            PreferenceManager.getDefaultSharedPreferences(mContext)
                    .edit().putBoolean(ANALYTICS, true).apply();
        }
    }

    /**
     * Getter for initialDayKey
     *
     * @param mContext: used to get sharedprefs
     * @return the date of the day the first usage happened
     */
    private static String getFirstUsageDate(Context mContext) {
        SharedPreferences mPrefs = mContext.getSharedPreferences(EXTRA_PREFS,
                MODE_PRIVATE);
        return mPrefs.getString(KEY_INITIAL_DAY, "2000-01-01");
    }

    /**
     * Convert calendar dialog results to a string that will be
     * saved in the events database.
     * </br>
     * Format: yyyy-mm-dd (Locale.IT format)
     *
     * @param mYear:  year from the date picker dialog
     * @param mMonth: month from the date picker dialog
     * @param mDay:   day of the month from the date picker dialog
     * @return string with formatted date
     */
    static Date rightDate(int mYear, int mMonth, int mDay) {
        Calendar mCal = Calendar.getInstance();
        mCal.set(mYear, mMonth, mDay);
        return mCal.getTime();
    }

    /**
     * Convert date to string for UI elements
     *
     * @param mDate given date
     * @return yyyy-MM-dd string
     */
    public static String dateToStr(Date mDate) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(mDate);
    }

    /**
     * Use for adaptive feature discovery
     *
     * @param mContext: used to call getFirstUsageDate(Context)
     * @return true if user has been using this for more than one week
     */
    static boolean hasUsedForMoreThanOneWeek(Context mContext) {
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
        String mFirstDay = getFirstUsageDate(mContext);

        if (mFirstDay.equals("2000-01-01")) {
            return false;
        }

        try {
            Date mDate = getToday();
            Calendar mFirstCal = Calendar.getInstance();
            Calendar mSecondCal = Calendar.getInstance();
            mSecondCal.setTimeInMillis(mDate.getTime());
            mDate = mFormat.parse(mFirstDay);
            mFirstCal.setTimeInMillis(mDate.getTime());

            int mDiff = mSecondCal.get(Calendar.DAY_OF_YEAR) - mFirstCal.get(Calendar.DAY_OF_YEAR);

            return mFirstCal.get(Calendar.YEAR) == mSecondCal.get(Calendar.YEAR) && mDiff > 7;
        } catch (ParseException e) {
            Log.e("Utils", e.getMessage());
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
    public static String[] getAverageElements(Context mContext, int mFilter) {
        int mSize = 0;
        Realm mRealm = Realm.getInstance(((BoldApp) mContext.getApplicationContext()).getConfig());
        List<Mark> mMarks;
        switch (mFilter) {
            case 1:
                mMarks = mRealm.where(Mark.class).equalTo("isFirstQuarter", true).findAll();
                break;
            case 2:
                mMarks = mRealm.where(Mark.class).equalTo("isFirstQuarter", false).findAll();
                break;
            default:
                mMarks = mRealm.where(Mark.class).findAll();
        }

        ArrayList<String> mElements = new ArrayList<>();

        for (Mark mMark : mMarks) {
            if (!mElements.contains(mMark.getTitle())) {
                mElements.add(mMark.getTitle());
                mSize++;
            }
        }

        return mElements.toArray(new String[mSize]);
    }

    /**
     * Get event category description from int
     *
     * @param mCategory: event icon value
     * @return category name
     */
    public static String eventCategoryToString(Context mContext, int mCategory) {
        switch (mCategory) {
            case 0:
                return mContext.getString(R.string.event_spinner_test);
            case 1:
                return mContext.getString(R.string.event_spinner_school);
            case 2:
                return mContext.getString(R.string.event_spinner_bday);
            case 3:
                return mContext.getString(R.string.event_spinner_homework);
            case 4:
                return mContext.getString(R.string.event_spinner_reminder);
            case 5:
                return mContext.getString(R.string.event_spinner_hang_out);
            default:
                return mContext.getString(R.string.event_spinner_other);
        }
    }

    /**
     * Convert string to date
     *
     * @param mStringDate yyyy-MM-dd date
     * @return java date
     */
    static Date stringToDate(String mStringDate) {
        if (mStringDate.length() != 10 || !mStringDate.contains("-")) {
            throw new IllegalArgumentException(mStringDate
                    + ": invalid format. Must be yyyy-MM-dd");
        }

        try {
            SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
            return mFormat.parse(mStringDate);
        } catch (ParseException e) {
            Log.e("Utils", e.getMessage());
            return new Date();
        }
    }

    /**
     * Determine if a mark has been assigned during the first or second quarter
     *
     * @param mDate given mark's date
     * @return true if first quarter, else false
     */
    public static boolean isFirstQuarter(Context mContext, Date mDate) {
        return stringToDate(mContext.getString(R.string.config_quarter_change)).after(mDate);
    }

    /**
     * Determine if given package is installed
     *
     * @param mContext to invoke pm
     * @param mPkg     package name
     * @return true if installed
     */
    public static boolean hasPackage(Context mContext, String mPkg) {
        try {
            PackageInfo mPackageInfo = mContext.getPackageManager()
                    .getPackageInfo(mPkg, 0);
            return mPackageInfo.applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            // Gotta catch 'em all
            return false;
        }

    }

    /**
     * Get notification topic
     *
     * @param mContext to read SharedPreferences
     * @return address-based topic
     */
    static String getTopic(Context mContext) {
        if (isTeacher(mContext)) {
            return "addr_6";
        } else {
            switch (getAddress(mContext)) {
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
    public static String getTomorrowInfo(Context mContext) {
        Resources mRes = mContext.getResources();
        String mContent = null;
        boolean isFirstElement = true;

        int mIcon;
        int mTest = 0;
        int mAtSchool = 0;
        int mBirthday = 0;
        int mHomework = 0;
        int mReminder = 0;
        int mHangout = 0;
        int mOther = 0;

        // Use realm instead of RealmController to avoid NPE when onBoot intent is broadcast'ed
        EventsController mController = new EventsController(
                ((BoldApp) mContext.getApplicationContext()).getConfig());
        List<Event> mEvents = mController.getAll();

        List<Event> mUpcomingEvents = new ArrayList<>();

        // Create tomorrow events list
        //noinspection Convert2streamapi
        for (Event mEvent : mEvents) {
            if (Utils.getToday().equals(mEvent.getDate())) {
                mUpcomingEvents.add(mEvent);
            }
        }

        if (mUpcomingEvents.isEmpty()) {
            return null;
        }

        // Get data
        for (Event mEvent : mUpcomingEvents) {
            mIcon = mEvent.getIcon();
            switch (mIcon) {
                case 0:
                    mTest++;
                    break;
                case 1:
                    mAtSchool++;
                    break;
                case 2:
                    mBirthday++;
                    break;
                case 3:
                    mHomework++;
                    break;
                case 4:
                    mReminder++;
                    break;
                case 5:
                    mHangout++;
                    break;
                case 6:
                    mOther++;
                    break;
            }
        }

        // Test
        if (mTest > 0) {
            // First element
            mContent = mRes.getQuantityString(R.plurals.notification_message_first, mTest, mTest)
                    + " " + mRes.getQuantityString(R.plurals.notification_test, mTest, mTest);
            isFirstElement = false;
        }

        // School
        if (mAtSchool > 0) {
            if (isFirstElement) {
                mContent = mRes.getQuantityString(R.plurals.notification_message_first,
                        mAtSchool, mAtSchool) + " ";
                isFirstElement = false;
            } else {
                mContent += mBirthday == 0 && mHangout == 0 && mOther == 0 ? " " +
                        String.format(mRes.getString(R.string.notification_message_half),
                                mAtSchool) :
                        String.format(mRes.getString(R.string.notification_message_half),
                                mAtSchool);
            }
            mContent += " " + mRes.getQuantityString(R.plurals.notification_school,
                    mAtSchool, mAtSchool);
        }

        // Birthday
        if (mBirthday > 0) {
            if (isFirstElement) {
                mContent = mRes.getQuantityString(R.plurals.notification_message_first,
                        mBirthday, mBirthday) + " ";
                isFirstElement = false;
            } else {
                mContent += String.format(mRes.getString(R.string.notification_message_half),
                        mBirthday);
            }
            mContent += " " + mRes.getQuantityString(R.plurals.notification_birthday,
                    mBirthday, mBirthday);
        }

        // Homework
        if (mHomework > 0) {
            if (isFirstElement) {
                mContent = mRes.getQuantityString(R.plurals.notification_message_first,
                        mHomework, mHomework) + " ";
                isFirstElement = false;
            } else {
                mContent += String.format(mRes.getString(R.string.notification_message_half),
                        mHomework);
            }

            mContent += " " + mRes.getQuantityString(R.plurals.notification_homework,
                    mHomework, mHomework);
        }

        // Reminder
        if (mReminder > 0) {
            if (isFirstElement) {
                mContent = mRes.getQuantityString(R.plurals.notification_message_first,
                        mReminder, mReminder) + " ";
                isFirstElement = false;
            } else {
                mContent += String.format(mRes.getString(R.string.notification_message_half),
                        mReminder);
            }
            mContent += " " + mRes.getQuantityString(R.plurals.notification_reminder,
                    mReminder, mReminder);
        }

        // Hangout
        if (mHangout > 0) {
            if (isFirstElement) {
                mContent = mRes.getQuantityString(R.plurals.notification_message_first,
                        mHangout, mHangout) + " ";
                isFirstElement = false;
            } else {
                mContent += String.format(mRes.getString(R.string.notification_message_half),
                        mAtSchool);
            }
            mContent += " " + mRes.getQuantityString(R.plurals.notification_meeting,
                    mHangout, mHangout);
        }

        // Other
        if (mOther > 0) {
            if (isFirstElement) {
                mContent = mRes.getQuantityString(R.plurals.notification_message_first,
                        mOther, mOther);
                mContent += " ";
            } else {
                mContent += String.format(mRes.getString(R.string.notification_message_half),
                        mOther);
            }
            mContent += " " + mRes.getQuantityString(R.plurals.notification_other,
                    mOther, mOther);
        }

        mContent += " " + mRes.getString(R.string.notification_message_end);

        return mContent;
    }

    /**
     * Create an event notification that will be fired later
     */
    public static void makeEventNotification(Context mContext) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());

        switch (getEventsNotificationTime(mContext)) {
            case "0":
                if (mCalendar.get(Calendar.HOUR_OF_DAY) >= 6) {
                    // If it's too late for today's notification, plan one for tomorrow
                    mCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.get(Calendar.DAY_OF_MONTH) + 1);
                }
                mCalendar.set(Calendar.HOUR_OF_DAY, 6);
                break;
            case "1":
                if (mCalendar.get(Calendar.HOUR_OF_DAY) >= 15) {
                    // If it's too late for today's notification, plan one for tomorrow
                    mCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.get(Calendar.DAY_OF_MONTH) + 1);
                }
                mCalendar.set(Calendar.HOUR_OF_DAY, 15);
                break;
            case "2":
                if (mCalendar.get(Calendar.HOUR_OF_DAY) >= 21) {
                    // If it's too late for today's notification, plan one for tomorrow
                    mCalendar.set(Calendar.DAY_OF_MONTH, mCalendar.get(Calendar.DAY_OF_MONTH) + 1);
                }
                mCalendar.set(Calendar.HOUR_OF_DAY, 21);
                break;
        }

        // Set alarm
        Intent mNotifIntent = new Intent(mContext, AlarmService.class);
        AlarmManager mAlarmManager = (AlarmManager)
                mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent mPendingIntent = PendingIntent.getService(mContext, 0, mNotifIntent, 0);
        mAlarmManager.set(AlarmManager.RTC, mCalendar.getTimeInMillis(), mPendingIntent);
    }

    public static boolean hasInternetConnection(Context mContext) {
        ConnectivityManager mManager = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return mManager.getActiveNetworkInfo() != null;
    }

    public static boolean hasPassedSafetyNetTest(Context mContext) {
        SharedPreferences mPrefs = mContext.getSharedPreferences(SAFE_PREFS, MODE_PRIVATE);
        return mPrefs.getBoolean(KEY_SAFE_PASSED, false);
    }

    public static void setSafetyNetResults(Context mContext, boolean hasPassed) {
        SharedPreferences mPrefs = mContext.getSharedPreferences(SAFE_PREFS, MODE_PRIVATE);
        mPrefs.edit().putBoolean(KEY_SAFE_PASSED, hasPassed).apply();
    }

    /*
     * SharedPreferences getters
     *
     * @param context: used to access SharedPreferences
     * @return the value from SharedPreferences
     */

    public static boolean isTeacher(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mPrefs.getBoolean(IS_TEACHER, false);
    }

    static boolean hasAnalytics(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mPrefs.getBoolean(ANALYTICS, true);
    }

    public static boolean hasSuggestions(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mPrefs.getBoolean(SUGGESTIONS, true);
    }

    public static boolean hasNewsNotification(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mPrefs.getBoolean(NOTIF_NEWS, true);
    }

    public static boolean hasEventsNotification(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mPrefs.getBoolean(NOTIF_EVENT, true);
    }

    private static String getEventsNotificationTime(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mPrefs.getString(NOTIF_EVENT_TIME, "0");
    }

    public static String getAddress(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mPrefs.getString(ADDRESS, "0");
    }

    static String appVersionKey(Context mContext) {
        mPrefs = mContext.getSharedPreferences(EXTRA_PREFS, MODE_PRIVATE);
        return mPrefs.getString(KEY_VERSION, "0");
    }

    public static String userNameKey(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mPrefs.getString(USERNAME, "");
    }

    public static void setAddress(Context mContext, String mValue) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPrefs.edit().putString(ADDRESS, mValue).putBoolean(IS_TEACHER, false)
                .apply();
    }

    public static void setTeacherMode(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPrefs.edit().putBoolean(IS_TEACHER, true).putString(ADDRESS, "0")
                .apply();
    }

    public static boolean hasSafe(Context mContext) {
        mPrefs = mContext.getSharedPreferences(SAFE_PREFS, MODE_PRIVATE);
        return mPrefs.getBoolean(SAFE_DONE, false);
    }

}
