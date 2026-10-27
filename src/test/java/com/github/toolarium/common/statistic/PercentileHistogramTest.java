/*
 * PercentileHistogramTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.statistic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.Test;


/**
 * Tests the {@link PercentileHistogram}.
 *
 * @author patrick
 */
public class PercentileHistogramTest {
    private static final double[] QUANTILES = {0.0, 0.01, 0.25, 0.5, 0.75, 0.9, 0.95, 0.98, 0.99, 0.999, 1.0};
    private static final double MAX_RELATIVE_ERROR = 1.0 / PercentileHistogram.SUB_BUCKET_COUNT;
    private static final long MB = 1024L * 1024L;
    private static final long GB = 1024L * MB;
    private static final int SAMPLES = 100_000;


    /**
     * Test an empty histogram returns 0.
     */
    @Test
    public void testEmpty() {
        PercentileHistogram histogram = new PercentileHistogram();
        assertEquals(0, histogram.getCount());
        assertEquals(0, histogram.getValueAtQuantile(0.5));
        assertArrayEquals(new long[] {0, 0}, histogram.getValuesAtQuantiles(0.5, 0.99));
        assertArrayEquals(new long[0], histogram.getValuesAtQuantiles());
    }


    /**
     * Test a single value and a constant series are reported exactly.
     */
    @Test
    public void testConstantValueExact() {
        PercentileHistogram histogram = new PercentileHistogram();
        long value = 8 * GB + 12345;
        histogram.add(value);
        assertEquals(value, histogram.getValueAtQuantile(0.5));

        for (int i = 0; i < 1000; i++) {
            histogram.add(value);
        }
        assertArrayEquals(new long[] {value, value, value, value}, histogram.getValuesAtQuantiles(0.5, 0.95, 0.98, 0.99));
        assertEquals(1001, histogram.getCount());
    }


    /**
     * Test small values (below 2048) are counted exactly.
     */
    @Test
    public void testSmallValuesExact() {
        PercentileHistogram histogram = new PercentileHistogram();
        for (long v = 1; v <= 2000; v++) {
            histogram.add(v);
        }
        assertEquals(1, histogram.getValueAtQuantile(0.0));
        assertEquals(1000, histogram.getValueAtQuantile(0.5));
        assertEquals(1900, histogram.getValueAtQuantile(0.95));
        assertEquals(1980, histogram.getValueAtQuantile(0.99));
        assertEquals(2000, histogram.getValueAtQuantile(1.0));
    }


    /**
     * Test the extreme values 0 and the maximum value.
     */
    @Test
    public void testExtremeValues() {
        PercentileHistogram histogram = new PercentileHistogram();
        histogram.add(0);
        histogram.add(PercentileHistogram.MAX_VALUE);
        assertEquals(0, histogram.getValueAtQuantile(0.0));
        assertEquals(0, histogram.getValueAtQuantile(0.5));
        assertEquals(PercentileHistogram.MAX_VALUE, histogram.getValueAtQuantile(1.0));
    }


    /**
     * Test invalid values and quantiles are rejected.
     */
    @Test
    public void testInvalidArguments() {
        PercentileHistogram histogram = new PercentileHistogram();
        assertThrows(IllegalArgumentException.class, () -> histogram.add(-1));
        assertThrows(IllegalArgumentException.class, () -> histogram.getValueAtQuantile(-0.1));
        assertThrows(IllegalArgumentException.class, () -> histogram.getValueAtQuantile(1.1));
        assertThrows(IllegalArgumentException.class, () -> histogram.getValueAtQuantile(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> histogram.getValuesAtQuantiles(0.99, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new PercentileHistogram(-1L));
        assertThrows(IllegalArgumentException.class, () -> new PercentileHistogram(Long.MAX_VALUE));
        assertEquals(0, histogram.getCount());
    }


    /**
     * Test the relative error stays within the precision for typical memory distributions, compared with
     * the exact nearest-rank percentiles.
     */
    @Test
    public void testPrecisionMemoryDistributions() {
        Random random = new Random(42);
        assertPrecision(() -> 2 * GB + (long) (random.nextDouble() * 4 * GB));
        assertPrecision(() -> 8 * GB + (long) (random.nextGaussian() * 10 * MB));
        assertPrecision(() -> 40 * MB + (long) (random.nextDouble() * 20 * MB));
        assertPrecision(() -> (long) Math.exp(20 + random.nextGaussian() * 0.5));
    }


    /**
     * Test the precision for distributions with a jump (bimodal, sparse spikes), where interpolating
     * estimators can report a value between the clusters.
     */
    @Test
    public void testPrecisionBimodalAndSpikes() {
        Random random = new Random(7);
        assertPrecision(() -> {
            long base = 3 * GB;
            if (random.nextDouble() < 0.95) {
                base = GB;
            }
            return base + (long) (random.nextGaussian() * 5 * MB);
        });
        assertPrecision(() -> {
            if (random.nextDouble() < 0.99) {
                return 500 * MB + (long) (random.nextGaussian() * MB);
            }
            return 2 * GB + (long) (random.nextDouble() * 2 * GB);
        });
    }


    /**
     * Test the precision over the whole supported value range.
     */
    @Test
    public void testPrecisionFullRange() {
        Random random = new Random(1);
        assertPrecision(() -> (long) Math.pow(2, random.nextDouble() * 46));
    }


    /**
     * Test the single-pass method returns the same values as separate calls.
     */
    @Test
    public void testSinglePassEqualsSeparateCalls() {
        Random random = new Random(3);
        PercentileHistogram histogram = new PercentileHistogram();
        for (int i = 0; i < SAMPLES; i++) {
            histogram.add((long) (random.nextDouble() * 16 * GB));
        }

        long[] values = histogram.getValuesAtQuantiles(QUANTILES);
        for (int i = 0; i < QUANTILES.length; i++) {
            assertEquals(histogram.getValueAtQuantile(QUANTILES[i]), values[i]);
        }
    }


    /**
     * Test clear resets the histogram.
     */
    @Test
    public void testClear() {
        PercentileHistogram histogram = new PercentileHistogram();
        histogram.add(5 * GB);
        histogram.clear();
        assertEquals(0, histogram.getCount());
        assertEquals(0, histogram.getValueAtQuantile(0.5));
        assertEquals(new PercentileHistogram(), histogram);
    }


    /**
     * Test equals, hashCode and toString.
     */
    @Test
    public void testEqualsHashCodeToString() {
        PercentileHistogram histogram1 = new PercentileHistogram();
        PercentileHistogram histogram2 = new PercentileHistogram();
        histogram1.add(100 * MB);
        histogram2.add(100 * MB);
        assertEquals(histogram1, histogram2);
        assertEquals(histogram1.hashCode(), histogram2.hashCode());
        assertEquals(histogram1, histogram1);
        assertNotEquals(histogram1, null);

        histogram2.add(200 * MB);
        assertNotEquals(histogram1, histogram2);
        assertTrue(histogram2.toString().contains("count=2"));
        assertTrue(new PercentileHistogram().toString().contains("min=0"));
        assertTrue(new PercentileHistogram().toString().contains("footprint="));
    }


    /**
     * Test the limited constructor caps memory and clamps out-of-range values.
     */
    @Test
    public void testLimitedRanges() {
        // 8 GB max value: rangesFor(8 GB) = 24, max memory = 16 + 23 * 8 = 200 KB
        long maxValue = 8 * GB;
        PercentileHistogram histogram = new PercentileHistogram(maxValue);
        assertEquals(200, histogram.getMemoryFootprintKB());

        // all non-negative values are accepted — out-of-range values are clamped, not rejected
        histogram.add(0);
        histogram.add(GB);
        histogram.add(4 * GB);
        histogram.add(100 * GB); // beyond range: clamped to last bucket
        assertEquals(4, histogram.getCount());

        // negative values are still rejected
        assertThrows(IllegalArgumentException.class, () -> histogram.add(-1));

        // two histograms with different rangeCount are not equal even if they contain the same data
        PercentileHistogram full = new PercentileHistogram();
        full.add(GB);
        PercentileHistogram limited = new PercentileHistogram(maxValue);
        limited.add(GB);
        assertNotEquals(full, limited);

        // precision is maintained for values within the configured range
        Random random = new Random(9);
        long maxSample = 4 * GB;
        assertPrecision(new PercentileHistogram(maxSample), maxSample, () -> (long) (random.nextDouble() * maxSample));
    }


    /**
     * Test clone produces an independent snapshot with equal content.
     */
    @Test
    public void testClone() {
        PercentileHistogram histogram = new PercentileHistogram(4 * GB);
        histogram.add(GB);
        histogram.add(2 * GB);

        PercentileHistogram clone = histogram.clone();
        assertNotSame(histogram, clone);
        assertEquals(histogram, clone);
        assertEquals(histogram.hashCode(), clone.hashCode());
        assertEquals(histogram.getMemoryFootprintKB(), clone.getMemoryFootprintKB());

        // clone is independent: clearing the original does not affect the clone
        histogram.clear();
        assertNotEquals(histogram, clone);
        assertEquals(2, clone.getCount());
    }


    /**
     * Test serialization round-trip preserves all data and produces a fully usable instance.
     *
     * @throws Exception in case of error
     */
    @Test
    public void testSerialization() throws Exception {
        PercentileHistogram original = new PercentileHistogram(4 * GB);
        original.add(GB);
        original.add(2 * GB);
        original.add(3 * GB);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }

        PercentileHistogram restored;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            restored = (PercentileHistogram) ois.readObject();
        }

        assertEquals(original, restored);
        assertEquals(original.getValueAtQuantile(0.5), restored.getValueAtQuantile(0.5));
        assertEquals(original.getMemoryFootprintKB(), restored.getMemoryFootprintKB());

        // lock is functional after deserialization
        restored.add(4 * GB);
        assertEquals(4, restored.getCount());
    }


    /**
     * Test that corrupt serialized data is rejected with an {@link InvalidObjectException}.
     *
     * @throws Exception in case of error
     */
    @Test
    public void testDeserializationOfCorruptData() throws Exception {
        final long value = 123_456_789L;
        PercentileHistogram original = new PercentileHistogram();
        original.add(value);
        byte[] data = serialize(original);
        assertEquals(original, deserialize(data));

        // the primitive fields are serialized sorted by name: max, min, rangeCount, totalCount
        byte[] pattern = ByteBuffer.allocate(2 * Long.BYTES).putLong(value).putLong(value).array();
        int maxPos = indexOf(data, pattern);
        assertTrue(maxPos >= 0);
        int minPos = maxPos + Long.BYTES;
        int rangeCountPos = minPos + Long.BYTES;
        int totalCountPos = rangeCountPos + Integer.BYTES;

        byte[] corruptRangeCount = data.clone();
        ByteBuffer.wrap(corruptRangeCount).putInt(rangeCountPos, 1);
        assertThrows(InvalidObjectException.class, () -> deserialize(corruptRangeCount));

        byte[] corruptTotalCount = data.clone();
        ByteBuffer.wrap(corruptTotalCount).putLong(totalCountPos, 2L);
        assertThrows(InvalidObjectException.class, () -> deserialize(corruptTotalCount));

        byte[] corruptMin = data.clone();
        ByteBuffer.wrap(corruptMin).putLong(minPos, value + 1);
        assertThrows(InvalidObjectException.class, () -> deserialize(corruptMin));
    }


    /**
     * Serializes an object.
     *
     * @param object the object to serialize
     * @return the serialized data
     * @throws IOException in case of an I/O error
     */
    private static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
        }
        return bos.toByteArray();
    }


    /**
     * Deserializes an object.
     *
     * @param data the serialized data
     * @return the object
     * @throws IOException in case of an I/O error
     * @throws ClassNotFoundException in case the class could not be found
     */
    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ois.readObject();
        }
    }


    /**
     * Finds the first position of a pattern in the data.
     *
     * @param data the data
     * @param pattern the pattern
     * @return the position or -1 if the pattern was not found
     */
    private static int indexOf(byte[] data, byte[] pattern) {
        for (int i = 0; i <= data.length - pattern.length; i++) {
            if (Arrays.equals(data, i, i + pattern.length, pattern, 0, pattern.length)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Records samples and asserts that all quantiles are within the relative precision of the exact nearest-rank percentiles.
     *
     * @param generator the sample generator
     */
    private static void assertPrecision(LongSupplier generator) {
        assertPrecision(new PercentileHistogram(), PercentileHistogram.MAX_VALUE, generator);
    }


    /**
     * Records samples and asserts that all quantiles are within the relative precision of the exact nearest-rank percentiles.
     *
     * @param histogram the histogram to use
     * @param maxSample the maximum sample value to generate (samples are clamped to this)
     * @param generator the sample generator
     */
    private static void assertPrecision(PercentileHistogram histogram, long maxSample, LongSupplier generator) {
        long[] samples = new long[SAMPLES];
        for (int i = 0; i < SAMPLES; i++) {
            samples[i] = Math.max(0, Math.min(maxSample, generator.getAsLong()));
            histogram.add(samples[i]);
        }
        Arrays.sort(samples);

        long[] values = histogram.getValuesAtQuantiles(QUANTILES);
        for (int i = 0; i < QUANTILES.length; i++) {
            int rank = (int) Math.max(1, Math.ceil(QUANTILES[i] * SAMPLES));
            long exact = samples[rank - 1];
            double error = Math.abs(values[i] - exact);
            assertTrue(error <= exact * MAX_RELATIVE_ERROR,
                    "Quantile " + QUANTILES[i] + ": expected " + exact + " but was " + values[i]);
        }
    }


    /**
     * Test that concurrent symmetric equals calls with active writers do not deadlock and that equals and hashCode are consistent.
     *
     * @throws InterruptedException in case of interrupt
     */
    @Test
    public void testConcurrentEqualsNoDeadlock() throws InterruptedException {
        final PercentileHistogram a = new PercentileHistogram();
        final PercentileHistogram b = new PercentileHistogram();
        final AtomicBoolean failed = new AtomicBoolean(false);
        final int loops = 20_000;

        Thread[] threads = new Thread[] {
            new Thread(() -> {
                for (int i = 0; i < loops; i++) {
                    a.equals(b);
                }
            }),
            new Thread(() -> {
                for (int i = 0; i < loops; i++) {
                    b.equals(a);
                }
            }),
            new Thread(() -> {
                for (int i = 0; i < loops; i++) {
                    a.add(i);
                }
            }),
            new Thread(() -> {
                for (int i = 0; i < loops; i++) {
                    b.add(i * 2L);
                }
            })
        };

        for (Thread t : threads) {
            t.setDaemon(true);
            t.start();
        }
        for (Thread t : threads) {
            t.join(30_000L);
            if (t.isAlive()) {
                failed.set(true);
            }
        }
        assertTrue(!failed.get(), "equals deadlocked");

        PercentileHistogram c = new PercentileHistogram();
        PercentileHistogram d = new PercentileHistogram();
        for (int i = 0; i < 100; i++) {
            c.add(i);
            d.add(i);
        }
        assertEquals(c, d);
        assertEquals(c.hashCode(), d.hashCode());
        assertEquals(c, c.clone());
        assertEquals(c.hashCode(), c.clone().hashCode());
    }


    /**
     * Test the rank of a quantile is calculated from its decimal value (nearest-rank method).
     */
    @Test
    public void testRankOfDecimalQuantile() {
        PercentileHistogram histogram = new PercentileHistogram();
        for (long i = 1; i <= 100; i++) {
            histogram.add(i);
        }

        assertEquals(7, histogram.getValueAtQuantile(0.07));
        assertEquals(55, histogram.getValueAtQuantile(0.55));
        assertArrayEquals(new long[] {1, 7, 55, 100}, histogram.getValuesAtQuantiles(0.0, 0.07, 0.55, 1.0));
        assertEquals(1, PercentileHistogram.rank(0.0, 100));
        assertEquals(100, PercentileHistogram.rank(1.0, 100));
    }


    /**
     * Test the maximum is reported exactly also if it exceeds the configured range.
     */
    @Test
    public void testMaxAboveConfiguredRange() {
        PercentileHistogram histogram = new PercentileHistogram(1000L);
        histogram.add(10);
        histogram.add(5000);
        histogram.add(1_000_000);

        assertEquals(1_000_000, histogram.getValueAtQuantile(1.0));
        assertEquals(10, histogram.getValueAtQuantile(0.0));
        assertTrue(histogram.getValueAtQuantile(0.5) >= 2047);
    }


    /**
     * Test the clone of a subclass keeps its class and is independent.
     */
    @Test
    public void testCloneOfSubclass() {
        PercentileHistogram histogram = new PercentileHistogram(4 * GB) {
            private static final long serialVersionUID = 1L;
        };
        histogram.add(GB);

        PercentileHistogram clone = histogram.clone();
        assertEquals(histogram.getClass(), clone.getClass());
        assertEquals(histogram, clone);

        clone.add(2 * GB);
        assertEquals(1, histogram.getCount());
        assertEquals(2, clone.getCount());
        assertEquals(GB, histogram.getValueAtQuantile(1.0));
    }
}
