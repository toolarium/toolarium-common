/*
 * MapDifference.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.compare.map.impl;

import com.github.toolarium.common.compare.map.IMapDifference;
import com.github.toolarium.common.compare.map.IValueDifference;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;


/**
 * Implements the {@link IMapDifference}.
 * 
 * @author patrick
 */
public class MapDifference<K, V> implements IMapDifference<K, V> {
    private final Map<K, V> onlyOnLeft;
    private final Map<K, V> onlyOnRight;
    private final Map<K, V> onBoth;
    private final Map<K, IValueDifference<V>> differences;

    
    /**
     * Constructor for MapDifference
     * 
     * @param onlyOnLeft only on left side
     * @param onlyOnRight only on right side
     * @param onBoth on both side
     * @param differences the differences
     */
    public MapDifference(Map<K, V> onlyOnLeft, Map<K, V> onlyOnRight, Map<K, V> onBoth, Map<K, IValueDifference<V>> differences) {
        this.onlyOnLeft = unmodifiableMap(onlyOnLeft);
        this.onlyOnRight = unmodifiableMap(onlyOnRight);
        this.onBoth = unmodifiableMap(onBoth);
        this.differences = unmodifiableMap(differences);
    }
      
    
    /**
     * @see com.github.toolarium.common.compare.map.IMapDifference#areEqual()
     */
    @Override
    public boolean areEqual() {
        return onlyOnLeft.isEmpty() && onlyOnRight.isEmpty() && differences.isEmpty();
    }

    
    /**
     * @see com.github.toolarium.common.compare.map.IMapDifference#entriesOnlyOnLeft()
     */
    @Override
    public Map<K, V> entriesOnlyOnLeft() {
        return onlyOnLeft;
    }

    
    /**
     * @see com.github.toolarium.common.compare.map.IMapDifference#entriesOnlyOnRight()
     */
    @Override
    public Map<K, V> entriesOnlyOnRight() {
        return onlyOnRight;
    }

    
    /**
     * @see com.github.toolarium.common.compare.map.IMapDifference#entriesInCommon()
     */
    @Override
    public Map<K, V> entriesInCommon() {
        return onBoth;
    }

    /**
     * @see com.github.toolarium.common.compare.map.IMapDifference#entriesDiffering()
     */
    @Override
    public Map<K, IValueDifference<V>> entriesDiffering() {
        return differences;
    }

    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
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
        
        IMapDifference<K, V> other = (IMapDifference<K, V>) obj;
        return java.util.Objects.equals(entriesDiffering(), other.entriesDiffering())
                && java.util.Objects.equals(entriesInCommon(), other.entriesInCommon())
                && java.util.Objects.equals(entriesOnlyOnLeft(), other.entriesOnlyOnLeft())
                && java.util.Objects.equals(entriesOnlyOnRight(), other.entriesOnlyOnRight());
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(differences, onBoth, onlyOnLeft, onlyOnRight);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (areEqual()) {
            return "equal";
        }

        StringBuilder result = new StringBuilder("not equal");
        if (!onlyOnLeft.isEmpty()) {
            result.append(": only on left=").append(onlyOnLeft);
        }
        
        if (!onlyOnRight.isEmpty()) {
            result.append(": only on right=").append(onlyOnRight);
        }
        
        if (!differences.isEmpty()) {
            result.append(": value differences=").append(differences);
        }
        
        return result.toString();
    }


    /**
     * Unmodifieble map
     *
     * @param <K> the key
     * @param <V> the value
     * @param map the map
     * @return the map
     */
    private static <K, V> Map<K, V> unmodifiableMap(Map<K, ? extends V> map) {
        if (map instanceof SortedMap) {
            return Collections.unmodifiableSortedMap((SortedMap<K, ? extends V>) map);
        } else {
            return Collections.unmodifiableMap(map);
        }
    }
}
