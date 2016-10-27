package it.liceoarzignano.bold.news;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.realm.RealmController;

class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder> {
    private final List<News> mNewsList;
    private final Activity mActivity;

    NewsAdapter(List<News> mNewsList, Activity mActivity) {
        this.mNewsList = mNewsList;
        this.mActivity = mActivity;
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
        mHolder.setData(mActivity, mNews);
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }


    class NewsHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout mLayout;
        private final TextView mTitle;
        private final TextView mMessage;
        private final ImageButton mUrlButton;

        NewsHolder(View mView) {
            super(mView);
            mLayout = (RelativeLayout) mView.findViewById(R.id.row_news_layout);
            mTitle = (TextView) mView.findViewById(R.id.row_news_title);
            mMessage = (TextView) mView.findViewById(R.id.row_news_message);
            mUrlButton = (ImageButton) mView.findViewById(R.id.row_news_url);
        }

        void setData(final Activity mActivity, final News mNews) {
            mTitle.setText(mNews.getTitle());
            mMessage.setText(String.format("%1$s\n%2$s", mNews.getMessage(), mNews.getDate()));

            final String mUrl = mNews.getUrl();
            if (mUrl != null && !mUrl.isEmpty()) {
                mUrlButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        NewsListActivity.showUrl(mUrl);
                    }
                });
            } else {
                mUrlButton.setVisibility(View.GONE);
            }

            // Bottom sheet dialog
            final BottomSheetDialog mSheet = new BottomSheetDialog(mActivity);

            View mSheetView = mActivity.getLayoutInflater()
                    .inflate(R.layout.dialog_sheet_news, null);
            LinearLayout mShareLayout =
                    (LinearLayout) mSheetView.findViewById(R.id.news_sheet_share);
            LinearLayout mToEventLayout =
                    (LinearLayout) mSheetView.findViewById(R.id.news_sheet_to_event);
            LinearLayout mDeleteLayout =
                    (LinearLayout) mSheetView.findViewById(R.id.news_sheet_delete);

            mShareLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSheet.hide();
                    Intent mShareIntent = new Intent(Intent.ACTION_SEND);
                    mShareIntent.setType("text/plain")
                            .putExtra(Intent.EXTRA_TEXT, String.format("%1$s (%2$s)\n%3$s\n%4$s",
                                    mNews.getTitle(), mNews.getDate(), mNews.getMessage(),
                                    mNews.getUrl()));
                    mActivity.startActivity(Intent.createChooser(mShareIntent,
                            mActivity.getString(R.string.news_sheet_share)));

                }
            });
            mDeleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSheet.hide();
                    RealmController.with(mActivity).deleteNews(mNews.getId());
                    NewsListActivity.refreshList(mActivity, null);
                }
            });
            mToEventLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSheet.hide();
                    Intent mToEventIntent = new Intent(mActivity, ManagerActivity.class);
                    mToEventIntent.putExtra("newsToEvent", mNews.getId());
                    mToEventIntent.putExtra("isMark", false);
                    mActivity.startActivity(mToEventIntent);
                }
            });

            mSheet.setContentView(mSheetView);

            mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSheet.show();
                }
            });
        }
    }
}
