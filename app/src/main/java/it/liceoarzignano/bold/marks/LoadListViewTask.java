package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmResults;
import it.liceoarzignano.bold.BoldApp;

class LoadListViewTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final ListView mMarksListView;
    private final String filter;

    public LoadListViewTask(Context context, ListView mMarksListView,
                            String filter) {
        this.context = context;
        this.mMarksListView = mMarksListView;
        this.filter = filter;
    }

    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }

    @Override
    protected void onPostExecute(Void arg0) {
        Realm realm = Realm.getInstance(BoldApp.getAppRealmConfiguration());
        RealmResults<Mark> marks = filter == null ? realm.where(Mark.class).findAll() :
                realm.where(Mark.class).equalTo("title", filter).findAll();
        ListArrayAdapter listArrayAdapter = new ListArrayAdapter(context,
                marks);
        if (mMarksListView != null) {
            mMarksListView.setAdapter(listArrayAdapter);
        }
    }
}