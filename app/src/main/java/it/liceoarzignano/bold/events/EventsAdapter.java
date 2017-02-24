package it.liceoarzignano.bold.events;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;

class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventHolder> {
    private final List<Event> mEvents;

    EventsAdapter(List<Event> mEvents) {
        this.mEvents = mEvents;
    }

    @Override
    public EventHolder onCreateViewHolder(ViewGroup parent, int type) {
        return new EventHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false));
    }

    @Override
    public void onBindViewHolder(EventHolder holder, int position) {
        holder.setData(mEvents.get(position));
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    class EventHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mValue;
        private final ImageView mIcon;

        EventHolder(View view) {
            super(view);
            mTitle = (TextView) view.findViewById(R.id.row_event_title);
            mValue = (TextView) view.findViewById(R.id.row_event_value);
            mIcon = (ImageView) view.findViewById(R.id.row_event_icon);
        }

        void setData(Event event) {
            mTitle.setText(event.getTitle());
            mValue.setText(Utils.dateToStr(event.getDate()));
            int iconAddress;

            switch (event.getIcon()) {
                case 0:
                    iconAddress = R.drawable.ic_event_test;
                    break;
                case 1:
                    iconAddress = R.drawable.ic_event_school;
                    break;
                case 2:
                    iconAddress = R.drawable.ic_event_bday;
                    break;
                case 3:
                    iconAddress = R.drawable.ic_event_homework;
                    break;
                case 4:
                    iconAddress = R.drawable.ic_event_reminder;
                    break;
                case 5:
                    iconAddress = R.drawable.ic_event_hangout;
                    break;
                default:
                    iconAddress = R.drawable.ic_event_other;
                    break;
            }

            mIcon.setImageResource(iconAddress);
        }
    }
}
