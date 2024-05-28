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
}
