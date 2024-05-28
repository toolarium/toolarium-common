/*
 * StatisticCounterTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.statistic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the StatisticCounter stuff
 * 
 * @author patrick
 */
public class StatisticCounterTest  {
    
    /**
     * Test
     */
    @Test
    public void testSample() {
        StatisticCounter c = new StatisticCounter();
        c.add(5);
        c.add(4);
        c.add(23);
        c.add(2);
        c.add(3);
        c.add(6);
        c.add(4);
        c.add(3);

        assertEquals(8, c.getCounter());
        assertEquals(2.0, c.getMinValue());
        assertEquals(23.0, c.getMaxValue());
        assertEquals(6.25, c.getAverage());
        assertEquals(50.0, c.getSum());
        assertEquals(644.0, c.getSquareSum());
        assertEquals(41.4375, c.getVariance());
        assertEquals(6.437196594791867, c.getStandardDeviation());
    }

    
    /**
     * Test
     */
    @Test
    public void testSample2() {
        StatisticCounter c = new StatisticCounter();
        for (int i = 1; i <= 6; i++) {
            c.add(i);
        }
        
        assertEquals(6, c.getCounter());
        assertEquals(1.0, c.getMinValue());
        assertEquals(6.0, c.getMaxValue());
        assertEquals(3.5, c.getAverage());
        assertEquals(21.0, c.getSum());
        assertEquals(91.0, c.getSquareSum());
        assertEquals(2.916666666666666, c.getVariance());
        assertEquals(1.707825127659933, c.getStandardDeviation());
    }

    
    /**
     * Test 3
     */
    @Test
    public void testSample3() {
        StatisticCounter c = new StatisticCounter();
        c.add(1);
        c.add(3);
        c.add(24);
        c.add(17);
        c.add(12);
        c.add(6);
        c.add(14);

        assertEquals(7, c.getCounter());
        assertEquals(1.0, c.getMinValue());
        assertEquals(24.0, c.getMaxValue());
        assertEquals(11.0, c.getAverage());
        assertEquals(77.0, c.getSum());
        assertEquals(1251.0, c.getSquareSum());
        assertEquals(57.71428571428572, c.getVariance());
        assertEquals(7.596991885890476, c.getStandardDeviation());
    }

    
    /**
     * Test 4
     */
    @Test
    public void testSample4() {
        StatisticCounter c = new StatisticCounter();
        c.add(39);
        c.add(39);
        c.add(38);
        c.add(38);
        c.add(37);
        c.add(41);
        c.add(38);
        c.add(38);
        c.add(40);
        c.add(37);

        assertEquals(10, c.getCounter());
        assertEquals(37.0, c.getMinValue());
        assertEquals(41.0, c.getMaxValue());
        assertEquals(38.5, c.getAverage());
        assertEquals(385.0, c.getSum());
        assertEquals(14837.0, c.getSquareSum());
        assertEquals(1.4500000000000455, c.getVariance());
        assertEquals(1.2041594578792485, c.getStandardDeviation());
    }

    
    /**
     * Test
     */
    @Test
    public void testAverage2() {
        StatisticCounter c = new StatisticCounter();

        for (int i = 0; i < 3; i++) {
            c.add(i);
        }

        assertEquals(3, c.getCounter());
        assertEquals(0.0, c.getMinValue());
        assertEquals(2.0, c.getMaxValue());
        assertEquals(1.0, c.getAverage());
        assertEquals(3.0, c.getSum());
        assertEquals(5.0, c.getSquareSum());
        assertEquals(0.6666666666666667, c.getVariance());
        assertEquals(0.816496580927726, c.getStandardDeviation());
    }

    
    /**
     * Test
     */
    @Test
    public void testAverage100() {
        StatisticCounter c = new StatisticCounter();

        for (int i = 0; i < 100; i++) {
            c.add(i);
        }

        assertEquals(100, c.getCounter());
        assertEquals(0.0, c.getMinValue());
        assertEquals(99.0, c.getMaxValue());
        assertEquals(49.5, c.getAverage());
        assertEquals(4950.0, c.getSum());
        assertEquals(328350.0, c.getSquareSum());
        assertEquals(833.25, c.getVariance());
        assertEquals(28.86607004772212, c.getStandardDeviation());

        assertEquals(9.128252844876723, c.getStandardDeviation() / Math.sqrt(10));
    }

    
    /**
     * Test
     */
    @Test
    public void testAverage() {
        StatisticCounter c1 = new StatisticCounter();
        StatisticCounter c2 = new StatisticCounter();
        StatisticCounter c3 = new StatisticCounter();
        StatisticCounter c4 = new StatisticCounter();

        for (int i = 0; i < 25; i++) {
            c1.add(i);
        }
        
        for (int i = 25; i < 50; i++) {
            c2.add(i);
        }
        
        for (int i = 50; i < 75; i++) {
            c3.add(i);
        }

        for (int i = 75; i < 100; i++) {
            c4.add(i);
        }

        StatisticCounter s = new StatisticCounter();
        s.add(c1);
        s.add(c2);
        s.add(c3);
        s.add(c4);

        assertEquals(100, s.getCounter());
        assertEquals(0.0, s.getMinValue());
        assertEquals(99.0, s.getMaxValue());
        assertEquals(49.5, s.getAverage());
        assertEquals(4950.0, s.getSum());
        assertEquals(328350.0, s.getSquareSum());
        assertEquals(833.25, s.getVariance());
        assertEquals(28.86607004772212, s.getStandardDeviation());
    }
}
