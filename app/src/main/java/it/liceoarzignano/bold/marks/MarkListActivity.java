package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.ViewerActivity;


public class MarkListActivity extends AppCompatActivity {

    private static Context fContext;
    private static String filter;

    private TabLayout tabLayout;

    /**
     * Fire ViewerActivity and pass the selected mark data
     *
     * @param id: mark id
     */
    public static void viewMark(int id) {

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
    public static void refreshList(final Context context) {
        LoadListViewTask loadMarkPostListViewTask = new LoadListViewTask(
                context, MarksListFragment.mMarksListView, filter);
        loadMarkPostListViewTask.execute();

        AverageListFragment.refresh(context, filter);
    }

    /**
     * Restart this activity with a filter for the ListView content
     *
     * @param filter: title filter
     */
    public static void showFilteredMarks(String filter) {
        Intent filteredList = new Intent(fContext, MarkListActivity.class);
        filteredList.putExtra("filteredList", filter);

        fContext.startActivity(filteredList);
    }

    /**
     * Initialize the viewpager and add the needed fragments
     *
     * @param viewPager: the viewpager we're going to play with
     */
    private void setUpViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MarksListFragment(), getString(R.string.title_fragment_marks));

        if (!Utils.isTeacher(this)) {
            adapter.addFragment(new AverageListFragment(), filter == null ?
                    getString(R.string.title_fragments_avg) :
                    getString(R.string.title_fragments_avgs));
        } else {
            tabLayout.setVisibility(View.GONE);
        }

        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_list);

        fContext = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        setUpViewPager(viewPager);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }

        Intent thisIntent = getIntent();
        filter = thisIntent.getStringExtra("filteredList");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_new_mark);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addressOk(false)) {
                    Intent i = new Intent(MarkListActivity.this, ManagerActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });

        if (filter == null) {
            Utils.animFab(true, fab);
        } else {
            String title = getString(R.string.title_filter) + " " + filter
                    + " " + getString(R.string.title_filter_end);
            assert toolbar != null;
            toolbar.setTitle(title);
            setSupportActionBar(toolbar);

            double avg = DatabaseConnection.getInstance(this).getAverage(filter);
            double excepted = DatabaseConnection.getInstance(this).whatShouldIGet(filter);
            AverageListFragment.setHint(this, filter, avg, excepted);
        }

        refreshList(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList(getApplicationContext());
    }

    /**
     * Check if user defined a valid address (or is a teacher)
     *
     * @param fromMenu: inform if it's been called from the fab (false) or a menu icon (true)
     * @return read comments to understand
     */
    private boolean addressOk(boolean fromMenu) {

        if (Utils.isTeacher(this)) {
            // Teachers are not allowed to see averages
            if (fromMenu) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_noteacher),
                        Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            if (Utils.getAddress(this).equals("0")) {
                // Students must define a valid address
                Toast.makeText(getApplicationContext(), getString(R.string.error_noaddress),
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        // Teacher and Student who defined an address are allowed to add marks
        return true;
    }

    /**
     * Custom Fragment Page Adapter class
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
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

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

    }
}
