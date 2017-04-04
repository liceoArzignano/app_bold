package it.liceoarzignano.bold.marks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.analytics.FirebaseAnalytics;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.ui.recyclerview.DividerDecoration;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerClickListener;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerTouchListener;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt;


public class MarksActivity extends AppCompatActivity {

    private static final String PREF_QUARTER_SELECTOR = "quarterSelector";

    private MarksController mController;
    private RecyclerViewExt mList;
    private LinearLayout mEmptyLayout;

    private SharedPreferences mPrefs;

    private int mFilter;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_marks);

        mController = new MarksController(((BoldApp) getApplication()).getConfig());
        mPrefs = getSharedPreferences(Utils.EXTRA_PREFS, MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mList = (RecyclerViewExt) findViewById(R.id.marks_list);
        mEmptyLayout = (LinearLayout) findViewById(R.id.marks_empty_layout);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            new BoldAnalytics(this).log(FirebaseAnalytics.Event.SELECT_CONTENT,
                    FirebaseAnalytics.Param.ITEM_NAME, "Add mark");
            startActivity(new Intent(this, ManagerActivity.class));
        });

        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.addItemDecoration(new DividerDecoration(this));
        mList.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!Utils.isFirstQuarter(this, Utils.getToday())) {
            getMenuInflater().inflate(R.menu.marks, menu);

            MenuItem allQuarters = menu.findItem(R.id.filter_all);
            MenuItem firstQuarter = menu.findItem(R.id.filter_first);
            MenuItem secondQuarter = menu.findItem(R.id.filter_second);

            mFilter = mPrefs.getInt(PREF_QUARTER_SELECTOR, 0);
            switch (mFilter) {
                case 0:
                    allQuarters.setChecked(true);
                    break;
                case 1:
                    firstQuarter.setChecked(true);
                    break;
                case 2:
                    secondQuarter.setChecked(true);
                    break;
            }
            refresh();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.filter_all:
                mFilter = 0;
                break;
            case R.id.filter_first:
                mFilter = 1;
                break;
            case R.id.filter_second:
                mFilter = 2;
                break;
        }
        item.setChecked(true);
        mPrefs.edit().putInt(PREF_QUARTER_SELECTOR, mFilter).apply();
        refresh();

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        String[] marks = Utils.getAverageElements(this, mFilter);

        if (marks.length == 0) {
            mList.setVisibility(View.GONE);
            mEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            mEmptyLayout.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
            AverageAdapter adapter = new AverageAdapter(mController, marks);
            RecyclerClickListener listener = (mView, mPosition) -> {
                new BoldAnalytics(this).log(FirebaseAnalytics.Event.VIEW_ITEM,
                        FirebaseAnalytics.Param.ITEM_NAME, "Subject");
                Intent mIntent = new Intent(this, SubjectActivity.class);
                mIntent.putExtra(SubjectActivity.EXTRA_TITLE, marks[mPosition]);
                mIntent.putExtra(SubjectActivity.EXTRA_FILTER, mFilter);
                startActivity(mIntent);
            };
            mList.setAdapter(adapter);
            mList.addOnItemTouchListener(new RecyclerTouchListener(this, listener));
            adapter.notifyDataSetChanged();
        }
    }
}
