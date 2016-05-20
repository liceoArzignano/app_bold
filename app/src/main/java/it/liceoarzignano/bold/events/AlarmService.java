package it.liceoarzignano.bold.events;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import it.liceoarzignano.bold.MainActivity;
import it.liceoarzignano.bold.R;

public class AlarmService extends IntentService {

    public AlarmService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String message = MainActivity.getTomorrowInfo();

        Intent notifIntent = new Intent(this.getApplicationContext(),
                EventListActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(),
                0, notifIntent, 0);

        NotificationManager mManager = (NotificationManager) getSystemService(this
                .getApplicationContext().NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this.getApplicationContext())
                .setSmallIcon(R.drawable.ic_event)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (message != null) {
            mManager.notify(21, mBuilder.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
