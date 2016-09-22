package it.liceoarzignano.bold.events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;

class ListArrayAdapter extends RealmBaseAdapter<Event> {

    private final Context context;
    private final int layoutResourceId;
    private final RealmResults<Event> events;

    ListArrayAdapter(Context context,
                     RealmResults<Event> events) {
        super(context, events);
        this.context = context;
        layoutResourceId = R.layout.item_event;
        this.events = events;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        ListArrayAdapter.EventViewHolder eventViewHolder;

        if (row == null) {
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(layoutResourceId, parent, false);

            eventViewHolder = new ListArrayAdapter.EventViewHolder();
            eventViewHolder.eventTitle = (TextView) row.findViewById(R.id.row_event_title);
            eventViewHolder.eventValue = (TextView) row.findViewById(R.id.row_event_value);
            eventViewHolder.eventIcon = (ImageView) row.findViewById(R.id.row_icon);

            row.setTag(eventViewHolder);
        } else {
            eventViewHolder = (ListArrayAdapter.EventViewHolder) row.getTag();
        }

        final Event event = events.get(position);
        eventViewHolder.eventTitle.setText(event.getTitle());
        eventViewHolder.eventValue.setText(event.getDate());

        switch (event.getIcon()) {
            case 0:
                eventViewHolder.eventIcon.setBackgroundResource(R.drawable.ic_event_test);
                break;
            case 1:
                eventViewHolder.eventIcon.setBackgroundResource(R.drawable.ic_event_school);
                break;
            case 2:
                eventViewHolder.eventIcon.setBackgroundResource(R.drawable.ic_event_bday);
                break;
            case 3:
                eventViewHolder.eventIcon.setBackgroundResource(R.drawable.ic_event_homework);
                break;
            case 4:
                eventViewHolder.eventIcon.setBackgroundResource(R.drawable.ic_event_reminder);
                break;
            case 5:
                eventViewHolder.eventIcon.setBackgroundResource(R.drawable.ic_event_hangout);
                break;
            default:
                eventViewHolder.eventIcon.setBackgroundResource(R.drawable.ic_event_other);
                break;
        }

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventListActivity.viewEvent(event.getId());
            }
        });

        return row;
    }

    private static class EventViewHolder {
        TextView eventTitle;
        TextView eventValue;
        ImageView eventIcon;
    }
}
