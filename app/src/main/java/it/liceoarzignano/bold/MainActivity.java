package it.liceoarzignano.bold;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.realm.RealmConfiguration;
import io.realm.Sort;
import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventListActivity;
import it.liceoarzignano.bold.events.EventsController;
import it.liceoarzignano.bold.home.HomeAdapter;
import it.liceoarzignano.bold.home.HomeCard;
import it.liceoarzignano.bold.intro.BenefitsActivity;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.MarksActivity;
import it.liceoarzignano.bold.marks.MarksController;
import it.liceoarzignano.bold.news.News;
import it.liceoarzignano.bold.news.NewsController;
import it.liceoarzignano.bold.news.NewsListActivity;
import it.liceoarzignano.bold.safe.SafeActivity;
import it.liceoarzignano.bold.settings.SettingsActivity;
import it.liceoarzignano.bold.ui.DividerDecoration;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final Calendar mCalendar = Calendar.getInstance();
    private MarksController mController;
    private EventsController mEventsController;
    private NewsController mNewsController;
    private Toolbar mToolbar;
    private TextView mUserName;
    private ImageView mAddressLogo;

    private RecyclerView mCardsList;

    private CustomTabsClient mClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsIntent mCustomTabsIntent;
    private CustomTabsServiceConnection mCustomTabsServiceConnection;
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RealmConfiguration mConfig = ((BoldApp) getApplication()).getConfig();
        mController = new MarksController(mConfig);
        mEventsController = new EventsController(mConfig);
        mNewsController = new NewsController(mConfig);

        // Intro
        showIntroIfNeeded();

        // UI
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
        mCardsList.setLayoutManager(new LinearLayoutManager(this));
        mCardsList.setItemAnimator(new DefaultItemAnimator());
        mCardsList.addItemDecoration(new DividerDecoration(this));

        // Welcome dialog
        showWelcomeIfNeeded(this);

        // Notification
        if (Utils.hasEventsNotification(this)) {
            Utils.makeEventNotification(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Chrome custom tabs
        setupCCustomTabs();

        // Refresh Navigation Drawer header
        setupNavHeader();

        // Show cards
        populateCards();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCustomTabsServiceConnection != null) {
            unbindService(mCustomTabsServiceConnection);
            mCustomTabsServiceConnection = null;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem mItem) {
        DrawerLayout mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer.closeDrawer(GravityCompat.START);

        // Do action with some delay to prevent lags when
        // loading big lists of marks and events
        new Handler().postDelayed(() -> {
            switch (mItem.getItemId()) {
                case R.id.nav_my_marks:
                    startActivity(new Intent(this, MarksActivity.class));
                    break;
                case R.id.nav_calendar:
                    startActivity(new Intent(this, EventListActivity.class));
                    break;
                case R.id.nav_news:
                    startActivity(new Intent(this, NewsListActivity.class));
                    break;
                case R.id.nav_website:
                    showWebViewUI(0);
                    break;
                case R.id.nav_reg:
                    showWebViewUI(1);
                    break;
                case R.id.nav_moodle:
                    showWebViewUI(2);
                    break;
                case R.id.nav_copyboox:
                    showWebViewUI(3);
                    break;
                case R.id.nav_teacherzone:
                    showWebViewUI(4);
                    break;
                case R.id.nav_settings:
                    Intent settingsIntent = new Intent(this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
                case R.id.nav_safe:
                    Intent safeIntent = new Intent(this, SafeActivity.class);
                    startActivity(safeIntent);
                    break;
            }
        }, 130);

        return true;
    }

    /**
     * Init Chrome custom tabs
     */
    private void setupCCustomTabs() {
        if (mClient != null) {
            return;
        }


        mCustomTabsServiceConnection =
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

        CustomTabsClient.bindCustomTabsService(getBaseContext(), "com.android.chrome",
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
     * If there's no chrome / chromium 46+ it will just open the default browser
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
     *
     * @return events card
     */
    private HomeCard createEventsCard() {
        HomeCard.Builder mBuilder = new HomeCard.Builder()
                .setName(getString(R.string.upcoming_events));

        // Show 3 closest events
        List<Event> mEvents = mEventsController.getAll();

        for (int mCounter = 0; mCounter < 3 && mCounter < mEvents.size(); mCounter++) {
            Event mEvent = mEvents.get(mCounter);
            if (isThisWeek(mEvent.getDate())) {
                mBuilder.addEntry(mEvent.getTitle(), Utils.dateToStr(mEvent.getDate()));
            }
        }

        if (mBuilder.build().getSize() == 0) {
            return null;
        }

        return mBuilder.build();
    }

    /**
     * Show the last 3 news in
     * a card with their titles and dates.
     *
     * @return news card
     */
    private HomeCard createNewsCard() {
        HomeCard.Builder mBuilder = new HomeCard.Builder()
                .setName(getString(R.string.nav_news));

        // Show 3 lastest news
        List<News> mNews = mNewsController.getAll();
        if (mNews.isEmpty()) {
            return null;
        }

        for (int mCounter = 0; mCounter < 3 && mCounter < mNews.size(); mCounter++) {
            News mNewsObj = mNews.get(mCounter);
            mBuilder.addEntry(mNewsObj.getTitle(), mNewsObj.getDate());
        }

        return mBuilder.build();
    }

    /**
     * Show the last 3 marks in
     * a card with their titles and dates.
     *
     * @return marks card
     */
    private HomeCard createMarksCard() {
        HomeCard.Builder mBuilder = new HomeCard.Builder()
                .setName(getString(R.string.lastest_marks));

        List<Mark> mMarks = mController.getAll().sort("date", Sort.DESCENDING);
        if (mMarks.isEmpty()) {
            return null;
        }

        for (int mCounter = 0; mCounter < 3 && mCounter < mMarks.size(); mCounter++) {
            Mark mMark = mMarks.get(mCounter);
            mBuilder.addEntry(mMark.getTitle(), String.valueOf((double) mMark.getValue() / 100));
        }

        return mBuilder.build();
    }

    /**
     * Show a random suggestion in a card
     *
     * @return suggestions card
     */
    private HomeCard createSuggestionsCard() {
        return new HomeCard.Builder()
                .setName(getString(R.string.suggestions))
                .addEntry("", getSuggestion())
                .build();
    }

    /**
     * Check if mDate is one of the next 7 days
     *
     * @param mDate: string date from event database
     * @return true if it's within 7 days, false if not
     */
    private boolean isThisWeek(Date mDate) {
        Calendar mDateCal = Calendar.getInstance();
        mDateCal.setTime(mDate);

        int mDiff = mDateCal.get(Calendar.DAY_OF_YEAR) - mCalendar.get(Calendar.DAY_OF_YEAR);

        return mDateCal.get(Calendar.YEAR) == mCalendar.get(Calendar.YEAR) &&
                mDiff >= 0 && mDiff < 8;
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

        // Events
        HomeCard mEventsCard = createEventsCard();
        if (mEventsCard != null) {
            mCards.add(mEventsCard);
        }

        // News
        HomeCard mNewsCard = createNewsCard();
        if (mNewsCard != null) {
            mCards.add(mNewsCard);
        }

        // Marks
        if (Utils.hasUsedForMoreThanOneWeek(this)) {
            HomeCard mMarksCard = createMarksCard();
            if (mMarksCard != null) {
                mCards.add(mMarksCard);
            }
        }

        // Suggestions
        if (Utils.hasSuggestions(this)) {
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
            case BuildConfig.VERSION_NAME:
                break;
            case "0":
                // Used for feature discovery
                final String today = Utils.getTodayStr();
                mEditor.putString("appVersionKey", BuildConfig.VERSION_NAME).apply();
                mEditor.putString("initialDayKey", today).apply();
                break;
            default:
                new MaterialDialog.Builder(mContext)
                        .title(R.string.dialog_updated_title)
                        .content(R.string.dialog_updated_content)
                        .positiveText(android.R.string.ok)
                        .negativeText(R.string.dialog_updated_changelog)
                        .canceledOnTouchOutside(false)
                        .onPositive((dialog, which) -> mEditor.putString("appVersionKey",
                                BuildConfig.VERSION_NAME).apply())
                        .onNegative((dialog, which) -> {
                            dialog.hide();
                            showWebViewUI(-1);
                        })

                        .show();
                break;
        }

        if (mPrefs.getBoolean("drawerIntro", true)) {
            final Activity mActivity = this;
            String[] mAddresses = new String[]{
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
                    .itemsCallbackSingleChoice(-1, (dialog, itemView, which, text) -> {
                        boolean isAddressValid = which != -1;
                        if (which == 5) {
                            Utils.setTeacherMode(mContext);
                        } else if (isAddressValid) {
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

        if (Utils.isNotLegacy()) {
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
}
