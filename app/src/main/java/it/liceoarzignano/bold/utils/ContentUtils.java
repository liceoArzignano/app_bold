package it.liceoarzignano.bold.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.liceoarzignano.bold.BoldApp;
import it.liceoarzignano.bold.R;
import it.liceoarzignano.bold.events.AlarmService;
import it.liceoarzignano.bold.events.Event;
import it.liceoarzignano.bold.events.EventsController;
import it.liceoarzignano.bold.marks.Mark;
import it.liceoarzignano.bold.marks.MarksController;


public final class ContentUtils {

    private ContentUtils() {
    }

    /**
     * Get array of subjects with at least one mark for averages list
     *
     * @return array of subjects
     */
    public static String[] getAverageElements(Context context, int filter) {
        int size = 0;
        MarksController controller = new MarksController(
                ((BoldApp) context.getApplicationContext()).getConfig());

        List<Mark> marks = controller.getFilteredMarks(null, filter);
        ArrayList<String> elements = new ArrayList<>();

        for (Mark mark : marks) {
            if (!elements.contains(mark.getTitle())) {
                elements.add(mark.getTitle());
                size++;
            }
        }

        return elements.toArray(new String[size]);
    }

    /**
     * Get event category description from int
     *
     * @param category: event icon value
     * @return category name
     */
    public static String eventCategoryToString(Context context, int category) {
        switch (category) {
            case 0:
                return context.getString(R.string.event_spinner_test);
            case 1:
                return context.getString(R.string.event_spinner_school);
            case 2:
                return context.getString(R.string.event_spinner_bday);
            case 3:
                return context.getString(R.string.event_spinner_homework);
            case 4:
                return context.getString(R.string.event_spinner_reminder);
            case 5:
                return context.getString(R.string.event_spinner_hang_out);
            default:
                return context.getString(R.string.event_spinner_other);
        }
    }

    /**
     * Fetch all the upcoming events and create a description
     *
     * @return content for events notification
     */
    public static String getTomorrowInfo(Context context) {
        Resources res = context.getResources();
        String content = "";

        int categories[] = new int[] {
                0 /* test */, 0 /* atSchool */, 0 /* bday */,
                0 /* homeworks */, 0 /* reminder */, 0 /* meeting */,
                0 /* others */
        };

        int messages[] = new int[] {
                R.plurals.notification_test, R.plurals.notification_school,
                R.plurals.notification_birthday, R.plurals.notification_homework,
                R.plurals.notification_reminder, R.plurals.notification_meeting,
                R.plurals.notification_other
        };

        EventsController controller = new EventsController(
                ((BoldApp) context.getApplicationContext()).getConfig());
        List<Event> events = controller.getTomorrow();

        if (events.isEmpty()) {
            return null;
        }

        // Get data
        for (Event event : events) {
            categories[event.getIcon()]++;
        }

        // Build message
        for (int counter = 0; counter < categories.length; counter++) {
            if (categories[counter] > 0) {
                content = eventInfoBuilder(res, content, categories[counter], messages[counter]);
            }
        }
        content += " " + res.getString(R.string.notification_message_end);

        return content;
    }

    /**
     * Build part of summary for notification (only the given category)
     *
     * @param res to fetch strings
     * @param orig original message
     * @param size quantity of events
     * @param id string id
     * @return updated message
     */
    private static String eventInfoBuilder(Resources res, String orig, int size, int id) {
        String val = orig.isEmpty() ?
                res.getQuantityString(R.plurals.notification_message_first, size, size) :
                orig + res.getQuantityString(R.plurals.notification_message_half, size, size);

        return val + " " + res.getQuantityString(id, size, size);
    }

    /**
     * Create an event notification that will be fired later
     */
    public static void makeEventNotification(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        switch (PrefsUtils.getEventsNotificationTime(context)) {
            case "0":
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 6) {
                    // If it's too late for today's notification, plan one for tomorrow
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                }
                calendar.set(Calendar.HOUR_OF_DAY, 6);
                break;
            case "1":
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 15) {
                    // If it's too late for today's notification, plan one for tomorrow
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                }
                calendar.set(Calendar.HOUR_OF_DAY, 15);
                break;
            case "2":
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 21) {
                    // If it's too late for today's notification, plan one for tomorrow
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                }
                calendar.set(Calendar.HOUR_OF_DAY, 21);
                break;
        }

        // Set alarm
        AlarmManager manager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pIntent = PendingIntent.getService(context, 0,
                new Intent(context, AlarmService.class), 0);
        manager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pIntent);
    }
}
