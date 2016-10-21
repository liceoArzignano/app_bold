package it.liceoarzignano.bold.events;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

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

    private static RecyclerView mEventList;
    private static Context sContext;

    /**
     * Update the RecyclerView content
     *
     * @param mContext: needed to reload database data
     */
    public static void refreshList(Context mContext) {
        Realm mRealm = Realm.getInstance(BoldApp.getAppRealmConfiguration());
        final RealmResults<Event> mEvents =
                mRealm.where(Event.class).findAllSorted("date", Sort.DESCENDING);

        EventsAdapter mAdapter = new EventsAdapter(mEvents);
        RecyclerClickListener mListener = new RecyclerClickListener() {
            @Override
            public void onClick(View mView, int mPosition) {
                EventListActivity.viewEvent(mEvents.get(mPosition).getId());
            }
        };

        mEventList.setLayoutManager(new LinearLayoutManager(mContext));
        mEventList.setItemAnimator(new DefaultItemAnimator());
        mEventList.addItemDecoration(new DividerDecoration(mContext));
        mEventList.setAdapter(mAdapter);
        mEventList.addOnItemTouchListener(new RecyclerTouchListener(mContext, mListener));

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

        mEventList = (RecyclerView) findViewById(R.id.event_list);

        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(EventListActivity.this, ManagerActivity.class);
                mIntent.putExtra("isMark", false);
                startActivity(mIntent);
            }
        });

        refreshList(sContext);
        Utils.animFabIntro(this, mFab, getString(R.string.intro_fab_event_title),
                getString(R.string.intro_fab_event), "eventKey");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList(sContext);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
