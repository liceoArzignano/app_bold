package it.liceoarzignano.bold.realm;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;

import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.news.News;

public class RealmController {
    private static RealmController sInstance;
    private final Realm mRealm;

    @SuppressWarnings("UnusedParameters")
    private RealmController(Application mApplication) {
        mRealm = Realm.getDefaultInstance();
    }

    public static RealmController with(Activity mActivity) {
        if (sInstance == null) {
            sInstance = new RealmController(mActivity.getApplication());
        }
        return sInstance;
    }

    public static RealmController with(Fragment mFragment) {
        if (sInstance == null) {
            sInstance = new RealmController(mFragment.getActivity().getApplication());
        }
        return sInstance;
    }

    public static RealmController with(Application mApplication) {
        if (sInstance == null) {
            sInstance = new RealmController(mApplication);
        }
        return sInstance;
    }

    public Realm getmRealm() {
        return mRealm;
    }

    /*
     * Marks
     */

    public RealmResults<Mark> getAllMarks() {
        return mRealm.where(Mark.class).findAllSorted("date", Sort.ASCENDING);
    }

    private RealmResults<Mark> getFilteredMarks(String mTitle, int mQuarter) {
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

    public Mark getMark(long mId) {
        return mRealm.where(Mark.class).equalTo("id", mId).findFirst();
    }

    public long addMark(Mark mMark) {
        long mId = Calendar.getInstance().getTimeInMillis();
        mMark.setId(mId);
        mRealm.beginTransaction();
        mRealm.copyToRealm(mMark);
        mRealm.commitTransaction();
        return mId;
    }

    public long updateMark(Mark mMark) {
        long mId = mMark.getId();

        Mark mOldMark = getMark(mId);
        mRealm.beginTransaction();
        mOldMark.setTitle(mMark.getTitle());
        mOldMark.setValue(mMark.getValue());
        mOldMark.setDate(mMark.getDate());
        mOldMark.setNote(mMark.getNote());
        mRealm.commitTransaction();
        return mId;
    }

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


    /*
     * Events
     */

    public RealmResults<Event> getAllEventsInverted() {
        return mRealm.where(Event.class).findAllSorted("date", Sort.ASCENDING);
    }

    public Event getEvent(long mId) {
        return mRealm.where(Event.class).equalTo("id", mId).findFirst();
    }

    public long addEvent(Event mEvent) {
        long mId = Calendar.getInstance().getTimeInMillis();

        mEvent.setId(mId);
        mRealm.beginTransaction();
        mRealm.copyToRealm(mEvent);
        mRealm.commitTransaction();
        return mId;
    }

    public long updateEvent(Event mEvent) {
        long mId = mEvent.getId();

        Event mOldEvent = getEvent(mId);
        mRealm.beginTransaction();
        mOldEvent.setTitle(mEvent.getTitle());
        mOldEvent.setDate(mEvent.getDate());
        mOldEvent.setIcon(mEvent.getIcon());
        mOldEvent.setNote(mEvent.getNote());
        mRealm.commitTransaction();
        return mId;
    }

    /*
     * News
     */
    public List<News> getAllNews() {
        return mRealm.where(News.class).findAllSorted("date", Sort.DESCENDING);
    }
}
