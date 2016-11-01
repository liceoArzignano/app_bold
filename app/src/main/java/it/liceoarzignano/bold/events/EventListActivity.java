package it.liceoarzignano.bold.events;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.ViewerActivity;
import it.liceoarzignano.bold.ui.DividerDecoration;
import it.liceoarzignano.bold.ui.RecyclerClickListener;
import it.liceoarzignano.bold.ui.RecyclerTouchListener;

public class EventListActivity extends AppCompatActivity {

    private static RecyclerView sEventList;
    private static LinearLayout sEmptyLayout;
    private static TextView sEmptyText;
    private static Context sContext;

    private static String mQuery;

    /**
     * Update the RecyclerView content
     *
     * @param mContext: needed to reload database data
     * @param mQuery: search query
     */
    public static void refreshList(Context mContext, String mQuery) {
        boolean hasQuery = mQuery != null && !mQuery.isEmpty();

        Realm mRealm = Realm.getInstance(BoldApp.getAppRealmConfiguration());
        final RealmResults<Event> mEvents = hasQuery ?
                mRealm.where(Event.class).contains("title", mQuery)
                        .findAllSorted("date", Sort.DESCENDING) :
                mRealm.where(Event.class).findAllSorted("date", Sort.DESCENDING);

        sEmptyLayout.setVisibility(mEvents.isEmpty() ? View.VISIBLE : View.GONE);
        sEmptyText.setText(mContext.getString(hasQuery ?
                R.string.search_no_result : R.string.events_empty));

        EventsAdapter mAdapter = new EventsAdapter(mEvents);
        RecyclerClickListener mListener = new RecyclerClickListener() {
            @Override
            public void onClick(View mView, int mPosition) {
                EventListActivity.viewEvent(mEvents.get(mPosition).getId());
            }
        };

        sEventList.setAdapter(mAdapter);
        sEventList.addOnItemTouchListener(new RecyclerTouchListener(mContext, mListener));

        mAdapter.notifyDataSetChanged();
    }

    /**
     * Fire ViewerActivity and pass the selected event data
     *
     * @param id: event id
     */
    private static void viewEvent(long id) {
        Intent mIntent = new Intent(sContext, ViewerActivity.class);

        mIntent.putExtra("isMark", false);
        mIntent.putExtra("id", id);

        sContext.startActivity(mIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        sContext = this;

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sEventList = (RecyclerView) findViewById(R.id.event_list);
        sEmptyLayout = (LinearLayout) findViewById(R.id.event_empty_layout);
        sEmptyText = (TextView) findViewById(R.id.events_empty_text);

        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(EventListActivity.this, ManagerActivity.class);
                mIntent.putExtra("isMark", false);
                startActivity(mIntent);
            }
        });

        sEventList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        sEventList.setItemAnimator(new DefaultItemAnimator());
        sEventList.addItemDecoration(new DividerDecoration(getApplicationContext()));


        Utils.animFab(mFab, true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent mCallingIntent = getIntent();
        if (Intent.ACTION_SEARCH.equals(mCallingIntent.getAction())) {
            mQuery = mCallingIntent.getStringExtra(SearchManager.QUERY);
        }

        refreshList(sContext, mQuery);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mMenu) {
        getMenuInflater().inflate(R.menu.search, mMenu);
        setupSearchView(mMenu.findItem(R.id.menu_search));
        return true;
    }

    private void setupSearchView(MenuItem mItem) {
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(mItem);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String mQuery) {
                refreshList(getApplicationContext(), mQuery);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String mNewText) {
                refreshList(getApplicationContext(), mNewText);
                return true;
            }
        });

    }

}
