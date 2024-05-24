/*
 * ThreadUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

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
        ThreadUtil.getInstance().sleep(sleepTime);
        
        assertTrue((System.currentTimeMillis() - start) >= sleepTime);
    }
}
