package it.liceoarzignano.bold.marks;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import it.liceoarzignano.bold.R;


class AverageAdapter extends RecyclerView.Adapter<AverageAdapter.AverageHolder> {
    private final MarksController mController;
    private final String[] mResults;

    AverageAdapter(MarksController controller, String[] results) {
        mController = controller;
        mResults = results;
    }

    @Override
    public AverageAdapter.AverageHolder onCreateViewHolder(ViewGroup parent, int type) {
        return new AverageAdapter.AverageHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_average, parent, false));
    }

    @Override
    public void onBindViewHolder(AverageAdapter.AverageHolder holder, int position) {
        holder.setData(mResults[position]);
    }

    @Override
    public int getItemCount() {
        return mResults != null ? mResults.length : 0;
    }

    class AverageHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mValue;

        AverageHolder(View view) {
            super(view);
            mTitle = (TextView) view.findViewById(R.id.row_avg_title);
            mValue = (TextView) view.findViewById(R.id.row_avg_value);
        }

        void setData(String result) {
            mTitle.setText(result);

            Double val = mController.getAverage(result, 0);
            if (val < 6) {
                mValue.setTextColor(Color.RED);
            }
            mValue.setText(String.format(Locale.ENGLISH, "%.2f", val));
        }
    }

}
