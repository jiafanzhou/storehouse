package com.storehouse.app.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class DateUtils {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private DateUtils() {
    }

    public static Date getAsDateTime(final String dateTime) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(dateTime);
        } catch (final ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static String formatDateTime(final Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    public static Date currentDatePlusDays(final int days) {
        // this is a Java8 feature
        final LocalDateTime localDateTime = LocalDateTime.now(); // current date
        return Date.from(localDateTime.plusDays(days).atZone(ZoneId.systemDefault()).toInstant()); // add days
    }
}
