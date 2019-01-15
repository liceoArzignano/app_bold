package it.liceoarzignano.bold.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

abstract class DBHandler<T : DBItem>
protected constructor(context: Context, name: String, version: Int) :
        SQLiteOpenHelper(context, name, null, version) {

    fun add(item: T) {
        if (item.id == -1L) {
            item.id = count.toLong()
        }

        val db = writableDatabase
        db.insert(tableName, null, getValues(item, true))
    }

    fun update(item: T) {
        val db = writableDatabase
        db.update(tableName, getValues(item, false), "$KEY_ID=?",
                arrayOf(item.id.toString()))
    }

    fun delete(id: Long) {
        val db = writableDatabase
        db.delete(tableName, "$KEY_ID=?", arrayOf(id.toString()))
    }

    fun refillTable(items: List<T>) {
        val db = writableDatabase
        val list = all
        for (item in list) {
            db.delete(tableName, "$KEY_ID=?", arrayOf(item.id.toString()))
        }

        for (item in items) {
            db.insert(tableName, null, getValues(item, true))
        }
    }

    fun clearTable() {
        val db = writableDatabase
        val list = all
        for (item in list) {
            db.delete(tableName, "$KEY_ID=?", arrayOf(item.id.toString()))
        }
    }

    private val count: Int
        get() {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $tableName", null)
            val count = cursor.count
            cursor.close()
            return count
        }

    protected abstract val all: List<T>
    abstract operator fun get(id: Long): T?
    protected abstract fun getValues(item: T, withId: Boolean): ContentValues
    protected abstract val tableName: String

    companion object {
        @JvmStatic val KEY_ID = BaseColumns._ID
    }
}
