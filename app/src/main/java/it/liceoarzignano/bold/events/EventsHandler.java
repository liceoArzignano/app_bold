package it.liceoarzignano.bold.events;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.liceoarzignano.bold.database.DBHandler;
import it.liceoarzignano.bold.utils.DateUtils;

public class EventsHandler extends DBHandler<Event2> {
    private static final String DB_NAME = "EventDatabase.db";
    private static final int DB_VERSION = 1;
    private static final String KEY_TITLE = "title";
    private static final String KEY_DATE = "date";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_CATEGORY = "category";

    private static EventsHandler sInstance;

    public static synchronized EventsHandler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new EventsHandler(context);
        }
        return sInstance;
    }

    private EventsHandler(Context context) {
        super(context, DB_NAME, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + getTableName() + " (" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_TITLE + " TEXT, " +
                KEY_DATE + " INTEGER, " +
                KEY_DESCRIPTION + " TEXT, " +
                KEY_CATEGORY + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Update this when db table will be changed
    }

    @Override
    public List<Event2> getAll() {
        List<Event2> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + getTableName() +
                " ORDER BY " + KEY_DATE + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new Event2(
                        Long.parseLong(cursor.getString(0)),
                        cursor.getString(1),
                        Long.parseLong(cursor.getString(2)),
                        cursor.getString(3),
                        Integer.parseInt(cursor.getString(4))
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    @Override
    @Nullable
    public Event2 get(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + getTableName() +
                " WHERE " + KEY_ID + "=?", new String[] { String.valueOf(id) });

        Event2 event = null;
        if (cursor.moveToFirst()) {
            event = new Event2(
                    Long.parseLong(cursor.getString(0)),
                    cursor.getString(1),
                    Long.parseLong(cursor.getString(2)),
                    cursor.getString(3),
                    Integer.parseInt(cursor.getString(4))
            );
        }
        cursor.close();
        return event;
    }

    @Override
    protected ContentValues getValues(@NonNull Event2 item, boolean withId) {
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, item.getTitle());
        values.put(KEY_DATE, item.getDate());
        values.put(KEY_DESCRIPTION, item.getDescription());
        values.put(KEY_CATEGORY, item.getCategory());

        if (withId) {
            values.put(KEY_ID, item.getId());
        }

        return values;
    }

    @Override
    protected String getTableName() {
        return "events";
    }

    public List<Event2> getTomorrow() {
        Calendar tomorrow = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 23);
        tomorrow.set(Calendar.MINUTE, 59);
        tomorrow.set(Calendar.SECOND, 59);
        today.set(Calendar.HOUR_OF_DAY, 23);
        today.set(Calendar.MINUTE, 59);
        today.set(Calendar.SECOND, 59);

        long timestampTomorrow = tomorrow.getTimeInMillis();
        long timestampToday = today.getTimeInMillis();

        List<Event2> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + getTableName() +
                " WHERE " + KEY_DATE + " > " + String.valueOf(timestampToday) +
                " AND " + KEY_DATE + " < " + String.valueOf(timestampTomorrow) +
                " ORDER BY " + KEY_DATE + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new Event2(
                        Long.parseLong(cursor.getString(0)),
                        cursor.getString(1),
                        Long.parseLong(cursor.getString(2)),
                        cursor.getString(3),
                        Integer.parseInt(cursor.getString(4))
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;

    }

    List<Event2> getByQuery(@Nullable String query) {
        List<Event2> list = getAll();

        if (query != null) {
            list.removeIf(it -> {
                String title = it.getTitle().toLowerCase();
                String date = DateUtils.dateToString(new Date(it.getDate())).toLowerCase();
                return !title.contains(query.toLowerCase()) &&
                        !date.contains(query.toLowerCase());
            });
        }

        return list;
    }
}
