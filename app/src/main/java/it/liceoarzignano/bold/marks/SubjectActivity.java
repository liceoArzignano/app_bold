package it.liceoarzignano.bold.marks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.ui.CircularProgressBar;
import it.liceoarzignano.bold.ui.ViewerDialog;
import it.liceoarzignano.bold.ui.recyclerview.DividerDecoration;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerClickListener;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerTouchListener;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt;


public class SubjectActivity extends AppCompatActivity {

    static final String EXTRA_TITLE = "extra_subject_position";
    static final String EXTRA_FILTER = "extra_subject_filter";

    private CircularProgressBar mProgressBar;
    private TextView mTextHint;
    private RecyclerViewExt mList;
    private NestedScrollView mNestedView;

    private MarksController mController;

    private String mTitle;
    private int mFilter;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_subject);

        mController = new MarksController(((BoldApp) getApplication()).getConfig());

        Intent callingIntent = getIntent();
        mTitle = callingIntent.getStringExtra(EXTRA_TITLE);
        mFilter = callingIntent.getIntExtra(EXTRA_FILTER, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mTitle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mNestedView = (NestedScrollView) findViewById(R.id.subject_nested_view);
        mProgressBar = (CircularProgressBar) findViewById(R.id.subject_hint_bar);
        mTextHint = (TextView) findViewById(R.id.subject_hint_text);
        mList = (RecyclerViewExt) findViewById(R.id.subject_list);

        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.addItemDecoration(new DividerDecoration(this));
        mList.setItemAnimator(new DefaultItemAnimator());

        setHint(mController.getAverage(mTitle, mFilter),
                mController.whatShouldIGet(mTitle, mFilter));
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    public void refresh() {
        List<Mark> mMarks = mController.getFilteredMarks(mTitle, mFilter);
        SubjectAdapter adapter = new SubjectAdapter(mMarks);
        RecyclerClickListener listener = (view, position) -> {
            BottomSheetDialog sheet = new BottomSheetDialog(this);
            View bottomView = new ViewerDialog(this, sheet)
                    .setData(mMarks.get(position).getId(), true);
            sheet.setContentView(bottomView);
            sheet.show();
        };

        mList.setAdapter(adapter);
        mList.addOnItemTouchListener(new RecyclerTouchListener(this, listener));
        adapter.notifyDataSetChanged();
        // Scroll to top
        new Handler().post(() -> mNestedView.scrollTo(0,0));
    }

    private void setHint(double average, double expected) {
        mTextHint.setText(String.format("%1$s %2$s", getString(R.string.hint_content_common),
                String.format(getString(expected < 6 ?
                        R.string.hint_content_above : R.string.hint_content_under),
                        expected)));
        int color;
        if (average < 5.5d) {
            color = R.color.red;
        } else if (average < 6d) {
            color = R.color.yellow;
        } else {
            color = R.color.green;
        }
        mProgressBar.setProgressColor(ContextCompat.getColor(this, color));
        mProgressBar.setProgress(average);
    }
}
