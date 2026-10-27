/*
 * DateTimeFormatTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.datetime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;


/**
 * Test {@link DateTimeFormat}.
 *
 * @author patrick
 */
public class DateTimeFormatTest {

    /**
     * The deprecated static formats must not share the instance with the thread-local formats.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedFormatsAreDistinctInstances() {
        // touch the class first so the initializing thread is the current thread
        assertNotSame(DateTimeFormat.RFC_1123_FORMAT, DateTimeFormat.getRfc1123Format());
        assertNotSame(DateTimeFormat.RFC_1036_FORMAT, DateTimeFormat.getRfc1036Format());
        assertNotSame(DateTimeFormat.ASCTIME_FORMAT, DateTimeFormat.getAsctimeFormat());

        Date date = new Date(784111777000L); // Sun, 06 Nov 1994 08:49:37 GMT
        assertEquals("Sun, 06 Nov 1994 08:49:37 GMT", DateTimeFormat.RFC_1123_FORMAT.format(date));
        assertEquals(DateTimeFormat.getRfc1123Format().format(date), DateTimeFormat.RFC_1123_FORMAT.format(date));
        assertEquals(DateTimeFormat.getRfc1036Format().format(date), DateTimeFormat.RFC_1036_FORMAT.format(date));
        assertEquals(DateTimeFormat.getAsctimeFormat().format(date), DateTimeFormat.ASCTIME_FORMAT.format(date));
        assertEquals("Sun Nov 6 08:49:37 1994", DateTimeFormat.ASCTIME_FORMAT.format(date));
    }

    /**
     * The formats are not lenient: out of range values are rejected.
     *
     * @throws ParseException in case of an unexpected parse error
     */
    @Test
    public void testNotLenient() throws ParseException {
        assertEquals(784111777000L, DateTimeFormat.getRfc1123Format().parse("Sun, 06 Nov 1994 08:49:37 GMT").getTime());
        assertThrows(ParseException.class, () -> DateTimeFormat.getRfc1123Format().parse("Wed, 32 Jan 2024 10:00:00 GMT"));
        assertThrows(ParseException.class, () -> DateTimeFormat.getRfc1123Format().parse("Wed, 31 Jan 2024 25:61:00 GMT"));
        assertThrows(ParseException.class, () -> DateTimeFormat.getOldCookieFormat().parse("Wed, 32-Jan-2024 10:00:00 GMT"));
    }


    /**
     * Changes of a returned format must not stick for the thread.
     */
    @Test
    public void testReturnedFormatChangesDoNotStick() {
        Date date = new Date(784111777000L); // Sun, 06 Nov 1994 08:49:37 GMT
        DateFormat format = DateTimeFormat.getRfc1123Format();
        String expected = format.format(date);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        assertEquals(expected, DateTimeFormat.getRfc1123Format().format(date));
    }
}
