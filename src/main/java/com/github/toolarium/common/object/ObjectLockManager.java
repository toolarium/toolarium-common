/*
 * ObjectLockManager.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.object;

import com.github.toolarium.common.statistic.StatisticCounter;
import com.github.toolarium.common.util.DateUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Defines the object lock manager
 * 
 * @author patrick
 */
public class ObjectLockManager implements IObjectLockManager, Serializable {
    private static final long serialVersionUID = -5129430604369864925L;
    private static final Logger LOG = LoggerFactory.getLogger(ObjectLockManager.class);
    private volatile Integer lockSize;
    private transient Map<Object, Long> lockMap;
    private Map<Object, Long> unlockMap;
    private StatisticCounter lockStatistic;
    private StatisticCounter unlockStatistic;
    private StatisticCounter ignoreLockStatistic;
    private volatile long lockSizeReachedCounter;
    private Long unlockTimeout;
    private volatile boolean isInitialized;
    private volatile boolean cleanupAfterUnlock;


    /**
     * Constructor
     */
    public ObjectLockManager() {
        this(null, null);
    }

    
    /**
     * Constructor
     * 
     * @param lockSize defines the lock size
     * @param unlockTimeout the timeout after unlock an object is still not be able to lock
     */
    public ObjectLockManager(Integer lockSize, Long unlockTimeout) {
        this.cleanupAfterUnlock = true;
        this.unlockTimeout = unlockTimeout;
        releaseResource();
        setObjectLockSize(lockSize);
    }


    /**
     * @see com.github.toolarium.common.object.IObjectLockManager#setObjectLockSize(Integer)
     */
    @Override
    public IObjectLockManager setObjectLockSize(Integer lockSize) throws IllegalArgumentException {
        if (lockSize != null && lockSize < 0) {
            throw new IllegalArgumentException("Invalid lock size!");
        }
        
        this.lockSize = lockSize;
        return this;
    }


    /**
     * @see com.github.toolarium.common.object.IObjectLockManager#setUnlockTimeout(java.lang.Long)
     */
    @Override
    public IObjectLockManager setUnlockTimeout(Long unlockTimeout) {
        if (unlockTimeout != null && unlockTimeout < 0) {
            throw new IllegalArgumentException("Invalid timeout!");
        }
        
        this.unlockTimeout = unlockTimeout;
        return this;
    }


    /**
     * @see com.github.toolarium.common.object.IObjectLockManager#lock(java.util.List)
     */
    @Override
    public synchronized <L extends Serializable> List<L> lock(List<L> theObjectLockList) {
        if (!isInitialized) {
            init();
        }

        List<L> resultList = new ArrayList<L>();

        long unlockStatisticCounter = 0;
        long ignoreStatisticCounter = 0;
        if (theObjectLockList != null && lockMap != null) {
            int numberOfLockedElements = 0;
            for (L lock : theObjectLockList) {
                
                Long unlockMapLockTimestamp = null;
                if (unlockTimeout != null) {
                    unlockMapLockTimestamp = unlockMap.get(lock); // check unlock cache
                    if (unlockMapLockTimestamp != null && unlockMapLockTimestamp <= System.currentTimeMillis()) {
                        unlockMapLockTimestamp = null;
                        unlockMap.remove(lock); // its too old
                    }
                }
                
                if (unlockMapLockTimestamp != null) { // hit found in unlock cache, ignore it
                    unlockStatisticCounter++;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found key in blocked unlock list: " + lock);
                    }
                } else if (lockMap.containsKey(lock)) { // lock found in lock map, ignore it
                    ignoreStatisticCounter++;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Key already locked: " + lock);
                    }
                } else {
                    long timestamp = System.currentTimeMillis();
                    lockMap.put(lock, timestamp);
                    resultList.add(lock);
                    numberOfLockedElements++;
                    
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Add lock (" + DateUtil.getInstance().toTimestampString(new Date(timestamp)) + "): " + lock);
                    }
                }

                if (lockSize != null && lockSize <= numberOfLockedElements) {
                    // max of locks reached
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Max lock size reached: " + numberOfLockedElements);
                    }
                    
                    lockSizeReachedCounter++;
                    break;
                }
            }
        }

        unlockStatistic.add(unlockStatisticCounter);
        ignoreLockStatistic.add(ignoreStatisticCounter);
        lockStatistic.add(resultList.size());
        return resultList;
    }


    /**
     * @see com.github.toolarium.common.object.IObjectLockManager#unlock(java.util.List)
     */
    @Override
    public synchronized <L extends Serializable> List<L> unlock(List<L> theObjectLockList) {
        if (!isInitialized) {
            init();
        }

        if (theObjectLockList == null || lockMap == null) {
            return theObjectLockList;
        }

        for (L lock : theObjectLockList) {
            Long timestamp = lockMap.remove(lock);
            
            if (unlockTimeout != null && timestamp != null) {
                unlockMap.put(lock, System.currentTimeMillis() + unlockTimeout); // set to unlock map cache

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Removed lock (" + DateUtil.getInstance().toTimestampString(new Date(timestamp)) + "): " + lock);
                }
                
                if (cleanupAfterUnlock) {
                    cleanup();
                }
            }
        }
        
        return theObjectLockList;
    }

    
    /**
     * Define if the cleanup of unlock timeout should be forced after an unlock 
     *
     * @param cleanupAfterUnlock true or false
     */
    public void cleanupAfterUnlock(boolean cleanupAfterUnlock) {
        this.cleanupAfterUnlock = cleanupAfterUnlock;
    }

    
    /**
     * Cleanup too old unlock elements 
     */
    public synchronized void cleanup() {
        long now = System.currentTimeMillis();
        for (Map.Entry<Object, Long> e : unlockMap.entrySet()) {
            if (now > e.getValue()) {
                unlockMap.remove(e.getKey());
            }
        }
    }


    /**
     * @see com.github.toolarium.common.object.IObjectLockManager#releaseResource()
     */
    @Override
    public synchronized void releaseResource() {
        this.lockMap = null;
        this.unlockMap = null;
        this.lockStatistic = null;
        this.unlockStatistic = null;
        this.ignoreLockStatistic = null;
        this.lockSizeReachedCounter = 0;
        this.isInitialized = false;        
    }


    /**
     * @see com.github.toolarium.common.object.IObjectLockManager#getLockStatistic()
     */
    @Override
    public StatisticCounter getLockStatistic() {
        return lockStatistic;
    }


    /**
     * @see com.github.toolarium.common.object.IObjectLockManager#getUnlockStatistic()
     */
    @Override
    public StatisticCounter getUnlockStatistic() {
        return unlockStatistic;
    }


    /**
     * @see com.github.toolarium.common.object.IObjectLockManager#getIgnoreLockStatistic()
     */
    @Override
    public StatisticCounter getIgnoreLockStatistic() {
        return ignoreLockStatistic;
    }


    /**
     * @see com.github.toolarium.common.object.IObjectLockManager#getNumberOfLockSizeReached()
     */
    @Override
    public long getNumberOfLockSizeReached() {
        return lockSizeReachedCounter;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        int lockMapSize = 0;
        if (lockMap != null) {
            lockMapSize = lockMap.size();
        }

        int unlockMapSize = 0;
        if (unlockMap != null) {
            unlockMapSize = unlockMap.keySet().size();
        }

        double lockAvg = 0d;
        if (lockStatistic != null) {
            lockAvg = lockStatistic.getAverage();
        }

        return "ObjectLockManager [isInitialized=" + isInitialized 
                + ", lockSize=" + lockSize + ", lockMap=" + lockMapSize + ", unlockMap=" + unlockMapSize
                + ", lockStatistic=" + lockAvg + ", ignoreLockStatistic=" + ignoreLockStatistic 
                + ", unlockStatistic=" + unlockStatistic 
                + ", lockSizeReachedCounter=" + lockSizeReachedCounter + "]";
    }


    /**
     * Initialize
     */
    protected void init() {
        if (isInitialized) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initialize...");
        }
        
        if (lockMap == null) {
            this.lockMap = new HashMap<Object, Long>();
        }
        
        this.unlockMap = new HashMap<Object, Long>();
        this.ignoreLockStatistic = new StatisticCounter();
        this.lockStatistic = new StatisticCounter();
        this.unlockStatistic = new StatisticCounter();
        this.lockSizeReachedCounter = 0;
        this.isInitialized = true;
    }
}
