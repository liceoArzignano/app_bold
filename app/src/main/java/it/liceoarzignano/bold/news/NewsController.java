package it.liceoarzignano.bold.news;

import java.util.Calendar;

import io.realm.Case;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.realm.RealmController;

public class NewsController extends RealmController<News> {

    public NewsController(RealmConfiguration config) {
        super(config);
    }

    @Override
    public RealmResults<News> getAll() {
        return mRealm.where(News.class).findAllSorted("date", Sort.DESCENDING);
    }

    @Override
    public RealmResults<News> getById(long id) {
        return mRealm.where(News.class).equalTo("id", id).findAllSorted("date", Sort.ASCENDING);
    }

    @Override
    public long add(News news) {
        long id = Calendar.getInstance().getTimeInMillis();

        news.setId(id);
        mRealm.beginTransaction();
        mRealm.copyToRealm(news);
        mRealm.commitTransaction();
        return id;
    }

    @Override
    public long update(News news) {
        long id = news.getId();
        News old = getById(id).first();
        mRealm.beginTransaction();
        old.setTitle(news.getTitle());
        old.setDate(news.getDate());
        old.setMessage(news.getMessage());
        old.setUrl(news.getUrl());
        mRealm.commitTransaction();

        return id;
    }

    @Override
    public void delete(long id) {
        mRealm.beginTransaction();
        getById(id).first().deleteFromRealm();
        mRealm.commitTransaction();
    }

    RealmResults<News> getByQuery(String query) {
        return mRealm.where(News.class).contains("title", query, Case.INSENSITIVE)
                .or().contains("message", query, Case.INSENSITIVE)
                .findAllSorted("date", Sort.DESCENDING);
    }
}
