package it.liceoarzignano.bold.backup

import android.app.Activity
import android.content.Context
import android.util.Log
import it.liceoarzignano.bold.events.Event
import it.liceoarzignano.bold.events.EventsHandler
import it.liceoarzignano.bold.marks.Mark
import it.liceoarzignano.bold.marks.MarksHandler
import it.liceoarzignano.bold.news.News
import it.liceoarzignano.bold.news.NewsHandler
import java.io.*
import java.util.*

internal class BackupFile(activity: Activity) {

    private val mBuilder = StringBuilder()
    private val mDataPath = activity.cacheDir.absolutePath

    private var mRestore: File? = null

    fun createBackup(context: Context) {
        addMarks(MarksHandler.getInstance(context).all)
        addEvents(EventsHandler.getInstance(context).all)
        addNews(NewsHandler.getInstance(context).all)
    }

    private fun addMarks(list: List<Mark>) {
        mBuilder.append(MARK_HEADER).append('\n')
        for (item in list) {
            mBuilder.append(item.id)
                    .append(SEPARATOR)
                    .append(item.subject.replace(",", COMMA_REPLACER))
                    .append(SEPARATOR)
                    // Some locales use comma instead of dot as separator
                    .append(item.value.toString().replace(",", COMMA_REPLACER))
                    .append(SEPARATOR)
                    .append(item.date)
                    .append(SEPARATOR)
                    .append(item.description.replace(",", COMMA_REPLACER))
                    .append(SEPARATOR)
                    .append(item.isFirstQuarter.toString())
                    .append('\n')
        }
    }

    private fun addEvents(list: List<Event>) {
        mBuilder.append(EVENT_HEADER).append('\n')
        for (item in list) {
            mBuilder.append(item.id)
                    .append(SEPARATOR)
                    .append(item.title.replace(",", COMMA_REPLACER))
                    .append(SEPARATOR)
                    .append(item.date)
                    .append(SEPARATOR)
                    .append(item.description.replace(",", COMMA_REPLACER))
                    .append(SEPARATOR)
                    .append(item.category.toString())
                    .append('\n')
        }
    }

    private fun addNews(list: List<News>) {
        mBuilder.append(NEWS_HEADER).append('\n')
        for (item in list) {
            mBuilder.append(item.id)
                    .append(SEPARATOR)
                    .append(item.title.replace(",", COMMA_REPLACER))
                    .append(SEPARATOR)
                    .append(item.date)
                    .append(SEPARATOR)
                    .append(item.description.replace(",", COMMA_REPLACER))
                    .append(SEPARATOR)
                    .append(item.url.replace(",", COMMA_REPLACER))
                    .append('\n')
        }
    }

    val output: File
        get() {
            val file = File(filePath)
            try {
                val writer = FileWriter(file)
                writer.write(mBuilder.toString())
                writer.close()
            } catch (e: IOException) {
                Log.e(TAG, e.message)
            }

            return file
        }

    private val filePath: String
        get() = mDataPath + "/" + FILE_NAME

    fun fetch(iStream: InputStream) {
        mRestore = File(mDataPath + "/" + FILE_NAME)

        try {
            // Create file using bytes from gdrive
            val oStream = FileOutputStream(mRestore!!)
            val buffer = ByteArray(4 * 1024)
            var read = iStream.read(buffer)
            while (read != -1) {
                oStream.write(buffer, 0, read)
                read = iStream.read(buffer)
            }
            oStream.flush()
            oStream.close()
        } catch (e: IOException) {
            Log.e(TAG, e.message)
        }

    }

    // Marks header
    val marks: List<Mark>
        get() {
            val list = ArrayList<Mark>()
            try {
                val reader = BufferedReader(FileReader(mRestore!!))
                reader.readLine()
                var line: String? = reader.readLine()
                do {
                    val data = line!!
                            .split(SEPARATOR.toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    list.add(Mark(
                            data[0].toLong(),
                            data[1].replace(COMMA_REPLACER, ","),
                            data[2].replace(COMMA_REPLACER, ",").toInt(),
                            data[3].replace(COMMA_REPLACER, ",").toLong(),
                            data[4].replace(COMMA_REPLACER, ","),
                            "1" == data[5]))
                    line = reader.readLine()
                } while (line != null && EVENT_HEADER != line)
            } catch (e: IOException) {
                Log.e(TAG, e.message)
            }

            return list
        }

    // Move cursor to events part
    val events: List<Event>
        get() {
            val list = ArrayList<Event>()
            try {
                val reader = BufferedReader(FileReader(mRestore!!))
                var line: String? = reader.readLine()
                while (EVENT_HEADER != line) {
                    line = reader.readLine()
                }

                line = reader.readLine()

                do {
                    val data = line!!
                            .split(SEPARATOR.toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    list.add(Event(
                            data[0].toLong(),
                            data[1].replace(COMMA_REPLACER, ","),
                            data[2].replace(COMMA_REPLACER, ",").toLong(),
                            data[3].replace(COMMA_REPLACER, ","),
                            data[4].toInt(),
                            data[5].replace(COMMA_REPLACER, ",")))
                    line = reader.readLine()
                } while (line != null && NEWS_HEADER != line)
            } catch (e: IOException) {
                Log.e(TAG, e.message)
            }

            return list
        }

    // Move cursor to news part
    val news: List<News>
        get() {
            val list = ArrayList<News>()
            try {
                val reader = BufferedReader(FileReader(mRestore!!))
                var line: String? = reader.readLine()
                while (NEWS_HEADER != line) {
                    line = reader.readLine()
                }
                line = reader.readLine()

                do {
                    val data = line!!
                            .split(SEPARATOR.toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    list.add(News(
                            data[0].toLong(),
                            data[1].replace(COMMA_REPLACER, ","),
                            data[2].replace(COMMA_REPLACER, ",").toLong(),
                            data[3].replace(COMMA_REPLACER, ","),
                            data[4].replace(COMMA_REPLACER, ","))
                    )
                    line = reader.readLine()
                } while (line != null)
            } catch (e: IOException) {
                Log.e(TAG, e.message)
            }

            return list
        }

    companion object {
        private val TAG = "BackupFile"
        val FILE_NAME = "Liceo.backup"
        private val MARK_HEADER = "_ID, subject, value, time, description, firstQuarter"
        private val EVENT_HEADER = "_ID, title, time, description, category"
        private val NEWS_HEADER = "_ID, title, time, description, url"
        private val COMMA_REPLACER = "\u2016"
        private val SEPARATOR = ", "
    }
}
