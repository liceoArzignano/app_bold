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

    private static Resources res;
    private static RealmController controller;

    private static RecyclerView mAvgListview;
    private static LinearLayout mHintLayout;
    private static TextView mHint;
    private static CircularProgressBar mProgressBar;

    public AverageListFragment() {
        controller = RealmController.with(getActivity());
    }

    static void refresh(Context context, final Pair<String, Integer> filter) {
        if (mAvgListview != null) {
            final String[] mResults = Utils.getAverageElements(filter.second);

            final AverageAdapter mAdapter = new AverageAdapter(controller, mResults);
            RecyclerView.LayoutManager mManager = new LinearLayoutManager(context);
            RecyclerClickListener mListener = new RecyclerClickListener() {
                @Override
                public void onClick(View mView, int mPosition) {
                    MarkListActivity.showFilteredMarks(mResults[mPosition]);
                }
            };

            mAvgListview.setLayoutManager(mManager);
            mAvgListview.setItemAnimator(new DefaultItemAnimator());
            mAvgListview.addItemDecoration(new DividerDecoration(context));
            mAvgListview.setAdapter(mAdapter);
            mAvgListview.addOnItemTouchListener(new RecyclerTouchListener(context, mListener));
            mAvgListview.setVisibility(filter.first != null ? View.GONE : View.VISIBLE);
            mAdapter.notifyDataSetChanged();
        }

        if (mHintLayout != null) {
            if (filter.first != null) {
                double avg = controller.getAverage(filter.first, filter.second);
                double excepted = controller.whatShouldIGet(filter.first, filter.second);
                setHint(context, avg, excepted);
                mHintLayout.setVisibility(View.VISIBLE);
            } else {
                mHintLayout.setVisibility(View.GONE);
            }
        }
    }

    static void setHint(Context context, double avg, double excepted) {
        String msg = res.getString(R.string.hint_content_common) + " " +
                String.format(res.getString(excepted < 6 ? R.string.hint_content_above :
                        R.string.hint_content_under), excepted);
        mHint.setText(msg);

        int colorAddress;
        if (avg < 5.5) {
             colorAddress = R.color.red;
        } else if (avg < 6) {
            colorAddress = R.color.yellow;
        } else {
            colorAddress = R.color.green;
        }
        mProgressBar.setProgressColor(ContextCompat.getColor(context, colorAddress));
        mProgressBar.setProgress(avg);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstance) {
        Context context = container.getContext();
        res = getResources();

        View view = inflater.inflate(R.layout.fragment_mark_average, container, false);

        mAvgListview = (RecyclerView) view.findViewById(R.id.average_listView);
        mHintLayout = (LinearLayout) view.findViewById(R.id.hint_layout);
        mHint = (TextView) view.findViewById(R.id.hint);
        mProgressBar = (CircularProgressBar) view.findViewById(R.id.value);

        refresh(context, MarkListActivity.getSubjectFilter());

        return view;
    }


}
