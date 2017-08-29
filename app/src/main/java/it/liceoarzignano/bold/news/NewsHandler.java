package it.liceoarzignano.bold.news;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.liceoarzignano.bold.database.DBHandler;
import it.liceoarzignano.bold.utils.DateUtils;

public class NewsHandler extends DBHandler<News> {
    private static final String DB_NAME = "NewsDatabase.db";
    private static final int DB_VERSION = 1;
    private static final String KEY_TITLE = "title";
    private static final String KEY_DATE = "date";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_URL = "url";

    private static NewsHandler sInstance;

    public static synchronized NewsHandler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NewsHandler(context);
        }
        return sInstance;
    }

    private NewsHandler(Context context) {
        super(context, DB_NAME, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + getTableName() + " (" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_TITLE + " TEXT, " +
                KEY_DATE + " INTEGER, " +
                KEY_DESCRIPTION + " TEXT, " +
                KEY_URL + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Update this when db table will be changed
    }

    @Override
    public List<News> getAll() {
        List<News> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + getTableName() +
                " ORDER BY " + KEY_DATE + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new News(
                        Long.parseLong(cursor.getString(0)),
                        cursor.getString(1),
                        Long.parseLong(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getString(4)
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    @Override
    @Nullable
    public News get(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + getTableName() +
                " WHERE " + KEY_ID + "=?", new String[] { String.valueOf(id) });

        News news = null;
        if (cursor.moveToFirst()) {
            news = new News(
                    Long.parseLong(cursor.getString(0)),
                    cursor.getString(1),
                    Long.parseLong(cursor.getString(2)),
                    cursor.getString(3),
                    cursor.getString(4)
            );
        }
        cursor.close();
        return news;
    }

    @Override
    protected ContentValues getValues(@NonNull News item, boolean withId) {
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, item.getTitle());
        values.put(KEY_DATE, item.getDate());
        values.put(KEY_DESCRIPTION, item.getDescription());
        values.put(KEY_URL, item.getUrl());

        if (withId) {
            values.put(KEY_ID, item.getId());
        }

        return values;
    }

    @Override
    protected String getTableName() {
        return "news";
    }

    List<News> getByQuery(@Nullable String query) {
        List<News> list = getAll();

        if (query != null) {
            list.removeIf(it -> {
                String title = it.getTitle().toLowerCase();
                String message = it.getDescription().toLowerCase();
                String date = DateUtils.dateToString(new Date(it.getDate())).toLowerCase();
                return !title.contains(query.toLowerCase()) &&
                        !date.contains(query.toLowerCase()) &&
                        !message.contains(query.toLowerCase());
            });
        }

        return list;
    }
}
