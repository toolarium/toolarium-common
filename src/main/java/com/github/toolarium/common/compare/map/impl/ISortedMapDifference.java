/*
 * ISortedMapDifference.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.compare.map.impl;

import com.github.toolarium.common.compare.map.IMapDifference;
import com.github.toolarium.common.compare.map.IValueDifference;
import java.util.SortedMap;


/**
 * An object representing the differences between two sorted maps.
 *
 * @author patrick
 */
public interface ISortedMapDifference<K, V> extends IMapDifference<K, V> {

    /**
     * @see com.github.toolarium.common.compare.map.IMapDifference#entriesOnlyOnLeft()
     */
    @Override
    SortedMap<K, V> entriesOnlyOnLeft();

    
    /**
     * @see com.github.toolarium.common.compare.map.IMapDifference#entriesOnlyOnRight()
     */
    @Override
    SortedMap<K, V> entriesOnlyOnRight();

    
    /**
     * @see com.github.toolarium.common.compare.map.IMapDifference#entriesInCommon()
     */
    @Override
    SortedMap<K, V> entriesInCommon();

    
    /**
     * @see com.github.toolarium.common.compare.map.IMapDifference#entriesDiffering()
     */
    @Override
    SortedMap<K, IValueDifference<V>> entriesDiffering();
}
