/*
 * IValueDifference.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.compare.map;


/**
 * A difference between the mappings from two maps with the same key. The {@link #leftValue} and {@link #rightValue}
 * are not equal, and one but not both of them may be null.
 * 
 * @author patrick
 */
public interface IValueDifference<V> {
    
    /**
     * Returns the value from the left map (possibly null).
     * 
     * @return the value from the left map
     */
    V leftValue();

    
    /**
     * Returns the value from the right map (possibly null).
     * 
     * @return the value from the right map
     */
    V rightValue();
}
