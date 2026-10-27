/*
 * MemoryUsageCalculatorPerformanceIntegrationTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.common.runtime.ToolariumInstrumentation;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Performance measurement of the {@link MemoryUsageCalculator} with the toolarium-java-agent. The duration is only
 * logged as baseline: most of the time is spent by the traversal of the object graph, not by the size of the objects.
 *
 * @author patrick
 */
public class MemoryUsageCalculatorPerformanceIntegrationTest {
    private static final Logger LOG = LoggerFactory.getLogger(MemoryUsageCalculatorPerformanceIntegrationTest.class);
    private static final int ENTRIES = 300_000;
    private static final int RUNS = 3;


    /**
     * Measure a map with many entries: the size must be the same as the sum of the shallow sizes, the duration is logged.
     */
    @Test
    public void measureLargeMap() {
        final Instrumentation inst = ToolariumInstrumentation.getInstance().getInstrumentation();
        assertNotNull(inst, "The toolarium-java-agent is not loaded");

        final Map<Integer, String> map = new HashMap<Integer, String>();
        for (int i = 0; i < ENTRIES; i++) {
            map.put(Integer.valueOf(i), "value-" + i);
        }

        final MemoryUsageCalculator calculator = new MemoryUsageCalculator();
        final long[] size = new long[1];
        final long millis = bestOf(() -> {
            size[0] = calculator.memoryUsageOf(map);
            return size[0];
        });

        LOG.info("Deep size of a map with " + ENTRIES + " entries: " + size[0] + " bytes in " + millis + " ms.");
        assertEquals(calculator.deepSizeOf(map, inst::getObjectSize), size[0]);
        assertTrue(size[0] > ENTRIES);
    }


    /**
     * Run a measurement several times and return the best duration (the first runs warm up the JVM).
     *
     * @param measurement the measurement
     * @return the best duration in milliseconds
     */
    private long bestOf(LongSupplier measurement) {
        long best = Long.MAX_VALUE;
        for (int i = 0; i < RUNS; i++) {
            final long start = System.nanoTime();
            measurement.getAsLong();
            best = Math.min(best, (System.nanoTime() - start) / 1_000_000L);
        }

        return best;
    }
}
