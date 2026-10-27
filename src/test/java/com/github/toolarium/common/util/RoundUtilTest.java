/*
 * RoundUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


/**
 * Test {@link RoundUtil}.
 * 
 * @author patrick
 */
public class RoundUtilTest {
    
    /**
     * Test
     */
    @Test
    public void test() {
        assertEquals(2.35d, RoundUtil.getInstance().round(2.3456, 2));
        assertEquals(2.3d, RoundUtil.getInstance().round(2.3456, 1));
        assertEquals(2.0d, RoundUtil.getInstance().round(2.3456, 0));
        assertEquals(2, RoundUtil.getInstance().roundToInt(2.3456));
        assertEquals(2L, RoundUtil.getInstance().roundToLong(2.3456));

        assertEquals(2.57d, RoundUtil.getInstance().round(2.56789, 2));
        assertEquals(2.6d, RoundUtil.getInstance().round(2.56789, 1));
        assertEquals(3.0d, RoundUtil.getInstance().round(2.56789, 0));
        assertEquals(3, RoundUtil.getInstance().roundToInt(2.56789));
        assertEquals(3L, RoundUtil.getInstance().roundToLong(2.56789));

        assertEquals(69L, RoundUtil.getInstance().roundToLong(69.34042553191489));
    }


    /**
     * Test large values are not saturated
     */
    @Test
    public void testLargeValues() {
        assertEquals(1e17, RoundUtil.getInstance().round(1e17, 3));
        assertEquals(-1e17, RoundUtil.getInstance().round(-1e17, 3));
        assertEquals(Double.MAX_VALUE, RoundUtil.getInstance().round(Double.MAX_VALUE, 2));
        assertEquals(123.457d, RoundUtil.getInstance().round(123.4567, 3));
        assertEquals(2.3456d, RoundUtil.getInstance().round(2.3456, 400));
    }


    /**
     * Test values which have no exact binary representation are rounded based on their decimal representation
     */
    @Test
    public void testDecimalRepresentation() {
        assertEquals(1.01d, RoundUtil.getInstance().round(1.005, 2));
        assertEquals(2.68d, RoundUtil.getInstance().round(2.675, 2));
        assertEquals(1.13d, RoundUtil.getInstance().round(1.125, 2));
        assertEquals(-1.01d, RoundUtil.getInstance().round(-1.005, 2));
        assertEquals(-3, RoundUtil.getInstance().roundToInt(-2.5));
    }


    /**
     * Test NaN and infinite values are returned unchanged
     */
    @Test
    public void testNaNAndInfinity() {
        assertEquals(Double.NaN, RoundUtil.getInstance().round(Double.NaN, 2));
        assertEquals(Double.POSITIVE_INFINITY, RoundUtil.getInstance().round(Double.POSITIVE_INFINITY, 2));
        assertEquals(Double.NEGATIVE_INFINITY, RoundUtil.getInstance().round(Double.NEGATIVE_INFINITY, 0));
    }


    /**
     * Test very large negative and positive scales
     */
    @Test
    public void testExtremeScales() {
        assertEquals(100.0d, RoundUtil.getInstance().round(123, -2));
        assertEquals(0.0d, RoundUtil.getInstance().round(123, -400));
        assertEquals(0.0d, RoundUtil.getInstance().round(-123, -400));
        assertEquals(0.0d, RoundUtil.getInstance().round(Double.MAX_VALUE, -400));
        assertEquals(0.0d, RoundUtil.getInstance().round(123, Integer.MIN_VALUE));
        assertEquals(123.0d, RoundUtil.getInstance().round(123, 400));
        assertEquals(123.0d, RoundUtil.getInstance().round(123, Integer.MAX_VALUE));
        assertEquals(0.0d, RoundUtil.getInstance().round(0.0d, 400));
        assertEquals(1e-300, RoundUtil.getInstance().round(1e-300, 400));
    }
}
