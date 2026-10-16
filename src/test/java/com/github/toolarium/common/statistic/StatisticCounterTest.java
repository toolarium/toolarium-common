/*
 * StatisticCounterTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.statistic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;

/**
 * Tests the StatisticCounter stuff
 *
 * @author patrick
 */
public class StatisticCounterTest {
    private static final double DELTA = 1e-9;
    private static final double GB = 1024d * 1024d * 1024d;

    
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
        assertEquals(6.25, c.getAverage(), DELTA);
        assertEquals(50.0, c.getSum());
        assertEquals(644.0, c.getSquareSum());
        assertEquals(41.4375, c.getVariance(), DELTA);
        assertEquals(6.437196594791867, c.getStandardDeviation(), DELTA);
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
        assertEquals(3.5, c.getAverage(), DELTA);
        assertEquals(21.0, c.getSum());
        assertEquals(91.0, c.getSquareSum());
        assertEquals(2.916666666666666, c.getVariance(), DELTA);
        assertEquals(1.707825127659933, c.getStandardDeviation(), DELTA);
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
        assertEquals(11.0, c.getAverage(), DELTA);
        assertEquals(77.0, c.getSum());
        assertEquals(1251.0, c.getSquareSum());
        assertEquals(57.71428571428572, c.getVariance(), DELTA);
        assertEquals(7.596991885890476, c.getStandardDeviation(), DELTA);
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
        assertEquals(38.5, c.getAverage(), DELTA);
        assertEquals(385.0, c.getSum());
        assertEquals(14837.0, c.getSquareSum());
        assertEquals(1.45, c.getVariance(), DELTA);
        assertEquals(Math.sqrt(1.45), c.getStandardDeviation(), DELTA);
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
        assertEquals(1.0, c.getAverage(), DELTA);
        assertEquals(3.0, c.getSum());
        assertEquals(5.0, c.getSquareSum());
        assertEquals(0.6666666666666667, c.getVariance(), DELTA);
        assertEquals(0.816496580927726, c.getStandardDeviation(), DELTA);
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
     * Test empty counter returns zero for min
     */
    @Test
    public void testEmptyCounter() {
        StatisticCounter c = new StatisticCounter();
        assertEquals(0, c.getCounter());
        assertEquals(0.0, c.getMinValue());
        assertEquals(0.0, c.getMaxValue());
        assertEquals(0.0, c.getAverage());
        assertTrue(Double.isNaN(c.getVariance()));
        assertTrue(Double.isNaN(c.getStandardDeviation()));
    }


    /**
     * Test negative values are correctly tracked as min
     */
    @Test
    public void testNegativeValues() {
        StatisticCounter c = new StatisticCounter();
        c.add(-5);
        c.add(-3);
        c.add(-10);
        c.add(2);

        assertEquals(4, c.getCounter());
        assertEquals(-10.0, c.getMinValue());
        assertEquals(2.0, c.getMaxValue());
        assertEquals(-4.0, c.getAverage());

        // negative-only dataset: variance and average
        StatisticCounter neg = new StatisticCounter();
        neg.add(-3d);
        neg.add(-1d);
        neg.add(-2d);
        assertEquals(-3, neg.getMinValue(), DELTA);
        assertEquals(-1, neg.getMaxValue(), DELTA);
        assertEquals(-2, neg.getAverage(), DELTA);
        assertEquals(2d / 3d, neg.getVariance(), DELTA);

        // symmetric mixed: average must be 0, variance must be 25
        StatisticCounter mixed = new StatisticCounter();
        mixed.add(-5d);
        mixed.add(5d);
        assertEquals(0, mixed.getAverage(), DELTA);
        assertEquals(25, mixed.getVariance(), DELTA);
    }


    /**
     * Test clear resets min properly
     */
    @Test
    public void testClearResetsMin() {
        StatisticCounter c = new StatisticCounter();
        c.add(5);
        assertEquals(5.0, c.getMinValue());

        c.clear();
        assertEquals(0.0, c.getMinValue());
        assertEquals(0, c.getCounter());

        c.add(10);
        assertEquals(10.0, c.getMinValue());
    }


    /**
     * Test
     */
    @Test
    public void testAverage() {
        StatisticCounter c1 = new StatisticCounter();
        StatisticCounter c2 = new StatisticCounter();
        StatisticCounter c3 = new StatisticCounter();

        for (int i = 0; i < 25; i++) {
            c1.add(i);
        }

        for (int i = 25; i < 50; i++) {
            c2.add(i);
        }

        for (int i = 50; i < 75; i++) {
            c3.add(i);
        }

        StatisticCounter c4 = new StatisticCounter();
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


    /**
     * Test the basic statistics.
     */
    @Test
    public void testBasicStatistics() {
        StatisticCounter counter = new StatisticCounter();
        counter.add(2L);
        counter.add(4L);
        counter.add(4L);
        counter.add(4L);
        counter.add(5L);
        counter.add(5L);
        counter.add(7L);
        counter.add(9L);

        assertEquals(8, counter.getCounter());
        assertEquals(40, counter.getSum(), DELTA);
        assertEquals(5, counter.getAverage(), DELTA);
        assertEquals(2, counter.getMinValue(), DELTA);
        assertEquals(9, counter.getMaxValue(), DELTA);
        assertEquals(7, counter.getRange(), DELTA);
        assertEquals(4, counter.getVariance(), DELTA);
        assertEquals(2, counter.getStandardDeviation(), DELTA);
        assertEquals(232, counter.getSquareSum(), DELTA);
    }


    /**
     * Test the range of an empty counter is 0.
     */
    @Test
    public void testEmptyRange() {
        StatisticCounter counter = new StatisticCounter();
        assertEquals(0, counter.getRange(), DELTA);
        assertEquals(new StatisticCounter(0, 99, 99, 0, 0), counter);
    }


    /**
     * Test a single value has no deviation.
     */
    @Test
    public void testSingleValue() {
        StatisticCounter counter = new StatisticCounter();
        counter.add(42d);
        assertEquals(0, counter.getVariance(), DELTA);
        assertEquals(0, counter.getStandardDeviation(), DELTA);
    }


    /**
     * Test that constant large values (e.g. the total memory) never result in a negative variance or NaN.
     */
    @Test
    public void testConstantLargeValues() {
        StatisticCounter counter = new StatisticCounter();
        double value = 16 * GB + 12345;
        for (int i = 0; i < 1_000_000; i++) {
            counter.add(value);
        }

        assertEquals(value, counter.getAverage(), DELTA);
        assertEquals(0, counter.getVariance(), DELTA);
        assertEquals(0, counter.getStandardDeviation(), DELTA);
    }


    /**
     * Test numerical stability for large values with a small deviation.
     */
    @Test
    public void testLargeValuesWithSmallDeviation() {
        StatisticCounter counter = new StatisticCounter();
        double base = 64 * GB;
        for (int i = 0; i < 1_000_000; i++) {
            if (i % 2 == 0) {
                counter.add(base - 1);
            } else {
                counter.add(base + 1);
            }
        }

        assertEquals(base, counter.getAverage(), 1e-3);
        assertEquals(1, counter.getStandardDeviation(), 1e-6);
    }


    /**
     * Test merging counters gives the same result as adding all values to one counter.
     */
    @Test
    public void testMergeResult() {
        StatisticCounter all = new StatisticCounter();
        StatisticCounter first = new StatisticCounter();
        StatisticCounter second = new StatisticCounter();
        for (int i = 1; i <= 10; i++) {
            all.add((long) i * 100);
            if (i <= 4) {
                first.add((long) i * 100);
            } else {
                second.add((long) i * 100);
            }
        }

        first.add(second);
        assertEquals(all.getCounter(), first.getCounter());
        assertEquals(all.getAverage(), first.getAverage(), DELTA);
        assertEquals(all.getVariance(), first.getVariance(), 1e-6);
        assertEquals(all.getMinValue(), first.getMinValue(), DELTA);
        assertEquals(all.getMaxValue(), first.getMaxValue(), DELTA);
    }


    /**
     * Test merging with empty or null counters does not change the statistics.
     */
    @Test
    public void testMergeEmpty() {
        StatisticCounter counter = new StatisticCounter();
        counter.add(new StatisticCounter());
        counter.add((StatisticCounter) null);
        assertEquals(0, counter.getCounter());

        counter.add(10L);
        counter.add(new StatisticCounter());
        assertEquals(10, counter.getMinValue(), DELTA);
        assertEquals(1, counter.getCounter());

        StatisticCounter empty = new StatisticCounter();
        empty.add(counter);
        assertEquals(counter, empty);
        assertEquals(10, empty.getAverage(), DELTA);
        empty.add(20L);
        assertEquals(10, empty.getMinValue(), DELTA);
        assertEquals(15, empty.getAverage(), DELTA);
        assertEquals(5, empty.getStandardDeviation(), DELTA);
    }


    /**
     * Test the constructor with the aggregated values.
     */
    @Test
    public void testConstructorWithValues() {
        StatisticCounter counter = new StatisticCounter(2, 1, 3, 4, 10);
        assertEquals(2, counter.getAverage(), DELTA);
        assertEquals(1, counter.getVariance(), DELTA);
        assertEquals(1, counter.getMinValue(), DELTA);
        assertEquals(3, counter.getMaxValue(), DELTA);
    }


    /**
     * Test clear resets all values.
     */
    @Test
    public void testClearAll() {
        StatisticCounter counter = new StatisticCounter();
        counter.add(5L);
        counter.add(7L);
        counter.clear();
        assertEquals(new StatisticCounter(), counter);
        counter.add(3L);
        assertEquals(3, counter.getAverage(), DELTA);
        assertEquals(0, counter.getVariance(), DELTA);
    }


    /**
     * Test clone, equals, hashCode and toString.
     */
    @Test
    public void testCloneEqualsHashCodeToString() {
        StatisticCounter counter = new StatisticCounter();
        counter.add(5L);
        counter.add(7L);

        StatisticCounter clone = counter.clone();
        assertNotSame(counter, clone);
        assertEquals(counter, clone);
        assertEquals(counter.hashCode(), clone.hashCode());
        assertEquals(counter.getStandardDeviation(), clone.getStandardDeviation(), DELTA);

        clone.add(9L);
        assertNotEquals(counter, clone);
        assertNotEquals(counter, null);
        assertTrue(counter.toString().contains("number of elements: 2"));
        assertTrue(counter.toString("Title:").startsWith("Title:"));
    }


    /**
     * Test serialization round-trip preserves all statistics and produces a fully usable instance.
     *
     * @throws Exception in case of error
     */
    @Test
    public void testSerialization() throws Exception {
        StatisticCounter original = new StatisticCounter();
        original.add(10L);
        original.add(20L);
        original.add(30L);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }

        StatisticCounter restored;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            restored = (StatisticCounter) ois.readObject();
        }

        assertEquals(original, restored);
        assertEquals(original.getAverage(), restored.getAverage(), DELTA);
        assertEquals(original.getVariance(), restored.getVariance(), DELTA);

        // lock is functional after deserialization
        restored.add(40L);
        assertEquals(4, restored.getCounter());
    }
}
