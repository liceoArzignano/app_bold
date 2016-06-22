package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import it.liceoarzignano.bold.realm.RealmController;


public class MarkListActivity extends AppCompatActivity {

    private static Context fContext;
    private static String filter;

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
        adapter.addFragment(new AverageListFragment(), filter == null ?
                    getString(R.string.title_fragments_avg) :
                    getString(R.string.title_fragments_avgs));

        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_list);

        fContext = this;
        Resources res = getResources();

        RealmController controller = RealmController.with(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

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
                if (addressOk()) {
                    Intent i = new Intent(MarkListActivity.this, ManagerActivity.class);
                    startActivity(i);
                }
            }
        });

        if (filter == null) {
            Utils.animFabIntro(this, fab,
                    getString(R.string.intro_fab_mark), "markListIntro");
        } else {
            String title = String.format(res.getString(R.string.title_filter), filter);
            assert toolbar != null;
            toolbar.setTitle(title);
            setSupportActionBar(toolbar);

            double avg = controller.getAverage(filter);
            double excepted = controller.whatShouldIGet(filter);
            AverageListFragment.setHint(filter, avg, excepted);
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
        if (filter != null) {
            Intent backIntent = new Intent(this, MarkListActivity.class);
            startActivity(backIntent);

        }
        finish();
    }

    /**
     * Check if user defined a valid address (or is a teacher)
     *
     * @return read comments to understand
     */
    private boolean addressOk() {
         if (Utils.getAddress(this).equals("0")) {
                // Students must define a valid address
                Toast.makeText(getApplicationContext(), getString(R.string.error_noaddress),
                        Toast.LENGTH_LONG).show();
                return false;
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

    static String getFilter() {
        return filter;
    }
}
