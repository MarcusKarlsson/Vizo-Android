package com.vizo.news.utils;

import android.content.Context;

import com.vizo.news.R;

import org.joda.time.DateTimeZone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class which defines date management callbacks
 * <p/>
 * Created by nine3_marks on 6/6/2015.
 */
public class DateUtils {

    private Context context;
    private static DateUtils instance = null;
    private TimeZone defaultTimeZone = null;

    public static synchronized DateUtils init(Context context) {
        if (instance == null) {
            instance = new DateUtils(context);
        }
        return instance;
    }

    public static DateUtils getInstance() {
        return instance;
    }

    private DateUtils(Context context) {
        this.context = context;
        this.defaultTimeZone = TimeZone.getTimeZone("GMT-4");
    }

    /**
     * Convert string to time with format
     *
     * @param timeString String object which contains time
     * @param timeFormat The format to be used for parsing
     * @return Calendar object which corresponds to timeString
     */
    public Calendar convertStringToTimeWithFormat(String timeString,
                                                  String timeFormat) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(timeFormat, Locale.getDefault());
        formatter.setTimeZone(defaultTimeZone);
        Date date;
        try {
            date = formatter.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        calendar.setTime(date);
        return calendar;
    }

    /**
     * <p>
     * Checks if two calendars represent the same day ignoring time.
     * </p>
     *
     * @param cal1 the first calendar, not altered, not null
     * @param cal2 the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     */
    public boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1
                .get(Calendar.DAY_OF_YEAR) == cal2
                .get(Calendar.DAY_OF_YEAR));
    }

    /**
     * <p>
     * Checks if a calendar date is today.
     * </p>
     *
     * @param cal the calendar, not altered, not null
     * @return true if cal date is today
     * @throws IllegalArgumentException if the calendar is <code>null</code>
     */
    public boolean isToday(Calendar cal) {
        return isSameDay(cal, Calendar.getInstance());
    }

    /**
     * Convert time to string with format
     *
     * @param time   Calendar object for time
     * @param format Format in string type
     * @return Generated formatted string
     */
    public String convertTimeToStringWithFormat(Calendar time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        String timeString = sdf.format(time.getTime());
        return timeString;
    }

    /**
     * Generate string from hours which indicates Morning, Afternoon, Evening
     *
     * @param hours Hours in integer type
     * @return Generated greeting string
     */
    public String timePeriod(int hours) {
        String greeting;
        if (hours >= 0 && hours < 12) {

            // This time is Morning
            greeting = context.getResources().getString(R.string.morning);

        } else if (hours >= 12 && hours <= 16) {

            // This time is Afternoon
            greeting = context.getResources().getString(R.string.afternoon);

        } else {

            // This time is Evening
            greeting = context.getResources().getString(R.string.evening);
        }

        return greeting;
    }
}
