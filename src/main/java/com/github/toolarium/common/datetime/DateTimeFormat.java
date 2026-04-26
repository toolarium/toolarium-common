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


    private static final ThreadLocal<DateFormat> RFC_1123_FORMAT_TL = ThreadLocal.withInitial(() -> {
        SimpleDateFormat sdf = new SimpleDateFormat(RFC1123_PATTERN, LOCALE_US);
        sdf.setTimeZone(GMT_ZONE);
        return sdf;
    });

    private static final ThreadLocal<DateFormat> OLD_COOKIE_FORMAT_TL = ThreadLocal.withInitial(() -> {
        SimpleDateFormat sdf = new SimpleDateFormat(OLD_COOKIE_PATTERN, LOCALE_US);
        sdf.setTimeZone(GMT_ZONE);
        return sdf;
    });

    private static final ThreadLocal<DateFormat> RFC_1036_FORMAT_TL = ThreadLocal.withInitial(() -> {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEEEEEEE, dd-MMM-yy HH:mm:ss z", LOCALE_US);
        sdf.setTimeZone(GMT_ZONE);
        return sdf;
    });

    private static final ThreadLocal<DateFormat> ASCTIME_FORMAT_TL = ThreadLocal.withInitial(() -> {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", LOCALE_US);
        sdf.setTimeZone(GMT_ZONE);
        return sdf;
    });


    /**
     * DateFormat to be used to format dates.
     * @deprecated Use {@link #getRfc1123Format()} instead — this static instance is not thread-safe.
     */
    @Deprecated
    public static final DateFormat RFC_1123_FORMAT = RFC_1123_FORMAT_TL.get();


    /**
     * DateFormat to be used to format old netscape cookies.
     * @deprecated Use {@link #getOldCookieFormat()} instead — this static instance is not thread-safe.
     */
    @Deprecated
    public static final DateFormat oldCookieFormat = OLD_COOKIE_FORMAT_TL.get();


    /**
     * Format for RFC 1036 date string -- "Sunday, 06-Nov-94 08:49:37 GMT".
     * @deprecated Use {@link #getRfc1036Format()} instead — this static instance is not thread-safe.
     */
    @Deprecated
    public static final DateFormat RFC_1036_FORMAT = RFC_1036_FORMAT_TL.get();


    /**
     * Format for C asctime() date string -- "Sun Nov  6 08:49:37 1994".
     * @deprecated Use {@link #getAsctimeFormat()} instead — this static instance is not thread-safe.
     */
    @Deprecated
    public static final DateFormat ASCTIME_FORMAT = ASCTIME_FORMAT_TL.get();


    /**
     * Constructor for DateTimeFormat
     */
    private DateTimeFormat() {
        // NOP
    }


    /**
     * Returns a thread-safe RFC 1123 DateFormat instance.
     *
     * @return the DateFormat for RFC 1123
     */
    public static DateFormat getRfc1123Format() {
        return RFC_1123_FORMAT_TL.get();
    }


    /**
     * Returns a thread-safe old cookie DateFormat instance.
     *
     * @return the DateFormat for old cookies
     */
    public static DateFormat getOldCookieFormat() {
        return OLD_COOKIE_FORMAT_TL.get();
    }


    /**
     * Returns a thread-safe RFC 1036 DateFormat instance.
     *
     * @return the DateFormat for RFC 1036
     */
    public static DateFormat getRfc1036Format() {
        return RFC_1036_FORMAT_TL.get();
    }


    /**
     * Returns a thread-safe asctime DateFormat instance.
     *
     * @return the DateFormat for asctime
     */
    public static DateFormat getAsctimeFormat() {
        return ASCTIME_FORMAT_TL.get();
    }
}
