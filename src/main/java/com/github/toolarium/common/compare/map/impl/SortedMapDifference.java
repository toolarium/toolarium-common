/*
 * SortedMapDifference.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.compare.map.impl;


import com.github.toolarium.common.compare.map.IValueDifference;
import java.util.SortedMap;


/**
 * Implements he {@link ISortedMapDifference}.
 * 
 * @author patrick
 */
public class SortedMapDifference<K, V> extends MapDifference<K, V> implements ISortedMapDifference<K, V> {
    
    /**
_    * Constructor for SortedMapDifferenceImpl
     *
     * @param onlyOnLeft only on left side
     * @param onlyOnRight only on right side
     * @param onBoth on both side
     * @param differences the differences
     */
    public SortedMapDifference(SortedMap<K, V> onlyOnLeft, SortedMap<K, V> onlyOnRight, SortedMap<K, V> onBoth, SortedMap<K, IValueDifference<V>> differences) {
        super(onlyOnLeft, onlyOnRight, onBoth, differences);
    }

    
    @Override
    public SortedMap<K, IValueDifference<V>> entriesDiffering() {
        return (SortedMap<K, IValueDifference<V>>) super.entriesDiffering();
    }

    
    @Override
    public SortedMap<K, V> entriesInCommon() {
        return (SortedMap<K, V>) super.entriesInCommon();
    }

    
    @Override
    public SortedMap<K, V> entriesOnlyOnLeft() {
        return (SortedMap<K, V>) super.entriesOnlyOnLeft();
    }

    
    @Override
    public SortedMap<K, V> entriesOnlyOnRight() {
        return (SortedMap<K, V>) super.entriesOnlyOnRight();
    }
}
