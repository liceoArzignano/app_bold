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

import io.realm.Realm;
import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.MainActivity;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;
import it.liceoarzignano.bold.news.News;
import it.liceoarzignano.bold.news.NewsListActivity;

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
                JSONObject mJSON = new JSONObject(remoteMessage.getData().toString());
                JSONObject mData = mJSON.getJSONObject("data");
                String mTitle = mData.getString("title");
                String mMessage = mData.getString("message");
                String mUrl = mData.getString("url");
                Intent mIntent;

                if (mMessage == null || mMessage.isEmpty()) {
                    return;
                }

                mNews = new News();
                mNews.setTitle(mTitle);
                mNews.setMessage(mMessage);
                mNews.setDate(Utils.getToday());
                mNews.setUrl(mUrl);

                saveNews();

                if (Utils.hasNewsNotification(mContext)) {
                    mIntent = new Intent(mContext, NewsListActivity.class);
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    if (mUrl != null && !mUrl.isEmpty()) {
                        mIntent.putExtra("newsId", mNews.getId());
                    }
                    pubblishNotification(mIntent);
                }

            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void pubblishNotification(Intent mIntent) {
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, 0,
                mIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        String[] mContent = mNews.getMessage().split("[\\r\\n]+");
        NotificationCompat.InboxStyle mStyle = new NotificationCompat.InboxStyle();
        for (String mLine : mContent) {
            mStyle.addLine(mLine);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentTitle(mNews.getTitle())
                .setContentText(mNews.getMessage() + '\u2026')
                .setContentIntent(mPendingIntent)
                .setStyle(mStyle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mBuilder.setColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        }

        NotificationManager mManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        mManager.notify((int) Calendar.getInstance().getTimeInMillis() * 100000, mBuilder.build());
    }

    private void saveNews() {
        mNews.setId(Calendar.getInstance().getTimeInMillis());

        Realm mRealm = Realm.getInstance(BoldApp.getAppRealmConfiguration());
        mRealm.beginTransaction();
        mRealm.copyToRealm(mNews);
        mRealm.commitTransaction();
    }

}
