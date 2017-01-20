package it.liceoarzignano.bold.marks;

import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.List;

import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.realm.RealmController;

public class MarksController extends RealmController<Mark> {

    public MarksController(RealmConfiguration mConfig) {
        super(mConfig);
    }

    @Override
    public RealmResults<Mark> getAll() {
        return mRealm.where(Mark.class).findAllSorted("date", Sort.ASCENDING);
    }

    @Override
    public RealmResults<Mark> getById(long mId) {
        return mRealm.where(Mark.class).equalTo("id", mId).findAllSorted("date", Sort.ASCENDING);
    }

    @Override
    public long add(Mark mMark) {
        long mNewId = Calendar.getInstance().getTimeInMillis();

        mMark.setId(mNewId);
        mRealm.beginTransaction();
        mRealm.copyToRealm(mMark);
        mRealm.commitTransaction();
        return mNewId;
    }

    @Override
    public long update(Mark mMark) {
        long mId = mMark.getId();
        Mark mOld = getById(mId).first();
        mRealm.beginTransaction();
        mOld.setTitle(mMark.getTitle());
        mOld.setNote(mMark.getNote());
        mOld.setDate(mMark.getDate(), mMark.getIsFirstQuarter());
        mOld.setValue(mMark.getValue());
        mRealm.commitTransaction();

        return mId;
    }

    @Override
    public void delete(long mId) {
        mRealm.beginTransaction();
        getById(mId).first().deleteFromRealm();
        mRealm.commitTransaction();
    }

    /**
     * Fetch marks, filtered by subject and / or time
     *
     * @param mTitle subject name
     * @param mQuarter time filter
     * @return list of filtered marks
     */
    public RealmResults<Mark> getFilteredMarks(@Nullable String mTitle, int mQuarter) {
        if (mTitle == null || mTitle.isEmpty()) {
            switch (mQuarter) {
                case 1:
                    return mRealm.where(Mark.class).equalTo("isFirstQuarter", true)
                            .findAll();
                case 2:
                    return mRealm.where(Mark.class).equalTo("isFirstQuarter", false)
                            .findAll();
                default:
                    return mRealm.where(Mark.class).findAll();
            }
        } else {
            switch (mQuarter) {
                case 1:
                    return mRealm.where(Mark.class).equalTo("title", mTitle)
                            .equalTo("isFirstQuarter", true).findAll();
                case 2:
                    return mRealm.where(Mark.class).equalTo("title", mTitle)
                            .equalTo("isFirstQuarter", false).findAll();
                default:
                    return mRealm.where(Mark.class).equalTo("title", mTitle).findAll();
            }
        }
    }

    /**
     * Get average value, optionally filtered by subject and / or time
     *
     * @param mTitle subject name
     * @param mQuarter time filter
     * @return average mark
     */
    public double getAverage(String mTitle, int mQuarter) {
        List<Mark> mMarks = getFilteredMarks(mTitle, mQuarter);
        double mSum = 0;
        if (mMarks.isEmpty()) {
            return 0;
        } else {
            for (Mark mark : mMarks) {
                mSum += mark.getValue();
            }
            mSum /= 100;

            return mSum / mMarks.size();
        }
    }

    public double whatShouldIGet(String mTitle, int mQuarter) {
        double mSum = 0;

        List<Mark> mMarks = getFilteredMarks(mTitle, mQuarter);
        for (Mark mMark : mMarks) {
            mSum += mMark.getValue();
        }
        mSum /= 100;

        return !mMarks.isEmpty() ? 6 * (mMarks.size() + 1) - mSum : 0;
    }


}
