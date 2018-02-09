package it.liceoarzignano.bold.events

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat

import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService

import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.utils.ContentUtils

class EventsJobUtils : JobService() {

    override fun onStartJob(parameters: JobParameters): Boolean {
        val context = baseContext
        val message = ContentUtils.getTomorrowInfo(context)

        if (message.isBlank()) {
            return false
        }

        val pIntent = PendingIntent.getActivity(context, 0,
                Intent(context, EventListActivity::class.java), 0)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setGroup(CHANNEL)
                .setContentIntent(pIntent)
                .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel: NotificationChannel? = manager.getNotificationChannel(CHANNEL)
            if (channel == null) {
                channel = NotificationChannel(CHANNEL,
                        context.getString(R.string.channel_title_events),
                        NotificationManager.IMPORTANCE_DEFAULT)
                channel.description = context.getString(R.string.channel_description_events)
                channel.enableLights(true)
                manager.createNotificationChannel(channel)
            }
        }

        // Remove old notification
        manager.cancel(NOTIFICATION_ID)

        // Post new one
        manager.notify(NOTIFICATION_ID, builder.build())

        // Schedule next job
        ContentUtils.makeEventNotification(context)
        return false
    }


    override fun onStopJob(parameters: JobParameters): Boolean = false

    companion object {
        private const val CHANNEL = "channel_events"
        private const val NOTIFICATION_ID = 21
    }
}
