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
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.ui.DividerDecoration;
import it.liceoarzignano.bold.ui.RecyclerClickListener;
import it.liceoarzignano.bold.ui.RecyclerTouchListener;

public class NewsListActivity extends AppCompatActivity {
    private static Activity sActivity;
    private static RecyclerView sNewsList;
    private static LinearLayout sEmptyLayout;
    private static TextView sEmptyText;
    private static CustomTabsClient sClient;
    private static CustomTabsSession sCustomTabsSession;
    private static CustomTabsIntent sCustomTabIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sActivity = this;

        sNewsList = (RecyclerView) findViewById(R.id.news_list);
        sEmptyLayout = (LinearLayout) findViewById(R.id.news_empty_layout);
        sEmptyText = (TextView) findViewById(R.id.news_empty_text);

        Intent mCallingIntent = getIntent();
        long mId = mCallingIntent.getLongExtra("newsId", -1);

        if (mId > 0) {
            News mCalledNews = Realm.getInstance(BoldApp.getAppRealmConfiguration())
                    .where(News.class).equalTo("id", mId).findFirst();
            showUrl(mCalledNews.getUrl());
        }

        String mQuery = null;
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            mQuery = getIntent().getStringExtra(SearchManager.QUERY);
        }

        sNewsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        sNewsList.setItemAnimator(new DefaultItemAnimator());
        sNewsList.addItemDecoration(new DividerDecoration(getApplicationContext()));

        refreshList(getApplicationContext(), mQuery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mMenu) {
        getMenuInflater().inflate(R.menu.search, mMenu);
        setupSearchView(this, mMenu.findItem(R.id.menu_search));
        return true;
    }

    static void refreshList(Context mContext, String mQuery) {
        boolean hasQuery =  mQuery != null && !mQuery.isEmpty();

        Realm mRealm = Realm.getInstance(BoldApp.getAppRealmConfiguration());
        final RealmResults<News> mNews = hasQuery ?
                mRealm.where(News.class).contains("title", mQuery).or().contains("message", mQuery)
                        .findAllSorted("date", Sort.DESCENDING) :
                mRealm.where(News.class).findAllSorted("date", Sort.DESCENDING);

        sEmptyLayout.setVisibility(mNews.isEmpty() ? View.VISIBLE : View.GONE);
        sEmptyText.setText(mContext.getString(hasQuery ?
                R.string.search_no_result : R.string.news_empty));

        NewsAdapter mAdapter = new NewsAdapter(mNews, sActivity);

        sNewsList.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }

    static void setupCCustomTabs() {
        if (sCustomTabIntent != null) {
            return;
        }

        Context mContext = BoldApp.getBoldContext();
        CustomTabsServiceConnection mCustomTabsServiceConnection =
                new CustomTabsServiceConnection() {
                    @Override
                    public void onCustomTabsServiceConnected(ComponentName componentName,
                                                             CustomTabsClient customTabsClient) {
                        sClient = customTabsClient;
                        sClient.warmup(0L);
                        sCustomTabsSession = sClient.newSession(null);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        sClient = null;
                    }
                };

        CustomTabsClient.bindCustomTabsService(mContext, "com.android.chrome",
                mCustomTabsServiceConnection);

        sCustomTabIntent = new CustomTabsIntent.Builder(sCustomTabsSession)
                .setToolbarColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                .setShowTitle(true)
                .setStartAnimations(mContext, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(mContext, android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right)
                .build();
    }

    static void showUrl(String mUrl) {
        setupCCustomTabs();
        sCustomTabIntent.launchUrl(sActivity, Uri.parse(mUrl));
    }

    private void setupSearchView(final Context mContext, MenuItem mItem) {
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(mItem);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String mQuery) {
                refreshList(mContext, mQuery);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String mNewText) {
                refreshList(mContext, mNewText);
                return true;
            }
        });
    }
}
