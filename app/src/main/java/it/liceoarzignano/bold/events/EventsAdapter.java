package it.liceoarzignano.bold.events;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.liceoarzignano.bold.R;

class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventHolder> {
    private final List<Event> mEvents;

    EventsAdapter(List<Event> mEvents) {
        this.mEvents = mEvents;
    }

    @Override
    public EventHolder onCreateViewHolder(ViewGroup mParent, int mType) {
        View mItem = LayoutInflater.from(mParent.getContext())
                .inflate(R.layout.item_event, mParent, false);

        return new EventHolder(mItem);
    }

    @Override
    public void onBindViewHolder(EventHolder mHolder, int mPosition) {
        Event mEvent = mEvents.get(mPosition);
        mHolder.setData(mEvent);
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    class EventHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mValue;
        private final ImageView mIcon;

        EventHolder(View mView) {
            super(mView);
            mTitle = (TextView) mView.findViewById(R.id.row_event_title);
            mValue = (TextView) mView.findViewById(R.id.row_event_value);
            mIcon = (ImageView) mView.findViewById(R.id.row_icon);
        }

        void setData(Event mEvent) {
            mTitle.setText(mEvent.getTitle());
            mValue.setText(mEvent.getDate());
            int mIconAddress;

            switch (mEvent.getIcon()) {
                case 0:
                    mIconAddress = R.drawable.ic_event_test;
                    break;
                case 1:
                    mIconAddress = R.drawable.ic_event_school;
                    break;
                case 2:
                    mIconAddress = R.drawable.ic_event_bday;
                    break;
                case 3:
                    mIconAddress = R.drawable.ic_event_homework;
                    break;
                case 4:
                    mIconAddress = R.drawable.ic_event_reminder;
                    break;
                case 5:
                    mIconAddress = R.drawable.ic_event_hangout;
                    break;
                default:
                    mIconAddress = R.drawable.ic_event_other;
                    break;
            }

            mIcon.setImageResource(mIconAddress);
        }
    }
}
