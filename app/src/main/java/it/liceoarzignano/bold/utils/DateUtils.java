package it.liceoarzignano.bold.utils;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import it.liceoarzignano.bold.R;

public final class DateUtils {
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private DateUtils() {
    }

    public static Date getDate(int dayDiff) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, dayDiff);
        return calendar.getTime();
    }

    public static String getDateString(int dayDiff) {
        return dateToString(getDate(dayDiff));
    }

    public static Date intToDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    public static String dateToString(Date date) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date);
    }

    public static Date stringToDate(String string) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ITALIAN);
            return dateFormat.parse(string);
        } catch (ParseException e) {
            throw new IllegalArgumentException(string + ": invalid. Must be " + DATE_FORMAT);
        }
    }

    public static String dateToWorldsString(Context context, Date date) {
        String val = new SimpleDateFormat(context.getString(R.string.date_formatting),
                Locale.getDefault()).format(date);
        // Uppecase first char
        return val.substring(0, 1).toUpperCase() + val.substring(1, val.length());
    }

    public static String dateToWorldsString(Context context, String date) {
        return dateToWorldsString(context, stringToDate(date));
    }

    public static int dateDiff(Date a, Date b) {
        Calendar calA = Calendar.getInstance();
        Calendar calB = Calendar.getInstance();
        calA.setTime(a);
        calB.setTime(b);

        return calA.get(Calendar.DAY_OF_YEAR) - calB.get(Calendar.DAY_OF_YEAR);
    }


    public static boolean dateDiff(Date a, Date b, int minDiff) {
        Calendar calA = Calendar.getInstance();
        Calendar calB = Calendar.getInstance();
        calA.setTime(a);
        calB.setTime(b);

        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
                calA.get(Calendar.DAY_OF_YEAR) - calB.get(Calendar.DAY_OF_YEAR) >= minDiff;
    }

}
