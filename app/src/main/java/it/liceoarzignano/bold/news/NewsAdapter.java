package it.liceoarzignano.bold.news;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.ManagerActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.firebase.BoldAnalytics;

class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder> {
    private final List<News> mNewsList;
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

    class NewsHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout mLayout;
        private final TextView mTitle;
        private final TextView mMessage;
        private final ImageButton mUrlButton;

        NewsHolder(View view) {
            super(view);
            mLayout = (RelativeLayout) view.findViewById(R.id.row_news_layout);
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

            // Bottom sheet dialog
            final BottomSheetDialog sheet = new BottomSheetDialog(activity);

            @SuppressLint("InflateParams")
            View sheetView = activity.getLayoutInflater()
                    .inflate(R.layout.dialog_sheet_news, null);
            LinearLayout shareLayout =
                    (LinearLayout) sheetView.findViewById(R.id.news_sheet_share);
            LinearLayout toEventLayout =
                    (LinearLayout) sheetView.findViewById(R.id.news_sheet_to_event);
            LinearLayout deleteLayout =
                    (LinearLayout) sheetView.findViewById(R.id.news_sheet_delete);

            shareLayout.setOnClickListener(view -> {
                new BoldAnalytics(mActivity).log(FirebaseAnalytics.Event.SHARE,
                        FirebaseAnalytics.Param.ITEM_NAME, "Share news");
                sheet.hide();
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, String.format("%1$s (%2$s)\n%3$s\n%4$s",
                                news.getTitle(), news.getDate(), news.getMessage(), news.getUrl()));
                activity.startActivity(Intent.createChooser(shareIntent,
                        activity.getString(R.string.news_sheet_share)));

            });
            deleteLayout.setOnClickListener(view -> {
                new BoldAnalytics(mActivity).log(FirebaseAnalytics.Event.SELECT_CONTENT,
                        FirebaseAnalytics.Param.ITEM_NAME, "Delete news");
                sheet.hide();
                NewsController controller = new NewsController(
                        ((BoldApp) activity.getApplication()).getConfig());
                controller.delete(news.getId());
                ((NewsListActivity) activity).refresh(activity, null);
            });
            toEventLayout.setOnClickListener(view -> {
                new BoldAnalytics(mActivity).log(FirebaseAnalytics.Event.SELECT_CONTENT,
                        FirebaseAnalytics.Param.ITEM_NAME, "Convert news");
                sheet.hide();
                Intent toEventIntent = new Intent(activity, ManagerActivity.class);
                toEventIntent.putExtra("newsToEvent", news.getId());
                toEventIntent.putExtra("isMark", false);
                activity.startActivity(toEventIntent);
            });

            sheet.setContentView(sheetView);

            mLayout.setOnClickListener(view -> sheet.show());
        }
    }
}
