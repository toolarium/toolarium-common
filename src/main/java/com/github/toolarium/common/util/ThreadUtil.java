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
    private static final class HOLDER {
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
     * Sleep. If the thread is interrupted during sleep, the interrupt flag is restored
     * and this method returns {@code true} so callers can react accordingly.
     *
     * @param timeout the timeout in milliseconds
     * @return true if the sleep was interrupted, false otherwise
     */
    public boolean sleep(Long timeout) {
        if (timeout == null || timeout <= 0) {
            return false;
        }

        // sleep
        try {
            Thread.sleep(timeout);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
    }
}
