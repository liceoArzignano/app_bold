package it.liceoarzignano.bold.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.BuildConfig;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.news.News;
import it.liceoarzignano.bold.news.NewsController;
import it.liceoarzignano.bold.news.NewsListActivity;
import it.liceoarzignano.bold.utils.DateUtils;

public class BoldMessagingService extends FirebaseMessagingService {
    private static final String TAG = "BoldFireBase";
    private Context mContext;
    private News mNews;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage == null) {
            return;
        }

        mContext = getApplicationContext();

        if (!remoteMessage.getData().isEmpty()) {
            try {
                JSONObject jston = new JSONObject(remoteMessage.getData().toString());
                String title = jston.getString(BuildConfig.DEBUG ? "d_title" : "title");
                String message = jston.getString(BuildConfig.DEBUG ? "d_message" : "message");
                String url = jston.getString("url");
                boolean isPrivate = jston.getBoolean("isPrivate");
                Intent intent;

                if (message == null || message.isEmpty()) {
                    return;
                }

                if (isPrivate && !Utils.isTeacher(mContext)) {
                    return;
                }

                mNews = new News();
                mNews.setTitle(title);
                mNews.setMessage(message);
                mNews.setDate(DateUtils.getDateString(0));
                mNews.setUrl(url);

                saveNews();

                if (Utils.hasNewsNotification(mContext)) {
                    intent = new Intent(mContext, NewsListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    if (url != null && !url.isEmpty()) {
                        intent.putExtra("newsId", mNews.getId());
                    }
                    publishNotification(intent);
                }

            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void publishNotification(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentTitle(mNews.getTitle())
                .setContentText(mNews.getMessage() + '\u2026')
                .setContentIntent(pIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mNews.getMessage()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        }

        NotificationManager manager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify((int) Calendar.getInstance().getTimeInMillis() * 100000, builder.build());
    }

    private void saveNews() {
        mNews.setId(Calendar.getInstance().getTimeInMillis());
        NewsController mController = new NewsController(((BoldApp) mContext).getConfig());
        mController.add(mNews);
    }

}
