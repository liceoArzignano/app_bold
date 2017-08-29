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
import it.liceoarzignano.bold.utils.HelpToast;

class NewsAdapter extends SectionedRecyclerViewAdapter<HeaderViewHolder, NewsAdapter.NewsHolder> {
    private List<News2> mNewsList;
    private final Context mContext;

    NewsAdapter(List<News2> list, Context context) {
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
        Date eventDate = new Date(mNewsList.get(position).getDate());
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
        Date a = new Date(mNewsList.get(itemPosition).getDate());
        Date b = new Date(mNewsList.get(itemPosition + 1).getDate());

        return DateUtils.dateDiff(a, b) >= 1;
    }


    void updateList(List<News2> list) {
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
            mTitle = view.findViewById(R.id.row_news_title);
            mMessage = view.findViewById(R.id.row_news_message);
        }

        void setData(final Context context, final News2 news) {
            mTitle.setText(news.getTitle());
            mMessage.setText(news.getDescription());

            final String url = news.getUrl();
            mView.setOnClickListener(view -> {
                if (url != null && !url.isEmpty()) {
                    ((NewsListActivity) context).showUrl(url);
                }
                new HelpToast(mContext, R.string.intro_toast_news_long_press,
                        HelpToast.KEY_NEWS_LONG_PRESS);
            });

            mView.setOnLongClickListener(v -> ((NewsListActivity) mContext).newsActions(news));
        }
    }
}
