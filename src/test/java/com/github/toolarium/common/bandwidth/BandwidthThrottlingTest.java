/*
 * BandwidthThrottlingTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.bandwidth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.common.util.RandomGenerator;
import com.github.toolarium.common.util.RoundUtil;
import com.github.toolarium.common.util.ThreadUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test {@link BandwidthThrottling}.
 * 
 * @author patrick
 */
public class BandwidthThrottlingTest {
    private static final Logger LOG = LoggerFactory.getLogger(BandwidthThrottlingTest.class);
    
    
    /**
     * Test
     */
    @Test
    public void test1() {
        runBandwidthThrottling("Test1", 70, 500, 8);
    }

    
    /**
     * Test
     */
    @Test
    public void test2() {
        runBandwidthThrottling("Test2", 40, 200, 10);
    }

    
    /**
     * Test
     */
    @Test
    public void test3() {
        runBandwidthThrottling("Test3", 100, 500, 10);
    }

    
    /**
     * Test that an interrupted thread stops waiting
     */
    @Test
    public void testInterruptStopsWaiting() {
        BandwidthThrottling p = new BandwidthThrottling(1) {
            private static final long serialVersionUID = 1L;

            /**
             * @see com.github.toolarium.common.bandwidth.BandwidthThrottling#calculateWaitTime()
             */
            @Override
            protected long calculateWaitTime() {
                return 10_000L; // always wait
            }
        };

        Thread.currentThread().interrupt();
        long start = System.currentTimeMillis();
        try {
            p.bandwidthCheck();
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted(); // clear the flag
        }

        assertTrue(System.currentTimeMillis() - start < 5_000L);
        assertEquals(1, p.getCounter());
    }


    /**
     * Test that the monitor is not held while waiting
     *
     * @throws InterruptedException in case of interrupt
     */
    @Test
    public void testMonitorNotHeldWhileWaiting() throws InterruptedException {
        BandwidthThrottling p = new BandwidthThrottling(1) {
            private static final long serialVersionUID = 1L;

            /**
             * @see com.github.toolarium.common.bandwidth.BandwidthThrottling#calculateWaitTime()
             */
            @Override
            protected long calculateWaitTime() {
                return 10_000L; // always wait
            }
        };

        Thread t = new Thread(p::bandwidthCheck);
        t.start();
        ThreadUtil.getInstance().sleep(200L);

        // the getter must not block while the other thread waits
        long start = System.currentTimeMillis();
        assertEquals(0, p.getCounter());
        assertTrue(System.currentTimeMillis() - start < 1_000L);

        t.interrupt();
        t.join(5_000L);
        assertTrue(!t.isAlive());
        assertEquals(1, p.getCounter());
    }


    /**
     * Run bandwidth throttling
     * 
     * @param testName the name of the test
     * @param bandwidth  the bandwidth
     * @param testCount the test count
     * @param maxSleepTime the max sleep time
     * @return the {@link BandwidthThrottling} instance
     */
    private BandwidthThrottling runBandwidthThrottling(String testName, long bandwidth, long testCount, int maxSleepTime) {
        LOG.debug("" + testName + ", process bandwidth throttling: bandwidth=" + bandwidth + ", tests: " + testCount + ", maxSleepTime=" + maxSleepTime);
        BandwidthThrottling p = new BandwidthThrottling(bandwidth);

        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < testCount; i++) {
            p.bandwidthCheck();
            if (i < testCount - 1) {
                ThreadUtil.getInstance().sleep(RandomGenerator.getInstance().getRandomNumber(maxSleepTime, false));
            }
        }

        //LOG.debug("" + testName + ", process bandwidth throttling: ");
        final long stopTime = System.currentTimeMillis();
        assertEquals(p.getCounter(), testCount);
        assertEquals(p.getBandwidth(), bandwidth);
        assertTrue(Math.abs(p.getStartTime() - startTime) <= 50);
        assertTrue(Math.abs(p.getLastUpdateTime() - stopTime) <= 50);

        LOG.debug("" + testName + ", bandwidth avg: " + RoundUtil.getInstance().roundToLong(p.getBandwidthStatisticCounter().getAverage()));
        LOG.debug("" + testName + ", sleep time avg: " + RoundUtil.getInstance().roundToLong(p.getSleepStatisticCounter().getAverage()));
        return p;
    }

    /**
     * After an idle time a burst passes at most the calls of one second, then the bandwidth is kept
     */
    @Test
    public void testBurstAfterIdleTimeIsLimited() {
        BandwidthThrottling throttling = new BandwidthThrottling(10);
        throttling.bandwidthCheck();
        ThreadUtil.getInstance().sleep(1200L);

        // 10 calls pass immediately, the other 10 calls with 10 calls per second
        long start = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            throttling.bandwidthCheck();
        }

        long duration = System.currentTimeMillis() - start;
        assertTrue(duration >= 900L, "Burst was not throttled: " + duration + "ms");
    }


    /**
     * Steady slow traffic must not build up a credit which lets a later burst bypass the throttling (M15)
     */
    @Test
    public void testNoBurstAfterSlowTraffic() {
        BandwidthThrottling throttling = new BandwidthThrottling(20);
        for (int i = 0; i < 10; i++) {
            throttling.bandwidthCheck();
            ThreadUtil.getInstance().sleep(300L);
        }

        // 20 calls pass immediately, the other 40 calls with 20 calls per second
        long start = System.currentTimeMillis();
        for (int i = 0; i < 60; i++) {
            throttling.bandwidthCheck();
        }

        long duration = System.currentTimeMillis() - start;
        assertTrue(duration >= 1800L, "Burst was not throttled: " + duration + "ms");
        assertEquals(70, throttling.getCounter());
    }


    /**
     * The bandwidth statistic contains the number of calls of each second
     */
    @Test
    public void testBandwidthStatistic() {
        BandwidthThrottling throttling = new BandwidthThrottling(50);
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 2_300L) {
            throttling.bandwidthCheck();
        }

        assertTrue(throttling.getBandwidthStatisticCounter().getCounter() >= 1);
        // the first second contains the burst of the full bucket
        assertTrue(throttling.getBandwidthStatisticCounter().getMaxValue() <= 2 * 50 + 2, throttling.toString());
    }


    /**
     * Serialization round trip and toString
     *
     * @throws IOException in case of an I/O error
     * @throws ClassNotFoundException in case the class could not be found
     */
    @Test
    public void testSerializationAndToString() throws IOException, ClassNotFoundException {
        BandwidthThrottling throttling = new BandwidthThrottling(1000);
        for (int i = 0; i < 5; i++) {
            throttling.bandwidthCheck();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(throttling);
        }

        BandwidthThrottling copy;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            copy = (BandwidthThrottling) ois.readObject();
        }

        assertEquals(throttling.getCounter(), copy.getCounter());
        assertEquals(throttling.getBandwidth(), copy.getBandwidth());
        assertEquals(throttling.getStartTime(), copy.getStartTime());
        assertEquals(throttling.getSleepStatisticCounter(), copy.getSleepStatisticCounter());
        assertEquals(throttling.toString(), copy.toString());
        assertTrue(copy.toString().contains("used time"));
    }


    /**
     * With a token store the shared tokens are used instead of the local token bucket
     */
    @Test
    public void testTokenStoreIsUsed() {
        final AtomicInteger calls = new AtomicInteger();
        BandwidthThrottling throttling = new BandwidthThrottling(1, bandwidth -> {
            calls.incrementAndGet();
            return 0;
        });

        // the local bandwidth of 1 call per second would need about 19 seconds
        long start = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            throttling.bandwidthCheck();
        }

        assertTrue(System.currentTimeMillis() - start < 1_000L);
        assertEquals(20, calls.get());
        assertEquals(20, throttling.getCounter());
    }


    /**
     * The wait time of the token store is respected
     */
    @Test
    public void testTokenStoreWaitTime() {
        final AtomicInteger calls = new AtomicInteger();
        BandwidthThrottling throttling = new BandwidthThrottling(1000, bandwidth -> {
            if (calls.incrementAndGet() == 1) {
                return 200L;
            }
            return 0;
        });

        long start = System.currentTimeMillis();
        throttling.bandwidthCheck();
        assertTrue(System.currentTimeMillis() - start >= 150L);
        assertEquals(2, calls.get());
    }


    /**
     * If the token store fails, the local token bucket is used until the token store works again
     */
    @Test
    public void testTokenStoreFailureUsesLocalBandwidth() {
        final AtomicInteger calls = new AtomicInteger();
        BandwidthThrottling throttling = new BandwidthThrottling(10, bandwidth -> {
            calls.incrementAndGet();
            throw new IllegalStateException("store is down");
        });

        // 10 calls pass immediately, the other 10 calls with 10 calls per second
        long start = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            throttling.bandwidthCheck();
        }
        assertTrue(System.currentTimeMillis() - start >= 900L);

        // after a failure the token store is not called for 5 seconds
        assertEquals(1, calls.get());

        throttling.setTokenStore(bandwidth -> 0);
        start = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            throttling.bandwidthCheck();
        }
        assertTrue(System.currentTimeMillis() - start < 1_000L);
        assertEquals(40, throttling.getCounter());
    }


    /**
     * The token store is not serialized, also a not serializable token store can be used
     *
     * @throws IOException in case of an I/O error
     */
    @Test
    public void testTokenStoreIsNotSerialized() throws IOException {
        BandwidthThrottling throttling = new BandwidthThrottling(10, bandwidth -> 0);
        throttling.bandwidthCheck();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(throttling);
        }
        assertTrue(bos.size() > 0);
    }


    /**
     * Waiting threads reserve their call: every call checks the bandwidth only once, there is no thundering herd (M06)
     *
     * @throws InterruptedException in case of interrupt
     */
    @Test
    public void testWaitingThreadsReserveTheirCall() throws InterruptedException {
        final AtomicLong checks = new AtomicLong();
        BandwidthThrottling throttling = new BandwidthThrottling(50) {
            private static final long serialVersionUID = 1L;

            /**
             * @see com.github.toolarium.common.bandwidth.BandwidthThrottling#calculateWaitTime()
             */
            @Override
            protected long calculateWaitTime() {
                checks.incrementAndGet();
                return super.calculateWaitTime();
            }
        };

        Thread[] threads = new Thread[20];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 5; j++) {
                    throttling.bandwidthCheck();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join(30_000L);
        }

        // 100 calls with 50 calls per second: 50 pass immediately, the other 50 within about one second
        assertEquals(100, throttling.getCounter());
        assertEquals(100, checks.get());
    }


    /**
     * The bandwidth statistic contains the calls of the current second, reading it does not count them twice
     */
    @Test
    public void testBandwidthStatisticContainsCurrentSecond() {
        BandwidthThrottling throttling = new BandwidthThrottling(1000);
        for (int i = 0; i < 5; i++) {
            throttling.bandwidthCheck();
        }

        assertEquals(5, Math.round(throttling.getBandwidthStatisticCounter().getSum()));
        assertEquals(5, Math.round(throttling.getBandwidthStatisticCounter().getSum()));
        throttling.bandwidthCheck();
        assertEquals(6, Math.round(throttling.getBandwidthStatisticCounter().getSum()));
        String str = throttling.toString();
        String bandwidthStatistic = str.substring(str.indexOf("bandwidth statistic:"), str.indexOf("sleep time statistic:"));
        assertTrue(bandwidthStatistic.contains("sum: 6.0"), str);
    }


    /**
     * The token store is not called again once the maximum wait time is exceeded, the time in the token store counts
     */
    @Test
    public void testTokenStoreTimeCountsTowardMaxWaitTime() {
        final AtomicInteger calls = new AtomicInteger();
        BandwidthThrottling throttling = new BandwidthThrottling(1000, bandwidth -> {
            calls.incrementAndGet();
            ThreadUtil.getInstance().sleep(200L);
            return 50L;
        }) {
            private static final long serialVersionUID = 1L;

            /**
             * @see com.github.toolarium.common.bandwidth.BandwidthThrottling#getMaxWaitTime()
             */
            @Override
            long getMaxWaitTime() {
                return 300L;
            }
        };

        long start = System.currentTimeMillis();
        throttling.bandwidthCheck();
        assertTrue(System.currentTimeMillis() - start < 1_000L);
        assertTrue(calls.get() >= 1 && calls.get() <= 2, "calls: " + calls.get());
        assertEquals(1, throttling.getCounter());
    }
}
