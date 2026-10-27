/*
 * IBandwidthThrottlingTokenStore.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.bandwidth;


/**
 * Defines a shared token store, which allows to control the bandwidth over multiple instances (e.g. processes or
 * servers) of a {@link BandwidthThrottling}. The implementation keeps the token bucket in a shared place, e.g. in
 * Redis by a Lua script, and has to take a token atomically.
 *
 * @author patrick
 */
public interface IBandwidthThrottlingTokenStore {

    /**
     * Try to take one token of the shared token bucket. The bucket is refilled with the given bandwidth, at most
     * with the calls of one second.
     *
     * <p>The call can not be interrupted by the {@link BandwidthThrottling}: the implementation must enforce its own
     * timeout (e.g. a connect and read timeout of the connection to the shared store) and throw a runtime exception
     * if it is exceeded, so that the local token bucket is used. The time spent in this method counts toward the
     * maximum wait time of 60 seconds of {@link BandwidthThrottling#bandwidthCheck()}.</p>
     *
     * @param bandwidth the bandwidth in number of calls per second
     * @return 0 if a token was taken, otherwise the time in milliseconds to wait before the next try
     */
    long acquire(long bandwidth);
}
