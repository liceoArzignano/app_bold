package it.liceoarzignano.bold.news

import android.app.SearchManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.ui.ActionsDialog
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt
import it.liceoarzignano.bold.ui.recyclerview.ScrollListener
import it.liceoarzignano.bold.utils.SystemUtils
import it.liceoarzignano.bold.utils.Time

class NewsListActivity : AppCompatActivity() {
    private lateinit var mCoordinator: androidx.coordinatorlayout.widget.CoordinatorLayout
    private lateinit var mEmptyLayout: LinearLayout
    private lateinit var mEmptyText: TextView

    private lateinit var mNewsHandler: NewsHandler
    private lateinit var mAdapter: NewsAdapter
    private var mClient: CustomTabsClient? = null
    private var mCustomTabsSession: CustomTabsSession? = null
    private var mCustomTabsServiceConnection: CustomTabsServiceConnection? = null
    private var mCustomTabIntent: CustomTabsIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbar.setNavigationOnClickListener { finish() }

        mCoordinator = findViewById(R.id.coordinator_layout)
        val newsList = findViewById<RecyclerViewExt>(R.id.news_list)
        mEmptyLayout = findViewById(R.id.news_empty_layout)
        mEmptyText = findViewById(R.id.news_empty_text)

        val callingIntent = intent
        val url = callingIntent.getStringExtra("newsUrl")

        if (url != null && !url.isEmpty()) {
            showUrl(url)
        }

        mNewsHandler = NewsHandler.getInstance(baseContext)
        mAdapter = NewsAdapter(mNewsHandler.all, this)
        newsList.adapter = mAdapter
        if (SystemUtils.isNotLegacy) {
            newsList.addOnScrollListener(ScrollListener(toolbar, resources, newsList.top))
        }
    }

    override fun onResume() {
        super.onResume()

        // Chrome custom tabs
        setupCCustomTabs()

        var query: String? = null
        val callingIntent = intent
        if (Intent.ACTION_SEARCH == callingIntent.action) {
            query = callingIntent.getStringExtra(SearchManager.QUERY)
        }

        refresh(query)
    }

    override fun onPause() {
        super.onPause()
        if (mCustomTabsServiceConnection != null) {
            unbindService(mCustomTabsServiceConnection as ServiceConnection)
            mCustomTabsServiceConnection = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search, menu)
        setupSearchView(menu.findItem(R.id.menu_search))
        return true
    }

    private fun setupSearchView(item: MenuItem) {
        val searchView = item.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(mQuery: String): Boolean {
                refresh(mQuery)
                return true
            }

            override fun onQueryTextChange(mNewText: String): Boolean {
                refresh(mNewText)
                return true
            }
        })
    }

    private fun refresh(query: String?) {
        val news = mNewsHandler.getByQuery(query?.toLowerCase())
        mAdapter.updateList(news)
        mEmptyLayout.visibility = if (news.isEmpty()) View.VISIBLE else View.GONE
        mEmptyText.text = getString(if (query != null && !query.isEmpty())
            R.string.search_no_result
        else
            R.string.news_empty)

    }

    private fun setupCCustomTabs() {
        if (mCustomTabIntent != null) {
            return
        }

        mCustomTabsServiceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(componentName: ComponentName,
                                                      customTabsClient: CustomTabsClient) {
                mClient = customTabsClient
                mClient!!.warmup(0L)
                mCustomTabsSession = mClient!!.newSession(null)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mClient = null
            }
        }

        CustomTabsClient.bindCustomTabsService(this, "com.android.chrome",
                mCustomTabsServiceConnection)

        mCustomTabIntent = CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setShowTitle(true)
                .setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(this, android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right)
                .build()
    }

    internal fun showUrl(url: String) {
        setupCCustomTabs()
        mCustomTabIntent!!.launchUrl(this, Uri.parse(url))
    }

    internal fun newsActions(news: News): Boolean {
        val dialog = ActionsDialog(this, false, false, news.id)
        dialog.setOnActionsListener(object : ActionsDialog.OnActionsDialogListener {
            override fun onShare() {
                val message = String.format("%1\$s (%2\$s)\n%3\$s", news.title,
                        Time(news.date), news.description)
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, message),
                        getString(R.string.share_title)))
            }

            override fun onDelete() {
                mNewsHandler.delete(news.id)
                Snackbar.make(mCoordinator, getString(R.string.actions_removed), Snackbar.LENGTH_LONG)
                        .show()
                refresh(null)
            }
        })
        dialog.show()

        return true
    }
}
