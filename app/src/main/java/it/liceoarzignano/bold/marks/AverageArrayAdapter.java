package it.liceoarzignano.bold.marks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Locale;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.realm.RealmController;

class AverageArrayAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private final String[] result;
    private final RealmController controller;

    AverageArrayAdapter(Context context, RealmController controller, int quarterFilter) {
        result = Utils.getAverageElements(quarterFilter);
        this.controller = controller;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return result != null ? result.length : 0;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("AccessStaticViaInstance")
    @SuppressLint({"DefaultLocale", "InflateParams", "ViewHolder"})
    @Override
    public View getView(final int position, View row, ViewGroup parent) {
        AverageArrayAdapter.Holder holder = new AverageArrayAdapter.Holder();
        row = inflater.inflate(R.layout.item_average, null);
        holder.avg = (TextView) row.findViewById(R.id.row_avg_value);
        holder.title = (TextView) row.findViewById(R.id.row_avg_title);

        row.setTag(holder);
        holder.title.setText(result[position]);

        final Double doubleAvg = controller.getAverage(result[position], 0);
        holder.avg.setText(String.format(Locale.ENGLISH, "%.2f", doubleAvg));
        if (doubleAvg < 6) {
            holder.avg.setTextColor(Color.RED);
        }

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (doubleAvg != 0) {
                    MarkListActivity.showFilteredMarks(result[position]);
                }
            }
        });

        return row;
    }

    private static class Holder {
        static TextView avg;
        static TextView title;
    }

}
