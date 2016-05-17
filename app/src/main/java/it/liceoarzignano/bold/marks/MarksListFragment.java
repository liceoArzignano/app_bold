package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import it.liceoarzignano.bold.R;

public class MarksListFragment extends Fragment {

    public static ListView mMarksListView;

    public MarksListFragment() {

    }

    /**
     * Update the ListView content
     *
     * @param context: needed to reload database data
     */
    private static void refreshList(Context context) {
        MarkListActivity.refreshList(context);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstance) {
        View view = inflater.inflate(R.layout.fragment_mark_marks, container, false);

        mMarksListView = (ListView) view.findViewById(R.id.mark_list_view);

        refreshList(getContext());

        return view;
    }

}
