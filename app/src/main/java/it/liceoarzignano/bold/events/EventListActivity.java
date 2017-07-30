package it.liceoarzignano.bold.events;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Date;
import java.util.List;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.editor.EditorActivity;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.ui.ActionsDialog;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt;
import it.liceoarzignano.bold.utils.DateUtils;

public class EventListActivity extends AppCompatActivity {
    private CoordinatorLayout mCoordinator;
    private LinearLayout mEmptyLayout;
    private TextView mEmptyText;

    private EventsHandler mEventsHandler;
    private EventsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        mCoordinator = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        RecyclerViewExt eventList = (RecyclerViewExt) findViewById(R.id.event_list);
        mEmptyLayout = (LinearLayout) findViewById(R.id.event_empty_layout);
        mEmptyText = (TextView) findViewById(R.id.events_empty_text);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            new BoldAnalytics(this).log(FirebaseAnalytics.Event.SELECT_CONTENT, "Add event");
            Intent intent = new Intent(EventListActivity.this, EditorActivity.class);
            intent.putExtra(EditorActivity.EXTRA_IS_MARK, false);
            startActivity(intent);
        });

        mEventsHandler = EventsHandler.getInstance(this);

        // Load by query for reverse order
        mAdapter = new EventsAdapter(mEventsHandler.getByQuery(null), this);
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
    private void refreshList(String query) {
        boolean hasQuery = query != null && !query.isEmpty();

        List<Event2> events = mEventsHandler.getByQuery(query);
        mAdapter.updateList(events);
        mEmptyLayout.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
        mEmptyText.setText(getString(hasQuery ? R.string.search_no_result : R.string.events_empty));
    }

    @SuppressWarnings("SameReturnValue")
    boolean eventActions(Event2 event) {
        ActionsDialog dialog = new ActionsDialog(this, true, event.getId());
        dialog.setOnActionsListener(new ActionsDialog.OnActionsDialogListener() {
            @Override
            public void onShare() {
                String message = String.format("%1$s (%2$s)\n%3$s", event.getTitle(),
                        DateUtils.dateToString(new Date(event.getDate())), event.getDescription());
                startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, message),
                        getString(R.string.share_title)));
            }

            @Override
            public void onDelete() {
                mEventsHandler.delete(event.getId());
                Snackbar.make(mCoordinator, getString(R.string.actions_removed), Snackbar.LENGTH_LONG)
                        .show();
                refreshList(null);
            }
        });
        dialog.show();

        return true;
    }
}
