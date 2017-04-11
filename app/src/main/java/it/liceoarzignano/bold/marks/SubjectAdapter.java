package it.liceoarzignano.bold.marks;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.utils.DateUtils;

class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectHolder> {
    private final List<Mark> mMarks;

    SubjectAdapter(List<Mark> marks) {
        this.mMarks = marks;
    }

    @Override
    public SubjectHolder onCreateViewHolder(ViewGroup parent, int type) {
        return new SubjectHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mark, parent, false));
    }

    @Override
    public void onBindViewHolder(SubjectHolder holder, int position) {
        holder.setData(mMarks.get(position));
    }

    @Override
    public int getItemCount() {
        return mMarks.size();
    }

    class SubjectHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mValue;
        private final TextView mDate;

        SubjectHolder(View view) {
            super(view);
            mTitle = (TextView) view.findViewById(R.id.row_mark_title);
            mValue = (TextView) view.findViewById(R.id.row_mark_value);
            mDate = (TextView) view.findViewById(R.id.row_mark_date);
        }

        void setData(Mark mark) {
            mTitle.setText(mark.getTitle());
            mDate.setText(DateUtils.dateToString(mark.getDate()));

            Double val = (double) mark.getValue() / 100;
            if (val < 6) {
                mValue.setTextColor(Color.RED);
            }
            mValue.setText(String.format(Locale.ENGLISH, "%.2f", val));
        }
    }
}
