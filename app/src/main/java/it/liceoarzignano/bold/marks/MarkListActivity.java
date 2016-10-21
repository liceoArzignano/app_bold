package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.ViewerActivity;
import it.liceoarzignano.bold.realm.RealmController;
import it.liceoarzignano.bold.ui.DividerDecoration;
import it.liceoarzignano.bold.ui.RecyclerClickListener;
import it.liceoarzignano.bold.ui.RecyclerTouchListener;


public class MarkListActivity extends AppCompatActivity {

    private static final String PREF_QUARTER_SELECTOR = "quarterSelector";
    private static Context sContext;
    private static MenuItem sAllMarks;
    private static MenuItem sFirstQMarks;
    private static MenuItem sSecondQMarks;
    private static String sSubjectFilter;
    private static int sQuarterFilter;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_list);

        sContext = this;
        Resources mRes = getResources();

        RealmController mController = RealmController.with(this);
        mPrefs = getSharedPreferences("HomePrefs", MODE_PRIVATE);
        sQuarterFilter = mPrefs.getInt(PREF_QUARTER_SELECTOR, 0);

        Intent mIntent = getIntent();
        sSubjectFilter = mIntent.getStringExtra("filteredList");

        Toolbar mToobar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToobar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);

        setUpViewPager(mViewPager);
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(mViewPager);
        }

        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mNewMarkIntent = new Intent(MarkListActivity.this, ManagerActivity.class);
                startActivity(mNewMarkIntent);
            }
        });

        if (sSubjectFilter == null) {
            Utils.animFabIntro(this, mFab, getString(R.string.intro_fab_mark_title),
                    getString(R.string.intro_fab_mark), "markListIntro");
        } else {
            String mTitle = String.format(mRes.getString(R.string.title_filter), sSubjectFilter);
            mToobar.setTitle(mTitle);
            setSupportActionBar(mToobar);

            double mAvg = mController.getAverage(sSubjectFilter, 0);
            double mExcepted = mController.whatShouldIGet(sSubjectFilter, 0);
            mViewPager.setCurrentItem(1);
            AverageListFragment.setHint(this, mAvg, mExcepted);
        }
        refreshList(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        // If showing single mark view, roll back to the "all" view
        if (sSubjectFilter != null) {
            Intent mIntent = new Intent(this, MarkListActivity.class);
            startActivity(mIntent);
            finish();
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mMenu) {
        if (!Utils.isFirstQuarter(Utils.getToday())) {
            getMenuInflater().inflate(R.menu.marks, mMenu);
            sAllMarks = mMenu.findItem(R.id.filter_all);
            sFirstQMarks = mMenu.findItem(R.id.filter_first);
            sSecondQMarks = mMenu.findItem(R.id.filter_second);
            setSelectedItem();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mItem) {
        int mId = mItem.getItemId();

        switch (mId) {
            case R.id.filter_all:
                sQuarterFilter = 0;
                break;
            case R.id.filter_first:
                sQuarterFilter = 1;
                break;
            case R.id.filter_second:
                sQuarterFilter = 2;
                break;
        }
        mItem.setChecked(true);
        mPrefs.edit().putInt(PREF_QUARTER_SELECTOR, sQuarterFilter).apply();
        refreshList(BoldApp.getBoldContext());


        return super.onOptionsItemSelected(mItem);
    }

    /**
     * Set the current quarter as checked
     */
    private void setSelectedItem() {
        sQuarterFilter = mPrefs.getInt(PREF_QUARTER_SELECTOR, 0);
        switch (sQuarterFilter) {
            case 0:
                sAllMarks.setChecked(true);
                break;
            case 1:
                sFirstQMarks.setChecked(true);
                break;
            case 2:
                sSecondQMarks.setChecked(true);
                break;
        }
    }

    /**
     * Fire ViewerActivity and pass the selected mark data
     *
     * @param mId: mark id
     */
    private static void viewMark(long mId) {
        Intent mIntent = new Intent(sContext, ViewerActivity.class);

        mIntent.putExtra("isMark", true);
        mIntent.putExtra("id", mId);

        sContext.startActivity(mIntent);
    }

    /**
     * Update the ListView content
     *
     * @param mContext: needed to reload database data
     */
    public static void refreshList(Context mContext) {
        final RealmResults<Mark> mMarks = getFilteredMarks();

        if (MarksListFragment.sEmptyLayout != null) {
            MarksListFragment.sEmptyLayout.setVisibility(mMarks.isEmpty() ? View.VISIBLE : View.GONE);
        }

        MarksAdapter mAdapter = new MarksAdapter(mMarks);
        RecyclerView.LayoutManager mManager = new LinearLayoutManager(mContext);
        RecyclerClickListener mListener = new RecyclerClickListener() {
            @Override
            public void onClick(View mView, int mPosition) {
                MarkListActivity.viewMark(mMarks.get(mPosition).getId());
            }
        };

        if (MarksListFragment.sMarksListView != null) {
            MarksListFragment.sMarksListView.setLayoutManager(mManager);
            MarksListFragment.sMarksListView.setItemAnimator(new DefaultItemAnimator());
            MarksListFragment.sMarksListView.setAdapter(mAdapter);
            MarksListFragment.sMarksListView.addItemDecoration(new DividerDecoration(mContext));
            MarksListFragment.sMarksListView.addOnItemTouchListener(
                    new RecyclerTouchListener(mContext, mListener));
            mAdapter.notifyDataSetChanged();
            AverageListFragment.refresh(mContext, new Pair<>(sSubjectFilter, sQuarterFilter));
        }
    }

    /**
     * Restart this activity with a sSubjectFilter for the ListView content
     *
     * @param mFilter: title sSubjectFilter
     */
    public static void showFilteredMarks(String mFilter) {
        Intent mIntent = new Intent(sContext, MarkListActivity.class);
        mIntent.putExtra("filteredList", mFilter);

        sContext.startActivity(mIntent);
    }

    /**
     * Return subject / student + quarter filter
     *
     * @return current list filters
     */
    static Pair<String, Integer> getsSubjectFilter() {
        return new Pair<>(sSubjectFilter, sQuarterFilter);
    }

    /**
     * Initialize the viewpager and add the needed fragments
     *
     * @param mViewPager: the viewpager we're going to play with
     */
    private void setUpViewPager(ViewPager mViewPager) {
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new MarksListFragment(), getString(R.string.title_fragment_marks));
        mAdapter.addFragment(new AverageListFragment(), sSubjectFilter == null ?
                getString(R.string.title_fragments_avgs) : getString(R.string.title_fragments_avg));

        mViewPager.setAdapter(mAdapter);
    }

    private static RealmResults<Mark> getFilteredMarks() {
        Realm mRealm = Realm.getInstance(BoldApp.getAppRealmConfiguration());

        if (sSubjectFilter == null) {
            switch (sQuarterFilter) {
                case 1:
                    return mRealm.where(Mark.class).equalTo("isFirstQuarter", true).findAll();
                case 2:
                    return mRealm.where(Mark.class).equalTo("isFirstQuarter", false).findAll();
                default:
                    return mRealm.where(Mark.class).findAll();
            }
        } else {
            switch (sQuarterFilter) {
                case 1:
                    return mRealm.where(Mark.class).equalTo("title", sSubjectFilter)
                            .equalTo("isFirstQuarter", true).findAll();
                case 2:
                    return mRealm.where(Mark.class).equalTo("title", sSubjectFilter)
                            .equalTo("isFirstQuarter", false).findAll();
                default:
                    return mRealm.where(Mark.class).equalTo("title", sSubjectFilter).findAll();
            }
        }
    }

    /**
     * Custom Fragment Page Adapter class
     */
    static class ViewPagerAdapter extends FragmentPagerAdapter {
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
