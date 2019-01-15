package it.liceoarzignano.bold.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.IntDef
import it.liceoarzignano.bold.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Time : Date {
    constructor() : super()

    constructor(dayDiff: Int) {
        put { it.increase(DAY_OF_YEAR, dayDiff) }
    }

    constructor(longTime: Long) : super(longTime)

    constructor(year: Int, month: Int, day: Int) {
        put {
            it.put(YEAR, year)
            it.put(MONTH, month)
            it.put(DAY, day)
        }
    }

    constructor(year: Int, month: Int, day: Int, hours: Int, minutes: Int) {
        put {
            it.put(YEAR, year)
            it.put(MONTH, month)
            it.put(DAY, day)
            it.put(HOURS, hours)
            it.put(MINUTES, minutes)
        }
    }

    override fun toString():String = SimpleDateFormat(DATE_FORMAT, APP_LOCALE).format(this)

    @SuppressLint("StringFormatInvalid", "SimpleDateFormat")
    fun asString(context: Context): String {
        val string = SimpleDateFormat(context.getString(R.string.date_formatting, APP_LOCALE))
                .format(this)
        // Uppercase first char
        return "${string.substring(0, 1).toUpperCase()}${string.substring(1, string.length)}"
    }

    fun diff(compared: Time): Int = getField(DAY_OF_YEAR) - compared.getField(DAY_OF_YEAR)

    fun diff(compared: Time, minDiff: Int): Boolean = diff(compared) >= minDiff

    fun matchDayOfYear(compared: Time): Boolean = getField(YEAR) == compared.getField(YEAR) &&
            getField(DAY_OF_YEAR) >= compared.getField(DAY_OF_YEAR)

    fun isFirstQuarter(context: Context): Boolean {
        val start = parse(context.getString(R.string.config_end_of_year)).getField(DAY_OF_YEAR)
        val end = parse(context.getString(R.string.config_quarter_change)).getField(DAY_OF_YEAR)
        val thisDay = getField(DAY_OF_YEAR)

        return thisDay < end || thisDay > start
    }

    private fun Calendar.put(@CalendarField field: Int, value: Int) = set(field, value)

    private inline fun put(operation: (Calendar) -> Unit) {
        val cal = Calendar.getInstance()
        operation(cal)
        this.time = cal.timeInMillis
    }

    private fun getField(@CalendarField field: Int): Int {
        val cal = Calendar.getInstance()
        cal.time = this
        return cal.get(field)
    }

    private fun Calendar.increase(@CalendarField field: Int, value: Int) {
        time = this@Time
        add(field, value)
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd"
        private val APP_LOCALE = Locale.ITALIAN

        fun parse(value: String): Time = try {
            val date = SimpleDateFormat(DATE_FORMAT, APP_LOCALE).parse(value)
            Time(date.time)
        } catch (e: ParseException) {
            throw IllegalArgumentException("$value: invalid format. Must be $DATE_FORMAT")
        }

        @IntDef(YEAR, MONTH, DAY, DAY_OF_YEAR, HOURS, MINUTES)
        @Retention(AnnotationRetention.SOURCE)
        annotation class CalendarField
            private const val DAY_OF_YEAR = Calendar.DAY_OF_YEAR
            private const val YEAR = Calendar.YEAR
            private const val MONTH = Calendar.MONTH
            private const val DAY = Calendar.DAY_OF_MONTH
            private const val HOURS = Calendar.HOUR
            private const val MINUTES = Calendar.MINUTE
    }
}