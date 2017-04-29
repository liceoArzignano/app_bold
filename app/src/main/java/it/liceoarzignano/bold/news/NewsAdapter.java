package it.liceoarzignano.bold.news;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhukic.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.Date;
import java.util.List;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.ui.recyclerview.HeaderViewHolder;
import it.liceoarzignano.bold.utils.DateUtils;

class NewsAdapter extends SectionedRecyclerViewAdapter<HeaderViewHolder, NewsAdapter.NewsHolder> {
    private List<News> mNewsList;
    private final Context mContext;

    NewsAdapter(List<News> list, Context context) {
        this.mNewsList = list;
        this.mContext = context;
    }

    @Override
    public NewsHolder onCreateItemViewHolder(ViewGroup parent, int type) {
        return new NewsHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false));
    }

    @Override
    public void onBindItemViewHolder(NewsHolder holder, int position) {
        holder.setData(mContext, mNewsList.get(position));
    }

    @Override
    public HeaderViewHolder onCreateSubheaderViewHolder(ViewGroup parent, int type) {
        return new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subheader, parent, false));
    }

    @Override
    public void onBindSubheaderViewHolder(HeaderViewHolder holder, int position) {
        String title;
        Date eventDate = DateUtils.stringToDate(mNewsList.get(position).getDate());
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
        return mNewsList.size();
    }

    @Override
    public boolean onPlaceSubheaderBetweenItems(int itemPosition) {
        Date a = DateUtils.stringToDate(mNewsList.get(itemPosition).getDate());
        Date b = DateUtils.stringToDate(mNewsList.get(itemPosition + 1).getDate());

        return DateUtils.dateDiff(a, b) >= 1;
    }


    void updateList(List<News> list) {
        mNewsList = list;
        notifyDataChanged();
    }

    class NewsHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mTitle;
        private final TextView mMessage;

        NewsHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.row_news_title);
            mMessage = (TextView) view.findViewById(R.id.row_news_message);
        }

        void setData(final Context context, final News news) {
            mTitle.setText(news.getTitle());
            mMessage.setText(news.getMessage());

            final String url = news.getUrl();
            if (url != null && !url.isEmpty()) {
                mView.setOnClickListener(view -> ((NewsListActivity) context).showUrl(url));
            }

            mView.setOnLongClickListener(v -> ((NewsListActivity) mContext).newsActions(news));
        }
    }
}
