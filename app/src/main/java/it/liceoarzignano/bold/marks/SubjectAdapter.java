package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;
import java.util.Locale;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.ui.ViewerDialog;
import it.liceoarzignano.bold.utils.DateUtils;

class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectHolder> {
    private List<Mark> mMarks;
    private final Context mContext;

    SubjectAdapter(List<Mark> marks, Context context) {
        mMarks = marks;
        mContext = context;
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

    void updateList(List<Mark> marks) {
        mMarks = marks;
        notifyDataSetChanged();
    }

    class SubjectHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mValue;
        private final TextView mDate;
        private final TextView mSummary;

        SubjectHolder(View view) {
            super(view);
            mView = view;
            mValue = (TextView) view.findViewById(R.id.row_mark_value);
            mDate = (TextView) view.findViewById(R.id.row_mark_date);
            mSummary = (TextView) view.findViewById(R.id.row_mark_summary);
        }

        void setData(Mark mark) {
            mDate.setText(DateUtils.dateToWorldsString(mContext, mark.getDate()));
            mSummary.setText(mark.getNote());

            Double val = (double) mark.getValue() / 100;
            if (val < 6) {
                mValue.setTextColor(Color.RED);
            }
            mValue.setText(String.format(Locale.ENGLISH, "%.2f", val));

            mView.setOnClickListener(v -> {
                new BoldAnalytics(mContext).log(FirebaseAnalytics.Event.VIEW_ITEM, "Mark");
                BottomSheetDialog sheet = new BottomSheetDialog(mContext);
                View bottomView = new ViewerDialog(mContext, sheet)
                        .setData(mark.getId(), true);
                sheet.setContentView(bottomView);
                sheet.show();

            });
        }
    }
}
