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
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Defines the process bandwidth throttling. It works as token bucket: the credit is refilled with the configured
 * bandwidth and limited to the calls of one second, so a burst passes at most the calls of one second and
 * afterwards exactly the configured bandwidth. With an {@link IBandwidthThrottlingTokenStore} the token bucket is shared over multiple
 * instances; if the token store fails, the local token bucket of this instance is used.
 *
 * @author patrick
 */
public class BandwidthThrottling implements IBandwidthThrottling, Serializable {
    /** Bandwidth value which disables the throttling (no bandwidth limit). */
    public static final int NO_BANDWIDTH = -1;

    private static final long serialVersionUID = -129363107817850760L;
    private static final Logger LOG = LoggerFactory.getLogger(BandwidthThrottling.class);
    private static final long ONE_SECOND_NANOS = 1_000_000_000L;
    private static final long MAX_WAIT_TIME = 60_000L;
    private static final long TOKEN_STORE_RETRY_PAUSE = 5_000L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    private static final TimeDifferenceFormatter TIME_DIFFERENCE_FORMATTER = new TimeDifferenceFormatter();
    private long bandwidth;
    private long count;
    private Long startTime;
    private Long lastUpdateTime;
    private StatisticCounter bandwidthStatisticCounter;
    private StatisticCounter sleepStatisticCounter;

    // the optional shared token store, it is not serialized
    private transient IBandwidthThrottlingTokenStore tokenStore;
    private transient Long tokenStoreFailedTime; // the time of the last failure of the token store, null if it works

    // the local token bucket, based on a monotonic clock
    private transient boolean bucketStarted;
    private transient double tokens;
    private transient long lastRefillNanos;

    // the measurement of the calls of each second for the bandwidth statistic
    private transient long secondStartNanos;
    private transient long secondCount;


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
        this(bandwidth, null);
    }


    /**
     * Constructor for BandwidthThrottling
     *
     * @param bandwidth bandwidth in number of calls per second
     * @param tokenStore the shared token store to control the bandwidth over multiple instances, null to use the local token bucket
     */
    public BandwidthThrottling(long bandwidth, IBandwidthThrottlingTokenStore tokenStore) {
        this.bandwidth = bandwidth;
        this.tokenStore = tokenStore;
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
        this.bucketStarted = false;
        this.secondCount = 0;
    }


    /**
     * Checks the bandwidth and waits (at most 60 seconds) until the next call is allowed by the configured bandwidth.
     * The time spent in the token store counts toward the 60 seconds: once they are exceeded, the token store is not
     * called again and the call passes. The object monitor is not held while waiting. If the calling thread is interrupted, the wait stops immediately
     * and the interrupt flag of the thread remains set.
     *
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#bandwidthCheck()
     */
    @Override
    public void bandwidthCheck() {
        final long bandwidthCheckStart = System.currentTimeMillis();
        final long maxWaitTime = getMaxWaitTime();
        synchronized (this) {
            if (bandwidth <= 0) {
                return;
            }

            if (startTime == null) {
                startTime = bandwidthCheckStart;
            }
        }

        boolean waiting = true;
        while (waiting) {
            // the shared token store is called outside of the monitor
            Long waitTime = acquireFromTokenStore();
            boolean reserved = false;
            if (waitTime == null) {
                // local token bucket: the token is reserved immediately, the call passes after the wait time without a
                // further check, so the waiting calls pass in the order of their arrival
                synchronized (this) {
                    waitTime = calculateWaitTime();
                    tokens -= 1;
                }
                reserved = true;
            }

            // the time spent in the token store counts toward the maximum wait time
            final long remainingTime = maxWaitTime - (System.currentTimeMillis() - bandwidthCheckStart);
            if (waitTime <= 0 || remainingTime <= 0) {
                waiting = false;
            } else {
                // sleep outside of the monitor
                boolean interrupted = ThreadUtil.getInstance().sleep(Math.max(1L, Math.min(waitTime, remainingTime)));
                if (reserved || interrupted || (System.currentTimeMillis() - bandwidthCheckStart >= maxWaitTime)) {
                    waiting = false;
                }
            }
        }

        synchronized (this) {
            sleepStatisticCounter.add(System.currentTimeMillis() - bandwidthCheckStart);
            lastUpdateTime = System.currentTimeMillis();
            count++;
            countCallOfSecond();
        }
    }


    /**
     * Gets the maximum time a call of {@link #bandwidthCheck()} waits, including the time spent in the token store.
     *
     * @return the maximum wait time in milliseconds (60 seconds)
     */
    long getMaxWaitTime() {
        return MAX_WAIT_TIME;
    }


    /**
     * Calculate the time to wait until the next call is allowed. The token bucket is refilled with the configured
     * bandwidth, at most with the calls of one second. The caller must hold the object monitor.
     *
     * @return the time to wait in milliseconds, 0 if the next call is allowed immediately
     */
    protected long calculateWaitTime() {
        final long now = System.nanoTime();
        final double capacity = Math.max(1d, bandwidth);
        if (!bucketStarted) {
            bucketStarted = true;
            tokens = capacity;
            lastRefillNanos = now;
        } else {
            tokens = Math.min(capacity, tokens + ((now - lastRefillNanos) * (double) bandwidth) / ONE_SECOND_NANOS);
            lastRefillNanos = now;
        }

        if (tokens >= 1d || bandwidth <= 0) {
            return 0;
        }

        // round up, so that the token is available after the wait time
        return Math.max(1L, (long) Math.ceil(((1d - tokens) * 1000d) / bandwidth));
    }


    /**
     * Try to take a token of the shared token store. If the token store fails, a warning is logged once and the
     * local token bucket is used until the token store works again. After a failure the token store is not called
     * for 5 seconds, so that a token store which is down does not slow down every call.
     *
     * @return the time to wait in milliseconds (0 if a token was taken) or null if no token store is set or it failed
     */
    private Long acquireFromTokenStore() {
        final IBandwidthThrottlingTokenStore store;
        final long currentBandwidth;
        synchronized (this) {
            if (tokenStoreFailedTime != null && (System.currentTimeMillis() - tokenStoreFailedTime) < TOKEN_STORE_RETRY_PAUSE) {
                return null;
            }

            store = tokenStore;
            currentBandwidth = bandwidth;
        }

        if (store == null) {
            return null;
        }

        try {
            final long waitTime = store.acquire(currentBandwidth);
            synchronized (this) {
                if (tokenStoreFailedTime != null) {
                    tokenStoreFailedTime = null;
                    LOG.info("The token store works again, the shared bandwidth is used.");
                }
            }

            return waitTime;
        } catch (RuntimeException e) {
            final String message = "The token store failed, the local bandwidth of this instance is used: " + e.getMessage();
            synchronized (this) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message, e);
                }

                if (tokenStoreFailedTime == null) {
                    LOG.warn(message);
                }

                tokenStoreFailedTime = System.currentTimeMillis();
            }

            return null;
        }
    }


    /**
     * Count a call for the bandwidth statistic: the number of calls of each measured second is added to the
     * bandwidth statistic. The caller must hold the object monitor.
     */
    private void countCallOfSecond() {
        final long now = System.nanoTime();
        if (secondCount == 0) {
            secondStartNanos = now;
        } else if ((now - secondStartNanos) >= ONE_SECOND_NANOS) {
            bandwidthStatisticCounter.add(secondCount);
            secondStartNanos = now;
            secondCount = 0;
        }

        secondCount++;
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
     * Sets the shared token store, e.g. after a deserialization: the token store is not serialized.
     *
     * @param tokenStore the shared token store to control the bandwidth over multiple instances, null to use the local token bucket
     */
    public synchronized void setTokenStore(IBandwidthThrottlingTokenStore tokenStore) {
        this.tokenStore = tokenStore;
        this.tokenStoreFailedTime = null;
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
     * Gets the bandwidth statistic: the number of calls of each measured second. A completed second is added when
     * the statistic is read; the calls of the still running second are included in the returned statistic, in this
     * case a copy is returned which does not change with further calls.
     *
     * @see com.github.toolarium.common.bandwidth.IBandwidthThrottling#getBandwidthStatisticCounter()
     */
    @Override
    public synchronized StatisticCounter getBandwidthStatisticCounter() {
        return currentBandwidthStatisticCounter();
    }


    /**
     * Gets the bandwidth statistic including the current second: a completed second is added to the statistic,
     * the calls of a still running second are added to a copy of the statistic. The caller must hold the object monitor.
     *
     * @return the bandwidth statistic
     */
    private StatisticCounter currentBandwidthStatisticCounter() {
        if (secondCount <= 0) {
            return bandwidthStatisticCounter;
        }

        if ((System.nanoTime() - secondStartNanos) >= ONE_SECOND_NANOS) {
            bandwidthStatisticCounter.add(secondCount);
            secondCount = 0;
            return bandwidthStatisticCounter;
        }

        StatisticCounter result = bandwidthStatisticCounter.clone();
        result.add(secondCount);
        return result;
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
        StringBuilder builder = new StringBuilder();
        builder.append("Bandwidth throttling:").append(TextUtil.NL);

        String startTimeStr = "n/a";
        String lastUpdateTimeStr = "n/a";
        String usedTimeStr = "n/a";

        synchronized (this) {
            if (startTime != null) {
                startTimeStr = DATE_FORMATTER.format(Instant.ofEpochMilli(startTime));
            }
            if (lastUpdateTime != null) {
                lastUpdateTimeStr = DATE_FORMATTER.format(Instant.ofEpochMilli(lastUpdateTime));
            }
            if (startTime != null && lastUpdateTime != null) {
                usedTimeStr = TIME_DIFFERENCE_FORMATTER.formatAsString(lastUpdateTime - startTime);
            }
    
            builder.append("bandwidth  : " + bandwidth).append(TextUtil.NL);
            builder.append("counter    : " + count).append(TextUtil.NL);
            builder.append("start time : " + startTimeStr).append(TextUtil.NL);
            builder.append("last update: " + lastUpdateTimeStr).append(TextUtil.NL);
            builder.append("used time  : " + usedTimeStr).append(TextUtil.NL);
            builder.append(currentBandwidthStatisticCounter().toString("bandwidth statistic:")).append(TextUtil.NL);
            builder.append(sleepStatisticCounter.toString("sleep time statistic:")).append(".");
        }
        
        return builder.toString();
    }


    /**
     * Writes the state under the object monitor, so that a consistent snapshot is serialized. The bandwidth statistic
     * is written including the calls of the current second.
     *
     * @param out the object output stream
     * @throws IOException if an I/O error occurs
     */
    private synchronized void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("bandwidth", bandwidth);
        fields.put("count", count);
        fields.put("startTime", startTime);
        fields.put("lastUpdateTime", lastUpdateTime);
        fields.put("bandwidthStatisticCounter", currentBandwidthStatisticCounter());
        fields.put("sleepStatisticCounter", sleepStatisticCounter);
        out.writeFields();
    }
}
