package it.liceoarzignano.bold;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
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
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.realm.RealmConfiguration;
import io.realm.Sort;
import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventListActivity;
import it.liceoarzignano.bold.events.EventsController;
import it.liceoarzignano.bold.home.HomeAdapter;
import it.liceoarzignano.bold.home.HomeCard;
import it.liceoarzignano.bold.home.HomeCardBuilder;
import it.liceoarzignano.bold.intro.BenefitsActivity;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.MarksActivity;
import it.liceoarzignano.bold.marks.MarksController;
import it.liceoarzignano.bold.news.News;
import it.liceoarzignano.bold.news.NewsController;
import it.liceoarzignano.bold.news.NewsListActivity;
import it.liceoarzignano.bold.safe.SafeActivity;
import it.liceoarzignano.bold.settings.SettingsActivity;
import it.liceoarzignano.bold.ui.recyclerview.DividerDecoration;
import it.liceoarzignano.bold.utils.ContentUtils;
import it.liceoarzignano.bold.utils.DateUtils;
import it.liceoarzignano.bold.utils.PrefsUtils;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseRemoteConfig mRemoteConfig;

    private MarksController mMarksController;
    private EventsController mEventsController;
    private NewsController mNewsController;

    private Toolbar mToolbar;
    private ImageView mBanner;
    private RecyclerView mCardsList;
    private TextView mUserName;
    private ImageView mAddressLogo;

    private CustomTabsClient mClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsIntent mCustomTabsIntent;
    private CustomTabsServiceConnection mCustomTabsServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Realm config
        RealmConfiguration config = ((BoldApp) getApplication()).getConfig();
        mMarksController = new MarksController(config);
        mEventsController = new EventsController(config);
        mNewsController = new NewsController(config);

        // Firebase config
        setupRemoteConfig();

        // Intro
        showIntroIfNeeded();

        // UI
        setContentView(R.layout.activity_main);

        // Toolbar and NavDrawer
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mBanner = (ImageView) findViewById(R.id.home_toolbar_banner);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        mUserName = (TextView) header.findViewById(R.id.header_username);
        mAddressLogo = (ImageView) header.findViewById(R.id.header_logo);
        if (PrefsUtils.isTeacher(this)) {
            navigationView.getMenu().findItem(R.id.nav_share).setVisible(false);
        }

        // Cards List
        mCardsList = (RecyclerView) findViewById(R.id.home_list);
        mCardsList.setLayoutManager(new LinearLayoutManager(this));
        mCardsList.setItemAnimator(new DefaultItemAnimator());
        mCardsList.addItemDecoration(new DividerDecoration(this));

        // Welcome dialog
        showWelcomeIfNeeded(this);

        // Notification
        if (PrefsUtils.hasEventsNotification(this)) {
            ContentUtils.makeEventNotification(this);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        // Do action with some delay to prevent lags when
        // loading big lists of marks and events
        new Handler().postDelayed(() -> {
            switch (item.getItemId()) {
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
                case R.id.nav_share:
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, mRemoteConfig.getString("share_url"));
                    startActivity(Intent.createChooser(shareIntent,
                            getString(R.string.share_title)));
                    break;
                case R.id.nav_help:
                    showWebViewUI(5);
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

        mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
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
     * @param index: the selected item from the nav drawer menu
     */
    private void showWebViewUI(int index) {
        String url = null;
        switch (index) {
            case -1:
                url = getString(R.string.config_url_changelog);
                break;
            case 0:
                url = getString(R.string.config_url_home);
                break;
            case 1:
                url = getString(PrefsUtils.isTeacher(this) ?
                        R.string.config_url_reg_teacher : R.string.config_url_reg_student);
                break;
            case 2:
                url = getString(R.string.config_url_moodle);
                break;
            case 3:
                url = getString(R.string.config_url_copybook);
                break;
            case 4:
                url = getString(R.string.config_url_teacherzone);
                break;
            case 5:
                url = getString(R.string.config_url_help);
                break;
        }

        if (url != null) {
            mCustomTabsIntent.launchUrl(this, Uri.parse(url));
        }
    }

    /**
     * Show the first 3 events in the following 7 days in
     * a card with their titles and dates.
     *
     * @return events card
     */
    private HomeCard createEventsCard() {
        HomeCardBuilder builder = new HomeCardBuilder().setName(getString(R.string.upcoming_events));

        // Show 3 closest events
        List<Event> events = mEventsController.getAll();

        int added = 0;
        for (Event event : events) {
            if (!DateUtils.dateDiff(DateUtils.getDate(7), event.getDate(), 8)) {
                added++;
                builder.addEntry(event.getTitle(),
                        DateUtils.dateToWordsString(this, event.getDate()));
            }
            if (added == 3) {
                break;
            }
        }

        if (builder.build().getSize() == 0) {
            return null;
        }

        return builder.build();
    }

    /**
     * Show the last 3 news in
     * a card with their titles and dates.
     *
     * @return news card
     */
    private HomeCard createNewsCard() {
        HomeCardBuilder builder = new HomeCardBuilder().setName(getString(R.string.nav_news));

        // Show 3 lastest news
        List<News> newsList = mNewsController.getAll();
        if (newsList.isEmpty()) {
            return null;
        }

        for (int counter = 0; counter < 3 && counter < newsList.size(); counter++) {
            News news = newsList.get(counter);
            builder.addEntry(news.getTitle(), DateUtils.dateToWordsString(this, news.getDate()));
        }

        return builder.build();
    }

    /**
     * Show the last 3 marks in
     * a card with their titles and dates.
     *
     * @return marks card
     */
    private HomeCard createMarksCard() {
        HomeCardBuilder builder = new HomeCardBuilder().setName(getString(R.string.lastest_marks));

        List<Mark> marks = mMarksController.getAll().sort("date", Sort.DESCENDING);
        if (marks.isEmpty()) {
            return null;
        }

        for (int counter = 0; counter < 3 && counter < marks.size(); counter++) {
            Mark mark = marks.get(counter);
            builder.addEntry(mark.getTitle(), String.valueOf((double) mark.getValue() / 100));
        }

        return builder.build();
    }

    /**
     * Show a random suggestion in a card
     *
     * @return suggestions card
     */
    private HomeCard createSuggestionsCard() {
        return new HomeCardBuilder()
                .setName(getString(R.string.suggestions))
                .addEntry("", getSuggestion())
                .build();
    }

    /**
     * Get random suggestion text to be shown in
     * the suggestions card
     *
     * @return string with text for suggestion card
     */
    private String getSuggestion() {
        Random random = new SecureRandom();
        switch (random.nextInt(11) + 1) {
            case 1:
                return getString(PrefsUtils.hasSafe(this) ?
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
        List<HomeCard> cards = new ArrayList<>();

        // Events
        HomeCard eventsCard = createEventsCard();
        if (eventsCard != null) {
            cards.add(eventsCard);
        }

        // News
        HomeCard newsCard = createNewsCard();
        if (newsCard != null) {
            cards.add(newsCard);
        }

        // Marks
        if (DateUtils.dateDiff(DateUtils.getDate(0), PrefsUtils.getFirstUsageDate(this), 7)) {
            HomeCard marksCard = createMarksCard();
            if (marksCard != null) {
                cards.add(marksCard);
            }
        }

        // Suggestions
        if (PrefsUtils.hasSuggestions(this)) {
            cards.add(createSuggestionsCard());
        }

        HomeAdapter adapter = new HomeAdapter(cards);
        mCardsList.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    /**
     * Load remote configs, they get applied once the app is
     */
    private void setupRemoteConfig() {
        mRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mRemoteConfig.setConfigSettings(configSettings);
        mRemoteConfig.setDefaults(R.xml.firebase_remote_config_defaults);
        mRemoteConfig.fetch(BuildConfig.DEBUG ? 0 : TimeUnit.HOURS.toMillis(12))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mRemoteConfig.activateFetched();
                    }
                });
    }

    /**
     * Show the intro / tutorial activity
     * if it's the first time we fire the app
     */
    private void showIntroIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PrefsUtils.EXTRA_PREFS, MODE_PRIVATE);
        if (!prefs.getBoolean(PrefsUtils.KEY_INTRO_SCREEN, false)) {
            Intent intent = new Intent(this, BenefitsActivity.class);
            startActivity(intent);
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
        final SharedPreferences prefs = getSharedPreferences(PrefsUtils.EXTRA_PREFS, MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits")
        final SharedPreferences.Editor editor = prefs.edit();

        if (!prefs.getBoolean(PrefsUtils.KEY_INTRO_SCREEN, false)) {
            // If we're showing intro, don't display dialog
            return;
        }

        switch (PrefsUtils.appVersionKey(this)) {
            case BuildConfig.VERSION_NAME:
                break;
            case "0":
                // Used for feature discovery
                final String today = DateUtils.getDateString(0);
                editor.putString(PrefsUtils.KEY_VERSION, BuildConfig.VERSION_NAME).apply();
                editor.putString(PrefsUtils.KEY_INITIAL_DAY, today).apply();
                break;
            default:
                new MaterialDialog.Builder(context)
                        .title(R.string.dialog_updated_title)
                        .content(R.string.dialog_updated_content)
                        .positiveText(android.R.string.ok)
                        .negativeText(R.string.dialog_updated_changelog)
                        .canceledOnTouchOutside(false)
                        .onPositive((dialog, which) -> editor.putString(PrefsUtils.KEY_VERSION,
                                BuildConfig.VERSION_NAME).apply())
                        .onNegative((dialog, which) -> {
                            dialog.hide();
                            showWebViewUI(-1);
                        })
                        .show();
                break;
        }

        if (prefs.getBoolean(PrefsUtils.KEY_INTRO_DRAWER, true)) {
            editor.putBoolean(PrefsUtils.KEY_INTRO_DRAWER, false).apply();
            new MaterialTapTargetPrompt.Builder(this)
                    .setTarget(mToolbar.getChildAt(1))
                    .setPrimaryText(getString(R.string.intro_drawer_title))
                    .setSecondaryText(getString(R.string.intro_drawer))
                    .setBackgroundColourFromRes(R.color.colorAccentDark)
                    .setFocalColourFromRes(R.color.colorPrimaryDark)
                    .show();
        }
    }

    /**
     * Update navigation header
     * according to user settings
     * (api 21+ only)
     */
    private void setupNavHeader() {
        mUserName.setText(PrefsUtils.userNameKey(this));

        byte[] imgB64 = Base64.decode(mRemoteConfig.getString("home_banner").getBytes(),
                Base64.DEFAULT);
        mBanner.setImageBitmap(BitmapFactory.decodeByteArray(imgB64, 0, imgB64.length));

        if (!PrefsUtils.isNotLegacy()) {
            return;
        }

        if (PrefsUtils.isTeacher(this)) {
            mAddressLogo.setBackground(getDrawable(R.drawable.ic_address_6));
        } else {
            switch (PrefsUtils.getAddress(this)) {
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
