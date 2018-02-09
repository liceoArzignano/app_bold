package it.liceoarzignano.bold.firebase

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.liceoarzignano.bold.BuildConfig
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.news.News
import it.liceoarzignano.bold.news.NewsHandler
import it.liceoarzignano.bold.news.NewsListActivity
import it.liceoarzignano.bold.settings.AppPrefs
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class BoldMessagingService : FirebaseMessagingService() {
    private lateinit var mNews: News

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        if (remoteMessage == null || remoteMessage.data.isEmpty()) {
            return
        }

        try {
            val json = JSONObject(remoteMessage.data.toString())
            val title = json.getString(if (BuildConfig.DEBUG) "d_title" else "title")
            var message: String? = json.getString(if (BuildConfig.DEBUG) "d_message" else "message")
            val url = json.getString("url")
            val isPrivate = json.getBoolean("isPrivate")
            val intent: Intent
            val prefs = AppPrefs(baseContext)

            if (message.isNullOrBlank() || (isPrivate &&
                    !prefs.get(AppPrefs.KEY_IS_TEACHER, false))) {
                return
            }

            if (isPrivate) {
                message = getString(R.string.news_type_private, message)
            }
            mNews = News(title, System.currentTimeMillis(), message?: "", url, true)
            saveNews()

            if (prefs.get(AppPrefs.KEY_NOTIF_NEWS, true)) {
                intent = Intent(applicationContext, NewsListActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (url.isNotBlank()) {
                    intent.putExtra("newsUrl", mNews.url)
                }
                publishNotification(intent)
            }
        } catch (e: JSONException) {
            Log.e(TAG, e.message)
        }
    }

    private fun publishNotification(intent: Intent) {
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pIntent = PendingIntent.getActivity(application, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(baseContext, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentTitle(mNews.title)
                .setContentText(mNews.description + '\u2026')
                .setContentIntent(pIntent)
                .setGroup(CHANNEL)
                .setColor(ContextCompat.getColor(baseContext, R.color.colorAccent))
                .setStyle(NotificationCompat.BigTextStyle().bigText(mNews.description))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(manager)
        }

        manager.notify(Calendar.getInstance().timeInMillis.toInt() * 1000, builder.build())
    }

    @TargetApi(26)
    private fun prepareChannel(manager: NotificationManager) {
        var channel = manager.getNotificationChannel(CHANNEL)
        if (channel != null) {
            return
        }

        channel = NotificationChannel(CHANNEL, getString(R.string.channel_title_news),
                NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = getString(R.string.channel_description_news)
        channel.enableLights(true)
        manager.createNotificationChannel(channel)
    }

    private fun saveNews() {
        val handler = NewsHandler.getInstance(baseContext)
        // Prevent duplicated items
        if (mNews.url.isNotBlank() && handler.all.none { it.url == mNews.url }) {
            handler.add(mNews)
        }
    }

    companion object {
        private const val TAG = "BoldFireBase"
        private const val CHANNEL = "channel_news"
    }
}
