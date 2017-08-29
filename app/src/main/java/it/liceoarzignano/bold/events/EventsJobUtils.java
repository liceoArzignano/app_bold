package it.liceoarzignano.bold.events;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.utils.ContentUtils;

public final class EventsJobUtils extends JobService {
    private static final String CHANNEL = "channel_events";
    private static final int NOTIFICATION_ID = 21;

    @Override
    public boolean onStartJob(JobParameters parameters) {
        Context context = getApplicationContext();
        String message = ContentUtils.getTomorrowInfo(context);

        if (message == null) {
            return false;
        }

        PendingIntent pIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, EventListActivity.class), 0);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(), CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setGroup(CHANNEL)
                .setContentIntent(pIntent)
                .setAutoCancel(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel(CHANNEL);
            if (channel == null) {
                channel = new NotificationChannel(CHANNEL,
                        context.getString(R.string.channel_title_events),
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(context.getString(R.string.channel_description_events));
                channel.enableLights(true);
                manager.createNotificationChannel(channel);
            }
        }

        // Remove old notification
        manager.cancel(NOTIFICATION_ID);

        // Post new one
        manager.notify(NOTIFICATION_ID, builder.build());

        // Schedule next job
        ContentUtils.makeEventNotification(context);
        return false;
    }


    @Override
    public boolean onStopJob(JobParameters parameters) {
        return false;
    }
}
