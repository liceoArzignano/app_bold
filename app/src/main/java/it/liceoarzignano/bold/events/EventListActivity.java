package it.liceoarzignano.bold.events;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.ViewerActivity;

public class EventListActivity extends AppCompatActivity {

    private static ListView mEventListView;
    private static Context fContext;

    /**
     * Update the ListView content
     *
     * @param context: needed to reload database data
     */
    public static void refreshList(Context context) {
        LoadListViewTask loadListViewTask = new LoadListViewTask(
                context, mEventListView);
        loadListViewTask.execute();
    }

    /**
     * Fire ViewerActivity and pass the selected event data
     *
     * @param id: event id
     */
    public static void viewEvent(int id) {
        Intent editIntent = new Intent(fContext, ViewerActivity.class);

        editIntent.putExtra("isMark", false);
        editIntent.putExtra("id", id);

        fContext.startActivity(editIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        fContext = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEventListView = (ListView) findViewById(R.id.event_list_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EventListActivity.this, ManagerActivity.class);
                intent.putExtra("isMark", false);
                startActivity(intent);
                finish();
            }
        });

        refreshList(fContext);
        Utils.animFabIntro(this, fab, getString(R.string.intro_fab_event), "eventKey");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList(fContext);
    }
}
