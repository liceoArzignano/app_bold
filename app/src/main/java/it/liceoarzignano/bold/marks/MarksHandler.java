package it.liceoarzignano.bold.marks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import it.liceoarzignano.bold.database.DBHandler;

public class MarksHandler extends DBHandler<Mark2> {
    private static final String DB_NAME = "MarksDatabase.db";
    private static final int DB_VERSION = 1;
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_VALUE = "value";
    private static final String KEY_DATE = "date";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_FIRSTQUARTER = "firstQuarter";

    private static MarksHandler sInstance;

    public static synchronized MarksHandler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MarksHandler(context);
        }
        return sInstance;
    }

    private MarksHandler(Context context) {
        super(context, DB_NAME, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String command = "CREATE TABLE " + getTableName() + " (" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_SUBJECT + " TEXT, " +
                KEY_VALUE + " INTEGER, " +
                KEY_DATE + " INTEGER, " +
                KEY_DESCRIPTION + " TEXT, " +
                KEY_FIRSTQUARTER + " INTEGER)";
        db.execSQL(command);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Update this when db table will be changed
    }

    @Override
    public List<Mark2> getAll() {
        return getFilteredMarks(null, 0);
    }

    @Override
    @Nullable
    public Mark2 get(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + getTableName() +
                " WHERE " + KEY_ID + "=?", new String[]{String.valueOf(id)});

        Mark2 mark = null;
        if (cursor.moveToFirst()) {
            mark = new Mark2(
                    Long.parseLong(cursor.getString(0)),
                    cursor.getString(1),
                    Integer.parseInt(cursor.getString(2)),
                    Long.parseLong(cursor.getString(3)),
                    cursor.getString(4),
                    Integer.parseInt(cursor.getString(5)) == 1
            );
        }
        cursor.close();
        return mark;
    }

    @Override
    protected ContentValues getValues(@NonNull Mark2 item, boolean withId) {
        ContentValues values = new ContentValues();
        if (withId) {
            values.put(KEY_ID, item.getId());
        }
        values.put(KEY_SUBJECT, item.getSubject());
        values.put(KEY_VALUE, item.getValue());
        values.put(KEY_DATE, item.getDate());
        values.put(KEY_DESCRIPTION, item.getDescription());
        values.put(KEY_FIRSTQUARTER, item.isFirstQuarter() ? 1 : 0);

        return values;
    }

    @Override
    protected String getTableName() {
        return "marks";
    }

    public List<Mark2> getFilteredMarks(@Nullable String filter, int quarter) {
        String query;
        String[] args;

        if (TextUtils.isEmpty(filter)) {
            args = null;
            switch (quarter) {
                case 1:
                    query = "SELECT * FROM " + getTableName() + " WHERE " + KEY_FIRSTQUARTER +
                            " = 1 ORDER BY " + KEY_DATE + " DESC";
                    break;
                case 2:
                    query = "SELECT * FROM " + getTableName() + " WHERE " + KEY_FIRSTQUARTER +
                            " = 0 ORDER BY " + KEY_DATE + " DESC";
                    break;
                default:
                    query = "SELECT * FROM " + getTableName() + " ORDER BY " + KEY_DATE + " DESC";
                    break;
            }
        } else {
            args = new String[]{filter};
            switch (quarter) {
                case 1:
                    query = "SELECT * FROM " + getTableName() + " WHERE " + KEY_FIRSTQUARTER +
                            " = 1 AND  " + KEY_SUBJECT + "=? ORDER BY " + KEY_DATE + " DESC";
                    break;
                case 2:
                    query = "SELECT * FROM " + getTableName() + " WHERE " + KEY_FIRSTQUARTER +
                            " = 0 AND  " + KEY_SUBJECT + "=? ORDER BY " + KEY_DATE + " DESC";
                    break;
                default:
                    query = "SELECT * FROM " + getTableName() + " WHERE  " +
                            KEY_SUBJECT + "=? ORDER BY " + KEY_DATE + " DESC";
                    break;
            }
        }

        List<Mark2> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, args);

        if (cursor.moveToFirst()) {
            do {
                list.add(new Mark2(
                        Long.parseLong(cursor.getString(0)),
                        cursor.getString(1),
                        Integer.parseInt(cursor.getString(2)),
                        Long.parseLong(cursor.getString(3)),
                        cursor.getString(4),
                        Integer.parseInt(cursor.getString(5)) == 1
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    double getAverage(@Nullable String filter, int quarter) {
        List<Mark2> list = getFilteredMarks(filter, quarter);
        double sum = 0;
        if (list.isEmpty()) {
            return 0;
        }

        for (Mark2 item : list) {
            sum += item.getValue();
        }
        return sum / (100 * list.size());
    }

    double whatShouldIGet(@Nullable String filter, int quarter) {
        double sum = 0;
        List<Mark2> list = getFilteredMarks(filter, quarter);
        if (list.isEmpty()) {
            return 0;
        }

        for (Mark2 item : list) {
            sum += item.getValue();
        }

        return 6 * (list.size() + 1) - (sum / 100);
    }
}
