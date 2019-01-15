package it.liceoarzignano.bold.events

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import it.liceoarzignano.bold.database.DBHandler
import it.liceoarzignano.bold.utils.Time
import java.util.*

class EventsHandler
private constructor(context: Context) : DBHandler<Event>(context, DB_NAME, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) = db.execSQL("CREATE TABLE $tableName (" +
            "$KEY_ID INTEGER PRIMARY KEY, " +
            "$KEY_TITLE TEXT, " +
            "$KEY_DATE INTEGER, " +
            "$KEY_DESCRIPTION TEXT, " +
            "$KEY_CATEGORY INTEGER)")

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = // Update this when db table will be changed
            Unit

    public override val all: MutableList<Event>
        get() {
            val list = ArrayList<Event>()
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $tableName ORDER BY $KEY_DATE DESC", null)
            if (cursor.moveToFirst()) {
                do {
                    list.add(Event(
                            cursor.getString(0).toLong(),
                            cursor.getString(1),
                            cursor.getString(2).toLong(),
                            cursor.getString(3),
                            cursor.getString(4).toInt()))
                } while (cursor.moveToNext())
            }

            cursor.close()
            return list
        }

    override fun get(id: Long): Event? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName  WHERE " +
                "${DBHandler.KEY_ID} = ?", arrayOf(id.toString()))

        var event: Event? = null
        if (cursor.moveToFirst()) {
            event = Event(
                    cursor.getString(0).toLong(),
                    cursor.getString(1),
                    cursor.getString(2).toLong(),
                    cursor.getString(3),
                    cursor.getString(4).toInt())
        }
        cursor.close()
        return event
    }

    override fun getValues(item: Event, withId: Boolean): ContentValues {
        val values = ContentValues()
        values.put(KEY_TITLE, item.title)
        values.put(KEY_DATE, item.date)
        values.put(KEY_DESCRIPTION, item.description)
        values.put(KEY_CATEGORY, item.category)

        if (withId) {
            values.put(DBHandler.KEY_ID, item.id)
        }

        return values
    }

    override val tableName: String
        get() = "events"

    val tomorrow: List<Event>
        get() {
            val tomorrow = Calendar.getInstance()
            val today = Calendar.getInstance()
            tomorrow.add(Calendar.DAY_OF_YEAR, 1)

            // Set at the end of the day to include events at any hour
            tomorrow.set(Calendar.HOUR_OF_DAY, 23)
            tomorrow.set(Calendar.MINUTE, 59)
            tomorrow.set(Calendar.SECOND, 59)
            today.set(Calendar.HOUR_OF_DAY, 23)
            today.set(Calendar.MINUTE, 59)
            today.set(Calendar.SECOND, 59)

            val timestampTomorrow = tomorrow.timeInMillis
            val timestampToday = today.timeInMillis

            val list = ArrayList<Event>()
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $tableName  WHERE $KEY_DATE  > " +
                    "$timestampToday AND $KEY_DATE  < $timestampTomorrow ORDER BY $KEY_DATE DESC",
                    null)

            if (cursor.moveToFirst()) {
                do {
                    list.add(Event(
                            cursor.getString(0).toLong(),
                            cursor.getString(1),
                            cursor.getString(2).toLong(),
                            cursor.getString(3),
                            cursor.getString(4).toInt()))
                } while (cursor.moveToNext())
            }

            cursor.close()
            return list

        }

    fun getByQuery(query: String?): List<Event> {
        var list = all

        if (query != null) {
            list = list.filter {
                val title = it.title.toLowerCase()
                val date = Time(it.date).toString().toLowerCase()
                title.contains(query.toLowerCase()) || date.contains(query.toLowerCase())
            }.toMutableList()
        }

        return list
    }

    companion object {
        private const val DB_NAME = "EventDatabase.db"
        private const val DB_VERSION = 1
        private const val KEY_TITLE = "title"
        private const val KEY_DATE = "date"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_CATEGORY = "category"

        // Singleton
        @Volatile private var INSTANCE: EventsHandler? = null
        fun getInstance(context: Context): EventsHandler =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: EventsHandler(context).also { INSTANCE = it }
                }
    }
}
