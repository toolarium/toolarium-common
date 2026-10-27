/*
 * DateUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Calendar;
import java.util.Date;


/**
 * Date converter utility.
 * 
 * @author patrick
 */
public final class DateUtil {
    private static final DateTimeFormatter EUROPEAN_DATE_AT = DateTimeFormatter.ofPattern("dd.MM.yyyy@HH:mm:ss");
    private static final DateTimeFormatter EUROPEAN_DATE_T = DateTimeFormatter.ofPattern("dd.MM.yyyy'T'HH:mm:ss");
    private static final DateTimeFormatter EUROPEAN_DATE_SPACE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");


    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final DateUtil INSTANCE = new DateUtil();
    }

    
    /**
     * Constructor
     */
    private DateUtil() {
        // NOP
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static DateUtil getInstance() {
        return HOLDER.INSTANCE;
    }

    
    /**
     * Creates a date from the given data
     *
     * @param year the year
     * @param month the month (January starts with 0)
     * @param day the day (start with 1)
     * @return the date
     * @throws IllegalArgumentException In case of an invalid date, e.g. 30 February
     */
    public Date createDate(int year, int month, int day) {
        return createTimeStamp(year, month, day, 0, 0, 0, 0);
    }
    

    /**
     * Creates a date from the given data
     *
     * @param year the year
     * @param month the month January starts with 0)
     * @param day the day (start with 1)
     * @return the date
     * @throws IllegalArgumentException In case of an invalid date, e.g. 30 February
     */
    public Date createTimeStamp(int year, int month, int day) {
        final Calendar cal = Calendar.getInstance();
        return createTimeStamp(year, month, day, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 0);
    }

    
    /**
     * Creates a date from the given data
     *
     * @param year the year
     * @param month the month (January starts with 0)
     * @param day the day (start with 1)
     * @param hour the hour (24h)
     * @param min the minute
     * @param sec the seconds
     * @param miliSec the milliseconds
     * @return the date
     * @throws IllegalArgumentException In case of an invalid date or time, e.g. 30 February or the hour 25
     */
    public Date createTimeStamp(int year, int month, int day, int hour, int min, int sec, int miliSec) {
        //return toDate(LocalDateTime.of(year, month + 1, day, hour, min, sec, miliSec * 1000));
        final Calendar cal = Calendar.getInstance();
        cal.setLenient(false);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, sec);
        cal.set(Calendar.MILLISECOND, miliSec);

        try {
            return cal.getTime();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid date: " + year + "-" + (month + 1) + "-" + day + " " + hour + ":" + min + ":" + sec + "." + miliSec);
        }
    }
    
    
    /**
     * Convert to {@link LocalDate}.
     *
     * @param dateToConvert the date to convert
     * @return the local date
     */
    public LocalDate toLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    
    /**
     * Convert to {@link LocalDateTime}.
     *
     * @param dateToConvert the date to convert
     * @return the local date time
     */
    public LocalDateTime toLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    
    /**
     * Convert to {@link Date}.
     *
     * @param dateToConvert the date to convert
     * @return the date
     */
    public Date toDate(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }    
    
    
    /**
     * Convert to {@link Date}.
     *
     * @param dateToConvert the date to convert
     * @return the date
     */
    public Date toDate(LocalDateTime dateToConvert) {
        return java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    
    /**
     * Parse the time
     * 
     * @param time the time to parse like: 11:22
     * @return the parsed time as date
     * @throws IllegalArgumentException In case of invalid time format
     */
    public Date parseTime(String time) {
        if (time == null || time.trim().length() == 0) {
            return null;
        }

        int[] timeFields = parseTimeFields(time);
        Calendar cal = Calendar.getInstance();
        cal.setLenient(false);
        setTimeFields(cal, timeFields);

        try {
            return cal.getTime();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid start time: " + time);
        }
    }


    /**
     * Parse the time fields
     * 
     * @param time the time to parse like: 11:22
     * @return the parsed time fields: hour, minute, second and millisecond
     * @throws IllegalArgumentException In case of invalid time format
     */
    private int[] parseTimeFields(String time) {
        // keep empty parts (e.g. "10:"), they are invalid like additional parts (e.g. "10:00:00:99")
        String[] split = time.trim().split(":", -1);
        if (split.length < 1 || split.length > 3) {
            throw new IllegalArgumentException("Invalid start time: " + time);
        }

        int hour = 0;
        int minute = 0;
        int second = 0;
        int miliSecond = 0;

        try {
            hour = Integer.parseInt(split[0]);
            
            if (split.length > 1) {
                minute = Integer.parseInt(split[1]);
                if (split.length > 2) {
                    String secStr = split[2];
                    if (secStr.indexOf('.') > 0) {
                        split = secStr.split("\\.");
                        second = Integer.parseInt(split[0]);
    
                        if (split.length > 1) {
                            miliSecond = parseFractionOfSecond(split[1]);
                        }
                    } else {
                        second = Integer.parseInt(secStr);
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid start time: " + time);
        }

        return new int[] {hour, minute, second, miliSecond};
    }


    /**
     * Set the time fields into the calendar
     * 
     * @param cal the calendar
     * @param timeFields the time fields: hour, minute, second and millisecond
     */
    private void setTimeFields(Calendar cal, int[] timeFields) {
        cal.set(Calendar.HOUR_OF_DAY, timeFields[0]);
        cal.set(Calendar.MINUTE, timeFields[1]);
        cal.set(Calendar.SECOND, timeFields[2]);
        cal.set(Calendar.MILLISECOND, timeFields[3]);
    }


    /**
     * Parse the fraction of a second into milliseconds, e.g. "5" is 500, "24" is 240 and "2421" is 242 milliseconds.
     *
     * @param fraction the fraction of the second (digits after the decimal point)
     * @return the milliseconds
     * @throws NumberFormatException In case of an invalid number
     */
    private int parseFractionOfSecond(String fraction) {
        StringBuilder millis = new StringBuilder(fraction.trim());
        if (millis.length() > 3) {
            millis.setLength(3);
        }

        while (millis.length() < 3) {
            millis.append('0');
        }

        return Integer.parseInt(millis.toString());
    }

    
    /**
     * Parse a date
     *
     * @param date the date to parse like: 10.02.2004, 10.2.04@11:22
     * @return the date or null if the date is null or empty
     * @throws IllegalArgumentException In case of invalid dateformat
     */
    public Date parseDate(String date) {
        if (date == null || date.trim().length() == 0) {
            return null;
        }

        if (date.indexOf('T') > 0) {
            return parseDate(date, "T");
        } else if (date.indexOf('@') > 0) {
            return parseDate(date, "@");
        }
        
        return parseDate(date, " ");
    }

    
    /**
     * Parse a date
     *
     * @param date the date to parse like: 10.02.2004, 10.2.04@11:22
     * @param timeSeparator the time separator
     * @return the date
     * @throws IllegalArgumentException In case of invalid dateformat
     */
    public Date parseDate(String date, String timeSeparator) {
        if (date == null || date.trim().length() == 0) {
            return null;
        }

        // surrounding whitespace is ignored
        final String trimmedDate = date.trim();
        Calendar cal = Calendar.getInstance();
        cal.setLenient(false);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String dayStr = trimmedDate;
        int[] timeFields = new int[] {0, 0, 0, 0};
        int idx = trimmedDate.indexOf(timeSeparator);
        if (idx > 0) {
            dayStr = trimmedDate.substring(0, idx);

            // parse time: it is validated together with the parsed date (not with the current date)
            String timeStr = trimmedDate.substring(idx + timeSeparator.length());
            if (timeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid start time: " + date);
            }
            timeFields = parseTimeFields(timeStr);
        }

        char dateSep = '-';
        if (dayStr.indexOf('.') > 0) {
            dateSep = '.';
        }

        if (dayStr.indexOf(dateSep) > 0) {
            // the limit -1 keeps trailing empty parts, so that e.g. 01.02.2024. is rejected
            String[] split = dayStr.split("\\" + dateSep, -1);
            if (split == null || split.length != 3) {
                throw new IllegalArgumentException("Invalid start date: " + dayStr);
            }

            try {
                day = Integer.parseInt(split[0]);
                month = Integer.parseInt(split[1]) - 1; // to be compliant: month starts in java with 0
                year = Integer.parseInt(split[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid start date: " + dayStr);
            }
        } else {
            throw new IllegalArgumentException("Invalid start date (no date separator): " + dayStr);
        }

        if (day > 31) {
            int y = year;
            year = day;
            day = y;
        }

        if (year < 0) {
            throw new IllegalArgumentException("Invalid start date: " + date);
        }

        if (year < 100) {
            year += 2000;
        }

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        setTimeFields(cal, timeFields);

        try {
            return cal.getTime();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid start date: " + date);
        }
    }


    /**
     * Converts a given date back into toolarium-date-notation and returns a well formed string of the following format:
     * &lt;dd.mm.yyyy@HH24:MI:SS&gt;
     *
     * @param date the date object
     * @return date as string
     */
    public String toDateString(Date date) {
        return toDateString(date, "@");
    }

    
    /**
     * Converts a given date back into toolarium-date-notation and returns a well formed string of the following format:
     * &lt;dd.mm.yyyy@HH24:MI:SS&gt;
     *
     * @param date the date object
     * @param sep the separator, null for the default separator @
     * @return date as string
     */
    public String toDateString(Date date, String sep) {
        return resolveFormatter(sep).format(toLocalDateTime(date));
    }

    
    /**
     * Converts a given date back into toolarium-date-notation and returns a well formed string of the following format:
     * &lt;dd.mm.yyyy@HH24:MI:SS&gt;. The instant is formatted in the system default time zone.
     *
     * @param instant the instant object
     * @return date as string
     */
    public String toDateString(Instant instant) {
        return toDateString(instant, "@");
    }

    
    /**
     * Converts a given date back into toolarium-date-notation and returns a well formed string of the following format:
     * &lt;dd.mm.yyyy@HH24:MI:SS&gt;. The instant is formatted in the system default time zone.
     *
     * @param instant the instant object
     * @param sep the separator, null for the default separator @
     * @return date as string
     */
    public String toDateString(Instant instant, String sep) {
        return resolveFormatter(sep).format(instant.atZone(ZoneId.systemDefault()));
    }


    /**
     * Resolve the cached formatter for the given separator
     *
     * @param sep the separator, null for the default separator @
     * @return the formatter
     */
    private DateTimeFormatter resolveFormatter(String sep) {
        if (sep == null || "@".equals(sep)) {
            // null is the default separator
            return EUROPEAN_DATE_AT;
        } else if ("T".equals(sep)) {
            return EUROPEAN_DATE_T;
        } else if (" ".equals(sep)) {
            return EUROPEAN_DATE_SPACE;
        }

        // the separator is always added as literal text (letters are not interpreted as pattern symbols)
        return new DateTimeFormatterBuilder()
                .appendPattern("dd.MM.yyyy")
                .appendLiteral(String.valueOf(sep))
                .appendPattern("HH:mm:ss")
                .toFormatter();
    }


    /**
     * Converts a given date into the ISO-8601 instant format in UTC, e.g. 2016-02-15T11:44:23.242Z
     * (see {@link java.time.format.DateTimeFormatter#ISO_INSTANT}). The fraction of a second is only added if it is not 0.
     *
     * @param date the date object
     * @return date as string
     */
    public String toTimestampString(Date date) {
        return DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
    }
}
