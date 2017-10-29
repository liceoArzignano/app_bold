package it.liceoarzignano.bold.events

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import it.liceoarzignano.bold.database.DBHandler
import java.util.*
import kotlin.collections.LinkedHashMap

class EventsHandler
private constructor(context: Context) : DBHandler<Event>(context, DB_NAME, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) = db.execSQL("CREATE TABLE $tableName (" +
            "$KEY_ID INTEGER PRIMARY KEY, " +
            "$KEY_TITLE TEXT, " +
            "$KEY_DATE INTEGER, " +
            "$KEY_DESCRIPTION TEXT, " +
            "$KEY_CATEGORY INTEGER, " +
            "$KEY_HASHTAGS TEXT)")

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("ALTER TABLE $tableName ADD COLUMN $KEY_HASHTAGS TEXT DEFAULT \"\"")
        }
    }

    public override val all: MutableList<Event>
        get() {
            val list = ArrayList<Event>()
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $tableName ORDER BY $KEY_DATE DESC", null)
            if (cursor.moveToFirst()) {
                do {
                    list.add(Event(
                            cursor.getLong(0),
                            cursor.getString(1),
                            cursor.getLong(2),
                            cursor.getString(3),
                            cursor.getInt(4),
                            cursor.getString(5)))
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
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getLong(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getString(5))
        }
        cursor.close()
        return event
    }

    override fun getValues(item: Event): ContentValues {
        val values = ContentValues()
        values.put(KEY_TITLE, item.title)
        values.put(KEY_DATE, item.date)
        values.put(KEY_DESCRIPTION, item.description)
        values.put(KEY_CATEGORY, item.category)
        values.put(KEY_HASHTAGS, item.hashtags)
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
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getLong(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getString(5)))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    fun getByQuery(query: String?): List<Event> {
        if (query == null || query.isBlank()) {
            return all
        }

        val list = ArrayList<Event>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE $KEY_TITLE LIKE ? OR " +
                "$KEY_HASHTAGS LIKE ?", arrayOf("%$query%", "%$query%"))
        if (cursor.moveToFirst()) {
            do {
                list.add(Event(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getLong(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getString(5)))
            } while (cursor.moveToNext())
        }

        cursor.close()
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

        cursor.close()
        return map
    }

    companion object {
        private val DB_NAME = "EventDatabase.db"
        private val DB_VERSION = 2
        private val KEY_TITLE = "title"
        private val KEY_DATE = "date"
        private val KEY_DESCRIPTION = "description"
        private val KEY_CATEGORY = "category"
        private val KEY_HASHTAGS = "hashtags"

        // Singleton
        @Volatile private var INSTANCE: EventsHandler? = null
        fun getInstance(context: Context): EventsHandler =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: EventsHandler(context).also { INSTANCE = it }
                }
    }
}
