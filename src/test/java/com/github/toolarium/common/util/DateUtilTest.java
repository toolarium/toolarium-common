/*
 * DateUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Calendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author patrick
 */
public class DateUtilTest {
    private static final String SPACE = " ";
    private static final String TIME_0230 = " 02:30";
    private static final int ONE_HOUR = 60 * 60 * 1000;


    /**
     * Test
     */
    @Test
    public void testDateToString() {
        assertEquals("01.01.2008@11:56:22", DateUtil.getInstance().toDateString(DateUtil.getInstance().parseDate("1.1.2008@11:56:22")));
        assertEquals("31.01.1999@00:00:00", DateUtil.getInstance().toDateString(DateUtil.getInstance().parseDate("31.1.1999@00:00:00")));
        assertEquals("01.01.2008@11:56:22", DateUtil.getInstance().toDateString(DateUtil.getInstance().parseDate("01.01.2008@11:56:22")));
        assertEquals("31.01.1999@00:00:00", DateUtil.getInstance().toDateString(DateUtil.getInstance().parseDate("31.01.1999@0:0:0")));
        assertEquals("15.02.2016 12:44:23", DateUtil.getInstance().toDateString(DateUtil.getInstance().parseDate("15.2.2016 12:44:23.242", SPACE), SPACE));
        assertEquals("15.02.2016 12:44:23", DateUtil.getInstance().toDateString(DateUtil.getInstance().parseDate("15.2.2016 12:44:23.242", SPACE), SPACE));
    }


    /**
     * Test
     */
    @Test
    public void testParseDate() {
        assertEquals(1104534000000L, DateUtil.getInstance().createDate(2005, 0, 1).getTime());
        assertEquals(1104534000000L, DateUtil.getInstance().parseDate("1.1.05").getTime());
        assertEquals(DateUtil.getInstance().createDate(2005, 0, 1), DateUtil.getInstance().parseDate("1.1.05"));
        assertEquals(DateUtil.getInstance().createDate(2005, 11, 31), DateUtil.getInstance().parseDate("31.12.5"));
        assertEquals(DateUtil.getInstance().createDate(2005, 11, 31), DateUtil.getInstance().parseDate("31-12-5"));
        assertEquals(DateUtil.getInstance().createDate(2005, 11, 31), DateUtil.getInstance().parseDate("2005-12-31"));
        assertEquals(DateUtil.getInstance().createTimeStamp(2005, 11, 31, 22, 0, 0, 0), DateUtil.getInstance().parseDate("31.12.5@22"));
        assertEquals(DateUtil.getInstance().createTimeStamp(2005, 11, 31, 22, 11, 0, 0), DateUtil.getInstance().parseDate("31.12.5@22:11"));
        assertEquals(DateUtil.getInstance().createTimeStamp(2005, 11, 31, 22, 11, 05, 0), DateUtil.getInstance().parseDate("31.12.5@22:11:05"));
        assertEquals(DateUtil.getInstance().createTimeStamp(2005, 11, 31, 22, 11, 0, 0), DateUtil.getInstance().parseDate("31.12.5T22:11"));
        assertEquals(DateUtil.getInstance().createTimeStamp(2005, 11, 31, 22, 11, 05, 0), DateUtil.getInstance().parseDate("31.12.5T22:11:05"));
        assertEquals(DateUtil.getInstance().createTimeStamp(2005, 11, 31, 22, 11, 0, 0), DateUtil.getInstance().parseDate("31.12.5 22:11"));
        assertEquals(DateUtil.getInstance().createTimeStamp(2005, 11, 31, 22, 11, 05, 0), DateUtil.getInstance().parseDate("31.12.5 22:11:05"));
        assertEquals(DateUtil.getInstance().createTimeStamp(2005, 11, 31, 22, 11, 05, 0), DateUtil.getInstance().parseDate("2005-12-31 22:11:05"));
        assertEquals(DateUtil.getInstance().createTimeStamp(2005, 11, 31, 22, 11, 05, 0), DateUtil.getInstance().parseDate("2005.12.31 22:11:05"));
    }


    /**
     * Test the instant to date string conversion (system default time zone)
     */
    @Test
    public void testInstantToDateString() {
        java.util.Date date = DateUtil.getInstance().parseDate("15.2.2016 12:44:23", SPACE);
        Instant instant = date.toInstant();
        assertEquals("15.02.2016@12:44:23", DateUtil.getInstance().toDateString(instant));
        assertEquals("15.02.2016 12:44:23", DateUtil.getInstance().toDateString(instant, SPACE));
        assertEquals("15.02.2016T12:44:23", DateUtil.getInstance().toDateString(instant, "T"));
        assertEquals(DateUtil.getInstance().toDateString(date, "-"), DateUtil.getInstance().toDateString(instant, "-"));
    }


    /**
     * Test invalid input is rejected
     */
    @Test
    public void testParseDateInvalidInput() {
        assertNull(DateUtil.getInstance().parseDate(null));
        assertNull(DateUtil.getInstance().parseDate(SPACE));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseDate("abc"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseDate("31.12.2020@25:99"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseDate("32.13.2020"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseDate("30.2.2020"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseTime("12:60"));

        // incomplete or additional parts are rejected (L51), short forms are valid
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseTime("10:"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseTime("10:00:"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseTime("10:00:00:99"));
        assertNotNull(DateUtil.getInstance().parseTime("10"));
        assertNotNull(DateUtil.getInstance().parseTime("10:30"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseDate("01.02.-5"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseDate("01.02.-2024"));
    }


    /**
     * Test invalid dates are rejected by create date and create time stamp
     */
    @Test
    public void testCreateInvalidDate() {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().createDate(2024, 1, 30));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().createDate(2024, 12, 1));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().createTimeStamp(2024, 1, 30));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().createTimeStamp(2024, 0, 1, 25, 0, 0, 0));
        assertEquals(DateUtil.getInstance().parseDate("29.2.2024"), DateUtil.getInstance().createDate(2024, 1, 29));
    }


    /**
     * Test a null separator is the default separator
     */
    @Test
    public void testToDateStringNullSeparator() {
        java.util.Date date = DateUtil.getInstance().createTimeStamp(2016, 1, 15, 12, 44, 23, 0);
        assertEquals("15.02.2016@12:44:23", DateUtil.getInstance().toDateString(date, null));
        assertEquals(DateUtil.getInstance().toDateString(date), DateUtil.getInstance().toDateString(date, null));
        assertEquals(DateUtil.getInstance().toDateString(date.toInstant()), DateUtil.getInstance().toDateString(date.toInstant(), null));
    }


    /**
     * Test the fraction of a second
     */
    @Test
    public void testParseFractionOfSecond() {
        assertEquals(DateUtil.getInstance().createTimeStamp(2016, 1, 15, 12, 44, 23, 500), DateUtil.getInstance().parseDate("15.2.2016 12:44:23.5", SPACE));
        assertEquals(DateUtil.getInstance().createTimeStamp(2016, 1, 15, 12, 44, 23, 240), DateUtil.getInstance().parseDate("15.2.2016 12:44:23.24", SPACE));
        assertEquals(DateUtil.getInstance().createTimeStamp(2016, 1, 15, 12, 44, 23, 242), DateUtil.getInstance().parseDate("15.2.2016 12:44:23.242", SPACE));
        assertEquals(DateUtil.getInstance().createTimeStamp(2016, 1, 15, 12, 44, 23, 242), DateUtil.getInstance().parseDate("15.2.2016 12:44:23.2429", SPACE));
    }

    /**
     * The separator is always added as literal text
     */
    @Test
    public void testInstantToDateStringLetterSeparator() {
        Instant instant = DateUtil.getInstance().parseDate("15.2.2016 12:44:23", SPACE).toInstant();
        assertEquals("15.02.2016a12:44:23", DateUtil.getInstance().toDateString(instant, "a"));
        assertEquals("15.02.2016b12:44:23", DateUtil.getInstance().toDateString(instant, "b"));
        assertEquals("15.02.2016'12:44:23", DateUtil.getInstance().toDateString(instant, "'"));
    }


    /**
     * Surrounding whitespace is ignored
     */
    @Test
    public void testParseDateTrailingWhitespace() {
        assertEquals(DateUtil.getInstance().createDate(2024, 1, 1), DateUtil.getInstance().parseDate("01.02.2024 "));
        assertEquals(DateUtil.getInstance().createDate(2024, 1, 1), DateUtil.getInstance().parseDate(" 01.02.2024\t"));
        assertEquals(DateUtil.getInstance().createTimeStamp(2024, 1, 1, 11, 22, 0, 0), DateUtil.getInstance().parseDate("01.02.2024@11:22 "));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseDate("01.02.2024@"));
    }


    /**
     * Extra date parts are rejected
     */
    @Test
    public void testParseDateExtraDateParts() {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseDate("01.02.2024.99"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseDate("01.02.2024."));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseDate("2024-02-01-99"));
        assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseDate("01.02.2024.99@11:22"));
    }


    /**
     * The time of a date is validated with the parsed date and not with the current date: if today is a DST switch day,
     * a time in the DST gap (e.g. 02:30) of another date is valid. The parse API uses the default time zone, therefore
     * a time zone is set as default where today is the DST switch day (02:00 to 03:00).
     */
    @Test
    public void testParseDateOnDstSwitchDay() {
        TimeZone previousTimeZone = TimeZone.getDefault();
        try {
            Calendar today = Calendar.getInstance(new SimpleTimeZone(ONE_HOUR, "Test/NoDst"));
            int month = today.get(Calendar.MONTH);
            int day = today.get(Calendar.DAY_OF_MONTH);
            TimeZone timeZone = new SimpleTimeZone(ONE_HOUR, "Test/DstToday", month, day, 0, 2 * ONE_HOUR, (month + 6) % 12, 1, 0, 2 * ONE_HOUR);
            TimeZone.setDefault(timeZone);

            // today 02:30 does not exist
            String todayStr = day + "." + (month + 1) + "." + today.get(Calendar.YEAR);
            assertThrows(IllegalArgumentException.class, () -> DateUtil.getInstance().parseDate(todayStr + TIME_0230, SPACE));

            // 02:30 of another date is valid
            Calendar otherDay = (Calendar) today.clone();
            otherDay.add(Calendar.DAY_OF_MONTH, 10);
            String otherDayStr = otherDay.get(Calendar.DAY_OF_MONTH) + "." + (otherDay.get(Calendar.MONTH) + 1) + "." + otherDay.get(Calendar.YEAR);
            Calendar parsed = Calendar.getInstance(timeZone);
            parsed.setTime(DateUtil.getInstance().parseDate(otherDayStr + TIME_0230, SPACE));
            assertEquals(otherDay.get(Calendar.DAY_OF_MONTH), parsed.get(Calendar.DAY_OF_MONTH));
            assertEquals(2, parsed.get(Calendar.HOUR_OF_DAY));
            assertEquals(30, parsed.get(Calendar.MINUTE));
        } finally {
            TimeZone.setDefault(previousTimeZone);
        }
    }
}
