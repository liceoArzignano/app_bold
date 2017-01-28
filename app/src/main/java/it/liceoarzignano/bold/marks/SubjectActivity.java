package it.liceoarzignano.bold.marks;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.ui.CircularProgressBar;
import it.liceoarzignano.bold.ui.DividerDecoration;
import it.liceoarzignano.bold.ui.RecyclerClickListener;
import it.liceoarzignano.bold.ui.RecyclerTouchListener;
import it.liceoarzignano.bold.ui.ViewerDialog;


public class SubjectActivity extends AppCompatActivity {

    final static String EXTRA_TITLE = "extra_subject_position";
    final static String EXTRA_FILTER = "extra_subject_filter";

    private CircularProgressBar mProgressBar;
    private TextView mTextHint;
    private RecyclerView mList;

    private MarksController mController;
    private List<Mark> mMarks;

    private String mTitle;
    private int mFilter;

    @Override
    protected void onCreate(Bundle mSavedInstance) {
        super.onCreate(mSavedInstance);

        setContentView(R.layout.activity_subject);

        mController = new MarksController(((BoldApp) getApplication()).getConfig());

        Intent mIntent = getIntent();
        mTitle = mIntent.getStringExtra(EXTRA_TITLE);
        mFilter = mIntent.getIntExtra(EXTRA_FILTER, 0);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(mTitle);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mProgressBar = (CircularProgressBar) findViewById(R.id.subject_hint_bar);
        mTextHint = (TextView) findViewById(R.id.subject_hint_text);
        mList = (RecyclerView) findViewById(R.id.subject_list);

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
        mMarks = mController.getFilteredMarks(mTitle, mFilter);
        SubjectAdapter mAdapter = new SubjectAdapter(mMarks);
        RecyclerClickListener mListener = (mView, mPosition) -> {
            BottomSheetDialog mSheet = new BottomSheetDialog(this);
            View mBottomView = new ViewerDialog(this, mSheet)
                    .setData(mMarks.get(mPosition).getId(), true);
            mSheet.setContentView(mBottomView);
            mSheet.show();
        };

        mList.setAdapter(mAdapter);
        mList.addOnItemTouchListener(new RecyclerTouchListener(this, mListener));
        mAdapter.notifyDataSetChanged();
    }

    private void setHint(double mAverage, double mExpected) {
        mTextHint.setText(String.format("%1$s %2$s", getString(R.string.hint_content_common),
                String.format(getString(mExpected < 6 ?
                        R.string.hint_content_above : R.string.hint_content_under),
                        mExpected)));
        int mColor;
        if (mAverage < 5.5d) {
            mColor = R.color.red;
        } else if (mAverage < 6d) {
            mColor = R.color.yellow;
        } else {
            mColor = R.color.green;
        }
        mProgressBar.setProgressColor(ContextCompat.getColor(this, mColor));
        mProgressBar.setProgress(mAverage);
    }
}
