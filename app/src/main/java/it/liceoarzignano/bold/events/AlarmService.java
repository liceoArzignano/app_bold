package it.liceoarzignano.bold.events;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import it.liceoarzignano.bold.MainActivity;
import it.liceoarzignano.bold.R;

public class AlarmService extends Service {

    @Override
    public void onCreate() {
        Context mContext = getApplicationContext();
        String mMessage = MainActivity.getTomorrowInfo();
        Intent mIntent = new Intent(mContext, EventListActivity.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, 0,
                mIntent, 0);
        NotificationManager mManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);

        if (mMessage != null) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    getApplicationContext())
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(mMessage)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(mMessage))
                    .setContentIntent(mPendingIntent)
                    .setAutoCancel(true);

            mManager.notify(21, mBuilder.build());
        }
    }

    @Override
    public IBinder onBind(Intent mIntent) {
        return null;
    }
}
