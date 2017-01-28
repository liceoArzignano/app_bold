package it.liceoarzignano.bold.events;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
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
import it.liceoarzignano.bold.ui.DividerDecoration;
import it.liceoarzignano.bold.ui.RecyclerClickListener;
import it.liceoarzignano.bold.ui.RecyclerTouchListener;
import it.liceoarzignano.bold.ui.ViewerDialog;

public class EventListActivity extends AppCompatActivity {

    RecyclerView mEventList;
    LinearLayout mEmptyLayout;
    TextView mEmptyText;

    private String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mEventList = (RecyclerView) findViewById(R.id.event_list);
        mEmptyLayout = (LinearLayout) findViewById(R.id.event_empty_layout);
        mEmptyText = (TextView) findViewById(R.id.events_empty_text);

        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(view -> {
            Intent mIntent = new Intent(EventListActivity.this, ManagerActivity.class);
            mIntent.putExtra("isMark", false);
            startActivity(mIntent);
            finish();
        });

        mEventList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mEventList.setItemAnimator(new DefaultItemAnimator());
        mEventList.addItemDecoration(new DividerDecoration(getApplicationContext()));


        Utils.animFab(mFab, true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent mCallingIntent = getIntent();
        if (Intent.ACTION_SEARCH.equals(mCallingIntent.getAction())) {
            mQuery = mCallingIntent.getStringExtra(SearchManager.QUERY);
        }

        refreshList(this, mQuery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mMenu) {
        getMenuInflater().inflate(R.menu.search, mMenu);
        setupSearchView(mMenu.findItem(R.id.menu_search));
        return true;
    }

    private void setupSearchView(MenuItem mItem) {
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(mItem);

        if (mSearchView != null) {
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

    /**
     * Update the RecyclerView content
     *
     * @param mContext: needed to reload database data
     * @param mQuery:   search query
     */
    public void refreshList(Context mContext, String mQuery) {
        boolean hasQuery = mQuery != null && !mQuery.isEmpty();

        Realm mRealm = Realm.getInstance(((BoldApp) mContext.getApplicationContext()).getConfig());
        final RealmResults<Event> mEvents = hasQuery ?
                mRealm.where(Event.class).contains("title", mQuery)
                        .findAllSorted("date", Sort.DESCENDING) :
                mRealm.where(Event.class).findAllSorted("date", Sort.DESCENDING);

        mEmptyLayout.setVisibility(mEvents.isEmpty() ? View.VISIBLE : View.GONE);
        mEmptyText.setText(mContext.getString(hasQuery ?
                R.string.search_no_result : R.string.events_empty));

        EventsAdapter mAdapter = new EventsAdapter(mEvents);
        RecyclerClickListener mListener = (mView, mPosition) ->
                viewEvent(mEvents.get(mPosition).getId());

        mEventList.setAdapter(mAdapter);
        mEventList.addOnItemTouchListener(new RecyclerTouchListener(mContext, mListener));

        mAdapter.notifyDataSetChanged();
    }

    /**
     * Fire ViewerDialog and pass the selected event data
     *
     * @param mId: event id
     */
    private void viewEvent(long mId) {
        final BottomSheetDialog mSheet = new BottomSheetDialog(this);
        View mBottomView = new ViewerDialog(this, mSheet).setData(mId, false);
        mSheet.setContentView(mBottomView);
        mSheet.show();
    }
}
