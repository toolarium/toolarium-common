/*
 * BandwidthThrottling.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.bandwidth;

import com.github.toolarium.common.formatter.TimeDifferenceFormatter;
import com.github.toolarium.common.statistic.StatisticCounter;
import com.github.toolarium.common.util.TextUtil;
import com.github.toolarium.common.util.ThreadUtil;
import java.io.Serializable;
import java.text.SimpleDateFormat;


/**
 * Defines the process bandwidth throttling
 *
 * @author patrick
 */
public class BandwidthThrottling implements IBandwidthThrottling, Serializable {
    /** NO_BANDWIDTH */
    public static final int NO_BANDWIDTH = -1;
    
    private static final long serialVersionUID = -129363107817850760L;
    private long bandwidth;
    private long count;
    private int updateInterval;
    private Long startTime;
    private Long lastUpdateTime;
    private StatisticCounter bandwidthStatisticCounter;
    private StatisticCounter sleepStatisticCounter;


    /**
     * Constructor for BandwidthThrottling
     */
    public BandwidthThrottling() {
        this(NO_BANDWIDTH);
    }

    
    /**
     * Constructor for BandwidthThrottling
     *
     * @param bandwidth bandwidth in number of calls per second
     */
    public BandwidthThrottling(long bandwidth) {
        this(bandwidth, 10);
    }

    
    /**
     * Constructor for BandwidthThrottling
     *
     * @param bandwidth bandwidth in number of calls per second
     * @param updateInterval the update interval (default 10)
     */
    public BandwidthThrottling(long bandwidth, int updateInterval) {
        this.updateInterval = 10;
        this.bandwidth = bandwidth;

        if (updateInterval >= 0) {
            this.updateInterval = updateInterval;
        }
        
        this.count = 0;
        this.startTime = null;
        this.lastUpdateTime = null;
        this.bandwidthStatisticCounter = new StatisticCounter();
        this.sleepStatisticCounter = new StatisticCounter();
    }


    /**
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#reset()
     */
    @Override
    public synchronized void reset() {
        this.count = 0;
        this.startTime = null;
        this.lastUpdateTime = null;
        this.bandwidthStatisticCounter = new StatisticCounter();
        this.sleepStatisticCounter = new StatisticCounter();
    }


    /**
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#bandwidthCheck()
     */
    @Override
    public synchronized void bandwidthCheck() {
        if (bandwidth <= 0) {
            return;
        }

        if (startTime == null) {
            startTime = System.currentTimeMillis();
        }

        long bandwidthCheckStart = System.currentTimeMillis();
        long currentBandwidth = 0;

        // do bandwidth check only if bandwidth
        if ((count % updateInterval) == 0) {
            do {
                currentBandwidth = calculateCurrentBandwidth();
                if (currentBandwidth > bandwidth) {
                    ThreadUtil.getInstance().sleep(100L);
                    bandwidthStatisticCounter.add(calculateCurrentBandwidth());
                } else {
                    bandwidthStatisticCounter.add(currentBandwidth);
                }
            } while (currentBandwidth > bandwidth);
            sleepStatisticCounter.add(System.currentTimeMillis() - bandwidthCheckStart);
        } else {
            sleepStatisticCounter.add(0);
        }

        lastUpdateTime = System.currentTimeMillis();
        count++;
    }

    
    /**
     * Calculate the current bandwidth
     *
     * @return the current bandwidth
     */
    protected long calculateCurrentBandwidth() {
        long usedtime = System.currentTimeMillis() - startTime;
        if (usedtime > 0) {
            return (count * 1000) / usedtime;
        }
        return 0;
    }


    /**
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#getBandwidth()
     */
    @Override
    public synchronized long getBandwidth() {
        return bandwidth;
    }


    /**
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#setBandwidth(long)
     */
    @Override
    public synchronized void setBandwidth(long bandwidth) {
        this.bandwidth = bandwidth;
    }


    /**
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#getUpdateInterval()
     */
    @Override
    public synchronized int getUpdateInterval() {
        return updateInterval;
    }


    /**
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#setUpdateInterval(int)
     */
    @Override
    public synchronized void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }


    /**
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#getCounter()
     */
    @Override
    public synchronized long getCounter() {
        return count;
    }


    /**
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#getStartTime()
     */
    @Override
    public synchronized Long getStartTime() {
        return startTime;
    }


    /**
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#getLastUpdateTime()
     */
    @Override
    public synchronized Long getLastUpdateTime() {
        return lastUpdateTime;
    }


    /**
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#getBandwidthStatisticCounter()
     */
    @Override
    public synchronized StatisticCounter getBandwidthStatisticCounter() {
        return bandwidthStatisticCounter;
    }


    /**
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#getSleepStatisticCounter()
     */
    @Override
    public synchronized StatisticCounter getSleepStatisticCounter() {
        return sleepStatisticCounter;
    }

    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        TimeDifferenceFormatter df = new TimeDifferenceFormatter();

        String startTimeStr = "n/a";
        if (startTime != null) {
            startTimeStr = formatter.format(startTime);
        }
        
        String lastUpdateTimeStr = "n/a";
        if (lastUpdateTime != null) {
            lastUpdateTimeStr = formatter.format(lastUpdateTime);
        }
        
        String usedTimeStr = "n/a";
        if (startTime != null && lastUpdateTime != null) {
            usedTimeStr = df.formatAsString(lastUpdateTime - startTime);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Process bandwidth throttling:").append(TextUtil.NL);
        builder.append("bandwidth  : " + bandwidth).append(TextUtil.NL);
        builder.append("counter    : " + count).append(TextUtil.NL);
        builder.append("start time : " + startTimeStr).append(TextUtil.NL);
        builder.append("last update: " + lastUpdateTimeStr).append(TextUtil.NL);
        builder.append("used time  : " + usedTimeStr).append(TextUtil.NL);
        builder.append(bandwidthStatisticCounter.toString("bandwidth statistic:")).append(TextUtil.NL);
        builder.append(sleepStatisticCounter.toString("sleep time statistic:")).append(".");
        return builder.toString();
    }
}
