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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarListener;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.ui.recyclerview.DividerDecoration;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerClickListener;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerTouchListener;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt;
import it.liceoarzignano.bold.ui.ViewerDialog;

public class EventListActivity extends AppCompatActivity {

    private RecyclerViewExt mEventList;
    private LinearLayout mEmptyLayout;
    private TextView mEmptyText;

    private Date mDate;
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

        mEventList = (RecyclerViewExt) findViewById(R.id.event_list);
        mEmptyLayout = (LinearLayout) findViewById(R.id.event_empty_layout);
        mEmptyText = (TextView) findViewById(R.id.events_empty_text);

        mDate = new Date();

        Calendar mStart = Calendar.getInstance();
        mStart.add(Calendar.YEAR, -1);
        Calendar mEnd = Calendar.getInstance();
        mEnd.add(Calendar.YEAR, 1);
        HorizontalCalendar mHCalendar = new HorizontalCalendar.Builder(this, R.id.events_calendar)
                .startDate(mStart.getTime())
                .endDate(mEnd.getTime())
                .dayFormat("EEE")
                .centerToday(true)
                .build();

        mHCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Date date, int position) {
                mDate = date;
                refreshList(getApplication(), date, null);
            }
        });

        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(view -> {
            Intent mIntent = new Intent(EventListActivity.this, ManagerActivity.class);
            mIntent.putExtra("isMark", false);
            startActivity(mIntent);
        });

        mEventList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mEventList.setItemAnimator(new DefaultItemAnimator());
        mEventList.addItemDecoration(new DividerDecoration(getApplicationContext()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent mCallingIntent = getIntent();
        if (Intent.ACTION_SEARCH.equals(mCallingIntent.getAction())) {
            mQuery = mCallingIntent.getStringExtra(SearchManager.QUERY);
        }

        refreshList(this, mDate, mQuery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mMenu) {
        getMenuInflater().inflate(R.menu.search, mMenu);
        setupSearchView(mMenu.findItem(R.id.menu_search));
        return true;
    }

    /**
     * Setup search menu item behaviour
     *
     * @param item search menu item
     */
    private void setupSearchView(MenuItem item) {
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(item);

        if (mSearchView == null) {
            return;
        }

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String mQuery) {
                refreshList(getApplicationContext(), null, mQuery);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String mNewText) {
                refreshList(getApplicationContext(), null, mNewText);
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                refreshList(getApplicationContext(), null, null);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                refreshList(getApplicationContext(), mDate, null);
                return true;
            }
        });
    }

    /**
     * Update the RecyclerView content
     *
     * @param context: needed to reload database data
     * @param query:   search query
     */
    public void refreshList(Context context, Date date, String query) {
        boolean hasQuery = query != null && !query.isEmpty();

        Calendar mPrevious = Calendar.getInstance();
        if (date != null) {
            mPrevious.setTime(date);
            mPrevious.add(Calendar.DAY_OF_YEAR, -1);
        }

        Realm mRealm = Realm.getInstance(((BoldApp) getApplicationContext()).getConfig());

        RealmResults<Event> mEvents = date == null ?
                hasQuery ?
                        mRealm.where(Event.class)
                                .contains("title", query, Case.INSENSITIVE)
                                .findAllSorted("date", Sort.DESCENDING) :
                        mRealm.where(Event.class)
                                .findAllSorted("date", Sort.DESCENDING) :
                hasQuery ?
                        mRealm.where(Event.class)
                                .between("date", mPrevious.getTime(), date)
                                .contains("title", query,Case.INSENSITIVE)
                                .findAllSorted("date", Sort.DESCENDING) :
                        mRealm.where(Event.class)
                                .between("date", mPrevious.getTime(), date)
                                .findAllSorted("date", Sort.DESCENDING);

        mEmptyLayout.setVisibility(mEvents.isEmpty() ? View.VISIBLE : View.GONE);
        mEmptyText.setText(context.getString(hasQuery ?
                R.string.search_no_result : R.string.events_empty));

        EventsAdapter mAdapter = new EventsAdapter(mEvents);
        RecyclerClickListener mListener = (view, position) ->
                viewEvent(mEvents.get(position).getId());
        mEventList.setAdapter(mAdapter);
        mEventList.addOnItemTouchListener(new RecyclerTouchListener(context, mListener));

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
