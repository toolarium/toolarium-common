/*
 * ValueDifferenceImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.compare.map.impl;

import com.github.toolarium.common.compare.map.IValueDifference;
import java.util.Objects;


/**
 * Implements the {@link IValueDifference}.
 * 
 * @author patrick
 */
public class ValueDifference<V> implements IValueDifference<V> {
    private final V left;
    private final V right;

    /**
     * Constructor for ValueDifferenceImpl
     * 
     * @param left the left
     * @param right the right
     */
    public ValueDifference(V left, V right) {
        this.left = left;
        this.right = right;
    }


    /**
     * @see com.axonfintech.fp.data.comparator.IMapDifference.ValueDifference#leftValue()
     */
    @Override
    public V leftValue() {
        return left;
    }

    
    /**
     * @see com.axonfintech.fp.data.comparator.IMapDifference.ValueDifference#rightValue()
     */
    @Override
    public V rightValue() {
        return right;
    }

    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
            
        IValueDifference<?> other = (IValueDifference<?>) obj;
        return Objects.equals(left, other.leftValue()) && Objects.equals(right, other.rightValue());
    }

    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "(" + left + ", " + right + ")";
    }
}
