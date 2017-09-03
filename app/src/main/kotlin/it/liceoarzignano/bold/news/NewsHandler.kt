package it.liceoarzignano.bold.news

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import it.liceoarzignano.bold.database.DBHandler
import java.util.*

class NewsHandler private constructor(context: Context) : DBHandler<News>(context, DB_NAME, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) = db.execSQL("CREATE TABLE $tableName (" +
            "$KEY_ID INTEGER PRIMARY KEY, " +
            "$KEY_TITLE TEXT, " +
            "$KEY_DATE INTEGER, " +
            "$KEY_DESCRIPTION TEXT, " +
            "$KEY_URL TEXT)")

    // Update this when db table will be changed
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    public override val all: MutableList<News>
        get() {
            val list = ArrayList<News>()
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $tableName ORDER BY $KEY_DATE DESC", null)

            if (cursor.moveToFirst()) {
                do {
                    list.add(News(
                            cursor.getString(0).toLong(),
                            cursor.getString(1),
                            cursor.getString(2).toLong(),
                            cursor.getString(3),
                            cursor.getString(4)))
                } while (cursor.moveToNext())
            }

            cursor.close()
            return list
        }

    override fun get(id: Long): News? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE ${DBHandler.KEY_ID} = ?",
                arrayOf(id.toString()))

        var news: News? = null
        if (cursor.moveToFirst()) {
            news = News(
                    cursor.getString(0).toLong(),
                    cursor.getString(1),
                    cursor.getString(2).toLong(),
                    cursor.getString(3),
                    cursor.getString(4))
        }
        cursor.close()
        return news
    }

    override fun getValues(item: News, withId: Boolean): ContentValues {
        val values = ContentValues()
        values.put(KEY_TITLE, item.title)
        values.put(KEY_DATE, item.date)
        values.put(KEY_DESCRIPTION, item.description)
        values.put(KEY_URL, item.url)

        if (withId) {
            values.put(DBHandler.Companion.KEY_ID, item.id)
        }

        return values
    }

    override val tableName: String get() = "news"

    fun getByQuery(query: String?): List<News> {
        val list = all

        if (query != null) {
            list.removeIf { it ->
                val title = it.title.toLowerCase()
                val message = it.description.toLowerCase()
                val date = Date(it.date).toString().toLowerCase()
                !title.contains(query) &&
                        !date.contains(query) &&
                        !message.contains(query)
            }
        }

        return list
    }

    companion object {
        private val DB_NAME = "NewsDatabase.db"
        private val DB_VERSION = 1
        private val KEY_TITLE = "title"
        private val KEY_DATE = "date"
        private val KEY_DESCRIPTION = "description"
        private val KEY_URL = "url"

        // Singleton
        @Volatile private var INSTANCE: NewsHandler? = null
        fun getInstance(context: Context): NewsHandler =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: NewsHandler(context).also { INSTANCE = it }
                }
    }
}
