package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Locale;

import it.liceoarzignano.bold.R;

public class AverageListFragment extends Fragment {

    private static ListView mAvgListview;

    private static LinearLayout mHintLayout;
    private static TextView mTitle;
    private static TextView mValue;
    private static TextView mHint;
    private static Resources res;

    public AverageListFragment() {

    }

    public static void refresh(Context context, String filter) {
        if (mAvgListview != null) {
            mAvgListview.setAdapter(new AverageArrayAdapter(context));
            mAvgListview.setVisibility(filter != null ? View.GONE : View.VISIBLE);
        }
        if (mHintLayout != null) {
            if (filter != null) {
                double avg = DatabaseConnection.getInstance(context).getAverage(filter);
                double excepted = DatabaseConnection.getInstance(context).whatShouldIGet(filter);
                setHint(filter, avg, excepted);
                mHintLayout.setVisibility(View.VISIBLE);
            } else {
                mHintLayout.setVisibility(View.GONE);
            }
        }
    }

    public static void setHint(String subject, double avg, double excepted) {
        String msg;

        msg = String.format(res.getString(R.string.hint_title), subject);
        mTitle.setText(msg);
        msg = String.format(Locale.ITALIAN, "%.2f", avg);
        mValue.setText(msg);
        msg = String.format(res.getString(R.string.hint_content_common), subject)
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

        refresh(context, MarkListActivity.getFilter());

        return view;
    }


}
