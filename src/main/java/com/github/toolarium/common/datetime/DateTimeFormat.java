/*
 * DateTimeFormat.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.datetime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Collection of a date time format
 *
 * @author patrick
 */
public final class DateTimeFormat {

    /**
     * US locale - all HTTP dates are in English
     */
    public static final Locale LOCALE_US = Locale.US;


    /**
     * GMT time zone - all HTTP dates are on GMT
     */
    public static final TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");


    /**
     * format for RFC 1123 date string -- "Sun, 06 Nov 1994 08:49:37 GMT"
     */
    public static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";


    /**
     * Format for http response header date field
     */
    public static final String HTTP_RESPONSE_DATE_HEADER = "EEE, dd MMM yyyy HH:mm:ss zzz";


    /**
     * Pattern used for old cookies
     */
    public static final String OLD_COOKIE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss z";


    private static final String RFC_1036_PATTERN = "EEEEEEEEE, dd-MMM-yy HH:mm:ss z";
    private static final String ASCTIME_PATTERN = "EEE MMM d HH:mm:ss yyyy";
    private static final ThreadLocal<DateFormat> RFC_1123_FORMAT_TL = ThreadLocal.withInitial(() -> createFormat(RFC1123_PATTERN));
    private static final ThreadLocal<DateFormat> OLD_COOKIE_FORMAT_TL = ThreadLocal.withInitial(() -> createFormat(OLD_COOKIE_PATTERN));
    private static final ThreadLocal<DateFormat> RFC_1036_FORMAT_TL = ThreadLocal.withInitial(() -> createFormat(RFC_1036_PATTERN));
    private static final ThreadLocal<DateFormat> ASCTIME_FORMAT_TL = ThreadLocal.withInitial(() -> createFormat(ASCTIME_PATTERN));


    /**
     * DateFormat to be used to format dates.
     * @deprecated Use {@link #getRfc1123Format()} instead. This shared static instance is not thread-safe.
     */
    @Deprecated
    public static final DateFormat RFC_1123_FORMAT = createFormat(RFC1123_PATTERN);


    /**
     * Format for RFC 1036 date string -- "Sunday, 06-Nov-94 08:49:37 GMT".
     * @deprecated Use {@link #getRfc1036Format()} instead. This shared static instance is not thread-safe.
     */
    @Deprecated
    public static final DateFormat RFC_1036_FORMAT = createFormat(RFC_1036_PATTERN);


    /**
     * Format for C asctime() date string -- "Sun Nov  6 08:49:37 1994".
     * @deprecated Use {@link #getAsctimeFormat()} instead. This shared static instance is not thread-safe.
     */
    @Deprecated
    public static final DateFormat ASCTIME_FORMAT = createFormat(ASCTIME_PATTERN);


    /**
     * Constructor for DateTimeFormat
     */
    private DateTimeFormat() {
        // NOP
    }


    /**
     * Returns a new thread-safe RFC 1123 DateFormat instance.
     *
     * @return the DateFormat for RFC 1123
     */
    public static DateFormat getRfc1123Format() {
        return (DateFormat) RFC_1123_FORMAT_TL.get().clone();
    }


    /**
     * Returns a new thread-safe old cookie DateFormat instance.
     *
     * @return the DateFormat for old cookies
     */
    public static DateFormat getOldCookieFormat() {
        return (DateFormat) OLD_COOKIE_FORMAT_TL.get().clone();
    }


    /**
     * Returns a new thread-safe RFC 1036 DateFormat instance.
     *
     * @return the DateFormat for RFC 1036
     */
    public static DateFormat getRfc1036Format() {
        return (DateFormat) RFC_1036_FORMAT_TL.get().clone();
    }


    /**
     * Returns a new thread-safe asctime DateFormat instance.
     *
     * @return the DateFormat for asctime
     */
    public static DateFormat getAsctimeFormat() {
        return (DateFormat) ASCTIME_FORMAT_TL.get().clone();
    }


    /**
     * Creates a new date format with the US locale and the GMT time zone. The format is not lenient:
     * out of range values like 32 Jan or 25:61 are rejected by parse.
     *
     * @param pattern the pattern
     * @return the new date format instance
     */
    private static DateFormat createFormat(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, LOCALE_US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // own instance, the public GMT_ZONE is mutable
        sdf.setLenient(false);
        return sdf;
    }
}
