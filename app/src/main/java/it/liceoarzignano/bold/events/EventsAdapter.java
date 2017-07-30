package it.liceoarzignano.bold.events;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhukic.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.Date;
import java.util.List;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.ui.recyclerview.HeaderViewHolder;
import it.liceoarzignano.bold.utils.ContentUtils;
import it.liceoarzignano.bold.utils.DateUtils;
import it.liceoarzignano.bold.utils.HelpToast;

class EventsAdapter extends SectionedRecyclerViewAdapter<HeaderViewHolder,
        EventsAdapter.EventHolder> {
    private List<Event2> mEvents;
    private final Context mContext;

    EventsAdapter(List<Event2> events, Context context) {
        mEvents = events;
        mContext = context;
    }

    @Override
    public EventHolder onCreateItemViewHolder(ViewGroup parent, int type) {
        return new EventHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false));
    }

    @Override
    public void onBindItemViewHolder(EventHolder holder, int position) {
        holder.setData(mEvents.get(position));
    }

    @Override
    public HeaderViewHolder onCreateSubheaderViewHolder(ViewGroup parent, int type) {
        return new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subheader, parent, false));
    }

    @Override
    public void onBindSubheaderViewHolder(HeaderViewHolder holder, int position) {
        String title;
        Date eventDate = new Date(mEvents.get(position).getDate());
        Date yesterday = DateUtils.getDate(-1);
        Date today = DateUtils.getDate(0);
        Date tomorrow = DateUtils.getDate(1);

        if (DateUtils.dateDiff(eventDate, yesterday) == 0) {
            title = mContext.getString(R.string.events_time_yesterday);
        } else if (DateUtils.dateDiff(eventDate, today) == 0) {
            title = mContext.getString(R.string.events_time_today);
        } else if (DateUtils.dateDiff(eventDate, tomorrow) == 0) {
            title = mContext.getString(R.string.events_time_tomorrow);
        } else {
            title = DateUtils.dateToWordsString(mContext, eventDate);
        }

        holder.setTitle(title);
    }

    @Override
    public int getItemSize() {
        return mEvents.size();
    }

    @Override
    public boolean onPlaceSubheaderBetweenItems(int itemPosition) {
        Date a = new Date(mEvents.get(itemPosition).getDate());
        Date b = new Date(mEvents.get(itemPosition + 1).getDate());

        return DateUtils.dateDiff(a, b) >= 1;
    }

    void updateList(List<Event2> newList) {
        mEvents = newList;
        notifyDataChanged();
    }

    class EventHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mTitle;
        private final TextView mValue;
        private final TextView mTag;

        EventHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.row_event_title);
            mValue = (TextView) view.findViewById(R.id.row_event_value);
            mTag = (TextView) view.findViewById(R.id.row_event_tag);
        }

        void setData(Event2 event) {
            mTitle.setText(event.getTitle());

            if (!TextUtils.isEmpty(event.getDescription())) {
                mValue.setText(event.getDescription());
                mValue.setVisibility(View.VISIBLE);
            }

            mTag.setText(ContentUtils.eventCategoryToString(mContext, event.getCategory()));
            mView.setOnClickListener(v -> {
                mValue.setMaxLines(mValue.getMaxLines() == 1
                        ? Integer.MAX_VALUE : 1);
                new HelpToast(mContext, R.string.intro_toast_event_long_press,
                        HelpToast.KEY_EVENT_LONG_PRESS);
            });
            mView.setOnLongClickListener(v -> ((EventListActivity) mContext).eventActions(event));
        }
    }
}
