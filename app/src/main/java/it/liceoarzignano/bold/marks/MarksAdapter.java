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

class MarksAdapter extends RecyclerView.Adapter<MarksAdapter.MarkHolder> {
    private final List<Mark> mMarks;

    MarksAdapter(List<Mark> mMarks) {
        this.mMarks = mMarks;
    }

    @Override
    public MarksAdapter.MarkHolder onCreateViewHolder(ViewGroup mParent, int mType) {
        View mItem = LayoutInflater.from(mParent.getContext())
                .inflate(R.layout.item_mark, mParent, false);

        return new MarksAdapter.MarkHolder(mItem);
    }

    @Override
    public void onBindViewHolder(MarksAdapter.MarkHolder mHolder, int mPosition) {
        Mark mMark = mMarks.get(mPosition);
        mHolder.setData(mMark);
    }

    @Override
    public int getItemCount() {
        return mMarks.size();
    }

    class MarkHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mValue;
        private final TextView mDate;

        MarkHolder(View mView) {
            super(mView);
            mTitle = (TextView) mView.findViewById(R.id.row_mark_title);
            mValue = (TextView) mView.findViewById(R.id.row_mark_value);
            mDate = (TextView) mView.findViewById(R.id.row_mark_notes);
        }

        void setData(Mark mMark) {
            mTitle.setText(mMark.getTitle());
            mDate.setText(mMark.getDate());

            Double mDoubleVal = (double) mMark.getValue() / 100;
            if (mDoubleVal < 6) {
                mValue.setTextColor(Color.RED);
            }
            mValue.setText(String.format(Locale.ENGLISH, "%.2f", mDoubleVal));
        }
    }

}
