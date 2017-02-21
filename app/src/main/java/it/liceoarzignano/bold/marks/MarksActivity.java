package it.liceoarzignano.bold.marks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.ui.DividerDecoration;
import it.liceoarzignano.bold.ui.RecyclerClickListener;
import it.liceoarzignano.bold.ui.RecyclerTouchListener;


public class MarksActivity extends AppCompatActivity {

    private static final String PREF_QUARTER_SELECTOR = "quarterSelector";

    private MarksController mController;
    private RecyclerView mList;
    private LinearLayout mEmptyLayout;

    private SharedPreferences mPrefs;

    private int mFilter;

    @Override
    protected void onCreate(Bundle mSavedInstance) {
        super.onCreate(mSavedInstance);

        setContentView(R.layout.activity_marks);

        mController = new MarksController(((BoldApp) getApplication()).getConfig());
        mPrefs = getSharedPreferences(Utils.EXTRA_PREFS, MODE_PRIVATE);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mList = (RecyclerView) findViewById(R.id.marks_list);
        mEmptyLayout = (LinearLayout) findViewById(R.id.marks_empty_layout);
        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.fab);

        mFab.setOnClickListener(v -> startActivity(new Intent(this, ManagerActivity.class)));

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
    public boolean onCreateOptionsMenu(Menu mMenu) {
        if (!Utils.isFirstQuarter(this, Utils.getToday())) {
            getMenuInflater().inflate(R.menu.marks, mMenu);

            MenuItem mAllMarks = mMenu.findItem(R.id.filter_all);
            MenuItem mFirstQMarks = mMenu.findItem(R.id.filter_first);
            MenuItem mSecondQMarks = mMenu.findItem(R.id.filter_second);

            mFilter = mPrefs.getInt(PREF_QUARTER_SELECTOR, 0);
            switch (mFilter) {
                case 0:
                    mAllMarks.setChecked(true);
                    break;
                case 1:
                    mFirstQMarks.setChecked(true);
                    break;
                case 2:
                    mSecondQMarks.setChecked(true);
                    break;
            }
            refresh();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mItem) {
        int mId = mItem.getItemId();

        switch (mId) {
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
        mItem.setChecked(true);
        mPrefs.edit().putInt(PREF_QUARTER_SELECTOR, mFilter).apply();
        refresh();

        return super.onOptionsItemSelected(mItem);
    }

    private void refresh() {
        String[] mMarks = Utils.getAverageElements(this, mFilter);

        if (mMarks.length == 0) {
            mList.setVisibility(View.GONE);
            mEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            mEmptyLayout.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
            AverageAdapter mAdapter = new AverageAdapter(mController, mMarks);
            RecyclerClickListener mListener = (mView, mPosition) -> {
                Intent mIntent = new Intent(this, SubjectActivity.class);
                mIntent.putExtra(SubjectActivity.EXTRA_TITLE, mMarks[mPosition]);
                mIntent.putExtra(SubjectActivity.EXTRA_FILTER, mFilter);
                startActivity(mIntent);
            };
            mList.setAdapter(mAdapter);
            mList.addOnItemTouchListener(new RecyclerTouchListener(this, mListener));
            mAdapter.notifyDataSetChanged();
        }
    }
}
