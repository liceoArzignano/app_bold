package it.liceoarzignano.bold.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import it.liceoarzignano.bold.R;

public class DatabaseConnection extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Tasks";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_TASKS = "tasks";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DAY = "day";
    private static final String KEY_STAGE = "stage";
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
        final String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_TASKS + " ("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_TITLE + " TEXT, "
                + KEY_DAY + " INTEGER, " + KEY_STAGE + " INTEGER)";
        database.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            final String DROP_TASKS_TABLE = "DROP TABLE IF EXISTS " + TABLE_TASKS;
            database.execSQL(DROP_TASKS_TABLE);
            onCreate(database);
        }
    }

    /**
     * Change task values without loosing id
     *
     * @param updatedTask: new task with old id
     */
    public void updateTask(Task updatedTask) {
        SQLiteDatabase database = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID, updatedTask.getId());
        contentValues.put(KEY_TITLE, updatedTask.getTitle());
        contentValues.put(KEY_DAY, updatedTask.getDay());
        contentValues.put(KEY_STAGE, updatedTask.getStage());

        String whereClause = KEY_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(updatedTask.getId())};

        database.update(TABLE_TASKS, contentValues, whereClause, whereArgs);
        database.close();
    }

    /**
     * Add a new task with a new id
     *
     * @param task: new task
     */
    public void addTask(Task task) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID, NEXT_ROW_ID_NUMBER);
        contentValues.put(KEY_TITLE, task.getTitle());
        contentValues.put(KEY_DAY, task.getDay());
        contentValues.put(KEY_STAGE, task.getStage());
        database.insert(TABLE_TASKS, null, contentValues);
        NEXT_ROW_ID_NUMBER++;
        database.close();
    }

    /**
     * Get the task row id
     *
     * @param task: task we're looking for
     * @return int with row id
     */
    private int findTaskRowId(Task task) {
        List<Task> tasks = getAllTasks();

        for (Task taskInList : tasks) {
            if (taskInList.equals(task)) {
                return taskInList.getId();
            }
        }
        return -1;
    }


    /**
     * Get all the tasks in the database
     *
     * @return return a ListArray of all the Tasks
     */
    private List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE_TASKS, null, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                tasks.add(new Task(
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID))),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DAY))),
                        Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_STAGE)))));

            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return tasks;
    }

    /**
     * Get all the tasks in the database
     *
     * @return return a ListArray of all the Tasks
     */
    public List<Task> getFilteredTasks(int filter) {
        List<Task> tasks = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE_TASKS, null, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DAY))) == filter) {
                    tasks.add(new Task(
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID))),
                            cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_DAY))),
                            Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_STAGE)))));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return tasks;
    }

    /**
     * set right NEXT_ROW_ID_NUMBER when adding an task
     */
    private void setupNextRowIdNumber() {
        List<Task> tasks = getAllTasks();

        if (tasks.size() == 0) {
            NEXT_ROW_ID_NUMBER = 0;
        } else {
            NEXT_ROW_ID_NUMBER = 1 + tasks.get(tasks.size() - 1).getId();
        }
    }
}
