package it.liceoarzignano.bold.news;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import it.liceoarzignano.bold.R;

class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder> {
    private final List<News> mNewsList;
    protected Context mContext;

    NewsAdapter(List<News> mNewsList, Context mContext) {
        this.mNewsList = mNewsList;
        this.mContext = mContext;
    }

    @Override
    public NewsHolder onCreateViewHolder(ViewGroup mParent, int mType) {
        View mItem = LayoutInflater.from(mParent.getContext())
                .inflate(R.layout.item_news, mParent, false);

        return new NewsHolder(mItem);
    }


    @Override
    public void onBindViewHolder(NewsHolder mHolder, int mPostition) {
        News mNews = mNewsList.get(mPostition);
        mHolder.setData(mNews);
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }


    class NewsHolder extends RecyclerView.ViewHolder {
        private TextView mTitle;
        private TextView mMessage;
        private ImageButton mUrlButton;

        NewsHolder(View mView) {
            super(mView);
            mTitle = (TextView) mView.findViewById(R.id.row_news_title);
            mMessage = (TextView) mView.findViewById(R.id.row_news_message);
            mUrlButton = (ImageButton) mView.findViewById(R.id.row_news_url);
        }

        void setData(News mNews) {
            mTitle.setText(mNews.getTitle());
            mMessage.setText(String.format("%1$s\n%2$s", mNews.getMessage(), mNews.getDate()));

            final String mUrl = mNews.getUrl();
            if (mUrl != null && !mUrl.isEmpty()) {
                mUrlButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        NewsListActivity.showUrl((Activity) mContext, mUrl);
                    }
                });
            } else {
                mUrlButton.setVisibility(View.GONE);
            }
        }
    }
}
