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
import android.os.Handler;
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
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import it.liceoarzignano.bold.intro.BenefitsActivity;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.MarkListActivity;
import it.liceoarzignano.bold.realm.RealmController;
import it.liceoarzignano.bold.safe.SafeActivity;
import it.liceoarzignano.bold.settings.SettingsActivity;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String APP_VERSION = BuildConfig.VERSION_NAME;
    private static final Calendar c = Calendar.getInstance();
    private static Resources res;
    private static Context sContext;
    private static RealmController controller;
    // Firebase
    private BoldAnalytics mBoldAnalytics;
    private boolean isAnalyticsEnabled = false;
    // Header
    private Toolbar toolbar;
    private TextView mUserName;
    private ImageView mAddressLogo;
    // Chrome custom tabs
    private CustomTabsClient mClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsIntent customTabsIntent;
    private String mUrl;
    // Event card
    private View mEventsCardSeparatorView;
    private CardView mEventsCard;
    private final TextView[] mEventTitles = new TextView[3];
    private final TextView[] mEventDates = new TextView[3];
    private final LinearLayout[] mEventLayouts = new LinearLayout[3];
    private Button mMoreEventsButton;
    // Marks card
    private View mMarksCardSeparatorView;
    private CardView mMarksCard;
    private final TextView[] mMarksTitles = new TextView[3];
    private final TextView[] mMarksDates = new TextView[3];
    private final LinearLayout[] mMarksLayouts = new LinearLayout[3];
    // Suggestions card
    private View mSuggestionCardSeparatorView;
    private CardView mSuggestionCard;
    private TextView mSuggestionText;
    private boolean showEventsCard;
    private boolean showMarksCard;
    private boolean showSuggestionCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        res = getResources();
        sContext = getApplicationContext();
        controller = RealmController.with(this);

        // Analytics
        setupAnalytics();

        // Intro
        showIntroIfNeeded();

        setContentView(R.layout.activity_main);

        // Toolbar and NavDrawer
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        View mHeaderView = navigationView.getHeaderView(0);
        mUserName = (TextView) mHeaderView.findViewById(R.id.username_drawer);
        mAddressLogo = (ImageView) mHeaderView.findViewById(R.id.address_logo);
        setupNavHeader();

        // Events Card
        mEventsCard = (CardView) findViewById(R.id.events_card);
        mEventsCardSeparatorView = findViewById(R.id.events_separator);
        mEventTitles[0] = (TextView) findViewById(R.id.events_title_1);
        mEventTitles[1] = (TextView) findViewById(R.id.events_title_2);
        mEventTitles[2] = (TextView) findViewById(R.id.events_title_3);
        mEventDates[0] = (TextView) findViewById(R.id.events_sec_1);
        mEventDates[1] = (TextView) findViewById(R.id.events_sec_2);
        mEventDates[2] = (TextView) findViewById(R.id.events_sec_3);
        mEventLayouts[0] = (LinearLayout) findViewById(R.id.events_layout_1);
        mEventLayouts[1] = (LinearLayout) findViewById(R.id.events_layout_2);
        mEventLayouts[2] = (LinearLayout) findViewById(R.id.events_layout_3);
        mMoreEventsButton = (Button) findViewById(R.id.more_events_button);
        mMoreEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventIntent = new Intent(MainActivity.this, EventListActivity.class);
                startActivity(eventIntent);
            }
        });

        // Marks Card
        mMarksCard = (CardView) findViewById(R.id.marks_card);
        mMarksCardSeparatorView = findViewById(R.id.marks_separator);
        mMarksTitles[0] = (TextView) findViewById(R.id.marks_title_1);
        mMarksTitles[1] = (TextView) findViewById(R.id.marks_title_2);
        mMarksTitles[2] = (TextView) findViewById(R.id.marks_title_3);
        mMarksDates[0] = (TextView) findViewById(R.id.marks_sec_1);
        mMarksDates[1] = (TextView) findViewById(R.id.marks_sec_2);
        mMarksDates[2] = (TextView) findViewById(R.id.marks_sec_3);
        mMarksLayouts[0] = (LinearLayout) findViewById(R.id.marks_layout1);
        mMarksLayouts[1] = (LinearLayout) findViewById(R.id.marks_layout2);
        mMarksLayouts[2] = (LinearLayout) findViewById(R.id.marks_layout3);

        // Suggestions Card
        mSuggestionCardSeparatorView = findViewById(R.id.suggestions_separator);
        mSuggestionCard = (CardView) findViewById(R.id.suggestions_card);
        mSuggestionText = (TextView) findViewById(R.id.suggestions_text);
        loadSuggestion();

        // Chrome custom tabs
        setupCCustomTabs();

        // Firebase intent
        Intent mCallingIntent = getIntent();
        String mFirebaseUrl = mCallingIntent.getStringExtra("firebaseUrl");
        if (mFirebaseUrl != null && !mFirebaseUrl.isEmpty()) {
            customTabsIntent.launchUrl(this, Uri.parse(mFirebaseUrl));
        }

        // Welcome dialog
        showWelcomeIfNeeded(this);

        // Show cards
        populateCards();

        // Notification
        if (Utils.hasNotification(sContext)) {
            makeEventNotification();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh Navigation Drawer header
        setupNavHeader();

        // Refresh home cards if sth changed
        boolean hasEventsStatusChanged = showEventsCard;
        boolean hasMarksStatusChanged = showMarksCard;

        loadEvents();

        if (Utils.hasUsedForMoreThanOneWeek(this)) {
            loadMarks();
        }

        if (hasEventsStatusChanged != showEventsCard ||
                hasMarksStatusChanged != showMarksCard) {
            // Events or Suggestions card status has changed
            populateCards();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        int menuVal = 0;

        switch (id) {
            case R.id.nav_my_marks:
                menuVal = 1;
                Intent intent = new Intent(this, MarkListActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_calendar:
                menuVal = 2;
                Intent i = new Intent(this, EventListActivity.class);
                startActivity(i);
                break;
            case R.id.nav_website:
                menuVal = 3;
                showWebViewUI(0);
                break;
            case R.id.nav_news:
                menuVal = 4;
                showWebViewUI(1);
                break;
            case R.id.nav_reg:
                menuVal = 5;
                showWebViewUI(2);
                break;
            case R.id.nav_moodle:
                menuVal = 6;
                showWebViewUI(3);
                break;
            case R.id.nav_copyboox:
                menuVal = 7;
                showWebViewUI(4);
                break;
            case R.id.nav_teacherzone:
                menuVal = 8;
                showWebViewUI(5);
                break;
            case R.id.nav_settings:
                menuVal = 9;
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.nav_safe:
                menuVal = 10;
                Intent safeIntent = new Intent(this, SafeActivity.class);
                startActivity(safeIntent);
                break;
        }

        if (isAnalyticsEnabled) {
            // Track this action
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Drawer Item");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, String.valueOf(menuVal));
            mBoldAnalytics.sendEvent(bundle);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * @return content for notification
     */
    public static String getTomorrowInfo() {
        String content = null;
        boolean firstElement = true;

        int icon;
        int test = 0;
        int atSchool = 0;
        int birthday = 0;
        int homework = 0;
        int reminder = 0;
        int hangout = 0;
        int other = 0;

        // Use realm instead of RealmController to avoid NPE when onBoot intent is broadcast'ed
        List<Event> events = Realm.getInstance(BoldApp.getAppRealmConfiguration())
                .where(Event.class).findAllSorted("date", Sort.DESCENDING);

        List<Event> tomorrowEvents = new ArrayList<>();

        // Create tomorrow events list
        for (Event event : events) {
            if (Utils.getToday().equals(event.getDate())) {
                tomorrowEvents.add(event);
            }
        }

        if (tomorrowEvents.isEmpty()) {
            return null;
        }

        // Get data
        for (Event event : tomorrowEvents) {
            icon = event.getIcon();
            switch (icon) {
                case 0:
                    test++;
                    break;
                case 1:
                    atSchool++;
                    break;
                case 2:
                    birthday++;
                    break;
                case 3:
                    homework++;
                    break;
                case 4:
                    reminder++;
                    break;
                case 5:
                    hangout++;
                    break;
                case 6:
                    other++;
                    break;
            }
        }

        // Test
        if (test > 0) {
            // First element
            content = res.getQuantityString(R.plurals.notification_message_first, test, test)
                    + " " + res.getQuantityString(R.plurals.notification_test, test, test);
            firstElement = false;
        }

        // School
        if (atSchool > 0) {
            if (firstElement) {
                content = res.getQuantityString(R.plurals.notification_message_first,
                        atSchool, atSchool) + " ";
                firstElement = false;
            } else {
                content += birthday == 0 && hangout == 0 && other == 0 ? " " +
                        String.format(res.getString(R.string.notification_message_half), atSchool) :
                        String.format(res.getString(R.string.notification_message_half), atSchool);
            }
            content += " " + res.getQuantityString(R.plurals.notification_school,
                    atSchool, atSchool);
        }

        // Birthday
        if (birthday > 0) {
            if (firstElement) {
                content = res.getQuantityString(R.plurals.notification_message_first,
                        birthday, birthday) + " ";
                firstElement = false;
            } else {
                content += String.format(res.getString(R.string.notification_message_half),
                        birthday);
            }
            content += " " + res.getQuantityString(R.plurals.notification_birthday,
                    birthday, birthday);
        }

        // Homework
        if (homework > 0) {
            if (firstElement) {
                content = res.getQuantityString(R.plurals.notification_message_first,
                        homework, homework) + " ";
                firstElement = false;
            } else {
                content += String.format(res.getString(R.string.notification_message_half),
                        homework);
            }

            content += " " + res.getQuantityString(R.plurals.notification_homework,
                    homework, homework);
        }

        // Reminder
        if (reminder > 0) {
            if (firstElement) {
                content = res.getQuantityString(R.plurals.notification_message_first,
                        reminder, reminder) + " ";
                firstElement = false;
            } else {
                content += String.format(res.getString(R.string.notification_message_half),
                        reminder);
            }
            content += " " + res.getQuantityString(R.plurals.notification_reminder,
                    reminder, reminder);
        }

        // Hangout
        if (hangout > 0) {
            if (firstElement) {
                content = res.getQuantityString(R.plurals.notification_message_first,
                        hangout, hangout) + " ";
                firstElement = false;
            } else {
                content += String.format(res.getString(R.string.notification_message_half),
                        atSchool);
            }
            content += " " + res.getQuantityString(R.plurals.notification_meeting,
                    hangout, hangout);
        }

        // Other
        if (other > 0) {
            if (firstElement) {
                content = res.getQuantityString(R.plurals.notification_message_first,
                        other, other);
                content += " ";
            } else {
                content += String.format(res.getString(R.string.notification_message_half),
                        other);
            }
            content += " " + res.getQuantityString(R.plurals.notification_other,
                    other, other);
        }

        content += " " + res.getString(R.string.notification_message_end);

        return content;
    }

    /**
     * Create notification that will be fired later
     */
    public static void makeEventNotification() {
        // Guard against npe when called from service
        if (sContext == null) {
            sContext = BoldApp.getBoldContext();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));

        switch (Utils.getNotificationTime(sContext)) {
            case "0":
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 6) {
                    // If it's too late for today's notification, plan one for tomorrow
                    calendar.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
                }
                calendar.set(Calendar.HOUR_OF_DAY, 6);
                break;
            case "1":
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 15) {
                    // If it's too late for today's notification, plan one for tomorrow
                    calendar.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
                }
                calendar.set(Calendar.HOUR_OF_DAY, 15);
                break;
            case "2":
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 21) {
                    // If it's too late for today's notification, plan one for tomorrow
                    calendar.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
                }
                calendar.set(Calendar.HOUR_OF_DAY, 21);
                break;
        }

        Intent intent = new Intent(sContext, AlarmService.class);
        AlarmManager alarmManager = (AlarmManager) sContext.getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(sContext, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
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

        customTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
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
     * @param index: the selected item from the nav drawer menu
     */
    private void showWebViewUI(int index) {
        switch (index) {
            case -1:
                mUrl = getString(R.string.config_url_changelog);
                break;
            case 0:
                mUrl = getString(R.string.config_url_home);
                break;
            case 1:
                mUrl = getString(Utils.isTeacher(this) ?
                        R.string.config_url_news_teacher : R.string.config_url_news_student);
                break;
            case 2:
                mUrl = getString(Utils.isTeacher(this) ?
                        R.string.config_url_reg_teacher : R.string.config_url_reg_student);
                break;
            case 3:
                mUrl = getString(R.string.config_url_moodle);
                break;
            case 4:
                mUrl = getString(R.string.config_url_copybook);
                break;
            case 5:
                mUrl = getString(R.string.config_url_teacherzone);
                break;
        }

        if (mUrl != null) {
            customTabsIntent.launchUrl(this, Uri.parse(mUrl));
        }
    }

    /**
     * Show the first 3 events in the following 7 days in
     * a card with their titles and dates.
     * If there are more events a flat button will tell
     * the user there are more events
     */
    private void loadEvents() {
        int i = 0;
        int c = 0;

        // Show closest events first
        List<Event> events = controller.getAllEventsInverted();
        showEventsCard = false;

        for (Event eventInList : events) {
            if (isThisWeek(eventInList.getDate())) {
                showEventsCard = true;
                if (i < 3) {
                    mEventTitles[i].setText(eventInList.getTitle());
                    mEventDates[i].setText(eventInList.getDate());
                    mEventLayouts[i].setVisibility(View.VISIBLE);
                    i++;
                }
                c++;
            }
        }

        if (c > i) {
            mMoreEventsButton.setVisibility(View.VISIBLE);
            mMoreEventsButton.setText(res.getQuantityString(R.plurals.more_events, i, i));
        }
    }

    /**
     * Check if stringDate is one of the next 7 days
     *
     * @param stringDate: string date from event database
     * @return true if it's within 7 days, false if not
     */
    private boolean isThisWeek(String stringDate) {
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTimeInMillis(Utils.stringToDate(stringDate).getTime());

        int diff = dateCalendar.get(Calendar.DAY_OF_YEAR) - c.get(Calendar.DAY_OF_YEAR);

        return c.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) && diff >= 0 && diff < 8;
    }

    /**
     * Show the 3 lastest added marks
     */
    private void loadMarks() {
        List<Mark> marks = controller.getAllMarks().sort("date", Sort.DESCENDING);
        int i = 0;

        if (!marks.isEmpty()) {
            showMarksCard = true;
            for (Mark mark : marks) {

                if (i == 3) {
                    break;
                }
                mMarksTitles[i].setText(mark.getTitle() + ": " + ((double) mark.getValue() / 100));
                mMarksDates[i].setText(mark.getDate());
                mMarksLayouts[i].setVisibility(View.VISIBLE);
                i++;
            }
        }
    }

    /**
     * Show and set up suggestions card
     * if user enabled it from settings
     */
    private void loadSuggestion() {
        if (Utils.hasSuggestions(this)) {
            showSuggestionCard = true;
            mSuggestionText.setText(getSuggestion());
        } else {
            showSuggestionCard = false;
        }
    }

    /**
     * Get random suggestion text to be shown in
     * the suggestions card
     *
     * @return string with text for suggestion card
     */
    private String getSuggestion() {
        Random random = new SecureRandom();
        switch (random.nextInt(10) + 1) {
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
        int delay = 50;
        mEventsCard.setVisibility(View.GONE);
        mMarksCard.setVisibility(View.GONE);
        mSuggestionCard.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (showEventsCard) {
                    mEventsCard.setVisibility(View.VISIBLE);
                    mEventsCardSeparatorView.setVisibility(View.VISIBLE);
                }
            }
        }, delay = delay + 20);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (showMarksCard) {
                    mMarksCard.setVisibility(View.VISIBLE);
                    mMarksCardSeparatorView.setVisibility(View.VISIBLE);
                }
            }
        }, delay = delay + 20);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (showSuggestionCard) {
                    mSuggestionCard.setVisibility(View.VISIBLE);
                    mSuggestionCardSeparatorView.setVisibility(View.VISIBLE);
                }
            }
        }, delay + 20);
    }

    /**
     * Show the intro / tutorial activity
     * if it's the first time we fire the app
     */
    private void showIntroIfNeeded() {
        SharedPreferences prefs = getSharedPreferences("HomePrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("introKey", false)) {
            Intent mIntent = new Intent(this, BenefitsActivity.class);
            startActivity(mIntent);
            finish();
        }
    }

    /**
     * Show welcome / changelog dialog
     * when the app is installed / updated
     *
     * @param context: used to get SharedPreferences
     */
    private void showWelcomeIfNeeded(final Context context) {
        final SharedPreferences prefs = getSharedPreferences("HomePrefs", MODE_PRIVATE);

        if (!prefs.getBoolean("introKey", false)) {
            // If we're showing intro, don't display dialog
            return;
        }

        @SuppressLint("CommitPrefEdits")
        final SharedPreferences.Editor editor =
                getSharedPreferences("HomePrefs", MODE_PRIVATE).edit();

        switch (Utils.appVersionKey(this)) {
            case APP_VERSION:
                break;
            case "0":
                // Used for feature discovery
                final String today = Utils.getToday();
                editor.putString("appVersionKey", APP_VERSION).apply();
                editor.putString("initialDayKey", today).apply();
                break;
            default:
                new MaterialDialog.Builder(context)
                        .title(getString(R.string.dialog_updated_title))
                        .content(getString(R.string.dialog_updated_content))
                        .positiveText(android.R.string.ok)
                        .negativeText(R.string.dialog_updated_changelog)
                        .canceledOnTouchOutside(false)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog,
                                                @NonNull DialogAction which) {
                                editor.putString("appVersionKey", APP_VERSION).apply();
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

        if (prefs.getBoolean("drawerIntro", true)) {
            final Activity mActivity = this;
            String[] mAddresses = new String[] {
                    getString(R.string.pref_address_1),
                    getString(R.string.pref_address_2),
                    getString(R.string.pref_address_3),
                    getString(R.string.pref_address_4),
                    getString(R.string.pref_address_5),
                    getString(R.string.pref_address_teacher)
            };

            new MaterialDialog.Builder(context)
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
                                Utils.setTeacherMode(context);
                            } else if (isAddressValid){
                                Utils.setAddress(context, String.valueOf(which + 1));
                            }
                            if (isAddressValid) {
                                setupNavHeader();
                                dialog.dismiss();
                                prefs.edit().putBoolean("drawerIntro", false).apply();
                                new MaterialTapTargetPrompt.Builder(mActivity)
                                        .setTarget(toolbar.getChildAt(1))
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

        if (Utils.hasApi21()) {
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
