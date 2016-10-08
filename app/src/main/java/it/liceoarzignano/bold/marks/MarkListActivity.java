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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.ViewerActivity;
import it.liceoarzignano.bold.realm.RealmController;


public class MarkListActivity extends AppCompatActivity {

    private static final String PREF_QUARTER_SELECTOR = "quarterSelector";
    private static Context fContext;
    private static MenuItem allMarks;
    private static MenuItem firstQMarks;
    private static MenuItem secondQMarks;
    private static String subjectFilter;
    private static int quarterFilter;
    private SharedPreferences prefs;

    /**
     * Fire ViewerActivity and pass the selected mark data
     *
     * @param id: mark id
     */
    public static void viewMark(long id) {

        Intent viewIntent = new Intent(fContext, ViewerActivity.class);

        viewIntent.putExtra("isMark", true);
        viewIntent.putExtra("id", id);

        fContext.startActivity(viewIntent);
    }

    /**
     * Update the ListView content
     *
     * @param context: needed to reload database data
     */
    public static void refreshList(Context context) {
        LoadListViewTask loadMarkPostListViewTask = new LoadListViewTask(
                context, MarksListFragment.mMarksListView,
                new Pair<>(subjectFilter, quarterFilter));
        loadMarkPostListViewTask.execute();

        AverageListFragment.refresh(context, new Pair<>(subjectFilter, quarterFilter));
    }

    /**
     * Restart this activity with a subjectFilter for the ListView content
     *
     * @param filter: title subjectFilter
     */
    public static void showFilteredMarks(String filter) {
        Intent filteredList = new Intent(fContext, MarkListActivity.class);
        filteredList.putExtra("filteredList", filter);

        fContext.startActivity(filteredList);
    }

    /**
     * Return subject / student + quarter filter
     *
     * @return current list filters
     */
    static Pair<String, Integer> getSubjectFilter() {
        return new Pair<>(subjectFilter, quarterFilter);
    }

    /**
     * Initialize the viewpager and add the needed fragments
     *
     * @param viewPager: the viewpager we're going to play with
     */
    private void setUpViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MarksListFragment(), getString(R.string.title_fragment_marks));
        adapter.addFragment(new AverageListFragment(), subjectFilter == null ?
                getString(R.string.title_fragments_avgs) : getString(R.string.title_fragments_avg));

        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_list);

        fContext = this;
        Resources res = getResources();

        RealmController controller = RealmController.with(this);
        prefs = getSharedPreferences("HomePrefs", MODE_PRIVATE);
        quarterFilter = prefs.getInt(PREF_QUARTER_SELECTOR, 0);

        Intent thisIntent = getIntent();
        subjectFilter = thisIntent.getStringExtra("filteredList");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        setUpViewPager(viewPager);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_new_mark);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MarkListActivity.this, ManagerActivity.class);
                startActivity(i);
            }
        });

        if (subjectFilter == null) {
            Utils.animFabIntro(this, fab, getString(R.string.intro_fab_mark_title),
                    getString(R.string.intro_fab_mark), "markListIntro");
        } else {
            String title = String.format(res.getString(R.string.title_filter), subjectFilter);
            toolbar.setTitle(title);
            setSupportActionBar(toolbar);

            double avg = controller.getAverage(subjectFilter, 0);
            double excepted = controller.whatShouldIGet(subjectFilter, 0);
            viewPager.setCurrentItem(1);
            AverageListFragment.setHint(this, avg, excepted);
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
        if (subjectFilter != null) {
            Intent backIntent = new Intent(this, MarkListActivity.class);
            startActivity(backIntent);
            finish();
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!Utils.isFirstQuarter(Utils.getToday())) {
            getMenuInflater().inflate(R.menu.marks, menu);
            allMarks = menu.findItem(R.id.filter_all);
            firstQMarks = menu.findItem(R.id.filter_first);
            secondQMarks = menu.findItem(R.id.filter_second);
            setSelectedItem();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.filter_all:
                quarterFilter = 0;
                break;
            case R.id.filter_first:
                quarterFilter = 1;
                break;
            case R.id.filter_second:
                quarterFilter = 2;
                break;
        }
        item.setChecked(true);
        prefs.edit().putInt(PREF_QUARTER_SELECTOR, quarterFilter).apply();
        refreshList(BoldApp.getBoldContext());


        return super.onOptionsItemSelected(item);
    }

    /**
     * Set the current quarter as checked
     */
    private void setSelectedItem() {
        quarterFilter = prefs.getInt(PREF_QUARTER_SELECTOR, 0);
        switch (quarterFilter) {
            case 0:
                allMarks.setChecked(true);
                break;
            case 1:
                firstQMarks.setChecked(true);
                break;
            case 2:
                secondQMarks.setChecked(true);
                break;
        }
    }

    /**
     * Custom Fragment Page Adapter class
     */
    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

    }
}
