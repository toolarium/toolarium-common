/*
 * ProcessBandwidthThrottlingTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.bandwidth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.common.util.RandomGenerator;
import com.github.toolarium.common.util.RoundUtil;
import com.github.toolarium.common.util.ThreadUtil;
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
        runBandwidthThrottling("Test1", 70, 10, 500, 8);
    }

    
    /**
     * Test
     */
    @Test
    public void test2() {
        runBandwidthThrottling("Test2", 40, 20, 200, 10);
    }

    
    /**
     * Test
     */
    @Test
    public void test3() {
        runBandwidthThrottling("Test3", 100, 10, 500, 10);
    }

    
    /**
     * Run bandwidth throttling
     * 
     * @param testName the name of the test
     * @param bandwidth  the bandwidth
     * @param updateInterval the update interval
     * @param testCount the test count
     * @param maxSleepTime the max sleep time
     * @return the {@link BandwidthThrottling} instance
     */
    private BandwidthThrottling runBandwidthThrottling(String testName, long bandwidth, int updateInterval, long testCount, int maxSleepTime) {
        LOG.debug("" + testName + ", process bandwidth throttling: bandwidth=" + bandwidth + ", updateInterval=" + updateInterval + ", tests: " + testCount + ", maxSleepTime=" + maxSleepTime);
        final long startTime = System.currentTimeMillis();
        BandwidthThrottling p = new BandwidthThrottling(bandwidth, updateInterval);

        for (int i = 0; i < testCount; i++) {
            p.bandwidthCheck();
            ThreadUtil.getInstance().sleep(RandomGenerator.getInstance().getRandomNumber(maxSleepTime, false));
        }

        //LOG.debug("" + testName + ", process bandwidth throttling: ");
        final long stopTime = System.currentTimeMillis();
        assertEquals(p.getCounter(), testCount);
        assertEquals(p.getBandwidth(), bandwidth);
        assertEquals(p.getUpdateInterval(), updateInterval);
        assertTrue(p.getStartTime() - startTime <= 10);
        assertTrue(p.getLastUpdateTime() - stopTime <= 10);

        LOG.debug("" + testName + ", bandwidth avg: " + RoundUtil.getInstance().roundToLong(p.getBandwidthStatisticCounter().getAverage()));
        LOG.debug("" + testName + ", sleep time avg: " + RoundUtil.getInstance().roundToLong(p.getSleepStatisticCounter().getAverage()));
        return p;
    }
}
