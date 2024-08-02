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
import java.util.Calendar;
import java.util.Date;


/**
 * Date converter utility.
 * 
 * @author patrick
 */
public final class DateUtil {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
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
     */
    public Date createTimeStamp(int year, int month, int day, int hour, int min, int sec, int miliSec) {
        //return toDate(LocalDateTime.of(year, month + 1, day, hour, min, sec, miliSec * 1000));
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, sec);
        cal.set(Calendar.MILLISECOND, miliSec);
        return cal.getTime();

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

        Calendar cal = Calendar.getInstance();
        String[] split = time.split(":");
        if (split == null || split.length <= 0 || split.length < 1) {
            throw new IllegalArgumentException("Invalid start time: " + time);
        }

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        int miliSecond = 0;

        try {
            hour = Integer.parseInt(split[0]);
            minute = 0;
            second = 0;
            miliSecond = 0;
            
            if (split.length > 1) {
                minute = Integer.parseInt(split[1]);
                if (split.length > 2) {
                    String secStr = split[2];
                    if (secStr.indexOf('.') > 0) {
                        split = secStr.split("\\.");
                        second = Integer.parseInt(split[0]);
    
                        if (split.length > 1) {
                            miliSecond = Integer.parseInt(split[1]);
                        }
                    } else {
                        second = Integer.parseInt(secStr);
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid start time: " + time);
        }

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, miliSecond);

        return cal.getTime();
    }

    
    /**
     * Parse a date
     *
     * @param date the date to parse like: 10.02.2004, 10.2.04@11:22
     * @return the date
     * @throws IllegalArgumentException In case of invalid dateformat
     */
    public Date parseDate(String date) {
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

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String dayStr = date;
        int idx = date.indexOf(timeSeparator);
        if (idx > 0) {
            dayStr = date.substring(0, idx);

            // parse time
            cal.setTime(parseTime(date.substring(idx + 1)));
        } else {
            cal.setTime(parseTime("0:0"));
            
        }

        char dateSep = '-';
        if (dayStr.indexOf('.') > 0) {
            dateSep = '.';
        }

        if (dayStr.indexOf(dateSep) > 0) {
            String[] split = dayStr.split("\\" + dateSep);
            if (split == null || split.length <= 0 || split.length < 3) {
                throw new IllegalArgumentException("Invalid start date!");
            }

            try {
                day = Integer.parseInt(split[0]);
                month = Integer.parseInt(split[1]) - 1; // to be compliant: month starts in java with 0
                year = Integer.parseInt(split[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid start date: " + dayStr);
            }
        }

        if (day > 31) {
            int y = year;
            year = day;
            day = y;
        }

        if (year < 100) {
            year += 2000;
        }

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal.getTime();
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
     * @param sep the separator
     * @return date as string
     */
    public String toDateString(Date date, String sep) {
        String europeanDatePattern = "dd.MM.yyyy" + sep + "HH:mm:ss";
        DateTimeFormatter europeanDateFormatter = DateTimeFormatter.ofPattern(europeanDatePattern);
        return europeanDateFormatter.format(toLocalDateTime(date));
    }

    
    /**
     * Converts a given date back into toolarium-date-notation and returns a well formed string of the following format:
     * &lt;dd.mm.yyyy@HH24:MI:SS&gt;
     *
     * @param instant the instant object
     * @return date as string
     */
    public String toDateString(Instant instant) {
        return toDateString(instant, "@");
    }

    
    /**
     * Converts a given date back into toolarium-date-notation and returns a well formed string of the following format:
     * &lt;dd.mm.yyyy@HH24:MI:SS&gt;
     *
     * @param instant the instant object
     * @param sep the separator
     * @return date as string
     */
    public String toDateString(Instant instant, String sep) {
        String europeanDatePattern = "dd.MM.yyyy" + sep + "HH:mm:ss";
        DateTimeFormatter europeanDateFormatter = DateTimeFormatter.ofPattern(europeanDatePattern);
        return europeanDateFormatter.format(instant);
    }


    /**
     * Converts a given date back into an iso standard:
     * &lt;yyyy-mm-ddTHH24:MI:SS&gt;
     *
     * @param date the date object
     * @return date as string
     */
    public String toTimestampString(Date date) {
        return DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
    }
}
