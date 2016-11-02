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

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.ui.CircularProgressBar;
import it.liceoarzignano.bold.realm.RealmController;
import it.liceoarzignano.bold.ui.DividerDecoration;
import it.liceoarzignano.bold.ui.RecyclerClickListener;
import it.liceoarzignano.bold.ui.RecyclerTouchListener;

public class AverageListFragment extends Fragment {

    private static Resources sRes;
    private static RealmController sController;

    private static RecyclerView sAvgListview;
    private static LinearLayout sHintLayout;
    private static TextView sHint;
    private static CircularProgressBar sProgressBar;

    public AverageListFragment() {
        sController = RealmController.with(getActivity());
    }

    static void refresh(Context mContext, final Pair<String, Integer> mFilter) {
        if (sAvgListview != null) {
            final String[] mResults = Utils.getAverageElements(mFilter.second);

            final AverageAdapter mAdapter = new AverageAdapter(sController, mResults);
            RecyclerView.LayoutManager mManager = new LinearLayoutManager(mContext);
            RecyclerClickListener mListener = new RecyclerClickListener() {
                @Override
                public void onClick(View mView, int mPosition) {
                    if (mResults.length > 0) {
                        MarkListActivity.showFilteredMarks(mResults[mPosition]);
                    }
                }
            };

            sAvgListview.setLayoutManager(mManager);
            sAvgListview.setItemAnimator(new DefaultItemAnimator());
            sAvgListview.addItemDecoration(new DividerDecoration(mContext));
            sAvgListview.setAdapter(mAdapter);
            sAvgListview.addOnItemTouchListener(new RecyclerTouchListener(mContext, mListener));
            sAvgListview.setVisibility(mFilter.first != null ? View.GONE : View.VISIBLE);
            mAdapter.notifyDataSetChanged();
        }

        if (sHintLayout != null) {
            if (mFilter.first != null) {
                double avg = sController.getAverage(mFilter.first, mFilter.second);
                double excepted = sController.whatShouldIGet(mFilter.first, mFilter.second);
                setHint(mContext, avg, excepted);
                sHintLayout.setVisibility(View.VISIBLE);
            } else {
                sHintLayout.setVisibility(View.GONE);
            }
        }
    }

    static void setHint(Context mContext, double mAvg, double mExcepted) {
        String mMessage = sRes.getString(R.string.hint_content_common) + " " +
                String.format(sRes.getString(mExcepted < 6 ? R.string.hint_content_above :
                        R.string.hint_content_under), mExcepted);
        sHint.setText(mMessage);

        int mColor;

        if (mAvg < 5.5) {
             mColor = R.color.red;
        } else if (mAvg < 6) {
            mColor = R.color.yellow;
        } else {
            mColor = R.color.green;
        }
        sProgressBar.setProgressColor(ContextCompat.getColor(mContext, mColor));
        sProgressBar.setProgress(mAvg);
    }

    @Override
    public View onCreateView(LayoutInflater mInflater, ViewGroup mContainer,
                             Bundle mSavedInstance) {
        Context mContext = mContainer.getContext();
        sRes = getResources();

        View mView = mInflater.inflate(R.layout.fragment_mark_average, mContainer, false);

        sAvgListview = (RecyclerView) mView.findViewById(R.id.average_listView);
        sHintLayout = (LinearLayout) mView.findViewById(R.id.hint_layout);
        sHint = (TextView) mView.findViewById(R.id.avg_hint);
        sProgressBar = (CircularProgressBar) mView.findViewById(R.id.avg_value);

        refresh(mContext, MarkListActivity.getsSubjectFilter());

        return mView;
    }


}
