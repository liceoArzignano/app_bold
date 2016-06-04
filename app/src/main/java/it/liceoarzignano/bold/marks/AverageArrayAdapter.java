package it.liceoarzignano.bold.marks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import it.liceoarzignano.bold.R;

class AverageArrayAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    final private String[] result;
    final private Context context;

    public AverageArrayAdapter(Context context,
                               String[] subjTitleList) {
        result = subjTitleList;
        this.context = context;
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

    @SuppressLint({"DefaultLocale", "InflateParams", "ViewHolder"})
    @Override
    public View getView(final int position, View row, ViewGroup parent) {
        final Holder holder = new Holder();
        row = inflater.inflate(R.layout.item_average, null);
        holder.avg = (TextView) row.findViewById(R.id.row_avg_value);
        holder.title = (TextView) row.findViewById(R.id.row_avg_title);

        row.setTag(holder);
        holder.title.setText(result[position]);

        final Double doubleAvg = new DatabaseConnection(context).getAverage(result[position]);
        holder.avg.setText(String.format("%.2f", doubleAvg));
        if (doubleAvg < 6) {
            if (doubleAvg == 0) {
                holder.avg.setTextColor(Color.GRAY);
                holder.avg.setText("n\\a");
                holder.title.setTextColor(Color.GRAY);
            } else {
                holder.avg.setTextColor(Color.RED);
            }
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

    public class Holder {
        TextView avg;
        TextView title;
    }

}
