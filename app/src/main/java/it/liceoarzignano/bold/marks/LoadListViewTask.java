package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import it.liceoarzignano.bold.BoldApp;

class LoadListViewTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final ListView mMarksListView;
    private final String filter;
    private int quarter = 1;

    public LoadListViewTask(Context context, ListView mMarksListView,
                            Pair<String, Integer> filter) {
        this.context = context;
        this.mMarksListView = mMarksListView;
        this.filter = filter.first;
        quarter = filter.second;
    }

    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }

    @Override
    protected void onPostExecute(Void arg0) {
        Realm realm = Realm.getInstance(BoldApp.getAppRealmConfiguration());

        RealmResults<Mark> marks;
        if (filter == null) {
            switch (quarter) {
                case 1:
                    marks = realm.where(Mark.class).equalTo("isFirstQuarter", true).findAll();
                    break;
                case 2:
                    marks = realm.where(Mark.class).equalTo("isFirstQuarter", false).findAll();
                    break;
                default:
                    marks = realm.where(Mark.class).findAll();
                    break;
            }
        } else {
            switch (quarter) {
                case 1:
                    marks = realm.where(Mark.class).equalTo("title", filter)
                            .equalTo("isFirstQuarter", true).findAll();
                    break;
                case 2:
                    marks = realm.where(Mark.class).equalTo("title", filter)
                            .equalTo("isFirstQuarter", false).findAll();
                    break;
                default:
                    marks = realm.where(Mark.class).equalTo("title", filter).findAll();
                    break;
            }
        }
        ListArrayAdapter listArrayAdapter = new ListArrayAdapter(context,
                marks.sort("date", Sort.DESCENDING));
        if (mMarksListView != null) {
            mMarksListView.setAdapter(listArrayAdapter);
        }
    }
}