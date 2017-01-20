package it.liceoarzignano.bold.news;

import java.util.Calendar;

import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.realm.RealmController;

public class NewsController extends RealmController<News> {

    public NewsController(RealmConfiguration mConfig) {
        super(mConfig);
    }

    @Override
    public RealmResults<News> getAll() {
        return mRealm.where(News.class).findAllSorted("date", Sort.DESCENDING);
    }

    @Override
    public RealmResults<News> getById(long mId) {
        return mRealm.where(News.class).equalTo("id", mId).findAllSorted("date", Sort.ASCENDING);
    }

    @Override
    public long add(News mNews) {
        long mNewId = Calendar.getInstance().getTimeInMillis();

        mNews.setId(mNewId);
        mRealm.beginTransaction();
        mRealm.copyToRealm(mNews);
        mRealm.commitTransaction();
        return mNewId;
    }

    @Override
    public long update(News mNews) {
        long mId = mNews.getId();
        News mOld = getById(mId).first();
        mRealm.beginTransaction();
        mOld.setTitle(mNews.getTitle());
        mOld.setDate(mNews.getDate());
        mOld.setMessage(mNews.getMessage());
        mOld.setUrl(mNews.getUrl());
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
