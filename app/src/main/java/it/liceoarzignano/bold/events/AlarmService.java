package it.liceoarzignano.bold.events;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.Utils;

public class AlarmService extends Service {

    @Override
    public void onCreate() {
        Context context = getApplicationContext();
        String message = Utils.getTomorrowInfo(context);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, EventListActivity.class), 0);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (message != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    getApplicationContext())
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);

            manager.notify(21, builder.build());
        }
    }

    @Override
    public IBinder onBind(Intent mIntent) {
        return null;
    }
}
