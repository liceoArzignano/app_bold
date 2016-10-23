package it.liceoarzignano.bold;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.Sort;
import it.liceoarzignano.bold.events.AlarmService;
import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventListActivity;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.home.HomeAdapter;
import it.liceoarzignano.bold.home.HomeCard;
import it.liceoarzignano.bold.intro.BenefitsActivity;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.MarkListActivity;
import it.liceoarzignano.bold.news.NewsListActivity;
import it.liceoarzignano.bold.realm.RealmController;
import it.liceoarzignano.bold.safe.SafeActivity;
import it.liceoarzignano.bold.settings.SettingsActivity;
import it.liceoarzignano.bold.ui.DividerDecoration;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String APP_VERSION = BuildConfig.VERSION_NAME;
    private static final Calendar sCal = Calendar.getInstance();
    private static Resources sRes;
    private static Context sContext;
    private static RealmController sController;
    // Firebase
    private BoldAnalytics mBoldAnalytics;
    private boolean isAnalyticsEnabled = false;
    // Header
    private Toolbar mToolbar;
    private TextView mUserName;
    private ImageView mAddressLogo;
    // Cards
    private RecyclerView mCardsList;
    // Chrome custom tabs
    private CustomTabsClient mClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsIntent mCustomTabsIntent;
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sRes = getResources();
        sContext = getApplicationContext();
        sController = RealmController.with(this);

        // Analytics
        setupAnalytics();

        // Intro
        showIntroIfNeeded();

        setContentView(R.layout.activity_main);

        // Toolbar and NavDrawer
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        View mHeaderView = navigationView.getHeaderView(0);
        mUserName = (TextView) mHeaderView.findViewById(R.id.header_username);
        mAddressLogo = (ImageView) mHeaderView.findViewById(R.id.header_logo);

        // Cards List
        mCardsList = (RecyclerView) findViewById(R.id.home_list);
        mCardsList.setLayoutManager(new LinearLayoutManager(sContext));
        mCardsList.setItemAnimator(new DefaultItemAnimator());
        mCardsList.addItemDecoration(new DividerDecoration(sContext));

        // Chrome custom tabs
        setupCCustomTabs();

        // Firebase intent
        Intent mCallingIntent = getIntent();
        String mFirebaseUrl = mCallingIntent.getStringExtra("firebaseUrl");
        if (mFirebaseUrl != null && !mFirebaseUrl.isEmpty()) {
            mCustomTabsIntent.launchUrl(this, Uri.parse(mFirebaseUrl));
        }

        // Welcome dialog
        showWelcomeIfNeeded(this);

        // Notification
        if (Utils.hasEventsNotification(sContext)) {
            makeEventNotification();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh Navigation Drawer header
        setupNavHeader();

        // Show cards
        populateCards();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem mItem) {
        int mId = mItem.getItemId();
        int mMenuVal = 0;

        switch (mId) {
            case R.id.nav_my_marks:
                mMenuVal = 1;
                Intent mMarksIntent = new Intent(this, MarkListActivity.class);
                startActivity(mMarksIntent);
                break;
            case R.id.nav_calendar:
                mMenuVal = 2;
                Intent mEventsIntent = new Intent(this, EventListActivity.class);
                startActivity(mEventsIntent);
                break;
            case R.id.nav_news:
                mMenuVal = 3;
                Intent mNewsIntent = new Intent(this, NewsListActivity.class);
                startActivity(mNewsIntent);
                break;
            case R.id.nav_website:
                mMenuVal = 4;
                showWebViewUI(0);
                break;
            case R.id.nav_reg:
                mMenuVal = 5;
                showWebViewUI(1);
                break;
            case R.id.nav_moodle:
                mMenuVal = 6;
                showWebViewUI(2);
                break;
            case R.id.nav_copyboox:
                mMenuVal = 7;
                showWebViewUI(3);
                break;
            case R.id.nav_teacherzone:
                mMenuVal = 8;
                showWebViewUI(4);
                break;
            case R.id.nav_settings:
                mMenuVal = 9;
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.nav_safe:
                mMenuVal = 10;
                Intent safeIntent = new Intent(this, SafeActivity.class);
                startActivity(safeIntent);
                break;
        }

        if (isAnalyticsEnabled) {
            // Track this action
            Bundle mBundle = new Bundle();
            mBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Drawer Item");
            mBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, String.valueOf(mMenuVal));
            mBoldAnalytics.sendEvent(mBundle);
        }

        DrawerLayout mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * @return content for notification
     */
    public static String getTomorrowInfo() {
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
        List<Event> mEvents = Realm.getInstance(BoldApp.getAppRealmConfiguration())
                .where(Event.class).findAllSorted("date", Sort.DESCENDING);

        List<Event> mUpcomingEvents = new ArrayList<>();

        // Create tomorrow events list
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
            mContent = sRes.getQuantityString(R.plurals.notification_message_first, mTest, mTest)
                    + " " + sRes.getQuantityString(R.plurals.notification_test, mTest, mTest);
            isFirstElement = false;
        }

        // School
        if (mAtSchool > 0) {
            if (isFirstElement) {
                mContent = sRes.getQuantityString(R.plurals.notification_message_first,
                        mAtSchool, mAtSchool) + " ";
                isFirstElement = false;
            } else {
                mContent += mBirthday == 0 && mHangout == 0 && mOther == 0 ? " " +
                        String.format(sRes.getString(R.string.notification_message_half), mAtSchool) :
                        String.format(sRes.getString(R.string.notification_message_half), mAtSchool);
            }
            mContent += " " + sRes.getQuantityString(R.plurals.notification_school,
                    mAtSchool, mAtSchool);
        }

        // Birthday
        if (mBirthday > 0) {
            if (isFirstElement) {
                mContent = sRes.getQuantityString(R.plurals.notification_message_first,
                        mBirthday, mBirthday) + " ";
                isFirstElement = false;
            } else {
                mContent += String.format(sRes.getString(R.string.notification_message_half),
                        mBirthday);
            }
            mContent += " " + sRes.getQuantityString(R.plurals.notification_birthday,
                    mBirthday, mBirthday);
        }

        // Homework
        if (mHomework > 0) {
            if (isFirstElement) {
                mContent = sRes.getQuantityString(R.plurals.notification_message_first,
                        mHomework, mHomework) + " ";
                isFirstElement = false;
            } else {
                mContent += String.format(sRes.getString(R.string.notification_message_half),
                        mHomework);
            }

            mContent += " " + sRes.getQuantityString(R.plurals.notification_homework,
                    mHomework, mHomework);
        }

        // Reminder
        if (mReminder > 0) {
            if (isFirstElement) {
                mContent = sRes.getQuantityString(R.plurals.notification_message_first,
                        mReminder, mReminder) + " ";
                isFirstElement = false;
            } else {
                mContent += String.format(sRes.getString(R.string.notification_message_half),
                        mReminder);
            }
            mContent += " " + sRes.getQuantityString(R.plurals.notification_reminder,
                    mReminder, mReminder);
        }

        // Hangout
        if (mHangout > 0) {
            if (isFirstElement) {
                mContent = sRes.getQuantityString(R.plurals.notification_message_first,
                        mHangout, mHangout) + " ";
                isFirstElement = false;
            } else {
                mContent += String.format(sRes.getString(R.string.notification_message_half),
                        mAtSchool);
            }
            mContent += " " + sRes.getQuantityString(R.plurals.notification_meeting,
                    mHangout, mHangout);
        }

        // Other
        if (mOther > 0) {
            if (isFirstElement) {
                mContent = sRes.getQuantityString(R.plurals.notification_message_first,
                        mOther, mOther);
                mContent += " ";
            } else {
                mContent += String.format(sRes.getString(R.string.notification_message_half),
                        mOther);
            }
            mContent += " " + sRes.getQuantityString(R.plurals.notification_other,
                    mOther, mOther);
        }

        mContent += " " + sRes.getString(R.string.notification_message_end);

        return mContent;
    }

    /**
     * Create notification that will be fired later
     */
    public static void makeEventNotification() {
        // Guard against npe when called from service
        if (sContext == null) {
            sContext = BoldApp.getBoldContext();
        }

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR, sCal.get(Calendar.YEAR));
        mCalendar.set(Calendar.MONTH, sCal.get(Calendar.MONTH));
        mCalendar.set(Calendar.DAY_OF_MONTH, sCal.get(Calendar.DAY_OF_MONTH));

        switch (Utils.getEventsNotificationTime(sContext)) {
            case "0":
                if (mCalendar.get(Calendar.HOUR_OF_DAY) >= 6) {
                    // If it's too late for today's notification, plan one for tomorrow
                    mCalendar.set(Calendar.DAY_OF_MONTH, sCal.get(Calendar.DAY_OF_MONTH) + 1);
                }
                mCalendar.set(Calendar.HOUR_OF_DAY, 6);
                break;
            case "1":
                if (mCalendar.get(Calendar.HOUR_OF_DAY) >= 15) {
                    // If it's too late for today's notification, plan one for tomorrow
                    mCalendar.set(Calendar.DAY_OF_MONTH, sCal.get(Calendar.DAY_OF_MONTH) + 1);
                }
                mCalendar.set(Calendar.HOUR_OF_DAY, 15);
                break;
            case "2":
                if (mCalendar.get(Calendar.HOUR_OF_DAY) >= 21) {
                    // If it's too late for today's notification, plan one for tomorrow
                    mCalendar.set(Calendar.DAY_OF_MONTH, sCal.get(Calendar.DAY_OF_MONTH) + 1);
                }
                mCalendar.set(Calendar.HOUR_OF_DAY, 21);
                break;
        }

        Intent mNotifIntent = new Intent(sContext, AlarmService.class);
        AlarmManager mAlarmManager = (AlarmManager) sContext.getSystemService(ALARM_SERVICE);
        PendingIntent mPendingIntent = PendingIntent.getService(sContext, 0, mNotifIntent, 0);
        mAlarmManager.set(AlarmManager.RTC, mCalendar.getTimeInMillis(), mPendingIntent);
    }

    /**
     * Init Chrome custom tabs
     */
    private void setupCCustomTabs() {
        CustomTabsServiceConnection mCustomTabsServiceConnection =
                new CustomTabsServiceConnection() {
                    @Override
                    public void onCustomTabsServiceConnected(ComponentName componentName,
                                                             CustomTabsClient customTabsClient) {
                        mClient = customTabsClient;
                        mClient.warmup(0L);
                        mCustomTabsSession = mClient.newSession(null);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        mClient = null;
                    }
                };

        CustomTabsClient.bindCustomTabsService(sContext, "com.android.chrome",
                mCustomTabsServiceConnection);

        mCustomTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setShowTitle(true)
                .setStartAnimations(this,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left)
                .setExitAnimations(this,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right)
                .build();
    }

    /**
     * Open a chrome custom tab with the selected url and send
     * an Analytics event.
     * If there's no chrome / chromium 46+ it will
     * just open the browser
     *
     * @param mIndex: the selected item from the nav drawer menu
     */
    private void showWebViewUI(int mIndex) {
        switch (mIndex) {
            case -1:
                mUrl = getString(R.string.config_url_changelog);
                break;
            case 0:
                mUrl = getString(R.string.config_url_home);
                break;
            case 1:
                mUrl = getString(Utils.isTeacher(this) ?
                        R.string.config_url_reg_teacher : R.string.config_url_reg_student);
                break;
            case 2:
                mUrl = getString(R.string.config_url_moodle);
                break;
            case 3:
                mUrl = getString(R.string.config_url_copybook);
                break;
            case 4:
                mUrl = getString(R.string.config_url_teacherzone);
                break;
        }

        if (mUrl != null) {
            mCustomTabsIntent.launchUrl(this, Uri.parse(mUrl));
        }
    }

    /**
     * Show the first 3 events in the following 7 days in
     * a card with their titles and dates.
     * If there are more events a flat button will tell
     * the user there are more events
     */
    private HomeCard createEventsCard() {
        HomeCard.Builder mBuilder = new HomeCard.Builder()
                .setName(getString(R.string.upcoming_events));

        // Show 3 closest events
        List<Event> mEvents = sController.getAllEventsInverted();
        for (int mCounter = 0; mCounter < 3 && mCounter < mEvents.size(); mCounter++) {
            Event mEvent = mEvents.get(mCounter);
            if (isThisWeek(mEvent.getDate())) {
                mBuilder.addEntry(mEvent.getTitle(), mEvent.getDate());
            }
        }

        return mBuilder.build();
    }

    private HomeCard createMarksCard() {
        HomeCard.Builder mBuilder = new HomeCard.Builder()
                .setName(getString(R.string.lastest_marks));

        List<Mark> mMarks = sController.getAllMarks().sort("date", Sort.DESCENDING);
        for (int mCounter = 0; mCounter < 3 && mCounter < mMarks.size(); mCounter++) {
            Mark mMark = mMarks.get(mCounter);
            mBuilder.addEntry(mMark.getTitle(), String.valueOf((double) mMark.getValue() / 100));
        }

        return mBuilder.build();
    }

    private HomeCard createSuggestionsCard() {
        return new HomeCard.Builder()
                .setName(getString(R.string.suggestions))
                .addEntry("", getSuggestion())
                .build();
    }

    /**
     * Check if stringDate is one of the next 7 days
     *
     * @param stringDate: string date from event database
     * @return true if it's within 7 days, false if not
     */
    private boolean isThisWeek(String stringDate) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(Utils.stringToDate(stringDate).getTime());

        int mDiff = mCalendar.get(Calendar.DAY_OF_YEAR) - sCal.get(Calendar.DAY_OF_YEAR);

        return sCal.get(Calendar.YEAR) == mCalendar.get(Calendar.YEAR) && mDiff >= 0 && mDiff < 8;
    }

    /**
     * Get random suggestion text to be shown in
     * the suggestions card
     *
     * @return string with text for suggestion card
     */
    private String getSuggestion() {
        Random mRandom = new SecureRandom();
        switch (mRandom.nextInt(11) + 1) {
            case 1:
                return getString(Utils.hasSafe(this) ?
                        R.string.suggestion_safe_pwd : R.string.suggestion_safe);
            case 2:
                return getString(R.string.suggestion_avg);
            case 3:
                return getString(R.string.suggestion_quarter);
            case 4:
                return getString(R.string.suggestion_edit_event);
            case 5:
                return getString(R.string.suggestion_notification_remote);
            case 6:
                return getString(R.string.suggestion_get_touch);
            case 7:
                return getString(R.string.suggestion_address);
            case 8:
                return getString(R.string.suggestion_backups);
            case 9:
                return getString(R.string.suggestion_suggestions);
            case 10:
                return getString(R.string.suggestion_news);
            default:
                return getString(R.string.suggestion_notification);
        }
    }

    /**
     * Show cards with delay so
     * animated LinearLayout will make
     * a nice enter effect
     */
    private void populateCards() {
        List<HomeCard> mCards = new ArrayList<>();
        mCards.add(createEventsCard());
        if (Utils.hasUsedForMoreThanOneWeek(sContext)) {
            mCards.add(createMarksCard());
        }
        if (Utils.hasSuggestions(sContext)) {
            mCards.add(createSuggestionsCard());
        }

        HomeAdapter mAdapter = new HomeAdapter(mCards);
        mCardsList.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }

    /**
     * Show the intro / tutorial activity
     * if it's the first time we fire the app
     */
    private void showIntroIfNeeded() {
        SharedPreferences mPrefs = getSharedPreferences("HomePrefs", MODE_PRIVATE);
        if (!mPrefs.getBoolean("introKey", false)) {
            Intent mIntent = new Intent(this, BenefitsActivity.class);
            startActivity(mIntent);
            finish();
        }
    }

    /**
     * Show welcome / changelog dialog
     * when the app is installed / updated
     *
     * @param mContext: used to get SharedPreferences
     */
    private void showWelcomeIfNeeded(final Context mContext) {
        final SharedPreferences mPrefs = getSharedPreferences("HomePrefs", MODE_PRIVATE);

        if (!mPrefs.getBoolean("introKey", false)) {
            // If we're showing intro, don't display dialog
            return;
        }

        @SuppressLint("CommitPrefEdits")
        final SharedPreferences.Editor mEditor =
                getSharedPreferences("HomePrefs", MODE_PRIVATE).edit();

        switch (Utils.appVersionKey(this)) {
            case APP_VERSION:
                break;
            case "0":
                // Used for feature discovery
                final String today = Utils.getToday();
                mEditor.putString("appVersionKey", APP_VERSION).apply();
                mEditor.putString("initialDayKey", today).apply();
                break;
            default:
                new MaterialDialog.Builder(mContext)
                        .title(R.string.dialog_updated_title)
                        .content(R.string.dialog_updated_content)
                        .positiveText(android.R.string.ok)
                        .negativeText(R.string.dialog_updated_changelog)
                        .canceledOnTouchOutside(false)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog,
                                                @NonNull DialogAction which) {
                                mEditor.putString("appVersionKey", APP_VERSION).apply();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog,
                                                @NonNull DialogAction which) {
                                dialog.hide();
                                showWebViewUI(-1);
                            }
                        })

                        .show();
                break;
        }

        if (mPrefs.getBoolean("drawerIntro", true)) {
            final Activity mActivity = this;
            String[] mAddresses = new String[] {
                    getString(R.string.pref_address_1),
                    getString(R.string.pref_address_2),
                    getString(R.string.pref_address_3),
                    getString(R.string.pref_address_4),
                    getString(R.string.pref_address_5),
                    getString(R.string.pref_address_teacher)
            };

            new MaterialDialog.Builder(mContext)
                    .title(R.string.pref_address_dialog)
                    .items((CharSequence[]) mAddresses)
                    .canceledOnTouchOutside(false)
                    .positiveText(android.R.string.ok)
                    .autoDismiss(false)
                    .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView,
                                                   int which, CharSequence text) {
                        boolean isAddressValid = which != -1;
                            if (which == 5) {
                                Utils.setTeacherMode(mContext);
                            } else if (isAddressValid){
                                Utils.setAddress(mContext, String.valueOf(which + 1));
                            }
                            if (isAddressValid) {
                                setupNavHeader();
                                dialog.dismiss();
                                mPrefs.edit().putBoolean("drawerIntro", false).apply();
                                new MaterialTapTargetPrompt.Builder(mActivity)
                                        .setTarget(mToolbar.getChildAt(1))
                                        .setPrimaryText(getString(R.string.intro_drawer_title))
                                        .setSecondaryText(getString(R.string.intro_drawer))
                                        .setBackgroundColourFromRes(R.color.colorAccentDark)
                                        .setFocalColourFromRes(R.color.colorPrimaryDark)
                                        .show();
                            }

                            return true;
                        }
                    })
                    .show();
        }
    }

    /**
     * Update navigation header
     * according to user settings
     * (api 21+ only)
     */
    private void setupNavHeader() {
        mUserName.setText(Utils.userNameKey(this));

        if (Utils.isLegacy()) {
            if (Utils.isTeacher(this)) {
                mAddressLogo.setBackground(getDrawable(R.drawable.ic_address_6));
            } else {
                switch (Utils.getAddress(this)) {
                    case "1":
                        mAddressLogo.setBackground(getDrawable(R.drawable.ic_address_1));
                        break;
                    case "2":
                        mAddressLogo.setBackground(getDrawable(R.drawable.ic_address_2));
                        break;
                    case "3":
                        mAddressLogo.setBackground(getDrawable(R.drawable.ic_address_3));
                        break;
                    case "4":
                        mAddressLogo.setBackground(getDrawable(R.drawable.ic_address_4));
                        break;
                    case "5":
                        mAddressLogo.setBackground(getDrawable(R.drawable.ic_address_5));
                        break;
                }
            }
        }
    }

    /**
     * Initialize firebase analytics
     */
    private void setupAnalytics() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.enableTrackerIfOverlayRequests(sContext,
                        getResources().getBoolean(R.bool.force_tracker));
                if (Utils.hasAnalytics(sContext)) {
                    isAnalyticsEnabled = true;
                    mBoldAnalytics = BoldApp.getBoldAnalytics();
                }
            }
        }).start();
    }
}
