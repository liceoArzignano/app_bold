package it.liceoarzignano.bold.news;

import android.app.Activity;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.ui.DividerDecoration;

public class NewsListActivity extends AppCompatActivity {
    private static RecyclerView sNewsList;
    private static LinearLayout sEmptyLayout;
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

        sNewsList = (RecyclerView) findViewById(R.id.news_list);
        sEmptyLayout = (LinearLayout) findViewById(R.id.news_empty_layout);

        Intent mCallingIntent = getIntent();
        long mId = mCallingIntent.getLongExtra("newsId", -1);

        if (mId > 0) {
            News mCalledNews = Realm.getInstance(BoldApp.getAppRealmConfiguration())
                    .where(News.class).equalTo("id", mId).findFirst();
            showUrl(this, mCalledNews.getUrl());
        }

        refreshList(this);
    }

    private static void refreshList(Context mContext) {
        Realm mRealm = Realm.getInstance(BoldApp.getAppRealmConfiguration());
        final RealmResults<News> mNews =
                mRealm.where(News.class).findAllSorted("date", Sort.DESCENDING);

        sEmptyLayout.setVisibility(mNews.isEmpty() ? View.VISIBLE : View.GONE);

        NewsAdapter mAdapter = new NewsAdapter(mNews, mContext);

        sNewsList.setLayoutManager(new LinearLayoutManager(mContext));
        sNewsList.setItemAnimator(new DefaultItemAnimator());
        sNewsList.addItemDecoration(new DividerDecoration(mContext));
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

    static void showUrl(Activity mActivity, String mUrl) {
        setupCCustomTabs();
        sCustomTabIntent.launchUrl(mActivity, Uri.parse(mUrl));
    }
}
