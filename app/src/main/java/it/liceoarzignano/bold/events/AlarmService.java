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
        Context context = getApplicationContext();
        String message = MainActivity.getTomorrowInfo();
        Intent notifIntent = new Intent(context, EventListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notifIntent, 0);
        NotificationManager manager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);

        if (message != null) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    getApplicationContext())
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            manager.notify(21, mBuilder.build());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
