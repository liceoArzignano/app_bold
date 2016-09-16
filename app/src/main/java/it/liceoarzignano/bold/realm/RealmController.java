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

public class RealmController {
    private static RealmController instance;
    private final Realm realm;

    @SuppressWarnings("UnusedParameters")
    private RealmController(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static RealmController with(Activity activity) {
        if (instance == null) {
            instance = new RealmController(activity.getApplication());
        }
        return instance;
    }

    public static RealmController with(Fragment fragment) {
        if (instance == null) {
            instance = new RealmController(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static RealmController with(Application application) {
        if (instance == null) {
            instance = new RealmController(application);
        }
        return instance;
    }

    public Realm getRealm() {
        return realm;
    }

    /*
     * Marks
     */

    public RealmResults<Mark> getAllMarks() {
        return realm.where(Mark.class).findAllSorted("date", Sort.ASCENDING);
    }

    private RealmResults<Mark> getFilteredMarks(String title, int quarter) {
        switch (quarter) {
            case 1:
                return realm.where(Mark.class).equalTo("title", title)
                        .equalTo("isFirstQuarter", true).findAll();
            case 2:
                return realm.where(Mark.class).equalTo("title", title)
                        .equalTo("isFirstQuarter", false).findAll();
            default:
                return realm.where(Mark.class).equalTo("title", title).findAll();
        }
    }

    public Mark getMark(long id) {
        return realm.where(Mark.class).equalTo("id", id).findFirst();
    }

    public long addMark(Mark mark) {
        long id = Calendar.getInstance().getTimeInMillis();
        mark.setId(id);
        realm.beginTransaction();
        realm.copyToRealm(mark);
        realm.commitTransaction();
        return id;
    }

    public long updateMark(Mark mark) {
        long id = mark.getId();

        Mark oldMark = getMark(id);
        realm.beginTransaction();
        oldMark.setTitle(mark.getTitle());
        oldMark.setValue(mark.getValue());
        oldMark.setContent(mark.getContent());
        realm.commitTransaction();
        return id;
    }

    public double getAverage(String title, int quarter) {
        List<Mark> marks = getFilteredMarks(title, quarter);
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

    public double whatShouldIGet(String title, int quarter) {
        double sum = 0;

        List<Mark> marks = getFilteredMarks(title, quarter);

        for (Mark markInList : marks) {
            sum += markInList.getValue();
        }
        sum /= 100;

        return !marks.isEmpty() ? 6 * (marks.size() + 1) - sum : 0;
    }


    /*
     * Events
     */

    public RealmResults<Event> getAllEventsInverted() {
        return realm.where(Event.class).findAllSorted("date", Sort.ASCENDING);
    }

    public Event getEvent(long id) {
        return realm.where(Event.class).equalTo("id", id).findFirst();
    }

    public long addEvent(Event event) {
        long id = Calendar.getInstance().getTimeInMillis();

        event.setId(id);
        realm.beginTransaction();
        realm.copyToRealm(event);
        realm.commitTransaction();
        return id;
    }

    public long updateEvent(Event event) {
        long id = event.getId();

        Event oldEvent = getEvent(id);
        realm.beginTransaction();
        oldEvent.setTitle(event.getTitle());
        oldEvent.setDate(event.getDate());
        oldEvent.setIcon(event.getIcon());
        realm.commitTransaction();
        return id;
    }
}
