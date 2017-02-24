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

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
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

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mActivity = this;

        mNewsList = (RecyclerViewExt) findViewById(R.id.news_list);
        mEmptyLayout = (LinearLayout) findViewById(R.id.news_empty_layout);
        mEmptyText = (TextView) findViewById(R.id.news_empty_text);

        Intent mCallingIntent = getIntent();
        long mId = mCallingIntent.getLongExtra("newsId", -1);

        if (mId > 0) {
            News mCalledNews = Realm.getInstance(((BoldApp)
                    mActivity.getApplicationContext()).getConfig())
                    .where(News.class).equalTo("id", mId).findFirst();
            showUrl(mCalledNews.getUrl());
        }

        String mQuery = null;
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            mQuery = getIntent().getStringExtra(SearchManager.QUERY);
        }

        mNewsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mNewsList.setItemAnimator(new DefaultItemAnimator());
        mNewsList.addItemDecoration(new DividerDecoration(getApplicationContext()));

        refresh(getApplicationContext(), mQuery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mMenu) {
        getMenuInflater().inflate(R.menu.search, mMenu);
        setupSearchView(this, mMenu.findItem(R.id.menu_search));
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
     * @param mContext used to fetch resources
     * @param mQuery search query
     */
    void refresh(Context mContext, String mQuery) {
        boolean hasQuery = mQuery != null && !mQuery.isEmpty();

        Realm mRealm = Realm.getInstance(((BoldApp) mContext.getApplicationContext()).getConfig());
        final RealmResults<News> mNews = hasQuery ?
                mRealm.where(News.class).contains("title", mQuery, Case.INSENSITIVE).or()
                        .contains("message", mQuery, Case.INSENSITIVE)
                        .findAllSorted("date", Sort.DESCENDING) :
                mRealm.where(News.class).findAllSorted("date", Sort.DESCENDING);

        mEmptyLayout.setVisibility(mNews.isEmpty() ? View.VISIBLE : View.GONE);
        mEmptyText.setText(mContext.getString(hasQuery ?
                R.string.search_no_result : R.string.news_empty));

        NewsAdapter mAdapter = new NewsAdapter(mNews, mActivity);

        mNewsList.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }

    /**
     * Initialize chrome custom tabs
     */
    private void setupCCustomTabs() {
        if (mCustomTabIntent != null) {
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
     * @param mUrl website url
     */
    void showUrl(String mUrl) {
        setupCCustomTabs();
        mCustomTabIntent.launchUrl(mActivity, Uri.parse(mUrl));
    }

    /**
     * Initialize search view
     *
     * @param mContext used to fetch resources
     * @param mItem menu item
     */
    private void setupSearchView(final Context mContext, MenuItem mItem) {
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(mItem);

        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String mQuery) {
                    refresh(mContext, mQuery);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String mNewText) {
                    refresh(mContext, mNewText);
                    return true;
                }
            });
        }
    }
}
