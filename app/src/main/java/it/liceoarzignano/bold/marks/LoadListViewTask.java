package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import java.util.List;

import it.liceoarzignano.bold.R;

class LoadListViewTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final ListView mMarksListView;
    private final String filter;
    private List<Mark> marks;

    public LoadListViewTask(Context applicationContext, ListView mMarksListView, String filter) {
        this.context = applicationContext;
        this.mMarksListView = mMarksListView;
        this.filter = filter;
    }

    @Override
    protected Void doInBackground(Void... params) {
        DatabaseConnection databaseConnectionMark = DatabaseConnection.getInstance(context);
        if (filter == null) {
            marks = databaseConnectionMark.getAllMarks();
        } else {
            marks = databaseConnectionMark.getFilteredMarks(filter);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void arg0) {
        ListArrayAdapter listArrayAdapter = new ListArrayAdapter(context,
                R.layout.item_mark, marks);
        if (mMarksListView != null)
            mMarksListView.setAdapter(listArrayAdapter);
    }
}