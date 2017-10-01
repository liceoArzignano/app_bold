package it.liceoarzignano.bold

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.annotation.IdRes
import android.support.customtabs.CustomTabsClient
import android.support.customtabs.CustomTabsIntent
import android.support.customtabs.CustomTabsServiceConnection
import android.support.customtabs.CustomTabsSession
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import it.liceoarzignano.bold.backup.BackupActivity
import it.liceoarzignano.bold.events.EventListActivity
import it.liceoarzignano.bold.events.EventsHandler
import it.liceoarzignano.bold.home.HomeAdapter
import it.liceoarzignano.bold.home.HomeCard
import it.liceoarzignano.bold.home.HomeCardBuilder
import it.liceoarzignano.bold.home.ShortcutAdapter
import it.liceoarzignano.bold.intro.IntroActivity
import it.liceoarzignano.bold.marks.MarksActivity
import it.liceoarzignano.bold.marks.MarksHandler
import it.liceoarzignano.bold.news.NewsHandler
import it.liceoarzignano.bold.news.NewsListActivity
import it.liceoarzignano.bold.safe.SafeActivity
import it.liceoarzignano.bold.settings.AppPrefs
import it.liceoarzignano.bold.settings.SettingsActivity
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt
import it.liceoarzignano.bold.utils.ContentUtils
import it.liceoarzignano.bold.utils.Time
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mRemoteConfig: FirebaseRemoteConfig? = null
    lateinit private var mMarksHandler: MarksHandler
    lateinit private var mEventsHandler: EventsHandler
    lateinit private var mNewsHandler: NewsHandler
    lateinit private var mPrefs: AppPrefs

    lateinit private var mToolbar: Toolbar
    lateinit private var mDrawer: DrawerLayout
    lateinit private var mShortcutsList: RecyclerViewExt
    lateinit private var mCardList: RecyclerViewExt
    lateinit private var mUsername: TextView
    lateinit private var mLogo: ImageView

    private var mTabsClient: CustomTabsClient? = null
    private var mTabsSession: CustomTabsSession? = null
    private var mTabsIntent: CustomTabsIntent? = null
    private var mTabsServiceConnection: CustomTabsServiceConnection? = null

    private var mShouldAnimate = true

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        mPrefs = AppPrefs(baseContext)
        mPrefs.migrate(baseContext)

        setupDBHandler()
        setupRemoteConfig()
        showIntroIfNeeded()

        setContentView(R.layout.activity_main)

        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
        mDrawer = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.navigation_view)
        navView.setNavigationItemSelectedListener(this)
        val header = navView.getHeaderView(0)
        mUsername = header.findViewById(R.id.header_username)
        mLogo = header.findViewById(R.id.header_logo)
        mShortcutsList = findViewById(R.id.home_shortcuts)
        mCardList = findViewById(R.id.home_list)

        if (savedInstance != null) {
            mShouldAnimate = savedInstance.getBoolean(BUNDLE_SHOULD_ANIMATE, true)
        }

        initializeDrawer()
        showWelcome()
        showNewYearHelper()
        ContentUtils.makeEventNotification(this)
        setupShortcuts()
    }

    override fun onResume() {
        super.onResume()

        setupTabs()
        setupNavHeader()
        setupCards()
    }

    override fun onPause() {
        super.onPause()

        stopTabsService()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BUNDLE_SHOULD_ANIMATE, false)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Do action with some delay to prevent lags when
        // loading big lists of marks and events
        Handler().postDelayed({ onDrawerClick(item.itemId) }, 200)
        mDrawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun stopTabsService() {
        if (mTabsServiceConnection == null) {
            return
        }

        unbindService(mTabsServiceConnection)
        mTabsServiceConnection = null
    }

    private fun initializeDrawer() {
        val toggle = ActionBarDrawerToggle(this, mDrawer, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mDrawer.addDrawerListener(toggle)
        toggle.isDrawerIndicatorEnabled = false
        toggle.setHomeAsUpIndicator(R.drawable.ic_nav_drawer)
        toggle.setToolbarNavigationClickListener { mDrawer.openDrawer(Gravity.LEFT) }
        toggle.syncState()
    }

    private fun onDrawerClick(@IdRes id: Int) {
        when (id) {
            R.id.nav_my_marks -> startActivity(Intent(this, MarksActivity::class.java))
            R.id.nav_calendar -> startActivity(Intent(this, EventListActivity::class.java))
            R.id.nav_news -> startActivity(Intent(this, NewsListActivity::class.java))
            R.id.nav_safe -> startActivity(Intent(this, SafeActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, mRemoteConfig!!.getString("share_url"))
                startActivity(Intent.createChooser(shareIntent,
                        getString(R.string.share_title)))
            }
        }
    }

    private fun setupDBHandler() {
        mMarksHandler = MarksHandler.getInstance(this)
        mEventsHandler = EventsHandler.getInstance(this)
        mNewsHandler = NewsHandler.getInstance(this)
    }

    private fun setupRemoteConfig() {
        val settings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        mRemoteConfig = FirebaseRemoteConfig.getInstance()
        mRemoteConfig!!.setConfigSettings(settings)
        mRemoteConfig!!.setDefaults(R.xml.firebase_remote_config_defaults)
        mRemoteConfig!!.fetch(if (BuildConfig.DEBUG) 0 else TimeUnit.HOURS.toSeconds(12))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mRemoteConfig!!.activateFetched()
                    }
                }
    }

    private fun setupNavHeader() {
        mUsername.text = mPrefs.get(AppPrefs.KEY_USERNAME, "")
        mUsername.visibility = if (mUsername.text.isBlank()) View.GONE else View.VISIBLE

        if (mPrefs.get(AppPrefs.KEY_IS_TEACHER, false)) {
            mLogo.setImageResource(R.drawable.ic_address_6)
        } else {
            when (mPrefs.get(AppPrefs.KEY_ADDRESS, "0")) {
                "1" -> mLogo.setImageResource(R.drawable.ic_address_1)
                "2" -> mLogo.setImageResource(R.drawable.ic_address_2)
                "3" -> mLogo.setImageResource(R.drawable.ic_address_3)
                "4" -> mLogo.setImageResource(R.drawable.ic_address_4)
                "5" -> mLogo.setImageResource(R.drawable.ic_address_5)
            }
        }
    }

    private fun setupTabs() {
        if (mTabsClient != null) {
            return
        }

        mTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                mTabsClient = client
                mTabsClient!!.warmup(0L)
                mTabsSession = mTabsClient!!.newSession(null)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mTabsClient = null
            }
        }

        CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", mTabsServiceConnection)
        mTabsIntent = CustomTabsIntent.Builder(mTabsSession)
                .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setShowTitle(true)
                .setStartAnimations(this,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left)
                .setExitAnimations(this,
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right)
                .build()
    }

    private fun showIntroIfNeeded() {
        if (AppPrefs(baseContext).get(AppPrefs.KEY_INTRO_SCREEN, false)) {
            return
        }

        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }

    private fun showWelcome() {
        if (!mPrefs.get(AppPrefs.KEY_INTRO_SCREEN, false)) {
            return
        }

        when (mPrefs.get(AppPrefs.KEY_INTRO_VERSION, "0")) {
            BuildConfig.VERSION_NAME -> return
            "0" -> {
                mPrefs.set(AppPrefs.KEY_INTRO_VERSION, BuildConfig.VERSION_NAME)
                mPrefs.set(AppPrefs.KEY_INTRO_DAY, Time().toString())
            }
            else -> {
                MaterialDialog.Builder(this)
                        .title(R.string.dialog_updated_title)
                        .content(R.string.dialog_updated_content)
                        .positiveText(android.R.string.ok)
                        .negativeText(R.string.dialog_updated_changelog)
                        .canceledOnTouchOutside(false)
                        .onPositive { _, _ ->
                            mPrefs.set(AppPrefs.KEY_INTRO_VERSION, BuildConfig.VERSION_NAME)
                        }
                        .onNegative { dialog, _ ->
                            dialog.dismiss()
                            showUrl(-1)
                        }
                        .show()
                return
            }
        }

        if (mPrefs.get(AppPrefs.KEY_INTRO_DRAWER)) {
            return
        }

        mPrefs.set(AppPrefs.KEY_INTRO_DRAWER, true)
        MaterialTapTargetPrompt.Builder(this, R.style.AppTheme_TapTargetHome)
                .setTarget(mToolbar.getChildAt(1))
                .setPrimaryText(getString(R.string.intro_drawer_title))
                .setSecondaryText(getString(R.string.intro_drawer))
                .show()
    }

    private fun showNewYearHelper() {
        val today = Time(0)
        val change = Time.parse(getString(R.string.config_end_of_year))
        val cal = Calendar.getInstance()
        val latestSavedYear: String = mPrefs.get(AppPrefs.KEY_CURRENT_YEAR, "2000-01-01")
        val thisYear = cal.get(Calendar.YEAR).toString()

        if (latestSavedYear == thisYear || !today.matchDayOfYear(change)) {
            return
        }

        val calendar = Calendar.getInstance()
        mPrefs.set(AppPrefs.KEY_CURRENT_YEAR, calendar.get(Calendar.YEAR))
        mPrefs.set(AppPrefs.KEY_QUARTER_SELECTOR, 0)

        MaterialDialog.Builder(this)
                .title(R.string.backup_end_of_year_prompt_title)
                .content(R.string.backup_end_of_year_prompt_message)
                .positiveText(R.string.backup_end_of_year_prompt_positive)
                .negativeText(R.string.backup_end_of_year_prompt_negative)
                .onPositive { _, _ ->
                    val intent = Intent(this, BackupActivity::class.java)
                    intent.putExtra(BackupActivity.EXTRA_EOY_BACKUP, "OK")
                    startActivity(intent)
                }
                .show()
    }

    fun showUrl(index: Int) {
        var url: String? = null
        when (index) {
            -1 -> url = getString(R.string.config_url_changelog)
            0 -> url = getString(R.string.config_url_home)
            1 -> url = getString(if (mPrefs.get(AppPrefs.KEY_IS_TEACHER, false))
                R.string.config_url_reg_teacher
            else
                R.string.config_url_reg_student)
            2 -> url = getString(R.string.config_url_moodle)
            3 -> url = getString(R.string.config_url_copybook)
            4 -> url = getString(R.string.config_url_teacherzone)
            5 -> url = getString(R.string.config_url_help)
        }

        if (url != null) {
            mTabsIntent!!.launchUrl(this, Uri.parse(url))
        }
    }

    private fun setupShortcuts() {
        val adapter = ShortcutAdapter(this)
        mShortcutsList.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun setupCards() {
        val cards = ArrayList<HomeCard>()

        val events = eventsCard
        if (events != null) {
            cards.add(events)
        }

        val news = newsCard
        if (news != null) {
            cards.add(news)
        }

        val marks = marksCard
        val date = Time.parse(mPrefs.get(AppPrefs.KEY_INTRO_DAY, "2017-01-01"))
        if (Time().diff(date, 7) && marks != null) {
            cards.add(marks)
        }

        if (mPrefs.get(AppPrefs.KEY_SUGGESTIONS)) {
            cards.add(suggestionsCard)
        }

        val adapter = HomeAdapter(this, cards, mShouldAnimate)
        mCardList.adapter = adapter
        adapter.notifyDataSetChanged()

        if (mShouldAnimate) {
            mShouldAnimate = false
        }
    }

    private val eventsCard: HomeCard?
        get() {
            val builder = HomeCardBuilder()
                    .setName(getString(R.string.upcoming_events))
                    .setOnClick(object : HomeCard.HomeCardClickListener {
                        override fun onClick() =
                                startActivity(Intent(baseContext, EventListActivity::class.java))
                    })

            val events = mEventsHandler.all
            var added = 0
            val nextWeek = Time(7)
            for (e in events) {
                val date = Time(e.date)
                if (!nextWeek.diff(date, 8)) {
                    added++
                    builder.addEntry(e.title, date.asString(baseContext))
                }
                if (added == 3) {
                    break
                }
            }

            val card = builder.build()
            return if (card.size == 0) null else card
        }

    private val newsCard: HomeCard?
        get() {
            val builder = HomeCardBuilder()
                    .setName(getString(R.string.nav_news))
                    .setOnClick(object : HomeCard.HomeCardClickListener {
                        override fun onClick() =
                                startActivity(Intent(baseContext, NewsListActivity::class.java))
                    })

            val news = mNewsHandler.all
            var i = 0
            while (i < 3 && i < news.size) {
                val n = news[i]
                builder.addEntry(n.title, Time(n.date).asString(baseContext))
                i++
            }

            val card = builder.build()
            return if (card.size == 0) null else card
        }

    private val marksCard: HomeCard?
        get() {
            val builder = HomeCardBuilder()
                    .setName(getString(R.string.lastest_marks))
                    .setOnClick(object : HomeCard.HomeCardClickListener {
                        override fun onClick() =
                                startActivity(Intent(baseContext, MarksActivity::class.java))
                    })


            val marks = mMarksHandler.all
            Collections.reverse(marks)
            var i = 0
            while (i < 3 && i < marks.size) {
                val m = marks[i]
                builder.addEntry(m.subject, (m.value / 100.0).toString())
                i++
            }

            val card = builder.build()
            return if (card.size == 0) null else card
        }

    private val suggestionsCard: HomeCard
        get() = HomeCardBuilder()
                .setName(getString(R.string.suggestions))
                .addEntry("", suggestion)
                .build()

    private val suggestion: String
        get() =
            when (SecureRandom().nextInt(13) + 1) {
                1 -> getString(R.string.suggestion_safe_pwd)
                2 -> getString(R.string.suggestion_avg)
                3 -> getString(R.string.suggestion_quarter)
                4 -> getString(R.string.suggestion_edit_event)
                5 -> getString(R.string.suggestion_notification_remote)
                6 -> getString(R.string.suggestion_get_touch)
                7 -> getString(R.string.suggestion_address)
                8 -> getString(R.string.suggestion_backups)
                9 -> getString(R.string.suggestion_suggestions)
                10 -> getString(R.string.suggestion_news)
                11 -> getString(R.string.suggestion_feedback)
                12 -> getString(R.string.suggestion_safe)
                else -> getString(R.string.suggestion_notification)
        }

    companion object {
        private val BUNDLE_SHOULD_ANIMATE = "homeShouldAnimate"
    }
}
