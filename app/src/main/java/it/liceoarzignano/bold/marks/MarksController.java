package it.liceoarzignano.bold.marks;

import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.List;

import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.realm.RealmController;

public class MarksController extends RealmController<Mark> {

    public MarksController(RealmConfiguration config) {
        super(config);
    }

    @Override
    public RealmResults<Mark> getAll() {
        return mRealm.where(Mark.class).findAllSorted("date", Sort.ASCENDING);
    }

    @Override
    public RealmResults<Mark> getById(long id) {
        return mRealm.where(Mark.class).equalTo("id", id).findAllSorted("date", Sort.ASCENDING);
    }

    @Override
    public long add(Mark mark) {
        long id = Calendar.getInstance().getTimeInMillis();

        mark.setId(id);
        mRealm.beginTransaction();
        mRealm.copyToRealm(mark);
        mRealm.commitTransaction();
        return id;
    }

    @Override
    public long update(Mark mark) {
        long id = mark.getId();
        Mark old = getById(id).first();
        mRealm.beginTransaction();
        old.setTitle(mark.getTitle());
        old.setNote(mark.getNote());
        old.setDate(mark.getDate(), mark.getIsFirstQuarter());
        old.setValue(mark.getValue());
        mRealm.commitTransaction();

        return id;
    }

    @Override
    public void delete(long id) {
        mRealm.beginTransaction();
        getById(id).first().deleteFromRealm();
        mRealm.commitTransaction();
    }

    /**
     * Fetch marks, filtered by subject and / or time
     *
     * @param filter subject name
     * @param quarter time filter
     * @return list of filtered marks
     */
    public RealmResults<Mark> getFilteredMarks(@Nullable String filter, int quarter) {
        if (filter == null || filter.isEmpty()) {
            switch (quarter) {
                case 1:
                    return mRealm.where(Mark.class).equalTo("isFirstQuarter", true)
                            .findAllSorted("date");
                case 2:
                    return mRealm.where(Mark.class).equalTo("isFirstQuarter", false)
                            .findAllSorted("date");
                default:
                    return mRealm.where(Mark.class).findAllSorted("date");
            }
        } else {
            switch (quarter) {
                case 1:
                    return mRealm.where(Mark.class).equalTo("title", filter)
                            .equalTo("isFirstQuarter", true).findAllSorted("date");
                case 2:
                    return mRealm.where(Mark.class).equalTo("title", filter)
                            .equalTo("isFirstQuarter", false).findAllSorted("date");
                default:
                    return mRealm.where(Mark.class).equalTo("title", filter).findAllSorted("date");
            }
        }
    }

    /**
     * Get average value, optionally filtered by subject and / or time
     *
     * @param filter subject name
     * @param quarter time filter
     * @return average mark
     */
    double getAverage(String filter, int quarter) {
        List<Mark> marks = getFilteredMarks(filter, quarter);
        double sum = 0;
        if (marks.isEmpty()) {
            return 0;
        } else {
            for (Mark mark : marks) {
                sum += mark.getValue();
            }
            sum /= 100;

            return sum / marks.size();
        }
    }

    /**
     * Get the next item needed to make average at least 6
     *
     * @param filter subject name
     * @param quarter time filter
     * @return next expected mark
     */
    double whatShouldIGet(String filter, int quarter) {
        double sum = 0;

        List<Mark> marks = getFilteredMarks(filter, quarter);
        for (Mark mark : marks) {
            sum += mark.getValue();
        }
        sum /= 100;

        return !marks.isEmpty() ? 6 * (marks.size() + 1) - sum : 0;
    }


}
