package it.liceoarzignano.bold.utils

import android.content.Context
import android.content.res.Resources
import com.firebase.jobdispatcher.*
import it.liceoarzignano.bold.R
import it.liceoarzignano.bold.events.EventsHandler
import it.liceoarzignano.bold.events.EventsJobUtils
import it.liceoarzignano.bold.marks.MarksHandler
import it.liceoarzignano.bold.settings.AppPrefs
import java.util.*

object ContentUtils {

    fun getAverageElements(context: Context, filter: Int): Array<String> {
        val handler = MarksHandler.getInstance(context)

        val list = handler.getFilteredMarks("", filter)
        val elements = ArrayList<String>()

        list.filterNot { elements.contains(it.subject) }
                .forEach { elements.add(it.subject) }

        return elements.toTypedArray()
    }

    fun eventCategoryToString(context: Context, category: Int): String =
            when (category) {
                0 -> context.getString(R.string.events_test)
                1 -> context.getString(R.string.event_school)
                2 -> context.getString(R.string.event_birthday)
                3 -> context.getString(R.string.event_homework)
                4 -> context.getString(R.string.event_reminder)
                5 -> context.getString(R.string.event_meeting)
                else -> context.getString(R.string.event_other)
    }

    fun getTomorrowInfo(context: Context): String {
        val res = context.resources
        var content = ""

        val categories = intArrayOf(0 /* test */, 0 /* atSchool */,
                0 /* bday */, 0 /* homeworks */,
                0 /* reminder */, 0 /* meeting */,
                0 /* others */)

        val messages = intArrayOf(R.plurals.notification_test, R.plurals.notification_school,
                R.plurals.notification_birthday, R.plurals.notification_homework,
                R.plurals.notification_reminder, R.plurals.notification_meeting,
                R.plurals.notification_other)

        val handler = EventsHandler.getInstance(context)
        val events = handler.tomorrow

        if (events.isEmpty()) {
            return ""
        }

        // Get data
        for (event in events) {
            categories[event.category]++
        }

        // Build message
        categories.indices
                .asSequence()
                .filter { categories[it] > 0 }
                .forEach { content = eventInfoBuilder(res, content, categories[it], messages[it]) }

        content += " " + res.getString(R.string.notification_message_end)

        return content
    }

    private fun eventInfoBuilder(res: Resources, orig: String, size: Int, id: Int): String {
        val message =
                if (orig.isEmpty())
                    res.getQuantityString(R.plurals.notification_message_first, size, size)
                else
                    orig + res.getQuantityString(R.plurals.notification_message_half, size, size)

        return message + " " + res.getQuantityString(id, size, size)
    }

    fun makeEventNotification(context: Context) {
        val calendar = Calendar.getInstance()
        val hr = calendar.get(Calendar.HOUR_OF_DAY)
        var userPreference = 21
        val prefs = AppPrefs(context)
        if (!prefs.get(AppPrefs.KEY_NOTIF_EVENT, true)) {
            return
        }

        when (prefs.get(AppPrefs.KEY_NOTIF_EVENT_TIME, "2")) {
            "0" -> userPreference = 6
            "1" -> userPreference = 15
        }

        val time =
                (if (hr >= userPreference)
                    hr - userPreference + 1
                else
                    24 + userPreference - hr) * 60 * 60

        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
        val job = dispatcher.newJobBuilder()
                .setService(EventsJobUtils::class.java)
                .setTag("Bold_EventsJob")
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(time, time + 120))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(false)
                .build()

        dispatcher.mustSchedule(job)
    }
}
