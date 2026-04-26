/*
 * ThreadUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * Test the {@link ThreadUtil}.
 *
 * @author patrick
 */
public class ThreadUtilTest {

    /**
     * Test sleep
     */
    @Test
    public void testSleep() {
        final long sleepTime = 100L;
        long start = System.currentTimeMillis();
        boolean interrupted = ThreadUtil.getInstance().sleep(sleepTime);

        assertTrue((System.currentTimeMillis() - start) >= sleepTime);
        assertFalse(interrupted);
    }


    /**
     * Test sleep returns false for null and zero
     */
    @Test
    public void testSleepEdgeCases() {
        assertFalse(ThreadUtil.getInstance().sleep(null));
        assertFalse(ThreadUtil.getInstance().sleep(0L));
        assertFalse(ThreadUtil.getInstance().sleep(-1L));
    }


    /**
     * Test sleep returns true when interrupted
     *
     * @throws Exception in case of error
     */
    @Test
    public void testSleepInterrupt() throws Exception {
        Thread testThread = Thread.currentThread();
        Thread interrupter = new Thread(() -> {
            ThreadUtil.getInstance().sleep(50L);
            testThread.interrupt();
        });
        interrupter.start();

        boolean interrupted = ThreadUtil.getInstance().sleep(5000L);
        assertTrue(interrupted);

        // clear interrupt flag
        Thread.interrupted();
        interrupter.join();
    }
}
