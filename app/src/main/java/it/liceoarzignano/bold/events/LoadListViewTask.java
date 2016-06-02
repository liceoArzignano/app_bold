package it.liceoarzignano.bold.events;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import java.util.List;

import it.liceoarzignano.bold.R;

class LoadListViewTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final ListView mEventsListView;
    private List<Event> events;

    LoadListViewTask(Context applicationContext, ListView mEventsListView) {
        this.context = applicationContext;
        this.mEventsListView = mEventsListView;
    }

    @Override
    protected Void doInBackground(Void... params) {
        DatabaseConnection databaseConnection = DatabaseConnection.getInstance(context);
        events = databaseConnection.getAllEvents(true);
        return null;
    }

    @Override
    protected void onPostExecute(Void arg0) {
        ListArrayAdapter listArrayAdapter = new ListArrayAdapter(context,
                R.layout.item_event, events);
        mEventsListView.setAdapter(listArrayAdapter);
    }
}
