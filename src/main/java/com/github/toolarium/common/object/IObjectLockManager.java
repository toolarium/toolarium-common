/*
 * IObjectLockManager.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.object;

import com.github.toolarium.common.statistic.StatisticCounter;
import java.io.Serializable;
import java.util.List;


/**
 * Defines the object lock manager
 *  
 * @author patrick
 */
public interface IObjectLockManager {
    
    /**
     * Sets the size of the object lock 
     *
     * @param size the object lock size (positive number) or null
     * @return the instance
     */
    IObjectLockManager setObjectLockSize(Integer size);

    
    /**
     * Sets the unlock timeout which defines how long an element can't be locked again after an unlock 
     *
     * @param timeout the unlock timeout (positive number) or null
     * @return the instance
     */
    IObjectLockManager setUnlockTimeout(Long timeout);

    
    /**
     * Locks a list of objects inside the same JVM.
     *
     * @param <L> the object lock type
     * @param theObjectLockList the list of objects to lock
     * @return Returns only the successfully locked objects. It returns in all cases a list but it doesn't throws any exceptions!
     */
    <L extends Serializable> List<L> lock(List<L> theObjectLockList);

    
    /**
     * Unlock a  list of objects inside the same JVM.
     * 
     * @param <L> the object lock type
     * @param theObjectLockList the list of objects to unlock
     * @return the list of objects to unlock
     */
    <L extends Serializable> List<L> unlock(List<L> theObjectLockList);


    /**
     * Release all object locks and resources
     */
    void releaseResource();

    
    /**
     * Get the object lock statistic (the number of lock elements)
     *
     * @return the object lock statistic
     */
    StatisticCounter getLockStatistic();

    
    /**
     * Get the statistics of locks that could not be obtained because of the unlock timeout.
     *
     * @return the object unlock statistic
     */
    StatisticCounter getUnlockStatistic();

    
    /**
     * Get the statistics of locks that could not be obtained because they are already locked.
     *
     * @return the ignore object lock statistic
     */
    StatisticCounter getIgnoreLockStatistic();

    
    /**
     * Get the count of how many times the max lock size was reached
     *
     * @return the count of how many times the max lock size was reached
     */
    long getNumberOfLockSizeReached();    
}
