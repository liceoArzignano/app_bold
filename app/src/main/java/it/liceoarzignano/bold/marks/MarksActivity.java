package it.liceoarzignano.bold.marks;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Date;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.editor.EditorActivity;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.ui.recyclerview.DividerDecoration;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerClickListener;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerTouchListener;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt;
import it.liceoarzignano.bold.utils.ContentUtils;
import it.liceoarzignano.bold.utils.DateUtils;
import it.liceoarzignano.bold.utils.PrefsUtils;

public class MarksActivity extends AppCompatActivity {
    private RecyclerViewExt mList;
    private LinearLayout mEmptyLayout;
    private AverageAdapter mAdapter;

    private int mFilter;
    private boolean isFirstEnabled;
    private boolean isSecondEnabled;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_marks);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        mList = (RecyclerViewExt) findViewById(R.id.marks_list);
        mEmptyLayout = (LinearLayout) findViewById(R.id.marks_empty_layout);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            new BoldAnalytics(this).log(FirebaseAnalytics.Event.SELECT_CONTENT, "Add mark");
            startActivity(new Intent(this, EditorActivity.class));
        });

        mList.addItemDecoration(new DividerDecoration(this));
        mAdapter = new AverageAdapter(this, ContentUtils.getAverageElements(this, mFilter));
        mList.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Date today = DateUtils.getDate(0);
        Date change = DateUtils.stringToDate(getString(R.string.config_quarter_change));
        if (DateUtils.matchDayOfYear(today, change)) {
            getMenuInflater().inflate(R.menu.marks, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_filter) {
            createFilterDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        String[] marks = ContentUtils.getAverageElements(this, mFilter);

        if (marks.length == 0) {
            mList.setVisibility(View.GONE);
            mEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            mEmptyLayout.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);

            RecyclerClickListener listener = (mView, mPosition) -> {
                new BoldAnalytics(this).log(FirebaseAnalytics.Event.VIEW_ITEM, "Subject");
                Intent mIntent = new Intent(this, SubjectActivity.class);
                mIntent.putExtra(SubjectActivity.EXTRA_TITLE, marks[mPosition]);
                mIntent.putExtra(SubjectActivity.EXTRA_FILTER, mFilter);
                startActivity(mIntent);
            };
            mList.addOnItemTouchListener(new RecyclerTouchListener(this, listener));
            mAdapter.updateList(marks);
        }
    }

    private void createFilterDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.marks_menu_filter)
                .customView(getFilterView(), false)
                .canceledOnTouchOutside(false)
                .autoDismiss(false)
                .positiveText(R.string.marks_filter_action)
                .onPositive((dialog, which) -> {
                    if (!isFirstEnabled && !isSecondEnabled) {
                        Toast.makeText(this, getString(R.string.marks_filter_error),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    mFilter = isFirstEnabled ? isSecondEnabled ? 0 : 1 : 2;
                    PrefsUtils.setCurrentQuarter(this, mFilter);
                    dialog.dismiss();
                    refresh();
                })
                .show();
    }

    private View getFilterView() {
        int val = PrefsUtils.getCurrentQuarter(this);
        isFirstEnabled = val < 2;
        isSecondEnabled = val != 1;

        Drawable noBg = new ColorDrawable(Color.TRANSPARENT);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup group = (ViewGroup) findViewById(R.id.dialog_root);
        View view = inflater.inflate(R.layout.dialog_filter, group);
        ImageView firstSel = (ImageView) view.findViewById(R.id.filter_first);
        ImageView secondSel = (ImageView) view.findViewById(R.id.filter_second);

        firstSel.setBackground(isFirstEnabled ?
                ContextCompat.getDrawable(this, R.drawable.ic_filter_bg_enabled) : noBg);
        secondSel.setBackground(isSecondEnabled ?
                ContextCompat.getDrawable(this, R.drawable.ic_filter_bg_enabled) : noBg);

        firstSel.setOnClickListener(v -> {
            isFirstEnabled = !isFirstEnabled;
            firstSel.setBackground(isFirstEnabled ?
                    ContextCompat.getDrawable(this, R.drawable.ic_filter_bg_enabled) : noBg);
        });
        secondSel.setOnClickListener(v -> {
            isSecondEnabled = !isSecondEnabled;
            secondSel.setBackground(isSecondEnabled ?
                    ContextCompat.getDrawable(this, R.drawable.ic_filter_bg_enabled) : noBg);
        });

        return view;
    }
}
