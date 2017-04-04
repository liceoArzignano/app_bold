package it.liceoarzignano.bold.news;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.ui.recyclerview.DividerDecoration;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt;

public class NewsListActivity extends AppCompatActivity {
    private Activity mActivity;
    private RecyclerViewExt mNewsList;
    private LinearLayout mEmptyLayout;
    private TextView mEmptyText;
    private CustomTabsClient mClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsServiceConnection mCustomTabsServiceConnection;
    private CustomTabsIntent mCustomTabIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mActivity = this;

        mNewsList = (RecyclerViewExt) findViewById(R.id.news_list);
        mEmptyLayout = (LinearLayout) findViewById(R.id.news_empty_layout);
        mEmptyText = (TextView) findViewById(R.id.news_empty_text);

        Intent callingIntent = getIntent();
        long id = callingIntent.getLongExtra("newsId", -1);

        if (id > 0) {
            News mCalledNews = Realm.getInstance(((BoldApp)
                    mActivity.getApplicationContext()).getConfig())
                    .where(News.class).equalTo("id", id).findFirst();
            showUrl(mCalledNews.getUrl());
        }

        String query = null;
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            query = getIntent().getStringExtra(SearchManager.QUERY);
        }

        mNewsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mNewsList.setItemAnimator(new DefaultItemAnimator());
        mNewsList.addItemDecoration(new DividerDecoration(getApplicationContext()));

        refresh(getApplicationContext(), query);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        setupSearchView(this, menu.findItem(R.id.menu_search));
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Chrome custom tabs
        setupCCustomTabs();

        refresh(this, getIntent().getStringExtra(SearchManager.QUERY));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCustomTabsServiceConnection != null) {
            unbindService(mCustomTabsServiceConnection);
            mCustomTabsServiceConnection = null;
        }
    }

    /**
     * Refresh list
     *
     * @param context used to fetch resources
     * @param query search query
     */
    void refresh(Context context, String query) {
        boolean hasQuery = query != null && !query.isEmpty();

        Realm realm = Realm.getInstance(((BoldApp) context.getApplicationContext()).getConfig());
        final RealmResults<News> news = hasQuery ?
                realm.where(News.class).contains("title", query, Case.INSENSITIVE).or()
                        .contains("message", query, Case.INSENSITIVE)
                        .findAllSorted("date", Sort.DESCENDING) :
                realm.where(News.class).findAllSorted("date", Sort.DESCENDING);

        mEmptyLayout.setVisibility(news.isEmpty() ? View.VISIBLE : View.GONE);
        mEmptyText.setText(context.getString(hasQuery ?
                R.string.search_no_result : R.string.news_empty));

        NewsAdapter adapter = new NewsAdapter(news, mActivity);
        mNewsList.setAdapter(adapter);

        adapter.notifyDataSetChanged();
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

        CustomTabsClient.bindCustomTabsService(mActivity, "com.android.chrome",
                mCustomTabsServiceConnection);

        mCustomTabIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(ContextCompat.getColor(mActivity, R.color.colorPrimary))
                .setShowTitle(true)
                .setStartAnimations(mActivity, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(mActivity, android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right)
                .build();
    }

    /**
     * Open the url associated with a news
     *
     * @param url website url
     */
    void showUrl(String url) {
        new BoldAnalytics(this).log(FirebaseAnalytics.Event.VIEW_ITEM,
                FirebaseAnalytics.Param.ITEM_NAME, "News url");
        setupCCustomTabs();
        mCustomTabIntent.launchUrl(mActivity, Uri.parse(url));
    }

    /**
     * Initialize search view
     *
     * @param context used to fetch resources
     * @param item menu item
     */
    private void setupSearchView(final Context context, MenuItem item) {
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        if (searchView == null) {
            return;
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String mQuery) {
                refresh(context, mQuery);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String mNewText) {
                refresh(context, mNewText);
                return true;
            }
        });
    }
}
