package it.liceoarzignano.bold.marks;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;
import java.util.Locale;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.utils.DateUtils;
import it.liceoarzignano.bold.utils.UiUtils;

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
        private final ImageView mExpand;
        private final View mActions;
        private final ImageButton mShare;
        private final ImageButton mEdit;
        private final ImageButton mDelete;

        SubjectHolder(View view) {
            super(view);
            mView = view.findViewById(R.id.row_mark_root);
            mValue = (TextView) view.findViewById(R.id.row_mark_value);
            mDate = (TextView) view.findViewById(R.id.row_mark_date);
            mSummary = (TextView) view.findViewById(R.id.row_mark_summary);
            mExpand = (ImageView) view.findViewById(R.id.row_mark_expand);
            mActions = view.findViewById(R.id.row_mark_actions);
            mShare = (ImageButton) view.findViewById(R.id.row_mark_share);
            mEdit = (ImageButton) view.findViewById(R.id.row_mark_edit);
            mDelete = (ImageButton) view.findViewById(R.id.row_mark_delete);
        }

        void setData(Mark mark) {
            // Title
            mDate.setText(DateUtils.dateToWorldsString(mContext, mark.getDate()));

            // Value
            Double val = (double) mark.getValue() / 100;
            if (val < 6) {
                mValue.setTextColor(Color.RED);
            }
            mValue.setText(String.format(Locale.ENGLISH, "%.2f", val));

            // Summary
            String summary = mark.getNote();
            boolean hasSummary = summary != null && !summary.isEmpty();
            if (hasSummary) {
                mSummary.setText(summary);
            }

            // Actions
            mShare.setOnClickListener(v -> ((SubjectActivity) mContext).shareItem(mShare, mark));
            mEdit.setOnClickListener(v -> ((SubjectActivity) mContext).editItem(mEdit, mark));
            mDelete.setOnClickListener(v -> ((SubjectActivity) mContext).deleteItem(mDelete,
                    mark, getLayoutPosition()));

            mView.setOnClickListener(v -> {
                new BoldAnalytics(mContext).log(FirebaseAnalytics.Event.VIEW_ITEM, "Mark");
                boolean shouldExpand = mActions.getVisibility() == View.GONE;
                float elevation = UiUtils.dpToPx(mContext.getResources(), 4);
                ValueAnimator animator = ValueAnimator.ofFloat(shouldExpand ? 0 : elevation,
                        shouldExpand ? elevation : 0);
                animator.setDuration(350);
                animator.addUpdateListener(valueAnimator -> {
                    float progress = (float) valueAnimator.getAnimatedValue();
                    ViewCompat.setElevation(mView, progress);
                    mExpand.setRotation(progress * 180 / elevation);
                    mSummary.setAlpha(progress / elevation);
                    mActions.setAlpha(progress / elevation);
                });
                animator.start();

                new Handler().postDelayed(() -> {
                    if (hasSummary) {
                        mSummary.setVisibility(shouldExpand ? View.VISIBLE : View.GONE);
                    }
                    mActions.setVisibility(shouldExpand ? View.VISIBLE : View.GONE);
                    mView.setBackgroundColor(ContextCompat.getColor(mContext, shouldExpand ?
                            R.color.cardview_light_background : R.color.white));
                }, 250);
            });
        }
    }
}
