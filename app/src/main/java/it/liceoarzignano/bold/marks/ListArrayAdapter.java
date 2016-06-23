package it.liceoarzignano.bold.marks;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;
import it.liceoarzignano.bold.R;

class ListArrayAdapter extends RealmBaseAdapter<Mark> {

    private final Context context;
    private final int layoutResourceId;
    private final RealmResults<Mark> marks;


    public ListArrayAdapter(Context context, int layoutResourceId,
                            RealmResults<Mark> marks) {
        super(context, marks);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.marks = marks;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        MarkViewHolder markViewHolder;

        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(layoutResourceId, parent, false);

            markViewHolder = new MarkViewHolder();
            markViewHolder.markTitle = (TextView) row.findViewById(R.id.row_mark_title);
            markViewHolder.markValue = (TextView) row.findViewById(R.id.row_mark_value);
            markViewHolder.markNotes = (TextView) row.findViewById(R.id.row_mark_notes);

            row.setTag(markViewHolder);
        } else {
            markViewHolder = (MarkViewHolder) row.getTag();
        }

        final Mark mark = marks.get(position);
        markViewHolder.markTitle.setText(mark.getTitle());
        markViewHolder.markNotes.setText(mark.getContent());

        Double doubleValue = ((double) mark.getValue()) / 100;
        String mValue = Double.toString(doubleValue);
        if (doubleValue < 6) {
            markViewHolder.markValue.setTextColor(Color.RED);
        } else {
            markViewHolder.markValue.setTextColor(Color.BLACK);
        }
        markViewHolder.markValue.setText(mValue);

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MarkListActivity.viewMark(mark.getId());
            }
        });

        return row;
    }

    static class MarkViewHolder {
        TextView markTitle;
        TextView markValue;
        TextView markNotes;
    }
}
