package it.liceoarzignano.bold.events;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Event";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_EVENTS = "events";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_VALUE = "value";
    private static final String KEY_ICON = "icon";
    private static DatabaseConnection ourInstance;
    private static int NEXT_ROW_ID_NUMBER;

    public DatabaseConnection(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setupNextRowIdNumber();
    }


    public static synchronized DatabaseConnection getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new DatabaseConnection(context);
        }

        return ourInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        final String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + " ("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_TITLE + " TEXT, "
                + KEY_VALUE + " TEXT, " + KEY_ICON + " INTEGER)";
        database.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            final String DROP_EVENTS_TABLE = "DROP TABLE IF EXISTS " + TABLE_EVENTS;
            database.execSQL(DROP_EVENTS_TABLE);
            onCreate(database);
        }
    }

    /**
     * Description:
     * Event getter by id
     *
     * @param id: id of the event we're looking for
     * @return event with the id we asked
     */
    public Event getEvent(int id) {
        Event event = null;

        String selection = KEY_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        String limit = String.valueOf(1);
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(
                TABLE_EVENTS, null, selection, selectionArgs, null, null, null, limit);

        if (cursor.moveToFirst()) {
            event = new Event(
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID))),
                    cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                    cursor.getString(cursor.getColumnIndex(KEY_VALUE)),
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ICON))));
        }
        cursor.close();
        database.close();
        return event;
    }

    /**
     * Description:
     * Change event values without loosing id
     *
     * @param updatedEvent: new event with old id
     */
    public void updateEvent(Event updatedEvent) {
        SQLiteDatabase database = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID, updatedEvent.getId());
        contentValues.put(KEY_TITLE, updatedEvent.getTitle());
        contentValues.put(KEY_VALUE, updatedEvent.getValue());
        contentValues.put(KEY_ICON, updatedEvent.getIcon());

        String whereClause = KEY_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(updatedEvent.getId())};

        database.update(TABLE_EVENTS, contentValues, whereClause, whereArgs);
        database.close();
    }

    /**
     * Description:
     * Add a new event with a new id
     *
     * @param event: new event
     */
    public void addEvent(Event event) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID, NEXT_ROW_ID_NUMBER);
        contentValues.put(KEY_TITLE, event.getTitle());
        contentValues.put(KEY_VALUE, event.getValue());
        contentValues.put(KEY_ICON, event.getIcon());
        database.insert(TABLE_EVENTS, null, contentValues);
        NEXT_ROW_ID_NUMBER++;
        database.close();
    }

    /**
     * Description:
     * Remove an event from the database
     *
     * @param eventToDelete: event to be deleted from database
     */
    public void deleteEvent(Event eventToDelete) {
        if (eventToDelete.getId() == -1) {
            int eventId = findEventRowId(eventToDelete);

            if (eventId == -1) {
                return;
            } else {
                eventToDelete.setId(eventId);
            }
        }

        String whereClause = KEY_ID + "=?";
        String[] whereArgs = new String[]{
                String.valueOf(eventToDelete.getId())
        };
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_EVENTS, whereClause, whereArgs);
    }

    /**
     * Description
     * Get the event row id
     *
     * @param event: event we're looking for
     * @return int with row id
     */
    private int findEventRowId(Event event) {
        List<Event> events = getAllEvents();

        for (Event eventInList : events) {
            if (eventInList.equals(event)) {
                return eventInList.getId();
            }
        }
        return -1;
    }


    /**
     * Description:
     * Get all the events in the database
     *
     * @return return a ListArray of all the Events
     */
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE_EVENTS, null, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                events.add(new Event(
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID))),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(KEY_VALUE)),
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ICON)))));

            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return events;
    }

    /**
     * Description:
     * set right NEXT_ROW_ID_NUMBER when adding an event
     */
    private void setupNextRowIdNumber() {
        List<Event> events = getAllEvents();

        if (events.size() == 0) {
            NEXT_ROW_ID_NUMBER = 0;
        } else {
            NEXT_ROW_ID_NUMBER = 1 + events.get(events.size() - 1).getId();
        }
    }
}
