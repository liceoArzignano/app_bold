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
    public RealmResults<Event> getById(long mId) {
        return mRealm.where(Event.class).equalTo("id", mId).findAllSorted("date", Sort.ASCENDING);
    }

    @Override
    public long add(Event mEvent) {
        long mNewId = Calendar.getInstance().getTimeInMillis();

        mEvent.setId(mNewId);
        mRealm.beginTransaction();
        mRealm.copyToRealm(mEvent);
        mRealm.commitTransaction();
        return mNewId;
    }

    @Override
    public long update(Event mEvent) {
        long mId = mEvent.getId();
        Event mOld = getById(mId).first();
        mRealm.beginTransaction();
        mOld.setTitle(mEvent.getTitle());
        mOld.setNote(mEvent.getNote());
        mOld.setDate(mEvent.getDate());
        mOld.setIcon(mEvent.getIcon());
        mRealm.commitTransaction();

        return mId;
    }

    @Override
    public void delete(long mId) {
        mRealm.beginTransaction();
        getById(mId).first().deleteFromRealm();
        mRealm.commitTransaction();
    }

}
