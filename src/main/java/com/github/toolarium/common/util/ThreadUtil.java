/*
 * ThreadUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

/**
 * Thread sleep util.
 * 
 * @author patrick
 */
public final class ThreadUtil {

    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
        static final ThreadUtil INSTANCE = new ThreadUtil();
    }

    
    /**
     * Constructor
     */
    private ThreadUtil() {
        // NOP
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static ThreadUtil getInstance() {
        return HOLDER.INSTANCE;
    }

    
    /**
     * Sleep
     *
     * @param timeout the timeout
     */
    public void sleep(Long timeout) {
        if (timeout == null || timeout <= 0) {
            return;
        }

        // sleep
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
