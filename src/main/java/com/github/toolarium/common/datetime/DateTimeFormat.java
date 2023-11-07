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
    public static final String RFC1123_PATTERN = "EEE, dd MMM yyyyy HH:mm:ss z";

    
    /** 
     * Format for http response header date field
     */
    public static final String HTTP_RESPONSE_DATE_HEADER = "EEE, dd MMM yyyy HH:mm:ss zzz";

    
    /**
     * Pattern used for old cookies
     */
    public static final String OLD_COOKIE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss z";

    
    /**
     * DateFormat to be used to format dates
     */
    public static final DateFormat RFC_1123_FORMAT = new SimpleDateFormat(RFC1123_PATTERN, LOCALE_US);

    
    /**
     * DateFormat to be used to format old netscape cookies
     */
    public static final DateFormat oldCookieFormat = new SimpleDateFormat(OLD_COOKIE_PATTERN, LOCALE_US);

    
    /** 
     * Format for RFC 1036 date string -- "Sunday, 06-Nov-94 08:49:37 GMT"
     */
    public static final DateFormat RFC_1036_FORMAT = new SimpleDateFormat("EEEEEEEEE, dd-MMM-yy HH:mm:ss z", LOCALE_US);

    
    /** 
     * Format for C asctime() date string -- "Sun Nov  6 08:49:37 1994"
     */
    public static final DateFormat ASCTIME_FORMAT = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyyy", LOCALE_US);

    
    static {
        RFC_1123_FORMAT.setTimeZone(GMT_ZONE);
        oldCookieFormat.setTimeZone(GMT_ZONE);
        RFC_1036_FORMAT.setTimeZone(GMT_ZONE);
        ASCTIME_FORMAT.setTimeZone(GMT_ZONE);
    }
    
    
    /**
     * Constructor for DateTimeFormat
     */
    private DateTimeFormat() {
        // NOP
    }
}
