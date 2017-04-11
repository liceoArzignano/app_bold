package it.liceoarzignano.bold.events;

import android.app.SearchManager;
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

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.ui.ViewerDialog;
import it.liceoarzignano.bold.ui.recyclerview.DividerDecoration;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt;

public class EventListActivity extends AppCompatActivity {

    private LinearLayout mEmptyLayout;
    private TextView mEmptyText;

    private EventsController mController;
    private EventsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerViewExt eventList = (RecyclerViewExt) findViewById(R.id.event_list);
        mEmptyLayout = (LinearLayout) findViewById(R.id.event_empty_layout);
        mEmptyText = (TextView) findViewById(R.id.events_empty_text);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            new BoldAnalytics(this).log(FirebaseAnalytics.Event.SELECT_CONTENT, "Add event");
            Intent intent = new Intent(EventListActivity.this, ManagerActivity.class);
            intent.putExtra("isMark", false);
            startActivity(intent);
        });

        mController = new EventsController(((BoldApp) getApplication()).getConfig());

        eventList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        eventList.setItemAnimator(new DefaultItemAnimator());
        eventList.addItemDecoration(new DividerDecoration(getApplicationContext()));
        // Load by query for reverse order
        mAdapter = new EventsAdapter(mController.getByQuery(null), this);
        eventList.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String query = null;
        Intent callingIntent = getIntent();
        if (Intent.ACTION_SEARCH.equals(callingIntent.getAction())) {
            query = callingIntent.getStringExtra(SearchManager.QUERY);
        }

        refreshList(query);
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
                refreshList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                refreshList(text);
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                refreshList(null);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                refreshList(null);
                return true;
            }
        });
    }

    /**
     * Update the RecyclerView content
     *
     * @param query:   search query
     */
    public void refreshList(String query) {
        boolean hasQuery = query != null && !query.isEmpty();

        List<Event> events = mController.getByQuery(query);
        mAdapter.updateList(events);
        mEmptyLayout.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
        mEmptyText.setText(getString(hasQuery ? R.string.search_no_result : R.string.events_empty));
    }

    /**
     * Fire ViewerDialog and pass the selected event data
     *
     * @param id: event id
     */
    void viewEvent(long id) {
        new BoldAnalytics(this).log(FirebaseAnalytics.Event.VIEW_ITEM, "Event");
        final BottomSheetDialog sheet = new BottomSheetDialog(this);
        View bottomView = new ViewerDialog(this, sheet).setData(id, false);
        sheet.setContentView(bottomView);
        sheet.show();
    }
}
