package it.liceoarzignano.bold;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.liceoarzignano.bold.backup.BackupActivity;
import it.liceoarzignano.bold.events.Event2;
import it.liceoarzignano.bold.events.EventListActivity;
import it.liceoarzignano.bold.events.EventsHandler;
import it.liceoarzignano.bold.home.HomeAdapter;
import it.liceoarzignano.bold.home.HomeCard;
import it.liceoarzignano.bold.home.HomeCardBuilder;
import it.liceoarzignano.bold.intro.BenefitsActivity;
import it.liceoarzignano.bold.marks.Mark2;
import it.liceoarzignano.bold.marks.MarksActivity;
import it.liceoarzignano.bold.marks.MarksHandler;
import it.liceoarzignano.bold.news.News2;
import it.liceoarzignano.bold.news.NewsHandler;
import it.liceoarzignano.bold.news.NewsListActivity;
import it.liceoarzignano.bold.realm.MigrationTool;
import it.liceoarzignano.bold.safe.SafeActivity;
import it.liceoarzignano.bold.settings.SettingsActivity;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt;
import it.liceoarzignano.bold.utils.DateUtils;
import it.liceoarzignano.bold.utils.PrefsUtils;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String BUNDLE_SHOULD_ANIMATE = "homeShouldAnimate";

    private FirebaseRemoteConfig mRemoteConfig;
    private MarksHandler mMarksHandler;
    private EventsHandler mEventsHandler;
    private NewsHandler mNewsHandler;

    private Toolbar mToolbar;
    private ImageView mBanner;
    private RecyclerViewExt mCardList;
    private TextView mUsername;
    private ImageView mLogo;

    private CustomTabsClient mTabsClient;
    private CustomTabsSession mTabsSession;
    private CustomTabsIntent mTabsIntent;
    private CustomTabsServiceConnection mTabsServiceConnection;

    private boolean mShouldAnimate = true;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setupDBHandler();
        setupRemoteConfig();

        showIntroIfNeeded();

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mBanner = (ImageView) findViewById(R.id.home_toolbar_banner);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navView = (NavigationView) findViewById(R.id.navigation_view);
        navView.setNavigationItemSelectedListener(this);
        View header = navView.getHeaderView(0);
        mUsername = (TextView) header.findViewById(R.id.header_username);
        mLogo = (ImageView) header.findViewById(R.id.header_logo);
        mCardList = (RecyclerViewExt) findViewById(R.id.home_list);

        if (savedInstance != null) {
            mShouldAnimate = savedInstance.getBoolean(BUNDLE_SHOULD_ANIMATE, true);
        }

        showWelcome();
        showNewYearHelper();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupTabs();
        setupNavHeader();
        setupCards();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mTabsServiceConnection != null) {
            unbindService(mTabsServiceConnection);
            mTabsServiceConnection = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_SHOULD_ANIMATE, false);
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
                case R.id.nav_safe:
                    startActivity(new Intent(this, SafeActivity.class));
                    break;
                case R.id.nav_website:
                    showUrl(0);
                    break;
                case R.id.nav_reg:
                    showUrl(1);
                    break;
                case R.id.nav_moodle:
                    showUrl(2);
                    break;
                case R.id.nav_copyboox:
                    showUrl(3);
                    break;
                case R.id.nav_teacherzone:
                    showUrl(4);
                    break;
                case R.id.nav_settings:
                    startActivity(new Intent(this, SettingsActivity.class));
                    break;
                case R.id.nav_share:
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, mRemoteConfig.getString("share_url"));
                    startActivity(Intent.createChooser(shareIntent,
                            getString(R.string.share_title)));
                    break;
                case R.id.nav_help:
                    showUrl(5);
                    break;
            }
        }, 130);

        return true;
    }

    private void setupDBHandler() {
        mMarksHandler = MarksHandler.getInstance(this);
        mEventsHandler = EventsHandler.getInstance(this);
        mNewsHandler = NewsHandler.getInstance(this);
    }

    private void setupRemoteConfig() {
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mRemoteConfig = FirebaseRemoteConfig.getInstance();
        mRemoteConfig.setConfigSettings(settings);
        mRemoteConfig.setDefaults(R.xml.firebase_remote_config_defaults);
        mRemoteConfig.fetch(BuildConfig.DEBUG ? 0 : TimeUnit.HOURS.toSeconds(12))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mRemoteConfig.activateFetched();
                    }
                });
    }

    private void setupNavHeader() {
        mUsername.setText(PrefsUtils.userNameKey(this));

        byte[] imgB64 = Base64.decode(mRemoteConfig.getString("home_banner").getBytes(),
                Base64.DEFAULT);
        mBanner.setImageBitmap(BitmapFactory.decodeByteArray(imgB64, 0, imgB64.length));

        if (!PrefsUtils.isNotLegacy()) {
            return;
        }

        if (PrefsUtils.isTeacher(this)) {
            mLogo.setBackground(getDrawable(R.drawable.ic_address_6));
        } else {
            switch (PrefsUtils.getAddress(this)) {
                case "1":
                    mLogo.setBackground(getDrawable(R.drawable.ic_address_1));
                    break;
                case "2":
                    mLogo.setBackground(getDrawable(R.drawable.ic_address_2));
                    break;
                case "3":
                    mLogo.setBackground(getDrawable(R.drawable.ic_address_3));
                    break;
                case "4":
                    mLogo.setBackground(getDrawable(R.drawable.ic_address_4));
                    break;
                case "5":
                    mLogo.setBackground(getDrawable(R.drawable.ic_address_5));
                    break;
            }
        }
    }

    private void setupTabs() {
        if (mTabsClient != null) {
            return;
        }

        mTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                mTabsClient = client;
                mTabsClient.warmup(0L);
                mTabsSession = mTabsClient.newSession(null);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mTabsClient = null;
            }
        };

        CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", mTabsServiceConnection);
        mTabsIntent = new CustomTabsIntent.Builder(mTabsSession)
                .setStartAnimations(this,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left)
                .setExitAnimations(this,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right)
                .build();
    }

    private void showIntroIfNeeded() {
        if (PrefsUtils.hasDoneIntro(this)) {
            return;
        }

        startActivity(new Intent(this, BenefitsActivity.class));
        finish();
    }

    private void showWelcome() {
        if (!PrefsUtils.hasDoneIntro(this)) {
            return;
        }

        // Data migration Realm -> SQL
        MigrationTool migrationTool = new MigrationTool();
        if (!migrationTool.hasMigrated(this)) {
            migrateDatabase(migrationTool);
            return;
        }

        switch (PrefsUtils.getAppVersion(this)) {
            case BuildConfig.VERSION_NAME:
                break;
            case "0":
                PrefsUtils.updateAppVersion(this);
                PrefsUtils.setInitialDay(this);
                break;
            default:
                new MaterialDialog.Builder(this)
                        .title(R.string.dialog_updated_title)
                        .content(R.string.dialog_updated_content)
                        .positiveText(android.R.string.ok)
                        .negativeText(R.string.dialog_updated_changelog)
                        .canceledOnTouchOutside(false)
                        .onPositive(((dialog, which) -> PrefsUtils.updateAppVersion(this)))
                        .onNegative(((dialog, which) -> {
                            dialog.dismiss();
                            showUrl(-1);
                        }))
                        .show();
                return;
        }

        if (PrefsUtils.hasDoneDrawerIntro(this)) {
            return;
        }

        PrefsUtils.setDoneDrawerIntro(this);
        new MaterialTapTargetPrompt.Builder(this)
                .setTarget(mToolbar.getChildAt(1))
                .setPrimaryText(getString(R.string.intro_drawer_title))
                .setSecondaryText(getString(R.string.intro_drawer))
                .setBackgroundColourFromRes(R.color.colorAccentDark)
                .setFocalColourFromRes(R.color.colorPrimaryDark)
                .show();
    }

    private void showNewYearHelper() {
        Date today = DateUtils.getDate(0);
        Date change = DateUtils.stringToDate(getString(R.string.config_end_of_year));
        Calendar cal = Calendar.getInstance();
        String latestSavedYear = PrefsUtils.getCurrentSchoolYear(this);
        String thisYear = String.valueOf(cal.get(Calendar.YEAR));

        if (latestSavedYear.equals(thisYear) || !DateUtils.matchDayOfYear(today, change)) {
            return;
        }

        PrefsUtils.setCurrentSchoolYear(this);
        PrefsUtils.setCurrentQuarter(this, 0);

        new MaterialDialog.Builder(this)
                .title(R.string.backup_end_of_year_prompt_title)
                .content(R.string.backup_end_of_year_prompt_message)
                .positiveText(R.string.backup_end_of_year_prompt_positive)
                .negativeText(R.string.backup_end_of_year_prompt_negative)
                .onPositive((dialog, which) -> {
                    Intent intent = new Intent(this, BackupActivity.class);
                    intent.putExtra(BackupActivity.EXTRA_EOY_BACKUP, "OK");
                    startActivity(intent);
                })
                .show();
    }

    private void showUrl(int index) {
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
            mTabsIntent.launchUrl(this, Uri.parse(url));
        }
    }

    private void setupCards() {
        List<HomeCard> cards = new ArrayList<>();

        HomeCard events = getEventsCard();
        if (events != null) {
            cards.add(events);
        }

        HomeCard news = getNewsCard();
        if (news != null) {
            cards.add(news);
        }

        HomeCard marks = getMarksCard();
        if (DateUtils.dateDiff(DateUtils.getDate(0), PrefsUtils.getFirstUsageDate(this), 7) &&
                marks != null) {
            cards.add(marks);
        }

        if (PrefsUtils.hasSuggestions(this)) {
            cards.add(getSuggestionsCard());
        }

        HomeAdapter adapter = new HomeAdapter(this, cards, mShouldAnimate);
        mCardList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (mShouldAnimate) {
            mShouldAnimate = false;
        }
    }

    private void onCardClick(@NonNull View view, @NonNull Intent intent) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                view, "card_activity");
        ActivityCompat.startActivity(this, intent, options.toBundle());

    }

    @Nullable
    private HomeCard getEventsCard() {
        HomeCardBuilder builder = new HomeCardBuilder()
                .setName(getString(R.string.upcoming_events))
                .setOnClick(view -> onCardClick(view, new Intent(this, EventListActivity.class)));

        List<Event2> events = mEventsHandler.getAll();
        int added = 0;
        Date nextWeek = DateUtils.getDate(7);
        for (Event2 e : events) {
            Date date = new Date(e.getDate());
            if (!DateUtils.dateDiff(nextWeek, date, 8)) {
                added++;
                builder.addEntry(e.getTitle(), DateUtils.dateToWordsString(this, date));
            }
            if (added == 3) {
                break;
            }
        }

        HomeCard card = builder.build();
        return card.getSize() == 0 ? null : card;
    }

    @Nullable
    private HomeCard getNewsCard() {
        HomeCardBuilder builder = new HomeCardBuilder()
                .setName(getString(R.string.nav_news))
                .setOnClick(view -> onCardClick(view, new Intent(this, NewsListActivity.class)));

        List<News2> news = mNewsHandler.getAll();
        for (int i = 0; i < 3 && i < news.size(); i++) {
            News2 n = news.get(i);
            builder.addEntry(n.getTitle(), DateUtils.dateToWordsString(this,
                    new Date(n.getDate())));
        }

        HomeCard card = builder.build();
        return card.getSize() == 0 ? null : card;
    }

    @Nullable
    private HomeCard getMarksCard() {
        HomeCardBuilder builder = new HomeCardBuilder()
                .setName(getString(R.string.lastest_marks))
                .setOnClick(view -> onCardClick(view, new Intent(this, MarksActivity.class)));


        List<Mark2> marks = mMarksHandler.getAll();
        Collections.reverse(marks);
        for (int i = 0; i < 3 && i < marks.size(); i++) {
            Mark2 m = marks.get(i);
            builder.addEntry(m.getSubject(), String.valueOf(m.getValue() / 100d));
        }

        HomeCard card = builder.build();
        return card.getSize() == 0 ? null : card;
    }

    @Nullable
    private HomeCard getSuggestionsCard() {
        return new HomeCardBuilder()
                .setName(getString(R.string.suggestions))
                .addEntry("", getSuggestion())
                .build();
    }

    @NonNull
    private String getSuggestion() {
        switch (new SecureRandom().nextInt(12) + 1) {
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
            case 11:
                return getString(R.string.suggestion_feedback);
            default:
                return getString(R.string.suggestion_notification);
        }
    }

    private void migrateDatabase(MigrationTool migrationTool) {
        MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                .content(R.string.dialog_updated_database_upgrade)
                .canceledOnTouchOutside(false)
                .progressIndeterminateStyle(true)
                .progress(true, 10)
                .show();

        new AsyncTask<Void, Boolean, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                migrationTool.migrate((BoldApp) getApplication());
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(),
                        13092, new Intent(getApplication(), MainActivity.class),
                        PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC,
                        System.currentTimeMillis() + 1705, pendingIntent);
                new Handler().postDelayed(() -> {
                    progressDialog.dismiss();
                    finish();
                }, 1700);
            }
        }.execute();
    }
}
