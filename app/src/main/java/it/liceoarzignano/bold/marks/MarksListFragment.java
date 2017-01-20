package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.ui.DividerDecoration;
import it.liceoarzignano.bold.ui.RecyclerClickListener;
import it.liceoarzignano.bold.ui.RecyclerTouchListener;

public class MarksListFragment extends Fragment {

    RecyclerView mMarksListView;
    LinearLayout mEmptyLayout;

    public MarksListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater mInflater, ViewGroup mContainer,
                             Bundle mSavedInstance) {
        View mView = mInflater.inflate(R.layout.fragment_mark_marks, mContainer, false);

        mMarksListView = (RecyclerView) mView.findViewById(R.id.mark_list);
        mEmptyLayout = (LinearLayout) mView.findViewById(R.id.mark_empty_layout);

        return mView;
    }

    /**
     * Refresh marks list
     *
     * @param mContext used to fetch resources
     * @param mMarks list of marks that will be displayed in the list
     */
    void refresh(Context mContext, List<Mark> mMarks) {
        if (mMarksListView == null) {
            return;
        }

        MarksAdapter mAdapter = new MarksAdapter(mMarks);
        RecyclerClickListener mListener = (mView, mPosition) ->
                ((MarkListActivity) mContext).viewMark(mMarks.get(mPosition).getId());

        mMarksListView.setLayoutManager(new LinearLayoutManager(mContext));
        mMarksListView.addItemDecoration(new DividerDecoration(mContext));
        mMarksListView.setItemAnimator(new DefaultItemAnimator());
        mMarksListView.setAdapter(mAdapter);
        mMarksListView.addOnItemTouchListener(
                new RecyclerTouchListener(mContext, mListener));
        mAdapter.notifyDataSetChanged();
    }

}
