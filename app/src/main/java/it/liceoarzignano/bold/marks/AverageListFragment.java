package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.ui.CircularProgressBar;
import it.liceoarzignano.bold.ui.DividerDecoration;
import it.liceoarzignano.bold.ui.RecyclerClickListener;
import it.liceoarzignano.bold.ui.RecyclerTouchListener;

public class AverageListFragment extends Fragment {

    Resources mRes;
    MarksController mController;
    RecyclerView mListView;
    LinearLayout mHintLayout;
    TextView mHint;
    CircularProgressBar mProgressBar;
    private Context mContext;

    public AverageListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater mInflater, ViewGroup mContainer,
                             Bundle mSavedInstance) {
        Context mContext = mContainer.getContext();
        mRes = getResources();

        View mView = mInflater.inflate(R.layout.fragment_mark_average, mContainer, false);

        mListView = (RecyclerView) mView.findViewById(R.id.average_listView);
        mHintLayout = (LinearLayout) mView.findViewById(R.id.hint_layout);
        mHint = (TextView) mView.findViewById(R.id.avg_hint);
        mProgressBar = (CircularProgressBar) mView.findViewById(R.id.avg_value);

        refresh(((MarkListActivity) mContext).getsSubjectFilter());

        return mView;
    }

    /**
     * Initialize marks controller for fragment
     *
     * @param mContext used to fetch realm configuration
     */
    void init(Context mContext) {
        this.mContext = mContext;
        mController = new MarksController(((BoldApp) mContext.getApplicationContext()).getConfig());
    }

    /**
     * Refresh the list
     *
     * @param mFilter used to filter the subjects
     */
    void refresh(final Pair<String, Integer> mFilter) {
        if (mListView == null) {
            return;
        }

        final String[] mResults = Utils.getAverageElements(mContext, mFilter.second);

        final AverageAdapter mAdapter = new AverageAdapter(mController, mResults);
        RecyclerView.LayoutManager mManager = new LinearLayoutManager(mContext);
        RecyclerClickListener mListener = (mView, mPosition) -> {
            if (mResults.length > 0) {
                ((MarkListActivity) mContext).showFilteredMarks(mResults[mPosition]);
            }
        };

        mListView.setLayoutManager(mManager);
        mListView.setItemAnimator(new DefaultItemAnimator());
        mListView.addItemDecoration(new DividerDecoration(mContext));
        mListView.setAdapter(mAdapter);
        mListView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mListener));
        mListView.setVisibility(mFilter.first != null ? View.GONE : View.VISIBLE);
        mAdapter.notifyDataSetChanged();

        if (mHintLayout == null) {
            return;
        }

        if (mFilter.first != null) {
            double avg = mController.getAverage(mFilter.first, mFilter.second);
            double excepted = mController.whatShouldIGet(mFilter.first, mFilter.second);
            setHint(mContext, avg, excepted);

            // Animate transition
            mHintLayout.setVisibility(View.VISIBLE);
            mHintLayout.setAlpha(0f);
            mHintLayout.animate().alpha(1f).setDuration(280);
        } else {
            // Animate transition
            mHintLayout.setVisibility(View.GONE);
            mListView.setAlpha(0f);
            mListView.animate().alpha(1f).setDuration(280);
        }
    }

    /**
     * Show hint
     *
     * @param mContext to fetch resources
     * @param mAvg average
     * @param mExcepted expected mark
     */
    void setHint(Context mContext, double mAvg, double mExcepted) {
        String mMessage = mRes.getString(R.string.hint_content_common) + " " +
                String.format(mContext.getString(mExcepted < 6 ? R.string.hint_content_above :
                        R.string.hint_content_under), mExcepted);
        mHint.setText(mMessage);

        int mColor;

        if (mAvg < 5.5) {
            mColor = R.color.red;
        } else if (mAvg < 6) {
            mColor = R.color.yellow;
        } else {
            mColor = R.color.green;
        }
        mProgressBar.setProgressColor(ContextCompat.getColor(mContext, mColor));
        mProgressBar.setProgress(mAvg);
    }

}
