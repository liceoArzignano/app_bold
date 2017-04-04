package it.liceoarzignano.bold.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventListActivity;
import it.liceoarzignano.bold.firebase.BoldAnalytics;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.SubjectActivity;

public class ViewerDialog {
    private final Realm mRealm;
    private final Context mContext;

    private final View mView;
    private final BottomSheetDialog mDialog;

    private final LinearLayout mEditLayout;
    private final LinearLayout mShareLayout;
    private final LinearLayout mRemoveLayout;

    private final TextView mTitleText;
    private final TextView mValueText;
    private final TextView mDateText;
    private final TextView mNotesText;

    private final ImageView mValueIcon;
    private final ImageView mEditIcon;
    private final ImageView mShareIcon;
    private final ImageView mRemoveIcon;

    private Mark mMark = new Mark();
    private Event mEvent = new Event();

    @SuppressLint("InflateParams")
    public ViewerDialog(Context context, BottomSheetDialog dialog) {
        this.mContext = context;
        this.mDialog = dialog;

        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.dialog_viewer, null);

        mEditLayout = (LinearLayout) mView.findViewById(R.id.viewer_edit_layout);
        mShareLayout = (LinearLayout) mView.findViewById(R.id.viewer_share_layout);
        mRemoveLayout = (LinearLayout) mView.findViewById(R.id.viewer_remove_layout);

        mTitleText = (TextView) mView.findViewById(R.id.viewer_title);
        mValueText = (TextView) mView.findViewById(R.id.viewer_value);
        mDateText = (TextView) mView.findViewById(R.id.viewer_date);
        mNotesText = (TextView) mView.findViewById(R.id.viewer_notes);

        mValueIcon = (ImageView) mView.findViewById(R.id.viewer_value_icon);
        mEditIcon = (ImageView) mView.findViewById(R.id.viewer_edit_icon);
        mShareIcon = (ImageView) mView.findViewById(R.id.viewer_share_icon);
        mRemoveIcon = (ImageView) mView.findViewById(R.id.viewer_remove_icon);

        mRealm = Realm.getInstance(((BoldApp) context.getApplicationContext()).getConfig());
    }

    /**
     * Set ui basing on Event / Mark information
     *
     * @param id    object id
     * @param isMark is the object a mark
     * @return this view
     */
    public View setData(final long id, final boolean isMark) {
        if (isMark) {
            mMark = mRealm.where(Mark.class).equalTo("id", id).findFirst();
        } else {
            mEvent = mRealm.where(Event.class).equalTo("id", id).findFirst();
        }

        // UI
        mTitleText.setText(isMark ? mMark.getTitle() : mEvent.getTitle());

        if (isMark) {
            StringBuilder notes = new StringBuilder();
            notes.append(mMark.getNote());
            if (mMark.getNote() != null && !mMark.getNote().isEmpty()) {
                notes.append('\n');
            }
            notes.append(mContext.getString(mMark.getIsFirstQuarter() ?
                    R.string.viewer_first_quarter : R.string.viewer_second_quarter));
            mNotesText.setText(notes.toString());

            mValueText.setText(String.format(Locale.ENGLISH, "%.2f",
                    (double) (mMark.getValue() / 100d)));
            mValueIcon.setImageResource(R.drawable.ic_trophy);
        } else {
            mNotesText.setText(mEvent.getNote());
            mValueText.setText(Utils.eventCategoryToString(mContext, mEvent.getIcon()));
            mValueIcon.setImageResource(R.drawable.ic_category);
        }

        mDateText.setText(Utils.dateToStr(isMark ? mMark.getDate() : mEvent.getDate()));

        mShareLayout.setOnClickListener(view -> {
            new BoldAnalytics(mContext).log(FirebaseAnalytics.Event.SHARE,
                    FirebaseAnalytics.Param.ITEM_NAME, isMark ? "Share mark" : "Share event");
            String msg = isMark ?
                    String.format(mContext.getString(Utils.isTeacher(mContext) ?
                                    R.string.viewer_share_teacher : R.string.viewer_share_student),
                            mValueText.getText(), mTitleText.getText()) :
                    String.format("%1$s (%2$s)\n%3$s", mTitleText.getText(),
                            mValueText.getText(), mNotesText.getText());

            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, msg);

            if (Utils.isNotLegacy()) {
                Utils.animateAVD(mShareIcon.getDrawable());
                new Handler().postDelayed(() -> {
                    mDialog.dismiss();
                    mContext.startActivity(Intent.createChooser(shareIntent,
                            mContext.getString(R.string.viewer_share)));
                }, 1000);
            } else {
                mDialog.dismiss();
                mContext.startActivity(Intent.createChooser(shareIntent,
                        mContext.getString(R.string.viewer_share)));
            }
        });

        mRemoveLayout.setOnClickListener(view -> {
            new BoldAnalytics(mContext).log(FirebaseAnalytics.Event.VIEW_ITEM,
                    FirebaseAnalytics.Param.ITEM_NAME, isMark ? "Delete mark" : "Delete event");
            Utils.animateAVD(mRemoveIcon.getDrawable());

            if (isMark) {
                RealmResults<Mark> results =
                        mRealm.where(Mark.class).equalTo("id", id).findAll();
                mRealm.beginTransaction();
                results.deleteAllFromRealm();
                mRealm.commitTransaction();
            } else {
                RealmResults<Event> results =
                        mRealm.where(Event.class).equalTo("id", id).findAll();
                mRealm.beginTransaction();
                results.deleteAllFromRealm();
                mRealm.commitTransaction();
            }

            Snackbar.make(view, mContext.getString(R.string.removed),
                    Snackbar.LENGTH_SHORT).show();
            if (isMark) {
                ((SubjectActivity) mContext).refresh();
            } else {
                ((EventListActivity) mContext).refreshList(mContext, new Date(), null);
            }
            new Handler().postDelayed(mDialog::dismiss, 840);
        });

        mEditLayout.setOnClickListener(view -> {
            new BoldAnalytics(mContext).log(FirebaseAnalytics.Event.VIEW_ITEM,
                    FirebaseAnalytics.Param.ITEM_NAME, isMark ? "Edit mark" : "Edit event");
            final Intent editIntent = new Intent(mContext, ManagerActivity.class);

            editIntent.putExtra("isEditing", true);
            editIntent.putExtra("isMark", isMark);
            editIntent.putExtra("id", id);

            int time = 0;
            if (Utils.isNotLegacy()) {
                Utils.animateAVD(mEditIcon.getDrawable());
                time += 1000;
                new Handler().postDelayed(mDialog::dismiss, time);
            } else {
                mDialog.dismiss();
            }

            new Handler().postDelayed(() -> mContext.startActivity(editIntent), time + 40);
        });

        return mView;
    }
}
