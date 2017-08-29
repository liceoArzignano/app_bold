package it.liceoarzignano.bold.news;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.ui.ActionsDialog;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt;

public class NewsListActivity extends AppCompatActivity {
    private CoordinatorLayout mCoordinator;
    private LinearLayout mEmptyLayout;
    private TextView mEmptyText;

    private NewsHandler mNewsHandler;
    private NewsAdapter mAdapter;
    private CustomTabsClient mClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsServiceConnection mCustomTabsServiceConnection;
    private CustomTabsIntent mCustomTabIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        mCoordinator = findViewById(R.id.coordinator_layout);
        RecyclerViewExt newsList = findViewById(R.id.news_list);
        mEmptyLayout = findViewById(R.id.news_empty_layout);
        mEmptyText = findViewById(R.id.news_empty_text);

        Intent callingIntent = getIntent();
        long id = callingIntent.getLongExtra("newsId", -1);

        if (id > 0) {
            News2 called = NewsHandler.getInstance(this).get(id);
            if (called != null) {
                showUrl(called.getUrl());
            }
        }

        mNewsHandler = NewsHandler.getInstance(this);

        mAdapter = new NewsAdapter(mNewsHandler.getAll(), this);
        newsList.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Chrome custom tabs
        setupCCustomTabs();

        String query = null;
        Intent callingIntent = getIntent();
        if (Intent.ACTION_SEARCH.equals(callingIntent.getAction())) {
            query = callingIntent.getStringExtra(SearchManager.QUERY);
        }

        refresh(query);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        setupSearchView(menu.findItem(R.id.menu_search));
        return true;
    }

    /**
     * Initialize search view
     *
     * @param item menu item
     */
    private void setupSearchView(MenuItem item) {
        SearchView searchView = (SearchView) item.getActionView();

        if (searchView == null) {
            return;
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String mQuery) {
                refresh(mQuery);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String mNewText) {
                refresh(mNewText);
                return true;
            }
        });
    }

    /**
     * Refresh list
     *
     * @param query search query
     */
    private void refresh(String query) {
        final List<News2> news = mNewsHandler.getByQuery(query);
        mAdapter.updateList(news);
        mEmptyLayout.setVisibility(news.isEmpty() ? View.VISIBLE : View.GONE);
        mEmptyText.setText(getString(query != null && !query.isEmpty() ?
                R.string.search_no_result : R.string.news_empty));

    }

    /**
     * Initialize chrome custom tabs
     */
    private void setupCCustomTabs() {
        if (mCustomTabIntent != null) {
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

        CustomTabsClient.bindCustomTabsService(this, "com.android.chrome",
                mCustomTabsServiceConnection);

        mCustomTabIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setShowTitle(true)
                .setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(this, android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right)
                .build();
    }

    /**
     * Open the url associated with a news
     *
     * @param url website url
     */
    void showUrl(String url) {
        new BoldAnalytics(this).log(FirebaseAnalytics.Event.VIEW_ITEM, "News url");
        setupCCustomTabs();
        mCustomTabIntent.launchUrl(this, Uri.parse(url));
    }

    @SuppressWarnings("SameReturnValue")
    boolean newsActions(News2 news) {
        ActionsDialog dialog = new ActionsDialog(this, false, news.getId());
        dialog.setOnActionsListener(new ActionsDialog.OnActionsDialogListener() {
            @Override
            public void onShare() {
                String message = String.format("%1$s (%2$s)\n%3$s", news.getTitle(),
                        news.getDate(), news.getDescription());
                startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, message),
                        getString(R.string.share_title)));
            }

            @Override
            public void onDelete() {
                mNewsHandler.delete(news.getId());
                Snackbar.make(mCoordinator, getString(R.string.actions_removed), Snackbar.LENGTH_LONG)
                        .show();
                refresh(null);
            }
        });
        dialog.show();

        return true;
    }
}
