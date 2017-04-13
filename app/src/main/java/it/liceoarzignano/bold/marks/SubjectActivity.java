package it.liceoarzignano.bold.marks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.util.List;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.ui.CircularProgressBar;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt;


public class SubjectActivity extends AppCompatActivity {

    static final String EXTRA_TITLE = "extra_subject_position";
    static final String EXTRA_FILTER = "extra_subject_filter";

    private CircularProgressBar mProgressBar;
    private TextView mTextHint;
    private NestedScrollView mNestedView;

    private MarksController mController;
    private SubjectAdapter mAdapter;

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
        RecyclerViewExt marksList = (RecyclerViewExt) findViewById(R.id.subject_list);

        mAdapter = new SubjectAdapter(mController.getFilteredMarks(mTitle, mFilter), this);
        marksList.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    public void refresh() {
        List<Mark> marks = mController.getFilteredMarks(mTitle, mFilter);
        mAdapter.updateList(marks);
        // Scroll to top
        new Handler().post(() -> mNestedView.scrollTo(0,0));

        setHint(mController.getAverage(mTitle, mFilter),
                mController.whatShouldIGet(mTitle, mFilter));
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
