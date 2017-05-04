package it.liceoarzignano.bold.marks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.editor.EditorActivity;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.ui.CircularProgressBar;
import it.liceoarzignano.bold.ui.recyclerview.RecyclerViewExt;
import it.liceoarzignano.bold.utils.PrefsUtils;
import it.liceoarzignano.bold.utils.UiUtils;


public class SubjectActivity extends AppCompatActivity {
    static final String EXTRA_TITLE = "extra_subject_position";
    static final String EXTRA_FILTER = "extra_subject_filter";

    private CoordinatorLayout mCoordinator;
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
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        mCoordinator = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
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

    private void refresh() {
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

    void editItem(ImageView icon, Mark item) {
        new BoldAnalytics(this).log(FirebaseAnalytics.Event.VIEW_ITEM, "Edit mark");
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra(EditorActivity.EXTRA_ID, item.getId());

        if (PrefsUtils.isNotLegacy()) {
            UiUtils.animateAVD(icon.getDrawable());
            new Handler().postDelayed(() -> startActivity(intent), 1000);
        } else {
            startActivity(intent);
        }
    }

    void deleteItem(ImageView icon, Mark item, int position) {
        new BoldAnalytics(this).log(FirebaseAnalytics.Event.VIEW_ITEM, "Delete mark");
        UiUtils.animateAVD(icon.getDrawable());

        mController.delete(item.getId());
        Snackbar.make(mCoordinator, getString(R.string.actions_remove), Snackbar.LENGTH_LONG).show();
        new Handler().postDelayed(() -> mAdapter.notifyItemRemoved(position), 1000);
    }

    void shareItem(ImageView icon, Mark item) {
        new BoldAnalytics(this).log(FirebaseAnalytics.Event.SHARE, "Share mark");
        String message = getString(PrefsUtils.isTeacher(this) ?
                        R.string.marks_share_teacher : R.string.marks_share_student,
                item.getValue() / 100, item.getTitle());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (PrefsUtils.isNotLegacy()) {
            UiUtils.animateAVD(icon.getDrawable());
            new Handler().postDelayed(() ->
                    startActivity(Intent.createChooser(intent,
                            getString(R.string.actions_share))), 1000);
        } else {
            startActivity(Intent.createChooser(intent, getString(R.string.actions_share)));
        }
    }
}
