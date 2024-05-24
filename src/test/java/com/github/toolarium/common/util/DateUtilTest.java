/*
 * TimestampConverterUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author patrick
 */
public class DateUtilTest {
    /**
     * Test
     */
    @Test
    public void testDateToString() {
        assertEquals("01.01.2008@11:56:22", DateUtil.getInstance().toDateString(DateUtil.getInstance().parseDate("1.1.2008@11:56:22")));
        assertEquals("31.01.1999@00:00:00", DateUtil.getInstance().toDateString(DateUtil.getInstance().parseDate("31.1.1999@00:00:00")));
        assertEquals("01.01.2008@11:56:22", DateUtil.getInstance().toDateString(DateUtil.getInstance().parseDate("01.01.2008@11:56:22")));
        assertEquals("31.01.1999@00:00:00", DateUtil.getInstance().toDateString(DateUtil.getInstance().parseDate("31.01.1999@0:0:0")));
        assertEquals("15.02.2016 12:44:23", DateUtil.getInstance().toDateString(DateUtil.getInstance().parseDate("15.2.2016 12:44:23.242", " "), " "));
        assertEquals("15.02.2016 12:44:23", DateUtil.getInstance().toDateString(DateUtil.getInstance().parseDate("15.2.2016 12:44:23.242", " "), " "));
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
}
