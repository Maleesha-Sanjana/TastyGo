package com.tastygo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    private static final String PATTERN_DB = "yyyy-MM-dd HH:mm:ss";
    private static final String PATTERN_DATE_ONLY = "yyyy-MM-dd";
    private static final String PATTERN_DISPLAY = "MMM dd, yyyy - hh:mm a";
    private static final String PATTERN_DISPLAY_DATE = "MMM dd, yyyy";
    private static final String PATTERN_DISPLAY_TIME = "hh:mm a";

    public static String now() {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DB, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String today() {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DATE_ONLY, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String formatDisplay(String dbTimestamp) {
        if (dbTimestamp == null || dbTimestamp.trim().isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat(PATTERN_DB, Locale.getDefault());
            Date date = dbFormat.parse(dbTimestamp);
            if (date != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat(PATTERN_DISPLAY, Locale.getDefault());
                return displayFormat.format(date);
            }
        } catch (ParseException ignored) {
            try {
                SimpleDateFormat dateOnly = new SimpleDateFormat(PATTERN_DATE_ONLY, Locale.getDefault());
                Date date = dateOnly.parse(dbTimestamp);
                if (date != null) {
                    SimpleDateFormat displayDate = new SimpleDateFormat(PATTERN_DISPLAY_DATE, Locale.getDefault());
                    return displayDate.format(date);
                }
            } catch (ParseException e) {
                return dbTimestamp;
            }
        }
        return dbTimestamp;
    }

    public static String formatDateOnlyDisplay(String dbDate) {
        if (dbDate == null || dbDate.trim().isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat(PATTERN_DATE_ONLY, Locale.getDefault());
            Date date = dbFormat.parse(dbDate);
            if (date != null) {
                SimpleDateFormat display = new SimpleDateFormat(PATTERN_DISPLAY_DATE, Locale.getDefault());
                return display.format(date);
            }
        } catch (ParseException e) {
            return dbDate;
        }
        return dbDate;
    }
}
