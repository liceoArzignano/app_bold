package it.liceoarzignano.bold.marks;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import it.liceoarzignano.bold.R;

public class MarksListFragment extends Fragment {

    static RecyclerView sMarksListView;
    static LinearLayout sEmptyLayout;

    public MarksListFragment() {}

    @Override
    public View onCreateView(LayoutInflater mInflater, ViewGroup mContainer,
                             Bundle mSavedInstance) {
        View mView = mInflater.inflate(R.layout.fragment_mark_marks, mContainer, false);

        sMarksListView = (RecyclerView) mView.findViewById(R.id.mark_list);
        sEmptyLayout = (LinearLayout) mView.findViewById(R.id.mark_empty_layout);

        MarkListActivity.refresh(mContainer.getContext());

        return mView;
    }

}
