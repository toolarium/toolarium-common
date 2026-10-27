/*
 * IBandwidthThrottling.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.bandwidth;

import com.github.toolarium.common.statistic.StatisticCounter;

/**
 * Defines the process bandwith throttling interface.
 *
 * @author patrick
 */
public interface IBandwidthThrottling {
    
    /**
     * Reset the internal states
     */
    void reset();

    
    /**
     * The bandwidth check
     */
    void bandwidthCheck();

    
    /**
     * Gets the bandwidth
     *
     * @return Returns the bandwidth.
     */
    long getBandwidth();

    
    /**
     * Sets the bandwidth
     *
     * @param bandwidth The bandwidth to set in number of records per second.
     */
    void setBandwidth(long bandwidth);

    
    /**
     * Gets the counter
     *
     * @return Returns the counter.
     */
    long getCounter();

    
    /**
     * Gets the start time
     *
     * @return Returns the start time.
     */
    Long getStartTime();

    
    /**
     * Gets the last update time
     *
     * @return Returns the the last update time
     */
    Long getLastUpdateTime();

    
    /**
     * Gets the bandwidth statistic counter
     *
     * @return Returns the bandwidth statistic counter.
     */
    StatisticCounter getBandwidthStatisticCounter();

    
    /**
     * Gets the sleep statistic counter
     *
     * @return Returns the sleep statistic counter
     */
    StatisticCounter getSleepStatisticCounter();
}
