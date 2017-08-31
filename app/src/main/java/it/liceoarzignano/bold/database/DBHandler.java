package it.liceoarzignano.bold.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public abstract class DBHandler<T extends DBItem> extends SQLiteOpenHelper {
    protected static final String KEY_ID = BaseColumns._ID;

    protected DBHandler(Context context, @NonNull String name, int version) {
        super(context, name, null, version);
    }

    public void add(@NonNull T item) {
        if (item.getId() == -1) {
            item.setId(getCount());
        }

        SQLiteDatabase db = getWritableDatabase();
        db.insert(getTableName(), null, getValues(item, true));
    }

    public void update(T item) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(getTableName(), getValues(item, false), KEY_ID + "=?",
                new String[]{ String.valueOf(item.getId()) });
    }

    public void delete(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(getTableName(), KEY_ID + "=?", new String[] { String.valueOf(id) });
    }

    public void refillTable(List<T> items) {
        SQLiteDatabase db = getWritableDatabase();
        List<T> list = getAll();
        for (T item : list) {
            db.delete(getTableName(), KEY_ID + "=?", new String[] { String.valueOf(item.getId()) });
        }

        for (T item : items) {
            db.insert(getTableName(), null, getValues(item, true));
        }
    }

    public void clearTable() {
        SQLiteDatabase db = getWritableDatabase();
        List<T> list = getAll();
        for (T item : list) {
            db.delete(getTableName(), KEY_ID + "=?", new String[] { String.valueOf(item.getId()) });
        }
    }

    private int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + getTableName(), null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    protected abstract List<T> getAll();

    @Nullable
    public abstract T get(long id);
    protected abstract ContentValues getValues(@NonNull T item, boolean withId);
    protected abstract String getTableName();
}
