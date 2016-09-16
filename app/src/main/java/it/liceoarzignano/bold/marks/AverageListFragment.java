package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.realm.RealmController;

public class AverageListFragment extends Fragment {

    private static ListView mAvgListview;

    private static LinearLayout mHintLayout;
    private static TextView mTitle;
    private static TextView mValue;
    private static TextView mHint;
    private static Resources res;
    private static RealmController controller;

    private static boolean isTeacher = false;

    public AverageListFragment() {
        controller = RealmController.with(getActivity());
    }

    static void refresh(Context context, Pair<String, Integer> filter) {
        isTeacher = Utils.isTeacher(context);
        if (mAvgListview != null) {
            mAvgListview.setAdapter(new AverageArrayAdapter(context, controller, filter.second));
            mAvgListview.setVisibility(filter.first != null ? View.GONE : View.VISIBLE);
        }
        if (mHintLayout != null) {
            if (filter.first != null) {
                double avg = controller.getAverage(filter.first, filter.second);
                double excepted = controller.whatShouldIGet(filter.first, filter.second);
                setHint(filter.first, avg, excepted);
                mHintLayout.setVisibility(View.VISIBLE);
            } else {
                mHintLayout.setVisibility(View.GONE);
            }
        }
    }

    static void setHint(String subject, double avg, double excepted) {
        String msg;

        msg = String.format(res.getString(isTeacher ? R.string.hint_title_teacher :
                R.string.hint_title_student), subject);
        mTitle.setText(msg);
        msg = String.valueOf(avg);
        mValue.setText(msg);
        msg = String.format(res.getString(isTeacher ? R.string.hint_content_common_teacher :
                R.string.hint_content_common_student), subject)
                + " " + String.format(res.getString(excepted < 6 ?
                R.string.hint_content_above : R.string.hint_content_under), excepted);
        mHint.setText(msg);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstance) {
        Context context = container.getContext();
        res = getResources();

        View view = inflater.inflate(R.layout.fragment_mark_average, container, false);

        mAvgListview = (ListView) view.findViewById(R.id.average_listView);
        mHintLayout = (LinearLayout) view.findViewById(R.id.hint_layout);
        mTitle = (TextView) view.findViewById(R.id.title);
        mValue = (TextView) view.findViewById(R.id.value);
        mHint = (TextView) view.findViewById(R.id.hint);

        refresh(context, MarkListActivity.getSubjectFilter());

        return view;
    }


}
