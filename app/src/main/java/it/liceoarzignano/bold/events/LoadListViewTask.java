package it.liceoarzignano.bold.events;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.BoldApp;

class LoadListViewTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final ListView mEventsListView;

    LoadListViewTask(Context applicationContext, ListView mEventsListView) {
        context = applicationContext;
        this.mEventsListView = mEventsListView;
    }

    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }

    @Override
    protected void onPostExecute(Void arg0) {
        Realm realm = Realm.getInstance(BoldApp.getAppRealmConfiguration());
        RealmResults<Event> events =
                realm.where(Event.class).findAllSorted("date", Sort.DESCENDING);
        ListArrayAdapter listArrayAdapter = new ListArrayAdapter(context, events);
        mEventsListView.setAdapter(listArrayAdapter);
    }
}
