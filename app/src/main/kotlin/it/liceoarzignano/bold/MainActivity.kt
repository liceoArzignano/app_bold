package it.liceoarzignano.bold

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
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
import it.liceoarzignano.bold.editor.EditorActivity
import it.liceoarzignano.bold.events.EventListActivity
import it.liceoarzignano.bold.events.EventsHandler
import it.liceoarzignano.bold.home.HomeAdapter
import it.liceoarzignano.bold.home.HomeCard
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
import kotlin.collections.ArrayList

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
        val adapter = HomeAdapter(baseContext, cards, mShouldAnimate)
        mCardList.adapter = adapter

        loadCardASync(HomeCard.CardType.EVENTS, adapter)

        loadCardASync(HomeCard.CardType.NEWS, adapter)

        loadCardASync(HomeCard.CardType.MARKS, adapter)

        loadCardASync(HomeCard.CardType.SUGGESTIONS, adapter)
    }


    private fun loadCardASync(type: HomeCard.CardType, adapter: HomeAdapter) {
        object : AsyncTask<Unit, Unit, HomeCard?>() {
            override fun doInBackground(vararg p0: Unit?): HomeCard? =
                    when (type) {
                        HomeCard.CardType.EVENTS -> dayCard
                        HomeCard.CardType.NEWS -> newsCard
                        HomeCard.CardType.MARKS -> {
                            val date = Time.parse(mPrefs.get(AppPrefs.KEY_INTRO_DAY, "2017-01-01"))
                            if (Time().diff(date, 7)) marksCard else null
                        }
                        HomeCard.CardType.SUGGESTIONS -> {
                            if (mPrefs.get(AppPrefs.KEY_SUGGESTIONS)) suggestionsCard else null
                        }
                    }

            override fun onPostExecute(card: HomeCard?) {
                if (card == null || card.content.isBlank()) {
                    adapter.remove(type)
                } else {
                    adapter.update(card)
                }

                // Stop animations when the lastest is added
                if (type == HomeCard.CardType.SUGGESTIONS) {
                    if (mShouldAnimate) {
                        mShouldAnimate = false
                    }
                }
            }
        }.execute()

    }

    private val dayCard: HomeCard
        get() {
            val categories = intArrayOf(R.plurals.notification_test, R.plurals.notification_school,
                    R.plurals.notification_birthday, R.plurals.notification_homework,
                    R.plurals.notification_reminder, R.plurals.notification_meeting,
                    R.plurals.notification_other)

            val builder = StringBuilder()
            val hour = Time().getHour()

            builder.append(getString(when {
                hour < 13 -> R.string.home_card_week_morning
                hour < 19 -> R.string.home_card_week_afternoon
                else -> R.string.home_card_week_evening
            }))

            val username = mPrefs.get(AppPrefs.KEY_USERNAME, "")
            if (username.isNotBlank()) {
                builder.append(", $username")
            }
            builder.append(".\n")

            val map = mEventsHandler.getStats(Time(6).time)
            builder.append("${getString(
                    if (map.isEmpty()) R.string.home_card_week_free
                    else (R.string.home_card_week_recap))}\n")

            for (item in map) {
                val category = resources.getQuantityString(categories[item.value.second],
                        item.value.first)
                val day = Time(item.key).getWeekDay()
                builder.append("\n${resources.getQuantityString(R.plurals.home_card_week_item,
                        item.value.first, item.value.first, category, day)}")
            }

            val card = HomeCard(HomeCard.CardType.EVENTS)
            card.title = getString(R.string.home_card_week_title)
            card.content = builder.toString()
            card.action = getString(R.string.home_card_week_action)
            card.listener = object : HomeCard.Listener {
                override fun onCardClick() =
                        startActivity(Intent(baseContext, EventListActivity::class.java))

                override fun onActionClick() {
                    val intent = Intent(baseContext, EditorActivity::class.java)
                    intent.putExtra(EditorActivity.EXTRA_IS_MARK, false)
                    startActivity(intent)
                }
            }

            return card
        }

    private val newsCard: HomeCard?
        get() {
            // TODO: show unread news only
            val news = mNewsHandler.all
            if (news.isEmpty()) {
                return null
            }

            val builder = StringBuilder()
            for ((index, item) in news.withIndex()) {
                if (index != 0) {
                    builder.append('\n')
                }
                builder.append("\u2022 ${item.title}")

                if (index == 3) {
                    break
                }
            }

            val card = HomeCard(HomeCard.CardType.NEWS)
            card.title = getString(R.string.nav_news)
            card.content = builder.toString()
            card.listener = object : HomeCard.Listener {
                override fun onActionClick() = Unit
                override fun onCardClick() =
                        startActivity(Intent(baseContext, NewsListActivity::class.java))
            }

            return card
        }

    private val marksCard: HomeCard
        get() {
            val quarter = if (Time().isFirstQuarter(baseContext)) 0 else 1
            val map = mMarksHandler.getAllSubjectsAverages(quarter)

            val globalAverage = map.keys.average()
            var max = Pair(0.toDouble(), "")
            for (item in map) {
                val average = item.key
                val subject = item.value
                if (globalAverage > 6) {
                    if (average > max.first) {
                        max = Pair(average, subject)
                    }
                } else if (average < max.first || max.second.isEmpty()) {
                    max = Pair(average, subject)
                }
            }
            val card = HomeCard(HomeCard.CardType.MARKS)
            card.title = getString(R.string.nav_mymarks)
            card.content = getString(when {
                map.isEmpty() -> R.string.home_card_marks_empty
                globalAverage < 6 -> R.string.home_card_marks_negative
                globalAverage < 8 -> R.string.home_card_marks_neutral
                else -> R.string.home_card_marks_positive
            }, String.format("%.2f", globalAverage), max.second)
            card.action = getString(R.string.home_card_marks_action)
            card.listener = object : HomeCard.Listener {
                override fun onActionClick() =
                        startActivity(Intent(baseContext, EditorActivity::class.java))

                override fun onCardClick() =
                        startActivity(Intent(baseContext, MarksActivity::class.java))
            }

            return card
        }

    private val suggestionsCard: HomeCard
        get() {
            val card = HomeCard(HomeCard.CardType.SUGGESTIONS)
            card.title = getString(R.string.suggestions)
            card.content = suggestion
            return card
        }

    private val suggestion: String
        get() {
            if (!mPrefs.get(AppPrefs.KEY_INTRO_SAFE, false)) {
                return getString(R.string.suggestion_safe_intro)
            }

            if (mPrefs.get(AppPrefs.KEY_USERNAME, "").isBlank()) {
                return getString(R.string.suggestion_username)
            }

            return when (SecureRandom().nextInt(10) + 1) {
                1 -> getString(R.string.suggestion_safe_pwd)
                2 -> getString(R.string.suggestion_avg)
                3 -> getString(R.string.suggestion_quarter)
                4 -> getString(R.string.suggestion_edit_event)
                5 -> getString(R.string.suggestion_notification_remote)
                6 -> getString(R.string.suggestion_get_touch)
                7 -> getString(R.string.suggestion_address)
                8 -> getString(R.string.suggestion_backups)
                9 -> getString(R.string.suggestion_suggestions)
                else -> getString(R.string.suggestion_notification)
            }
        }

    companion object {
        private val BUNDLE_SHOULD_ANIMATE = "homeShouldAnimate"
    }
}
