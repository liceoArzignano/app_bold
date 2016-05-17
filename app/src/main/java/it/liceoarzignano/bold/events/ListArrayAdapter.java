package it.liceoarzignano.bold.events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;

class ListArrayAdapter extends ArrayAdapter<Event> {

    private final Context context;
    private final int layoutResourceId;
    private final List<Event> events;

    public ListArrayAdapter(Context context,
                            @SuppressWarnings("SameParameterValue") int layoutResourceId,
                            List<Event> events) {
        super(context, layoutResourceId, events);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.events = events;
    }

    @Override
    public View getView(final int position, View row, ViewGroup parent) {
        EventViewHolder eventViewHolder;

        if (row == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(layoutResourceId, parent, false);

            eventViewHolder = new EventViewHolder();
            eventViewHolder.eventTitle = (TextView) row.findViewById(R.id.row_event_title);
            eventViewHolder.eventValue = (TextView) row.findViewById(R.id.row_event_value);
            eventViewHolder.eventIcon = (ImageView) row.findViewById(R.id.row_icon);

            row.setTag(eventViewHolder);
        } else {
            eventViewHolder = (EventViewHolder) row.getTag();
        }

        final Event event = events.get(position);
        eventViewHolder.eventTitle.setText(event.getTitle());
        eventViewHolder.eventValue.setText(event.getValue());

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
                eventViewHolder.eventIcon.setBackgroundResource(R.drawable.ic_event_hang_out);
                break;
            default:
                eventViewHolder.eventIcon.setBackgroundResource(R.drawable.ic_event_other);
                break;
        }
        if (!Utils.isTeacher(context) && event.getIcon() == 3) {
            eventViewHolder.eventIcon.setBackgroundResource(R.drawable.ic_event_other);
        }

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventListActivity.viewEvent(event.getId());
            }
        });

        return row;
    }

    static class EventViewHolder {
        TextView eventTitle;
        TextView eventValue;
        ImageView eventIcon;
    }
}
