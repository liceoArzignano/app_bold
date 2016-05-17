package it.liceoarzignano.bold;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import it.liceoarzignano.bold.events.DatabaseConnection;
import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventListActivity;
import it.liceoarzignano.bold.marks.MarkListActivity;
import it.liceoarzignano.bold.safe.SafeActivity;
import it.liceoarzignano.bold.settings.AnalyticsTracker;
import it.liceoarzignano.bold.settings.SettingsActivity;
import it.liceoarzignano.bold.tasks.TasksActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String APP_VERSION = "1.0.10";

    // Header
    private TextView mUserName;
    private static ImageView mAddressLogo;

    // Chrome custom tabs
    private CustomTabsClient mClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsIntent customTabsIntent;
    private String mUrl;

    // Welcome card
    private CardView mWelcomeCard;

    // Upcoming card
    private View mUpcomingCardSeparatorView;
    private CardView mUpcomingCard;
    private TextView     mUpcomingTitle1,  mUpcomingTitle2,  mUpcomingTitle3;
    private TextView     mUpcomingDate1,   mUpcomingDate2,   mUpcomingDate3;
    private LinearLayout mUpcomingLayout1, mUpcomingLayout2, mUpcomingLayout3;
    private Button mMoreEventsButton;

    // Suggestions card
    private View mSuggestionCardSeparatorView;
    private CardView mSuggestionCard;
    private TextView mSuggestionText;

    private final Calendar c = Calendar.getInstance();

    private boolean showUpcomingCard;
    private boolean showSuggestionCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Google Analytics
        new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.enableTrackerIfOverlayRequests(getApplicationContext(),
                        getResources().getBoolean(R.bool.force_tracker));
                if (Utils.trackerEnabled(getApplicationContext())) {
                    AnalyticsTracker.initializeTracker(getApplicationContext());
                    AnalyticsTracker.getInstance().get();
                }
            }
        }).start();

        // Intro
        showIntroIfNeeded(false);

        // UI
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        View mHeaderView = navigationView.getHeaderView(0);
        mUserName = (TextView) mHeaderView.findViewById(R.id.username_drawer);
        mAddressLogo = (ImageView) mHeaderView.findViewById(R.id.address_logo);
        setupNavHeader();

        // Welcome Card
        mWelcomeCard = (CardView) findViewById(R.id.card_1);
        mUpcomingCard = (CardView) findViewById(R.id.upcomingCard);

        // Events Card
        mUpcomingCardSeparatorView = findViewById(R.id.upcomingCardSeparatorView);
        mUpcomingLayout1 = (LinearLayout) findViewById(R.id.upcomingLayout1);
        mUpcomingLayout2 = (LinearLayout) findViewById(R.id.upcomingLayout2);
        mUpcomingLayout3 = (LinearLayout) findViewById(R.id.upcomingLayout3);
        mUpcomingTitle1 = (TextView) findViewById(R.id.proactive_title_1);
        mUpcomingTitle2 = (TextView) findViewById(R.id.proactive_title_2);
        mUpcomingTitle3 = (TextView) findViewById(R.id.proactive_title_3);
        mUpcomingDate1 = (TextView) findViewById(R.id.proactive_sec_1);
        mUpcomingDate2 = (TextView) findViewById(R.id.proactive_sec_2);
        mUpcomingDate3 = (TextView) findViewById(R.id.proactive_sec_3);
        mMoreEventsButton = (Button) findViewById(R.id.more_events_button);
        mMoreEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventIntent = new Intent(MainActivity.this, EventListActivity.class);
                startActivity(eventIntent);
            }
        });
        upcomingEvents();

        // Suggestions Card
        mSuggestionCardSeparatorView = findViewById(R.id.suggestionSeparatorView);
        mSuggestionCard = (CardView) findViewById(R.id.suggestionCard);
        mSuggestionText = (TextView) findViewById(R.id.suggestion_text);
        loadSuggestion();

        // Show cards
        populateCards();

        // Chrome custom tabs
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

        CustomTabsClient.bindCustomTabsService(getApplicationContext(), "com.android.chrome",
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

        // Welcome dialog
        showWelcomeIfNeeded(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh Navigation Drawer header
        setupNavHeader();

        // Refresh home cards if sth changed
        boolean hasEventsStatusChanged = showUpcomingCard;
        boolean hasSuggestionsStatusChanged = showSuggestionCard;
        upcomingEvents();
        loadSuggestion();
        if (hasEventsStatusChanged != showUpcomingCard ||
                hasSuggestionsStatusChanged != showSuggestionCard) {
            // Events or Suggestions card status has changed
            populateCards();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_my_marks:
                Intent intent = new Intent(MainActivity.this, MarkListActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_calendar:
                Intent i = new Intent(MainActivity.this, EventListActivity.class);
                startActivity(i);
                break;
            case R.id.nav_website:
                showWebViewUI(0);
                break;
            case R.id.nav_news:
                showWebViewUI(1);
                break;
            case R.id.nav_reg:
                showWebViewUI(2);
                break;
            case R.id.nav_moodle:
                showWebViewUI(3);
                break;
            case R.id.nav_copyboox:
                showWebViewUI(4);
                break;
            case R.id.nav_teacherzone:
                showWebViewUI(5);
                break;
            case R.id.nav_help:
                showIntroIfNeeded(true);
                break;
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.nav_safe:
                Intent safeIntent = new Intent(MainActivity.this, SafeActivity.class);
                startActivity(safeIntent);
                break;
            case R.id.nav_tasks:
                Intent taskIntent = new Intent(MainActivity.this, TasksActivity.class);
                startActivity(taskIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        if (Utils.trackerEnabled(this)) {
            AnalyticsTracker.trackEvent("WebPage: " + index, getApplicationContext());
        }

        switch (index) {
            case 0:
                mUrl = getString(R.string.url_home);
                break;
            case 1:
                mUrl = getString(Utils.isTeacher(this) ?
                        R.string.url_news_teacher : R.string.url_news_student);
                break;
            case 2:
                mUrl = getString(Utils.isTeacher(this) ?
                        R.string.url_reg_teacher : R.string.url_reg_student);
                break;
            case 3:
                mUrl = getString(R.string.url_moodle);
                break;
            case 4:
                mUrl = getString(R.string.url_copybook);
                break;
            case 5:
                mUrl = getString(R.string.url_teacherzone);
                break;
        }

        if (mUrl != null) {
            customTabsIntent.launchUrl(MainActivity.this, Uri.parse(mUrl));
        }
    }

    /**
     * Show the first 3 events in the following 7 days in
     * a card with their titles and dates.
     * If there are more events a flat button will tell
     * the user there are more events
     */
    private void upcomingEvents() {
        int i = 0;
        int c = 0;
        List<Event> events
                = new DatabaseConnection(getApplicationContext()).getAllEvents();
        mUpcomingLayout1.setVisibility(View.GONE);
        mUpcomingLayout2.setVisibility(View.GONE);
        mUpcomingLayout3.setVisibility(View.GONE);
        showUpcomingCard = false;

        for (Event eventInList : events) {
            if (isThisWeek(eventInList.getValue())) {
                switch (i) {
                    case 0:
                        showUpcomingCard = true;
                        mUpcomingTitle1.setText(eventInList.getTitle());
                        mUpcomingDate1.setText(eventInList.getValue());
                        mUpcomingLayout1.setVisibility(View.VISIBLE);
                        i++;
                        break;
                    case 1:
                        mUpcomingTitle2.setText(eventInList.getTitle());
                        mUpcomingDate2.setText(eventInList.getValue());
                        mUpcomingLayout2.setVisibility(View.VISIBLE);
                        i++;
                        break;
                    case 2:
                        mUpcomingTitle3.setText(eventInList.getTitle());
                        mUpcomingDate3.setText(eventInList.getValue());
                        mUpcomingLayout3.setVisibility(View.VISIBLE);
                        i++;
                        break;
                }
                c++;
            }
        }

        if (c > i) {
            mMoreEventsButton.setVisibility(View.VISIBLE);
            mMoreEventsButton.setText((c - i) == 1 ?
                    getString(R.string.more_event_single) :
                    getString(R.string.more_event_multiple_1) + " " + (c - i)
                            + " " + getString(R.string.more_event_multiple_2));
        }
    }

    /**
     * Check if stringDate is one of the next 7 days
     *
     * @param stringDate: string date from event database
     * @return true if it's within 7 days, false if not
     */
    private boolean isThisWeek(String stringDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);

        try {
            Date date = format.parse(stringDate);
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTimeInMillis(date.getTime());


            int diff = dateCalendar.get(Calendar.DAY_OF_YEAR) - c.get(Calendar.DAY_OF_YEAR);

            return c.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) && diff >= 0 && diff < 8;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
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
        Random random = new Random();
        switch (random.nextInt(9) + 1) {
            case 1:
                return getString(Utils.hasSafe(this) ?
                        R.string.suggestion_safe_pwd : R.string.suggestion_safe);
            case 2:
                return getString(R.string.suggestion_avg);
            case 3:
                return getString(R.string.suggestion_tut);
            case 4:
                return getString(R.string.suggestion_edit_event);
            case 5:
                return getString(R.string.suggestion_mark_helper);
            case 6:
                return getString(R.string.suggestion_get_touch);
            case 7:
                return getString(R.string.suggestion_address);
            case 8:
                return getString(R.string.suggestion_tasks);
            default:
                return getString(R.string.suggestion_suggestions);
        }
    }

    /**
     * Show cards with delay so
     * animated LinearLayout will make
     * a nice enter effect
     */
    private void populateCards() {
        mWelcomeCard.setVisibility(View.GONE);
        mUpcomingCard.setVisibility(View.GONE);
        mSuggestionCard.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mWelcomeCard.setVisibility(View.VISIBLE);
            }
        }, 100);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (showUpcomingCard) {
                    mUpcomingCard.setVisibility(View.VISIBLE);
                    mUpcomingCardSeparatorView.setVisibility(View.VISIBLE);
                }
            }
        }, 200);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (showSuggestionCard) {
                    mSuggestionCard.setVisibility(View.VISIBLE);
                    mSuggestionCardSeparatorView.setVisibility(View.VISIBLE);
                }
            }
        }, 300);
    }

    /**
     * Show the intro / tutorial activity
     * if it's the first time we fire the app
     *
     * @param force: if true shows the intro even if the user has already seen it
     */
    private void showIntroIfNeeded(boolean force) {
        SharedPreferences prefs = getSharedPreferences("HomePrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("introKey", false) || force) {
            Intent i = new Intent(MainActivity.this, IntroActivity.class);
            startActivity(i);
            finish();
        }
    }

    /**
     * Show welcome / changelog dialog
     * when the app is installed / updated
     *
     * @param context: used to get SharedPreferences
     */
    private void showWelcomeIfNeeded(Context context) {
        SharedPreferences prefs = getSharedPreferences("HomePrefs", MODE_PRIVATE);

        if (!prefs.getBoolean("introKey", false)) {
            // If we're showing intro, don't display dialog
            return;
        }

        final SharedPreferences.Editor editor =
                getSharedPreferences("HomePrefs", MODE_PRIVATE).edit();

        switch (Utils.appVersionKey(this)) {
            case APP_VERSION:
                return;
            case "0":
                new MaterialDialog.Builder(context)
                        .title(getString(R.string.dialog_first_title))
                        .content(getString(R.string.dialog_first_content))
                        .positiveText(getString(R.string.dialog_first_positive))
                        .negativeText(getString(R.string.dialog_first_negative))
                        .canceledOnTouchOutside(false)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog,
                                                @NonNull DialogAction which) {
                                editor.putString("appVersionKey", APP_VERSION).apply();
                                Intent settingsIntent = new Intent(MainActivity.this,
                                        SettingsActivity.class);
                                startActivity(settingsIntent);

                            }
                        })
                        .show();
                break;
            default:
                new MaterialDialog.Builder(context)
                        .title(getString(R.string.dialog_updated_title))
                        .content(getString(R.string.dialog_updated_content))
                        .positiveText(getString(android.R.string.ok))
                        .canceledOnTouchOutside(false)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog,
                                                @NonNull DialogAction which) {
                                editor.putString("appVersionKey", APP_VERSION).apply();
                            }
                        })
                        .show();
                break;
        }
    }

    /**
     * Update navigation header
     * according to user settings
     */
    private void setupNavHeader() {
        mUserName.setText(Utils.userNameKey(this));

        if (Utils.isTeacher(this)) {
            mAddressLogo.setBackground(getResources().getDrawable(R.drawable.ic_address_6));
        } else {
            switch (Utils.getAddress(this)) {
                case "1":
                    mAddressLogo.setBackground(getResources().getDrawable(R.drawable.ic_address_1));
                    break;
                case "2":
                    mAddressLogo.setBackground(getResources().getDrawable(R.drawable.ic_address_2));
                    break;
                case "3":
                    mAddressLogo.setBackground(getResources().getDrawable(R.drawable.ic_address_3));
                    break;
                case "4":
                    mAddressLogo.setBackground(getResources().getDrawable(R.drawable.ic_address_4));
                    break;
                case "5":
                    mAddressLogo.setBackground(getResources().getDrawable(R.drawable.ic_address_5));
                    break;
            }
        }
    }


}
