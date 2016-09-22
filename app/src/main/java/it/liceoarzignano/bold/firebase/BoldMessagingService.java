package it.liceoarzignano.bold.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.compat.BuildConfig;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import it.liceoarzignano.bold.MainActivity;
import it.liceoarzignano.bold.R;

public class BoldMessagingService extends FirebaseMessagingService {
    private static final String TAG = "BoldFireBase";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "FCM Message Id: " + remoteMessage.getMessageId());
            Log.d(TAG, "FCM Notification Message: " + remoteMessage.getNotification());
            Log.d(TAG, "FCM Data Message: " + remoteMessage.getData());
        }

        Intent mIntent = new Intent(this, MainActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mIntent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setAutoCancel(false)
                .setContentIntent(mPendingIntent);

        NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mManager.notify(0, mBuilder.build());
    }
}
