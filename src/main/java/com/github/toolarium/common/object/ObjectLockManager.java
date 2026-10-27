/*
 * ObjectLockManager.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.object;

import com.github.toolarium.common.statistic.StatisticCounter;
import com.github.toolarium.common.util.DateUtil;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Defines the object lock manager
 * 
 * @author patrick
 */
public class ObjectLockManager implements IObjectLockManager, Serializable {
    private static final long serialVersionUID = -5129430604369864926L;
    private static final Logger LOG = LoggerFactory.getLogger(ObjectLockManager.class);
    private static final long CLEANUP_AFTER_UNLOCK_INTERVAL = 1000L;
    private volatile Integer lockSize;
    private transient Map<Object, Long> lockMap;
    private transient Map<Object, Long> unlockMap;
    private final StatisticCounter lockStatistic;
    private final StatisticCounter unlockStatistic;
    private final StatisticCounter ignoreLockStatistic;
    private volatile long lockSizeReachedCounter;
    private volatile Long unlockTimeout;
    private transient volatile boolean isInitialized;
    private volatile boolean cleanupAfterUnlock;
    private transient long lastCleanupAfterUnlock;
    private transient ReadWriteLock rwLock = new ReentrantReadWriteLock();


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
        this.lockSize = null;
        this.lockMap = null;
        this.unlockMap = null;
        this.lockStatistic = new StatisticCounter();
        this.unlockStatistic = new StatisticCounter();
        this.ignoreLockStatistic = new StatisticCounter();
        this.lockSizeReachedCounter = 0;
        this.unlockTimeout = unlockTimeout;
        this.isInitialized = false;
        this.cleanupAfterUnlock = true;
        
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
     * Sets the unlock timeout. In case the timeout is set to null, the blocked unlocked elements are released.
     *
     * @see com.github.toolarium.common.object.IObjectLockManager#setUnlockTimeout(java.lang.Long)
     */
    @Override
    public IObjectLockManager setUnlockTimeout(Long unlockTimeout) {
        if (unlockTimeout != null && unlockTimeout < 0) {
            throw new IllegalArgumentException("Invalid timeout!");
        }

        rwLock.writeLock().lock();
        try {
            this.unlockTimeout = unlockTimeout;
            if (unlockTimeout == null && unlockMap != null) {
                // without unlock timeout the entries are never checked or removed again
                unlockMap.clear();
            }
        } finally {
            rwLock.writeLock().unlock();
        }

        return this;
    }


    /**
     * @see com.github.toolarium.common.object.IObjectLockManager#lock(java.util.List)
     */
    @Override
    public <L extends Serializable> List<L> lock(List<L> theObjectLockList) {
        rwLock.writeLock().lock();
        try {
            if (!isInitialized) {
                init();
            }

            List<L> resultList = new ArrayList<L>();

            long unlockStatisticCounter = 0;
            long ignoreStatisticCounter = 0;
            final Integer currentLockSize = lockSize;
            if (currentLockSize != null && currentLockSize == 0) {
                // lock size 0: nothing can be locked
                if (theObjectLockList != null && !theObjectLockList.isEmpty()) {
                    lockSizeReachedCounter++;
                }
            } else if (theObjectLockList != null && lockMap != null) {
                int numberOfLockedElements = 0;
                Iterator<L> lockIterator = theObjectLockList.iterator();
                while (lockIterator.hasNext()) {
                    L lock = lockIterator.next();
                    if (lock == null) {
                        continue; // null elements can not be locked, they are ignored
                    }

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

                    if (currentLockSize != null && currentLockSize <= numberOfLockedElements) {
                        // max of locks reached: count it only if elements are left unlocked because of the limit
                        if (lockIterator.hasNext()) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Max lock size reached: " + numberOfLockedElements);
                            }

                            lockSizeReachedCounter++;
                        }
                        break;
                    }
                }
            }

            unlockStatistic.add(unlockStatisticCounter);
            ignoreLockStatistic.add(ignoreStatisticCounter);
            lockStatistic.add(resultList.size());
            return resultList;
        } finally {
            rwLock.writeLock().unlock();
        }
    }


    /**
     * @see com.github.toolarium.common.object.IObjectLockManager#unlock(java.util.List)
     */
    @Override
    public <L extends Serializable> List<L> unlock(List<L> theObjectLockList) {
        rwLock.writeLock().lock();
        try {
            if (!isInitialized) {
                init();
            }

            if (theObjectLockList == null || lockMap == null) {
                return theObjectLockList;
            }

            final Long currentUnlockTimeout = unlockTimeout; // read once, it may be changed concurrently
            boolean addedToUnlockMap = false;
            for (L lock : theObjectLockList) {
                if (lock == null) {
                    continue; // null elements are never locked, they are ignored
                }

                Long timestamp = lockMap.remove(lock);

                if (currentUnlockTimeout != null && timestamp != null) {
                    unlockMap.put(lock, getUnlockExpiration(currentUnlockTimeout)); // set to unlock map cache
                    addedToUnlockMap = true;

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Removed lock (" + DateUtil.getInstance().toTimestampString(new Date(timestamp)) + "): " + lock);
                    }
                }
            }

            // cleanup at most once per second: lock checks the unlock timeout itself, the cleanup only frees memory
            final long now = System.currentTimeMillis();
            if (addedToUnlockMap && cleanupAfterUnlock && (now - lastCleanupAfterUnlock) >= CLEANUP_AFTER_UNLOCK_INTERVAL) {
                lastCleanupAfterUnlock = now;
                cleanup();
            }

            return theObjectLockList;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    
    /**
     * Define if the cleanup of unlock timeout should be forced after an unlock. It runs at most once per second.
     *
     * @param cleanupAfterUnlock true or false
     */
    public void cleanupAfterUnlock(boolean cleanupAfterUnlock) {
        this.cleanupAfterUnlock = cleanupAfterUnlock;
    }

    
    /**
     * Cleanup too old unlock elements 
     */
    public void cleanup() {
        // the read lock prevents a concurrent releaseResource; the map itself is a ConcurrentHashMap and
        // supports concurrent removal (the write lock holder in unlock may also acquire the read lock)
        rwLock.readLock().lock();
        try {
            final Map<Object, Long> currentUnlockMap = unlockMap;
            if (currentUnlockMap == null) {
                return;
            }

            long now = System.currentTimeMillis();
            Iterator<Map.Entry<Object, Long>> it = currentUnlockMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Object, Long> e = it.next();
                if (now > e.getValue()) {
                    it.remove();
                }
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }


    /**
     * @see com.github.toolarium.common.object.IObjectLockManager#releaseResource()
     */
    @Override
    public void releaseResource() {
        rwLock.writeLock().lock();
        try {
            this.lockMap = null;
            this.unlockMap = null;
            this.isInitialized = false;
        } finally {
            rwLock.writeLock().unlock();
        }
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
        int unlockMapSize = 0;
        boolean initialized;
        rwLock.readLock().lock();
        try {
            final Map<Object, Long> currentLockMap = lockMap;
            if (currentLockMap != null) {
                lockMapSize = currentLockMap.size();
            }

            final Map<Object, Long> currentUnlockMap = unlockMap;
            if (currentUnlockMap != null) {
                unlockMapSize = currentUnlockMap.size();
            }
            initialized = isInitialized;
        } finally {
            rwLock.readLock().unlock();
        }

        double lockAvg = lockStatistic.getAverage();
        return "ObjectLockManager [isInitialized=" + initialized 
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
            this.lockMap = new ConcurrentHashMap<Object, Long>();
        }
        
        if (unlockMap == null) {
            this.unlockMap = new ConcurrentHashMap<Object, Long>();
        }
        
        this.isInitialized = true;
    }


    /**
     * Calculates the expiration timestamp of an unlocked element. The result saturates at {@link Long#MAX_VALUE}
     * instead of overflowing in case of a very large timeout.
     *
     * @param unlockTimeout the unlock timeout in milliseconds, not negative
     * @return the expiration timestamp
     */
    private static long getUnlockExpiration(long unlockTimeout) {
        long now = System.currentTimeMillis();
        if (unlockTimeout > Long.MAX_VALUE - now) {
            return Long.MAX_VALUE;
        }
        return now + unlockTimeout;
    }


    /**
     * Writes the state under the lock, so that a consistent snapshot is serialized.
     *
     * @param out the object output stream
     * @throws IOException if an I/O error occurs
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        rwLock.readLock().lock();
        try {
            out.defaultWriteObject();
        } finally {
            rwLock.readLock().unlock();
        }
    }


    /**
     * Restores the transient state after deserialization: the lock is re-created and the (transient) lock maps
     * are initialized again on the next lock or unlock call.
     *
     * @param in the object input stream
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the class cannot be found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        rwLock = new ReentrantReadWriteLock();
        isInitialized = false;
        lockMap = null;
        unlockMap = null;
    }
}
