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

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.ui.DividerDecoration;

public class NewsListActivity extends AppCompatActivity {
    private static RecyclerView mNewsList;
    private static CustomTabsClient mClient;
    private static CustomTabsSession mCustomTabsSession;
    private static CustomTabsIntent mCustomTabIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mNewsList = (RecyclerView) findViewById(R.id.news_list_view);

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

        NewsAdapter mAdapter = new NewsAdapter(mNews, mContext);

        mNewsList.setLayoutManager(new LinearLayoutManager(mContext));
        mNewsList.setItemAnimator(new DefaultItemAnimator());
        mNewsList.addItemDecoration(new DividerDecoration(mContext));
        mNewsList.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
    }

    static void setupCCustomTabs() {
        if (mCustomTabIntent != null) {
            return;
        }

        Context mContext = BoldApp.getBoldContext();
        CustomTabsServiceConnection mCustomTabsServiceConnection =
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

        CustomTabsClient.bindCustomTabsService(mContext, "com.android.chrome",
                mCustomTabsServiceConnection);

        mCustomTabIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
                .setShowTitle(true)
                .setStartAnimations(mContext, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(mContext, android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right)
                .build();
    }

    static void showUrl(Activity mActivity, String mUrl) {
        setupCCustomTabs();
        mCustomTabIntent.launchUrl(mActivity, Uri.parse(mUrl));
    }
}
