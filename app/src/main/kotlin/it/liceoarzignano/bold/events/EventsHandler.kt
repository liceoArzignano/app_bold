package it.liceoarzignano.bold.events

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import it.liceoarzignano.bold.database.DBHandler
import it.liceoarzignano.bold.utils.Time
import java.util.*
import kotlin.collections.LinkedHashMap

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
                "${DBHandler.Companion.KEY_ID} = ?", arrayOf(id.toString()))

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
            values.put(DBHandler.Companion.KEY_ID, item.id)
        }

        return values
    }

    override val tableName: String
        get() = "events"

    fun getDayEvents(diff: Int): List<Event> {
        val beginning = Calendar.getInstance()
        beginning.add(Calendar.DAY_OF_YEAR, diff)
        val end = beginning.clone() as Calendar

        beginning[Calendar.HOUR_OF_DAY] = 0
        beginning[Calendar.MINUTE] = 0
        beginning[Calendar.SECOND] = 0

        end[Calendar.HOUR_OF_DAY] = 23
        end[Calendar.MINUTE] = 59
        end[Calendar.SECOND] = 59

        val timestampBeginning = beginning.timeInMillis
        val timestampEnd = end.timeInMillis

        val list = ArrayList<Event>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName  WHERE $KEY_DATE  > " +
                "$timestampBeginning AND $KEY_DATE  < $timestampEnd ORDER BY $KEY_DATE DESC",
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
        val list = all

        if (query != null) {
            list.removeIf { it ->
                val title = it.title.toLowerCase()
                val date = Time(it.date).toString().toLowerCase()
                !title.contains(query.toLowerCase()) && !date.contains(query.toLowerCase())
            }
        }

        return list
    }

    fun getStats(limitTimestamp: Long): HashMap<Long, Pair<Int, Int>> {
        val map = LinkedHashMap<Long, Pair<Int, Int>>()
        val query = "SELECT COUNT($KEY_CATEGORY), $KEY_CATEGORY, $KEY_DATE " +
                "FROM $tableName WHERE $KEY_DATE > ? and $KEY_DATE < ? GROUP BY $KEY_CATEGORY " +
                "ORDER BY $KEY_DATE"

        val db = readableDatabase
        val cursor = db.rawQuery(query,
                arrayOf(System.currentTimeMillis().toString(), limitTimestamp.toString()))


        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    map.put(cursor.getLong(2), Pair(cursor.getInt(0), cursor.getInt(1)))
                } while (cursor.moveToNext())
            }
        }

        return map
    }

    companion object {
        private val DB_NAME = "EventDatabase.db"
        private val DB_VERSION = 1
        private val KEY_TITLE = "title"
        private val KEY_DATE = "date"
        private val KEY_DESCRIPTION = "description"
        private val KEY_CATEGORY = "category"

        // Singleton
        @Volatile private var INSTANCE: EventsHandler? = null
        fun getInstance(context: Context): EventsHandler =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: EventsHandler(context).also { INSTANCE = it }
                }
    }
}
