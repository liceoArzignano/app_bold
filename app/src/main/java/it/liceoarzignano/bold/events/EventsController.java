package it.liceoarzignano.bold.events;

import java.util.Calendar;

import io.realm.Case;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.realm.RealmController;

public class EventsController extends RealmController<Event> {

    public EventsController(RealmConfiguration mConfig) {
        super(mConfig);
    }

    @Override
    public RealmResults<Event> getAll() {
        return mRealm.where(Event.class).findAllSorted("date", Sort.ASCENDING);
    }

    @Override
    public RealmResults<Event> getById(long id) {
        return mRealm.where(Event.class).equalTo("id", id).findAllSorted("date", Sort.ASCENDING);
    }

    @Override
    public long add(Event event) {
        long id = Calendar.getInstance().getTimeInMillis();

        event.setId(id);
        mRealm.beginTransaction();
        mRealm.copyToRealm(event);
        mRealm.commitTransaction();
        return id;
    }

    @Override
    public long update(Event event) {
        long id = event.getId();
        Event old = getById(id).first();
        mRealm.beginTransaction();
        old.setTitle(event.getTitle());
        old.setNote(event.getNote());
        old.setDate(event.getDate());
        old.setIcon(event.getIcon());
        mRealm.commitTransaction();

        return id;
    }

    @Override
    public void delete(long id) {
        mRealm.beginTransaction();
        getById(id).first().deleteFromRealm();
        mRealm.commitTransaction();
    }

    public RealmResults<Event> getTomorrow() {
        Calendar tomorrow = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 23);
        tomorrow.set(Calendar.MINUTE, 59);
        tomorrow.set(Calendar.SECOND, 59);
        today.set(Calendar.HOUR_OF_DAY, 23);
        today.set(Calendar.MINUTE, 59);
        today.set(Calendar.SECOND, 59);

        return mRealm.where(Event.class).between("date", today.getTime(), tomorrow.getTime())
                .findAll();
    }

    RealmResults<Event> getByQuery(String query) {
        return query != null && !query.isEmpty() ?
                mRealm.where(Event.class)
                        .contains("title", query, Case.INSENSITIVE)
                        .findAllSorted("date", Sort.DESCENDING) :
                mRealm.where(Event.class)
                        .findAllSorted("date", Sort.DESCENDING);
    }

}
