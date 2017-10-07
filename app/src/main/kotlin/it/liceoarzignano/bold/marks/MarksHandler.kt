package it.liceoarzignano.bold.marks

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import it.liceoarzignano.bold.database.DBHandler
import java.util.*
import kotlin.collections.HashMap

class MarksHandler private constructor(context: Context) :
        DBHandler<Mark>(context, DB_NAME, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) =
            db.execSQL("CREATE TABLE $tableName (" +
                    "$KEY_ID INTEGER PRIMARY KEY, " +
                    "$KEY_SUBJECT TEXT, " +
                    "$KEY_VALUE INTEGER, " +
                    "$KEY_DATE INTEGER, " +
                    "$KEY_DESCRIPTION TEXT, " +
                    "$KEY_FIRSTQUARTER INTEGER)")

    // Update this when db table will be changed
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) =
            Unit

    public override val all: List<Mark> get() = getFilteredMarks("", 0)

    override fun get(id: Long): Mark? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE " +
                "${DBHandler.KEY_ID} = ?", arrayOf(id.toString()))

        var mark: Mark? = null
        if (cursor.moveToFirst()) {
            mark = Mark(
                    cursor.getString(0).toLong(),
                    cursor.getString(1),
                    cursor.getString(2).toInt(),
                    cursor.getString(3).toLong(),
                    cursor.getString(4),
                    cursor.getString(5) == "1")
        }
        cursor.close()
        return mark
    }

    override fun getValues(item: Mark, withId: Boolean): ContentValues {
        val values = ContentValues()
        if (withId) {
            values.put(DBHandler.Companion.KEY_ID, item.id)
        }
        values.put(KEY_SUBJECT, item.subject)
        values.put(KEY_VALUE, item.value)
        values.put(KEY_DATE, item.date)
        values.put(KEY_DESCRIPTION, item.description)
        values.put(KEY_FIRSTQUARTER, if (item.isFirstQuarter) 1 else 0)

        return values
    }

    override val tableName: String get() = "marks"

    fun getFilteredMarks(filter: String, quarter: Int): List<Mark> {
        val query: String
        val args: Array<String>

        if (filter.isBlank()) {
            args = emptyArray()
            query = when (quarter) {
                1 -> "SELECT * FROM $tableName  WHERE $KEY_FIRSTQUARTER = 1 " +
                        "ORDER BY $KEY_DATE DESC"
                2 -> "SELECT * FROM $tableName WHERE $KEY_FIRSTQUARTER = 0 " +
                        "ORDER BY $KEY_DATE DESC"
                else -> "SELECT * FROM $tableName ORDER BY $KEY_DATE DESC"
            }
        } else {
            args = arrayOf(filter)
            query = when (quarter) {
                1 -> "SELECT * FROM $tableName WHERE $KEY_FIRSTQUARTER = 1 " +
                        "AND  $KEY_SUBJECT = ? ORDER BY $KEY_DATE DESC"
                2 -> "SELECT * FROM $tableName WHERE $KEY_FIRSTQUARTER = 1 " +
                        "AND  $KEY_SUBJECT = ? ORDER BY $KEY_DATE DESC"
                else -> "SELECT * FROM $tableName WHERE  $KEY_SUBJECT = ? " +
                        "ORDER BY $KEY_DATE DESC"
            }
        }

        val list = ArrayList<Mark>()
        val db = readableDatabase
        val cursor = db.rawQuery(query, args)

        if (cursor.moveToFirst()) {
            do {
                list.add(Mark(
                        cursor.getString(0).toLong(),
                        cursor.getString(1),
                        cursor.getString(2).toInt(),
                        cursor.getString(3).toLong(),
                        cursor.getString(4),
                        cursor.getString(5) == "1"))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    fun getAverage(filter: String, quarter: Int): Double {
        val list = getFilteredMarks(filter, quarter)
        if (list.isEmpty()) {
            return 0.0
        }

        val sum = list.sumByDouble { it.value.toDouble() }
        return sum / (100 * list.size)
    }

    fun whatShouldIGet(filter: String, quarter: Int): Double {
        val list = getFilteredMarks(filter, quarter)
        if (list.isEmpty()) {
            return 0.0
        }

        val sum = list.sumByDouble { it.value.toDouble() }
        return 6 * (list.size + 1) - sum / 100
    }

    fun getAllSubjectsAverages(quarter: Int): HashMap<Double, String> {
        val query = "SELECT AVG($KEY_VALUE), $KEY_SUBJECT FROM $tableName " +
                "WHERE $KEY_FIRSTQUARTER = ? GROUP BY $KEY_SUBJECT"
        val map = HashMap<Double, String>()
        val db = readableDatabase
        val cursor = db.rawQuery(query, arrayOf("$quarter"))

        if (cursor.moveToFirst()) {
            do {
                map.put(cursor.getDouble(0) / 100, cursor.getString(1))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return map

    }

    companion object {
        private val DB_NAME = "MarksDatabase.db"
        private val DB_VERSION = 1
        private val KEY_SUBJECT = "subject"
        private val KEY_VALUE = "value"
        private val KEY_DATE = "date"
        private val KEY_DESCRIPTION = "description"
        private val KEY_FIRSTQUARTER = "firstQuarter"

        // Singleton
        @Volatile private var INSTANCE: MarksHandler? = null
        fun getInstance(context: Context): MarksHandler =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: MarksHandler(context).also { INSTANCE = it }
                }
    }
}
