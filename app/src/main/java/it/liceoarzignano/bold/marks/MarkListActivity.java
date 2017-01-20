package it.liceoarzignano.bold.marks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.ui.ViewerDialog;


public class MarkListActivity extends AppCompatActivity {

    private static final String PREF_QUARTER_SELECTOR = "quarterSelector";

    private Toolbar mToolbar;
    private ViewPager mViewPager;

    private MenuItem mAllMarks;
    private MenuItem mFirstQMarks;
    private MenuItem mSecondQMarks;
    private FloatingActionButton mFab;

    private MarksListFragment mMarksFragment;
    private AverageListFragment mAverageFragment;

    private String mSubjectFilter;
    private int mQuarterFilter;

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_list);

        mPrefs = getSharedPreferences("HomePrefs", MODE_PRIVATE);
        mQuarterFilter = mPrefs.getInt(PREF_QUARTER_SELECTOR, 0);

        Intent mIntent = getIntent();
        mSubjectFilter = mIntent.getStringExtra("filteredList");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);

        setUpViewPager();
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(mViewPager);
        }

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(view -> {
            startActivity(new Intent(MarkListActivity.this, ManagerActivity.class));
            // Hax: avoid averages sync issues by restarting the activity
            // once we're done with adding a new mark
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Hax: refresh in a new handler to make sure data is fetched before the adapters are built
        new Handler().post(this::refresh);
    }

    @Override
    public void onBackPressed() {
        // If showing single mark view, roll back to the "all" view
        if (mSubjectFilter != null) {
            mSubjectFilter = null;
            refresh();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mMenu) {
        if (!Utils.isFirstQuarter(this, Utils.getToday())) {
            getMenuInflater().inflate(R.menu.marks, mMenu);
            mAllMarks = mMenu.findItem(R.id.filter_all);
            mFirstQMarks = mMenu.findItem(R.id.filter_first);
            mSecondQMarks = mMenu.findItem(R.id.filter_second);
            setSelectedItem();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mItem) {
        int mId = mItem.getItemId();

        switch (mId) {
            case R.id.filter_all:
                mQuarterFilter = 0;
                break;
            case R.id.filter_first:
                mQuarterFilter = 1;
                break;
            case R.id.filter_second:
                mQuarterFilter = 2;
                break;
        }
        mItem.setChecked(true);
        mPrefs.edit().putInt(PREF_QUARTER_SELECTOR, mQuarterFilter).apply();
        refresh();


        return super.onOptionsItemSelected(mItem);
    }

    /**
     * Set the current quarter as checked
     */
    private void setSelectedItem() {
        mQuarterFilter = mPrefs.getInt(PREF_QUARTER_SELECTOR, 0);
        switch (mQuarterFilter) {
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
    }

    /**
     * Fire ViewerDialog and pass the selected mark data
     *
     * @param mId: mark id
     */
    void viewMark(long mId) {
        final BottomSheetDialog mSheet = new BottomSheetDialog(this);
        View mBottomView = new ViewerDialog(this, mSheet).setData(mId, true);
        mSheet.setContentView(mBottomView);
        mSheet.show();
    }

    /**
     * Update the ListView content
     */
    public void refresh() {
        MarksController mController = new MarksController(((BoldApp) getApplication()).getConfig());

        final RealmResults<Mark> mMarks = mController.getFilteredMarks(mSubjectFilter,
                mQuarterFilter);

        if (mMarksFragment.mEmptyLayout != null) {
            mMarksFragment.mEmptyLayout.setVisibility(mMarks.isEmpty() ?
                    View.VISIBLE : View.GONE);
        }

        if (mSubjectFilter != null) {
            mToolbar.setTitle(String.format(getString(R.string.title_filter),
                    mSubjectFilter));
            mFab.hide();
            mViewPager.setCurrentItem(1);
        } else {
            mToolbar.setTitle(getString(R.string.title_activity_mark_list));
            Utils.animFab(mFab, true);
        }
        setSupportActionBar(mToolbar);

        if (mMarksFragment.mMarksListView == null) {
            return;
        }

        mMarksFragment.refresh(this, mMarks);
        mAverageFragment.refresh(new Pair<>(mSubjectFilter, mQuarterFilter));
    }

    /**
     * Restart this activity with a mSubjectFilter for the ListView content
     *
     * @param mFilter: title mSubjectFilter
     */
    public void showFilteredMarks(String mFilter) {
        mSubjectFilter = mFilter;
        refresh();
    }

    /**
     * Return subject / student + quarter filter
     *
     * @return current list filters
     */
    Pair<String, Integer> getsSubjectFilter() {
        return new Pair<>(mSubjectFilter, mQuarterFilter);
    }

    /**
     * Initialize the viewpager and add the needed fragments
     */
    private void setUpViewPager() {
        mMarksFragment = new MarksListFragment();
        mAverageFragment = new AverageListFragment();
        mAverageFragment.init(this);
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(mMarksFragment, getString(R.string.title_fragment_marks));
        mAdapter.addFragment(mAverageFragment, getString(R.string.title_fragments_avgs));

        mViewPager.setAdapter(mAdapter);
    }

    /**
     * Custom Fragment Page Adapter class
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager mManager) {
            super(mManager);
        }

        @Override
        public Fragment getItem(int mPosition) {
            return mFragmentList.get(mPosition);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int mPosition) {
            return mFragmentTitleList.get(mPosition);
        }

        void addFragment(Fragment mFragment, String mTitle) {
            mFragmentList.add(mFragment);
            mFragmentTitleList.add(mTitle);
        }

    }
}
