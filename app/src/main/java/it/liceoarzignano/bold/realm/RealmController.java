package it.liceoarzignano.bold.realm;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

public abstract class RealmController<T extends RealmObject> {
    protected final Realm mRealm;

    /**
     * Default constructor
     *
     * @param mConfig realm configuration
     */
    protected RealmController(RealmConfiguration mConfig) {
        mRealm = Realm.getInstance(mConfig);
    }

    /**
     * Get all the items
     *
     * @return list of all the items
     */
    public RealmResults<T> getAll() {
        return null;
    }

    /**
     * Get items matching a given id
     *
     * @param mId item id
     * @return list of items with the given id
     */
    public RealmResults<T> getById(long mId) {
        return null;
    }

    /**
     * Add an object to the database
     *
     * @param mObject given object. Must extend RealmObject
     * @return new object id
     */
    public long add(T mObject) {
        return -1;
    }

    /**
     * Update an existing object in the database
     *
     * @param mObject given object. Must extend RealmObject
     * @return updated object id
     */
    public long update(T mObject) {
        return -1;
    }

    /**
     * Remove a given object from the database
     *
     * @param mId object id
     */
    public void delete(long mId) {
    }
}
