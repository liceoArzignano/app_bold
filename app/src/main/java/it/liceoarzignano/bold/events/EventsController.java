package it.liceoarzignano.bold.events;

import java.util.Calendar;

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

}
