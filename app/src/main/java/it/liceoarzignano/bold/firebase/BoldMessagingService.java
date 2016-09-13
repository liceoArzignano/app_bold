package it.liceoarzignano.bold.firebase;

import android.support.compat.BuildConfig;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class BoldMessagingService extends FirebaseMessagingService {
    private static final String TAG = "BoldFireBase";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "FCM Message Id: " + remoteMessage.getMessageId());
            Log.d(TAG, "FCM Notification Message: " + remoteMessage.getNotification());
            Log.d(TAG, "FCM Data Message: " + remoteMessage.getData());
        }
    }
}
