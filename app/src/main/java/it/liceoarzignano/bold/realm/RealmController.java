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
     *********
     * Marks *
     *********
     */

    public void clearAllMarks() {
        realm.beginTransaction();
        realm.delete(Mark.class);
        realm.commitTransaction();
    }


    public RealmResults<Mark> getAllMarks() {
        return realm.where(Mark.class).findAll();
    }

    public RealmResults<Mark> getFilteredMarks(String title) {
        return realm.where(Mark.class).equalTo("title", title).findAll();
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

    public void deleteMark(long id) {
        RealmResults<Mark> results = realm.where(Mark.class).equalTo("id", id).findAll();
        realm.beginTransaction();
        results.deleteAllFromRealm();
        realm.commitTransaction();
    }

    public double getAverage(String title) {
        List<Mark> marks = getFilteredMarks(title);
        double sum = 0;
        if (marks.size() > 0) {
            for (Mark mark : marks) {
                sum += mark.getValue();
            }
            sum /= 100;

            return sum / marks.size();
        } else {
            return 0;
        }
    }

    public double whatShouldIGet(String title) {
        double sum = 0;

        List<Mark> marks = getFilteredMarks(title);

        for (Mark markInList : marks) {
            sum += markInList.getValue();
        }
        sum /= 100;

        if (marks.size() > 0) {
            return 6 * (marks.size() + 1) - sum;
        } else {
            return 0;
        }
    }


    /*
     **********
     * Events *
     **********
     */

    public void clearAllEvents() {
        realm.beginTransaction();
        realm.delete(Event.class);
        realm.commitTransaction();
    }


    public RealmResults<Event> getAllEvents() {
        return realm.where(Event.class).findAllSorted("value", Sort.DESCENDING);
    }

    public RealmResults<Event> getAllEventsInverted() {
        return realm.where(Event.class).findAllSorted("value", Sort.ASCENDING);
    }

    public RealmResults<Event> getEventsById() {
        return realm.where(Event.class).findAll();
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
        oldEvent.setValue(event.getValue());
        oldEvent.setIcon(event.getIcon());
        realm.commitTransaction();
        return id;
    }

    public void deleteEvent(long id) {
        RealmResults<Event> results = realm.where(Event.class).equalTo("id", id).findAll();
        realm.beginTransaction();
        results.deleteAllFromRealm();
        realm.commitTransaction();
    }
}
