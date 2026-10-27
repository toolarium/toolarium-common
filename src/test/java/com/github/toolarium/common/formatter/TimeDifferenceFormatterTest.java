/*
 * TimeDifferenceFormatterTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * Tests the time difference formatter
 * 
 * @author patrick
 */
public class TimeDifferenceFormatterTest {

    /** Constant which represents a second in milliseconds */
    protected static final long SEC = 1000;

    /** Constant which represents a minute in milliseconds */
    protected static final long MIN = 60 * 1000;

    /** Constant which represents a hour in milliseconds */
    protected static final long HOUR = 60 * 60 * 1000;

    /** Constant which represents a day in milliseconds */
    protected static final long DAY = 24 * 60 * 60 * 1000;
    private static final String LESS_THAN_A_SECOND = "01'' <";


    /**
     * Test
     */
    @Test
    public void testFormatTimeDifference() {
        TimeDifferenceFormatter formatter = new TimeDifferenceFormatter();
        
        assertEquals("0ms", formatter.formatAsString(0));
        assertEquals("1ms", formatter.formatAsString(1));
        assertEquals("10ms", formatter.formatAsString(10));
        assertEquals("100ms", formatter.formatAsString(100));
        assertEquals("1sec", formatter.formatAsString(1000));
        assertEquals("1sec 10ms", formatter.formatAsString(1010));
        assertEquals("2sec 999ms", formatter.formatAsString(2999));
        assertEquals("1min 2sec 999ms", formatter.formatAsString(MIN + 2 * SEC + 999));

        assertEquals("59min 2sec 999ms", formatter.formatAsString(59 * MIN + 2 * SEC + 999));
        assertEquals("59min 2sec", formatter.formatAsString(59 * MIN + 2 * SEC));
        assertEquals("59min", formatter.formatAsString(59 * MIN));

        assertEquals("59min 10ms", formatter.formatAsString(59 * MIN + 10));

        assertEquals("59min 2sec 999ms", formatter.formatAsString(59 * MIN + 2 * SEC + 999));

        assertEquals("10h 999ms", formatter.formatAsString(10 * HOUR + 999));

        assertEquals("9h 2sec", formatter.formatAsString(9 * HOUR + 2 * SEC));

        assertEquals("9h 59min 2sec 999ms", formatter.formatAsString(9 * HOUR + 59 * MIN + 2 * SEC + 999));
        assertEquals("2d 999ms", formatter.formatAsString(2 * DAY + 999));
    }

    
    /**
     * Test
     */
    @Test
    public void testDoubleFormatTimeDifference() {
        TimeDifferenceFormatter formatter = new TimeDifferenceFormatter();
        assertEquals("0ms", formatter.formatAsString(0.1));
        assertEquals("0ms", formatter.formatAsString(0.4999));
        assertEquals("1ms", formatter.formatAsString(0.5));
        assertEquals("1ms", formatter.formatAsString(0.9));
        assertEquals("1ms", formatter.formatAsString(1.0));
        assertEquals("10ms", formatter.formatAsString(10.0));
        assertEquals("100ms", formatter.formatAsString(100.0));
        assertEquals("1sec", formatter.formatAsString(1000.0));
        assertEquals("1sec 10ms", formatter.formatAsString(1010.0));
        assertEquals("2sec 999ms", formatter.formatAsString(2999.0));
    }


    /**
     * Test
     */
    @Test
    public void testDoubleFormatTimeDifference2() {
        TimeDifferenceFormatter formatter = new TimeDifferenceFormatter(true, false);
        assertEquals(LESS_THAN_A_SECOND, formatter.formatAsString(0.1));
        assertEquals(LESS_THAN_A_SECOND, formatter.formatAsString(0.4999));
        assertEquals(LESS_THAN_A_SECOND, formatter.formatAsString(0.5));
        assertEquals(LESS_THAN_A_SECOND, formatter.formatAsString(0.9));
        assertEquals(LESS_THAN_A_SECOND, formatter.formatAsString(1.0));
        assertEquals(LESS_THAN_A_SECOND, formatter.formatAsString(10.0));
        assertEquals(LESS_THAN_A_SECOND, formatter.formatAsString(100.0));
        assertEquals("01''", formatter.formatAsString(1000.0));
        assertEquals("01''", formatter.formatAsString(1010.0));
        assertEquals("03''", formatter.formatAsString(2999.0));
        assertEquals("59' 03''", formatter.formatAsString(59 * MIN + 2 * SEC + 999));
        assertEquals("09h 59' 03''", formatter.formatAsString(9 * HOUR + 59 * MIN + 2 * SEC + 999));
        assertEquals("02d 01''", formatter.formatAsString(2 * DAY + 999));
    }

    /**
     * Test the carry over of full minutes, hours and days and the rounding of the short format
     */
    @Test
    public void testCarryOverAndRounding() {
        TimeDifferenceFormatter formatter = new TimeDifferenceFormatter();
        assertEquals("1min", formatter.formatAsString(MIN));
        assertEquals("1h", formatter.formatAsString(HOUR));
        assertEquals("1d", formatter.formatAsString(DAY));
        assertEquals("1h 1sec", formatter.formatAsString(HOUR + SEC));
        assertEquals("1sec 1ms", formatter.formatAsString(SEC + 1));

        String minValue = formatter.formatAsString(Long.MIN_VALUE);
        assertTrue(minValue.startsWith("-") && minValue.contains("d"), minValue);

        TimeDifferenceFormatter shortFormatter = new TimeDifferenceFormatter(true, false);
        assertEquals("03''", shortFormatter.formatAsString(2600));
        assertEquals("03''", shortFormatter.formatAsString(2500));
        assertEquals("02''", shortFormatter.formatAsString(2499));
        assertEquals("01'", shortFormatter.formatAsString(59 * SEC + 600));
    }


    /**
     * Formatters with different patterns can be used in the same thread and from other threads
     *
     * @throws InterruptedException in case of an interrupt
     */
    @Test
    public void testFormatterInstancesAreIndependent() throws InterruptedException {
        TimeDifferenceFormatter longFormatter = new TimeDifferenceFormatter();
        TimeDifferenceFormatter shortFormatter = new TimeDifferenceFormatter(true, false);
        TimeDifferenceFormatter padded = new TimeDifferenceFormatter("000", false, true);
        assertEquals("1sec", longFormatter.formatAsString(SEC));
        assertEquals("01''", shortFormatter.formatAsString(SEC));
        assertEquals("001 sec", padded.formatAsString(SEC));
        assertEquals("1sec", longFormatter.formatAsString(SEC));

        String[] result = new String[1];
        Thread thread = new Thread(() -> result[0] = padded.formatAsString(2 * SEC));
        thread.start();
        thread.join();
        assertEquals("002 sec", result[0]);
    }
}
