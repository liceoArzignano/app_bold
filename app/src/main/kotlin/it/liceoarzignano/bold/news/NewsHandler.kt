package it.liceoarzignano.bold.news

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import it.liceoarzignano.bold.database.DBHandler

class NewsHandler private constructor(context: Context) : DBHandler<News>(context, DB_NAME, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) = db.execSQL("CREATE TABLE $tableName (" +
            "$KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$KEY_TITLE TEXT, " +
            "$KEY_DATE INTEGER, " +
            "$KEY_DESCRIPTION TEXT, " +
            "$KEY_URL TEXT," +
            "$KEY_UNREAD INTEGER)")

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("ALTER TABLE $tableName ADD COLUMN $KEY_UNREAD INTEGER DEFAULT 0")
        }
    }

    public override val all: MutableList<News>
        get() {
            val list = ArrayList<News>()
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $tableName ORDER BY $KEY_DATE DESC", null)

            if (cursor.moveToFirst()) {
                do {
                    list.add(News(
                            cursor.getLong(0),
                            cursor.getString(1),
                            cursor.getLong(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getInt(5) == 1))
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
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getLong(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5) == 1)
        }
        cursor.close()
        return news
    }

    override fun getValues(item: News): ContentValues {
        val values = ContentValues()
        values.put(KEY_TITLE, item.title)
        values.put(KEY_DATE, item.date)
        values.put(KEY_DESCRIPTION, item.description)
        values.put(KEY_URL, item.url)
        values.put(KEY_UNREAD, if (item.unread) 1 else 0)
        return values
    }

    override val tableName: String get() = "news"

    fun getByQuery(query: String?): List<News> {
        if (query == null || query.isBlank()) {
            return all
        }

        val list = ArrayList<News>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE $KEY_TITLE LIKE ?",
                arrayOf("%$query%"))
        if (cursor.moveToFirst()) {
            do {
                list.add(News(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getLong(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5) == 1))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    fun getByUrl(query: String): List<News> {
        val list = ArrayList<News>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE $KEY_URL LIKE ?",
                arrayOf("%$query%"))
        if (cursor.moveToFirst()) {
            do {
                list.add(News(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getLong(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5) == 1))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list

    }

    fun getUnread(): List<News> {
        val list = ArrayList<News>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName ORDER BY $KEY_DATE DESC, $KEY_UNREAD DESC", emptyArray())
        if (cursor.moveToFirst()) {
            do {
                list.add(News(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getLong(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5) == 1))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    fun hasUnread(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE $KEY_UNREAD = 1", emptyArray())

        val hasItems = cursor.count != 0
        cursor.close()
        return hasItems
    }

    companion object {
        private val DB_NAME = "NewsDatabase.db"
        private val DB_VERSION = 2
        private val KEY_TITLE = "title"
        private val KEY_DATE = "date"
        private val KEY_DESCRIPTION = "description"
        private val KEY_URL = "url"
        private val KEY_UNREAD = "unread"

        // Singleton
        @Volatile private var INSTANCE: NewsHandler? = null
        fun getInstance(context: Context): NewsHandler =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: NewsHandler(context).also { INSTANCE = it }
                }
    }
}
