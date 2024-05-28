/*
 * IStatisticCounter.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.statistic;


import java.io.Serializable;


/**
 * Defines the statistic counter API
 * 
 * @author patrick
 * @param <T> the type
 */
public interface IStatisticCounter<T> extends Cloneable, Serializable {
    
    /**
     * Adds a statistic counter
     *
     * @param statisticCounter the data to add
     */
    void add(T statisticCounter);    
    
    
    /**
     * Clones the object
     * 
     * @return the cloned object
     */
    T clone();
}
