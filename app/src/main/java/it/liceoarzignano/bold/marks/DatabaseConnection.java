package it.liceoarzignano.bold.marks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Mark";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_MARKS = "marks";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_VALUE = "value";
    private static final String KEY_CONTENT = "content";
    private static DatabaseConnection ourInstance;
    private static int NEXT_ROW_ID_NUMBER;

    public DatabaseConnection(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setupNextRowIdNumber();
    }


    public static synchronized DatabaseConnection getInstance(Context context) {
        if (ourInstance == null)
            ourInstance = new DatabaseConnection(context);

        return ourInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        final String CREATE_MARKS_TABLE = "CREATE TABLE " + TABLE_MARKS + " ("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_TITLE + " TEXT, "
                + KEY_VALUE + " INTEGER, " + KEY_CONTENT + " TEXT)";
        database.execSQL(CREATE_MARKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            final String DROP_MARKS_TABLE = "DROP TABLE IF EXISTS " + TABLE_MARKS;
            database.execSQL(DROP_MARKS_TABLE);
            onCreate(database);
        }
    }

    /**
     * Mark getter by id
     *
     * @param id: id of the mark we're looking for
     * @return mark with the id we asked
     */
    public Mark getMark(int id) {
        Mark mark = null;

        String selection = KEY_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        String limit = String.valueOf(1);
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(
                TABLE_MARKS, null, selection, selectionArgs, null, null, null, limit);

        if (cursor.moveToFirst()) {
            mark = new Mark(
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID))),
                    cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VALUE))),
                    cursor.getString(cursor.getColumnIndex(KEY_CONTENT)));
        }

        cursor.close();
        database.close();
        return mark;
    }

    /**
     * Change mark values without loosing id
     *
     * @param updatedMark: new mark with old id
     */
    public void updateMark(Mark updatedMark) {
        SQLiteDatabase database = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID, updatedMark.getId());
        contentValues.put(KEY_TITLE, updatedMark.getTitle());
        contentValues.put(KEY_VALUE, updatedMark.getValue());
        contentValues.put(KEY_CONTENT, updatedMark.getContent());

        String whereClause = KEY_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(updatedMark.getId())};

        database.update(TABLE_MARKS, contentValues, whereClause, whereArgs);
        database.close();
    }

    /**
     * Add a new mark with a new id
     *
     * @param mark: new mark
     */
    public void addMark(Mark mark) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID, NEXT_ROW_ID_NUMBER);
        contentValues.put(KEY_TITLE, mark.getTitle());
        contentValues.put(KEY_VALUE, mark.getValue());
        contentValues.put(KEY_CONTENT, mark.getContent());
        database.insert(TABLE_MARKS, null, contentValues);
        NEXT_ROW_ID_NUMBER++;
        database.close();
    }

    /**
     * Remove a mark from the database
     *
     * @param markToDelete: mark to be deleted from database
     */
    public void deleteMark(Mark markToDelete) {
        if (markToDelete.getId() == -1) {
            int markId = findMarkRowId(markToDelete);

            if (markId == -1) {
                return;
            } else {
                markToDelete.setId(markId);
            }
        }

        String whereClause = KEY_ID + "=?";
        String[] whereArgs = new String[]{
                String.valueOf(markToDelete.getId())
        };
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_MARKS, whereClause, whereArgs);
    }

    /**
     * Get the mark row id
     *
     * @param mark: mark we're looking for
     * @return int with row id
     */
    private int findMarkRowId(Mark mark) {
        List<Mark> marks = getAllMarks();

        for (Mark markInList : marks) {
            if (markInList.equals(mark)) {
                return markInList.getId();
            }
        }
        return -1;
    }

    /**
     * Get all the marks in the database
     *
     * @return return a ListArray of all the marks
     */
    public List<Mark> getAllMarks() {
        List<Mark> marks = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE_MARKS, null, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                marks.add(new Mark(
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID))),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VALUE))),
                        cursor.getString(cursor.getColumnIndex(KEY_CONTENT))));

            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return marks;
    }

    /**
     * Get a ListArray of all the marks with a defined title.
     *
     * @param filter: title-filter for ListArray
     * @return ListArray of all the marks that matched the filter
     */
    public List<Mark> getFilteredMarks(String filter) {
        List<Mark> marks = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE_MARKS, null, null, null, null, null, null, null);

        try {
            if (cursor.moveToFirst() && filter != null) {
                do {
                    if (cursor.getString(cursor.getColumnIndex(KEY_TITLE)).equals(filter)) {
                        marks.add(new Mark(
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID))),
                                cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                                Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_VALUE))),
                                cursor.getString(cursor.getColumnIndex(KEY_CONTENT))));
                    }

                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return marks;
    }

    /**
     * set right NEXT_ROW_ID_NUMBER when adding a mark
     */
    private void setupNextRowIdNumber() {
        List<Mark> marks = getAllMarks();

        if (marks.size() == 0) {
            NEXT_ROW_ID_NUMBER = 0;
        } else {
            NEXT_ROW_ID_NUMBER = 1 + marks.get(marks.size() - 1).getId();
        }
    }

    /**
     * get the average value of all the marks that match
     * a filter
     *
     * @param filter: title-filter
     * @return double of the math average
     */
    public double getAverage(String filter) {
        double sum = 0;

        List<Mark> marks = getFilteredMarks(filter);
        for (Mark markInList : marks) {
            sum += markInList.getValue();
        }
        sum /= 100;

        if (marks.size() > 0) {
            return sum / marks.size();
        } else {
            return 0;
        }
    }

    /**
     * get the next mark we should get to have an
     * average major or equal 6.0
     *
     * @param filter: title-filter
     * @return double of the next mark
     */
    public double whatShouldIGet(String filter) {
        double sum = 0;

        List<Mark> marks = getFilteredMarks(filter);

        for (Mark markInList : marks) {
            sum += markInList.getValue();
        }
        sum /= 100;

        if (marks.size() > 0) {
            return 6 * (marks.size() + 1) - sum;
        } else {
            return 0;
        }
    }

    public void dropAll() {
        List<Mark> marks = getAllMarks();

        for (Mark markInList : marks) {
            deleteMark(markInList);
        }
    }
}
