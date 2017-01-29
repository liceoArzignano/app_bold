package it.liceoarzignano.bold.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventListActivity;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.SubjectActivity;

public class ViewerDialog {
    private final Realm mRealm;
    private final Context mContext;

    private final View mView;
    private final BottomSheetDialog mThisDialog;

    private final Toolbar mToolbar;
    private final Button mEditButton;
    private final Button mShareButton;
    private final Button mDeleteButton;
    private final TextView mValueTextView;
    private final TextView mValueTitle;
    private final TextView mDateTextView;
    private final TextView mNotesTexView;

    private Mark mMark = new Mark();
    private Event mEvent = new Event();

    @SuppressLint("InflateParams")
    public ViewerDialog(Context mContext, BottomSheetDialog mThisDialog) {
        this.mContext = mContext;
        this.mThisDialog = mThisDialog;

        LayoutInflater mInflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(R.layout.dialog_viewer, null);

        mToolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        mEditButton = (Button) mView.findViewById(R.id.viewer_btn_edit);
        mShareButton = (Button) mView.findViewById(R.id.viewer_btn_share);
        mDeleteButton = (Button) mView.findViewById(R.id.viewer_btn_delete);

        mValueTextView = (TextView) mView.findViewById(R.id.viewer_value);
        mValueTitle = (TextView) mView.findViewById(R.id.viewer_value_title);
        mDateTextView = (TextView) mView.findViewById(R.id.viewer_dates);
        mNotesTexView = (TextView) mView.findViewById(R.id.viewer_notes);

        mRealm = Realm.getInstance(((BoldApp) mContext.getApplicationContext()).getConfig());
    }

    /**
     * Set ui basing on Event / Mark information
     *
     * @param mId    object id
     * @param isMark is the object a mark
     * @return this view
     */
    public View setData(final long mId, final boolean isMark) {
        if (isMark) {
            mMark = mRealm.where(Mark.class).equalTo("id", mId).findFirst();
        } else {
            mEvent = mRealm.where(Event.class).equalTo("id", mId).findFirst();
        }

        // UI
        final String mTitle = isMark ? mMark.getTitle() : mEvent.getTitle();

        if (!mTitle.isEmpty() && mToolbar != null) {
            mToolbar.setTitle(mTitle);
        }

        StringBuilder mNotes = new StringBuilder();

        if (isMark) {
            mNotes.append(mMark.getNote());
            mNotes.append('\n');
            mNotes.append(mContext.getString(mMark.getIsFirstQuarter() ?
                    R.string.viewer_first_quarter : R.string.viewer_second_quarter));
            mValueTitle.setText(mContext.getString(R.string.viewer_values));
            mValueTextView.setText(String.format(Locale.ENGLISH, "%.2f",
                    (double) (mMark.getValue() / 100d)));
        } else {
            mNotes.append(mEvent.getNote());
            mValueTitle.setText(mContext.getString(R.string.viewer_category));
            mValueTextView.setText(Utils.eventCategoryToString(mContext, mEvent.getIcon()));
        }

        mNotesTexView.setText(mNotes.toString());
        mDateTextView.setText(Utils.dateToStr(isMark ? mMark.getDate() : mEvent.getDate()));

        mShareButton.setOnClickListener(v -> {
            String msg = isMark ?
                    String.format(mContext.getString(Utils.isTeacher(mContext) ?
                                    R.string.viewer_share_teacher : R.string.viewer_share_student),
                            mValueTextView.getText(), mTitle) :
                    String.format("%1$s (%2$s)\n%3$s", mTitle, mValueTextView.getText(),
                            mNotesTexView.getText());

            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, msg);

            if (Utils.isNotLegacy()) {
                ((AnimatedVectorDrawable) mShareButton.getCompoundDrawables()[1]).start();
                new Handler().postDelayed(() -> {
                    mThisDialog.dismiss();
                    mContext.startActivity(Intent.createChooser(shareIntent,
                            mContext.getString(R.string.viewer_share)));
                }, 1000);
            } else {
                mThisDialog.dismiss();
                mContext.startActivity(Intent.createChooser(shareIntent,
                        mContext.getString(R.string.viewer_share)));
            }
        });

        mDeleteButton.setOnClickListener(v -> {
            if (Utils.isNotLegacy()) {
                ((AnimatedVectorDrawable) mDeleteButton.getCompoundDrawables()[1]).start();
            }

            if (isMark) {
                RealmResults<Mark> mResults =
                        mRealm.where(Mark.class).equalTo("id", mId).findAll();
                mRealm.beginTransaction();
                mResults.deleteAllFromRealm();
                mRealm.commitTransaction();
            } else {
                RealmResults<Event> mResults =
                        mRealm.where(Event.class).equalTo("id", mId).findAll();
                mRealm.beginTransaction();
                mResults.deleteAllFromRealm();
                mRealm.commitTransaction();
            }

            Snackbar.make(v, mContext.getString(R.string.removed),
                    Snackbar.LENGTH_SHORT).show();
            if (isMark) {
                ((SubjectActivity) mContext).refresh();
            } else {
                ((EventListActivity) mContext).refreshList(mContext, null);
            }
            new Handler().postDelayed(mThisDialog::dismiss, 840);
        });

        mEditButton.setOnClickListener(view -> {
            final Intent editIntent = new Intent(mContext, ManagerActivity.class);

            editIntent.putExtra("isEditing", true);
            editIntent.putExtra("isMark", isMark);
            editIntent.putExtra("id", mId);

            int mTime = 0;
            if (Utils.isNotLegacy()) {
                ((AnimatedVectorDrawable) mEditButton.getCompoundDrawables()[1]).start();
                new Handler().postDelayed(mThisDialog::dismiss, mTime += 1000);
            } else {
                mThisDialog.dismiss();
            }

            new Handler().postDelayed(() -> mContext.startActivity(editIntent), mTime + 40);
        });

        return mView;
    }
}
