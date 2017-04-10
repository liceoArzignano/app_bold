package it.liceoarzignano.bold.news;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import it.liceoarzignano.bold.R;

class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder> {
    private List<News> mNewsList;
    private final Activity mActivity;

    NewsAdapter(List<News> list, Activity activity) {
        this.mNewsList = list;
        this.mActivity = activity;
    }

    @Override
    public NewsHolder onCreateViewHolder(ViewGroup parent, int type) {
        return new NewsHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false));
    }

    @Override
    public void onBindViewHolder(NewsHolder holder, int position) {
        holder.setData(mActivity, mNewsList.get(position));
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }

    void updateList(List<News> list) {
        mNewsList = list;
    }

    class NewsHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mMessage;
        private final ImageButton mUrlButton;

        NewsHolder(View view) {
            super(view);
            mTitle = (TextView) view.findViewById(R.id.row_news_title);
            mMessage = (TextView) view.findViewById(R.id.row_news_message);
            mUrlButton = (ImageButton) view.findViewById(R.id.row_news_url);
        }

        void setData(final Activity activity, final News news) {
            mTitle.setText(news.getTitle());
            mMessage.setText(String.format("%1$s\n%2$s", news.getMessage(), news.getDate()));

            final String url = news.getUrl();
            if (url != null && !url.isEmpty()) {
                mUrlButton.setOnClickListener(view -> ((NewsListActivity) activity).showUrl(url));
            } else {
                mUrlButton.setVisibility(View.GONE);
            }
        }
    }
}
