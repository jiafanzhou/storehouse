package com.storehouse.app.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Utility class to deal with date.
 *
 * @author ejiafzh
 *
 */
public final class DateUtils {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Make this a private constructor.
     */
    private DateUtils() {
    }

    /**
     * Convert the dateTime from String to a Date object.
     *
     * @param dateTime
     *            string representation of date and time.
     * @return object representation of date and time.
     */
    public static Date getAsDateTime(final String dateTime) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(dateTime);
        } catch (final ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Convert the dateTime from Object to a String object.
     *
     * @param dateTime
     *            object representation of date and time.
     * @return String representation of date and time.
     */
    public static String formatDateTime(final Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    /**
     * This method could be useful if we start to support a Timeout function
     * in our application, such as timeout all the pending order status.
     *
     * @param days
     *            number of days to be added or subtracted.
     * @return an added date
     */
    public static Date currentDatePlusDays(final int days) {
        // this is a Java8 feature
        final LocalDateTime localDateTime = LocalDateTime.now(); // current date
        return Date.from(localDateTime.plusDays(days).atZone(ZoneId.systemDefault()).toInstant()); // add days
    }
}
