package it.liceoarzignano.bold;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
    private static final String INITIAL_DAY = "initialDayKey";
    private static final String ANALYTICS = "analytics_key";
    public static final String ISTEACHER = "isTeacher_key";
    public static final String SUGGESTIONS = "showSuggestions_key";
    public static final String NOTIF_NEWS = "notification_news_key";
    public static final String NOTIF_EVENT = "notification_events_key";
    private static final String NOTIF_EVENT_TIME = "notification_events_time_key";
    public static final String ADDRESS = "address_key";
    private static final String USERNAME = "username_key";
    private static final String APP_VERSION = "appVersionKey";
    public static final String SAFE_DONE = "doneSetup";

    private static SharedPreferences mPrefs;

    /**
     * Animate fab with delay
     *
     * @param mFab :  the fab that will be animated
     * @param shouldShow: whether to show the fab
     */
    public static void animFab(final FloatingActionButton mFab, final boolean shouldShow) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (shouldShow) {
                    mFab.show();
                } else {
                    mFab.hide();
                }
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
        final SharedPreferences mPrefs = mContext.getSharedPreferences(HOME_PREFS, MODE_PRIVATE);
        final boolean isFirstTime = mPrefs.getBoolean(mKey, true);
        if (isNotLegacy()) {
            mFab.show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
            }
        }, 500);

    }

    /**
     * Get today date
     *
     * @return today formatted in Locale.ITALIAN (yyyy-mm-dd)
     */
    public static String getToday() {
        Calendar mCal = Calendar.getInstance();
        int mMonth = mCal.get(Calendar.MONTH) + 1;
        int mDay = mCal.get(Calendar.DAY_OF_MONTH);
        StringBuilder mBuilder = new StringBuilder();
        mBuilder.append(mCal.get(Calendar.YEAR)).append("-");
        if (mMonth < 10) {
            mBuilder.append("0");
        }
        mBuilder.append(mMonth).append("-");
        if (mDay < 10) {
            mBuilder.append("0");
        }
        mBuilder.append(mDay);
        return mBuilder.toString();
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
     * Getter for HomePrefs' initialDayKey
     *
     * @param mContext: used to get sharedprefs
     * @return the date of the day the first usage happened
     */
    private static String getFirstUsageDate(Context mContext) {
        SharedPreferences mPrefs = mContext.getSharedPreferences(HOME_PREFS, MODE_PRIVATE);
        return mPrefs.getString(INITIAL_DAY, "2000-01-01");
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
    static String rightDate(int mYear, int mMonth, int mDay) {
        String mDate;
        mDate = mYear + "-";
        if (mMonth < 10) {
            mDate += "0";
        }
        mDate = mDate + mMonth + "-";
        if (mDay < 10) {
            mDate += "0";
        }
        mDate += mDay;
        return mDate;
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
            Date mDate = mFormat.parse(getToday());
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
    public static String[] getAverageElements(int mFilter) {
        int mSize = 0;
        Realm mRealm = Realm.getInstance(BoldApp.getAppRealmConfiguration());
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
    public static String eventCategoryToString(int mCategory) {
        Context mContext = BoldApp.getContext();
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
        if (mStringDate == null || mStringDate.length() != 10 || !mStringDate.contains("-")) {
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
    public static boolean isFirstQuarter(String mDate) {
        return stringToDate(BoldApp.getContext().getString(R.string.config_quarter_change))
                .after(stringToDate(mDate));
    }

    /**
     * Determine if given package is installed
     *
     * @param mContext to invoke pm
     * @param mPkg package name
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

    /*
     * SharedPreferences getters
     *
     * @param context: used to access SharedPreferences
     * @return the value from SharedPreferences
     */

    public static boolean isTeacher(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mPrefs.getBoolean(ISTEACHER, false);
    }

    public static boolean hasAnalytics(Context mContext) {
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

    public static String getEventsNotificationTime(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mPrefs.getString(NOTIF_EVENT_TIME, "0");
    }

    public static String getAddress(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mPrefs.getString(ADDRESS, "0");
    }

    static String appVersionKey(Context mContext) {
        mPrefs = mContext.getSharedPreferences(HOME_PREFS, MODE_PRIVATE);
        return mPrefs.getString(APP_VERSION, "0");
    }

    public static String userNameKey(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mPrefs.getString(USERNAME, " ");
    }

    static void setAddress(Context mContext, String mValue) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPrefs.edit().putString(ADDRESS, mValue).putBoolean("isTeacher_key", false)
                .apply();
    }

    static void setTeacherMode(Context mContext) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPrefs.edit().putBoolean(ISTEACHER, true).putString(ADDRESS, "0")
                .apply();
    }

    public static boolean hasSafe(Context mContext) {
        mPrefs = mContext.getSharedPreferences(SAFE_PREFS, MODE_PRIVATE);
        return mPrefs.getBoolean(SAFE_DONE, false);
    }

}
