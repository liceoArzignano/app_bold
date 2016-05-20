package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import it.liceoarzignano.bold.R;

public class AverageListFragment extends Fragment {

    private static String[] subjects;
    private static ListView mAvgListview;

    private static LinearLayout mHintLayout;
    private static TextView mTitle;
    private static TextView mValue;
    private static TextView mHint;

    public AverageListFragment() {

    }

    public static void refresh(Context context, String filter) {
        if (mAvgListview != null) {
            mAvgListview.setAdapter(new AverageArrayAdapter(context, subjects));
            mAvgListview.setVisibility(filter != null ? View.GONE : View.VISIBLE);
        }
        if (mHintLayout != null) {
            if (filter != null) {
                double avg = DatabaseConnection.getInstance(context).getAverage(filter);
                double excepted = DatabaseConnection.getInstance(context).whatShouldIGet(filter);
                setHint(context, filter, avg, excepted);
                mHintLayout.setVisibility(View.VISIBLE);
            } else {
                mHintLayout.setVisibility(View.GONE);
            }
        }
    }

    public static void setHint(Context context, String subject, double avg, double excepted) {
        String msg;

        msg = context.getString(R.string.hint_title) + " " + subject + " "
                + context.getString(R.string.hint_title_end);
        mTitle.setText(msg);
        msg = String.format("%.2f", avg);
        mValue.setText(msg);
        msg = context.getString(R.string.hint_content_common) + " " + subject + ", ";
        msg += excepted < 6 ?
                context.getString(R.string.hint_content_under) :
                context.getString(R.string.hint_content_above);
        msg = msg + " " + excepted + " " + context.getString(R.string.hint_content_common_end);
        mHint.setText(msg);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstance) {
        Context context = container.getContext();

        View view = inflater.inflate(R.layout.fragment_mark_average, container, false);

        mAvgListview = (ListView) view.findViewById(R.id.average_listView);
        mHintLayout = (LinearLayout) view.findViewById(R.id.hint_layout);
        mTitle = (TextView) view.findViewById(R.id.title);
        mValue = (TextView) view.findViewById(R.id.value);
        mHint = (TextView) view.findViewById(R.id.hint);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String address = preferences.getString("address_key", "0");

        switch (address) {
            case "1":
                subjects = getResources().getStringArray(R.array.subjects_lists_1);
                break;
            case "2":
                subjects = getResources().getStringArray(R.array.subjects_lists_2);
                break;
            case "3":
                subjects = getResources().getStringArray(R.array.subjects_lists_3);
                break;
            case "4":
                subjects = getResources().getStringArray(R.array.subjects_lists_4);
                break;
            case "5":
                subjects = getResources().getStringArray(R.array.subjects_lists_5);
                break;
        }

        return view;
    }


}
