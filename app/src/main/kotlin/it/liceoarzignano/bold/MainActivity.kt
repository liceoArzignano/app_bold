package it.liceoarzignano.bold

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.navigation.NavigationView
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
import it.liceoarzignano.bold.utils.SystemUtils
import it.liceoarzignano.bold.utils.Time
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.security.SecureRandom
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mMarksHandler: MarksHandler
    private lateinit var mEventsHandler: EventsHandler
    private lateinit var mNewsHandler: NewsHandler
    private lateinit var mPrefs: AppPrefs

    private lateinit var mToolbar: Toolbar
    private lateinit var mShortcutsList: RecyclerViewExt
    private lateinit var mCardList: RecyclerViewExt
    private lateinit var mUsername: TextView
    private lateinit var mLogo: ImageView

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
        showIntroIfNeeded()

        setContentView(R.layout.activity_main)

        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
        val drawer = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.isDrawerIndicatorEnabled = false
        toggle.setHomeAsUpIndicator(R.drawable.ic_nav_drawer)
        toggle.setToolbarNavigationClickListener { drawer.openDrawer(Gravity.LEFT) }
        toggle.syncState()
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

        showWelcome()
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

        if (mTabsServiceConnection != null) {
            unbindService(mTabsServiceConnection as ServiceConnection)
            mTabsServiceConnection = null
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BUNDLE_SHOULD_ANIMATE, false)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawer = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)

        // Do action with some delay to prevent lags when
        // loading big lists of marks and events
        when (item.itemId) {
            R.id.nav_my_marks -> startActivity(Intent(this, MarksActivity::class.java))
            R.id.nav_calendar -> startActivity(Intent(this, EventListActivity::class.java))
            R.id.nav_news -> startActivity(Intent(this, NewsListActivity::class.java))
            R.id.nav_safe -> startActivity(Intent(this, SafeActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_url))
                startActivity(Intent.createChooser(shareIntent,
                        getString(R.string.share_title)))
            }
            R.id.nav_help -> showUrl(5)
        }

        return true
    }

    private fun setupDBHandler() {
        mMarksHandler = MarksHandler.getInstance(this)
        mEventsHandler = EventsHandler.getInstance(this)
        mNewsHandler = NewsHandler.getInstance(this)
    }

    private fun setupNavHeader() {
        mUsername.text = mPrefs.get(AppPrefs.KEY_USERNAME, "")
        mUsername.visibility = if (mUsername.text.isBlank()) View.GONE else View.VISIBLE

        if (!SystemUtils.isNotLegacy) {
            mLogo.visibility = View.GONE
            return
        }

        if (mPrefs.get(AppPrefs.KEY_IS_TEACHER, false)) {
            mLogo.background = getDrawable(R.drawable.ic_address_6)
        } else {
            when (mPrefs.get(AppPrefs.KEY_ADDRESS, "0")) {
                "1" -> mLogo.background = getDrawable(R.drawable.ic_address_1)
                "2" -> mLogo.background = getDrawable(R.drawable.ic_address_2)
                "3" -> mLogo.background = getDrawable(R.drawable.ic_address_3)
                "4" -> mLogo.background = getDrawable(R.drawable.ic_address_4)
                "5" -> mLogo.background = getDrawable(R.drawable.ic_address_5)
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
            mTabsIntent?.launchUrl(this, Uri.parse(url))
        }
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

    private fun onCardClick(view: View, intent: Intent) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                view, "card_activity")
        ActivityCompat.startActivity(this, intent, options.toBundle())

    }

    private val eventsCard: HomeCard?
        get() {
            val builder = HomeCardBuilder()
                    .setName(getString(R.string.upcoming_events))
                    .setOnClick(object : HomeCard.HomeCardClickListener {
                        override fun onClick(view: View) =
                                onCardClick(view, Intent(baseContext, EventListActivity::class.java))
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
                        override fun onClick(view: View) =
                                onCardClick(view, Intent(baseContext, NewsListActivity::class.java))
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
                        override fun onClick(view: View) =
                                onCardClick(view, Intent(baseContext, MarksActivity::class.java))
                    })


            val marks = mMarksHandler.all.reversed()
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
                9 -> getString(R.string.suggestion_suggestions)
                10 -> getString(R.string.suggestion_news)
                11 -> getString(R.string.suggestion_feedback)
                12 -> getString(R.string.suggestion_safe)
                else -> getString(R.string.suggestion_notification)
        }

    private fun setupShortcuts() {
        val adapter = ShortcutAdapter(this)
        mShortcutsList.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    companion object {
        private const val BUNDLE_SHOULD_ANIMATE = "homeShouldAnimate"
    }
}
