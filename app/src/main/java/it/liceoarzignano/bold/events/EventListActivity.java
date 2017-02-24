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
import android.util.Log;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mEventList = (RecyclerViewExt) findViewById(R.id.event_list);
        mEmptyLayout = (LinearLayout) findViewById(R.id.event_empty_layout);
        mEmptyText = (TextView) findViewById(R.id.events_empty_text);

        mDate = new Date();

        Calendar start = Calendar.getInstance();
        start.add(Calendar.YEAR, -1);
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 1);
        HorizontalCalendar hCalendar = new HorizontalCalendar.Builder(this, R.id.events_calendar)
                .startDate(start.getTime())
                .endDate(end.getTime())
                .dayFormat("EEE")
                .centerToday(true)
                .build();

        hCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Date date, int position) {
                mDate = date;
                refreshList(getApplication(), date, null);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(EventListActivity.this, ManagerActivity.class);
            intent.putExtra("isMark", false);
            startActivity(intent);
        });

        mEventList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mEventList.setItemAnimator(new DefaultItemAnimator());
        mEventList.addItemDecoration(new DividerDecoration(getApplicationContext()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent callingIntent = getIntent();
        if (Intent.ACTION_SEARCH.equals(callingIntent.getAction())) {
            mQuery = callingIntent.getStringExtra(SearchManager.QUERY);
        }

        refreshList(this, mDate, mQuery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        setupSearchView(menu.findItem(R.id.menu_search));
        return true;
    }

    /**
     * Setup search menu item behaviour
     *
     * @param item search menu item
     */
    private void setupSearchView(MenuItem item) {
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        if (searchView == null) {
            return;
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                refreshList(getApplicationContext(), null, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                refreshList(getApplicationContext(), null, text);
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

        Calendar today = Calendar.getInstance();
        Calendar previous = Calendar.getInstance();
        if (date != null) {
            // Set hours, mins and second for a better query
            previous.setTime(date);
            previous.add(Calendar.DAY_OF_YEAR, -1);
            previous.set(Calendar.HOUR, 23);
            previous.set(Calendar.MINUTE, 59);
            previous.set(Calendar.SECOND, 59);
            previous.set(Calendar.MILLISECOND, 999);
            today.setTime(date);
            today.set(Calendar.HOUR, 23);
            today.set(Calendar.MINUTE, 59);
            today.set(Calendar.SECOND, 59);
            today.set(Calendar.MILLISECOND, 999);
        }

        Realm realm = Realm.getInstance(((BoldApp) getApplicationContext()).getConfig());

        RealmResults<Event> events = date == null ?
                hasQuery ?
                        realm.where(Event.class)
                                .contains("title", query, Case.INSENSITIVE)
                                .findAllSorted("date", Sort.DESCENDING) :
                        realm.where(Event.class)
                                .findAllSorted("date", Sort.DESCENDING) :
                hasQuery ?
                        realm.where(Event.class)
                                .between("date", previous.getTime(), today.getTime())
                                .contains("title", query,Case.INSENSITIVE)
                                .findAllSorted("date", Sort.DESCENDING) :
                        realm.where(Event.class)
                                .between("date", previous.getTime(), today.getTime())
                                .findAllSorted("date", Sort.DESCENDING);

        mEmptyLayout.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
        mEmptyText.setText(context.getString(hasQuery ?
                R.string.search_no_result : R.string.events_empty));

        EventsAdapter adapter = new EventsAdapter(events);
        RecyclerClickListener listener = (view, position) ->
                viewEvent(events.get(position).getId());
        mEventList.setAdapter(adapter);
        mEventList.addOnItemTouchListener(new RecyclerTouchListener(context, listener));

        adapter.notifyDataSetChanged();
    }

    /**
     * Fire ViewerDialog and pass the selected event data
     *
     * @param id: event id
     */
    private void viewEvent(long id) {
        final BottomSheetDialog sheet = new BottomSheetDialog(this);
        View bottomView = new ViewerDialog(this, sheet).setData(id, false);
        sheet.setContentView(bottomView);
        sheet.show();
    }
}
