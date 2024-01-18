/*
 * IMapDifference.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.compare.map;

import java.util.Map;


/**
 * An object representing the differences between two maps.
 *
 * @author patrick
 */
public interface IMapDifference<K, V> {
    
    /**
     * Returns {@code true} if there are no differences between the two maps; that is, if the maps are equal.
     * 
     * @return true if they are equal
     */
    boolean areEqual();
  
  
    /**
     * Returns a map containing the entries from the left map whose keys are not present in the right map.
     * 
     * @return map containing the entries from the left map whose keys are not present in the right map
     */
    Map<K, V> entriesOnlyOnLeft();

    
    /**
     * Returns a map containing the entries from the right map whose keys are not present in the left map.
     * 
     * @return map containing the entries from the right map whose keys are not present in the left map
     */
    Map<K, V> entriesOnlyOnRight();

    
    /**
     * Returns a map containing the entries that appear in both maps; that is, the intersection of the two maps.
     * 
     * @return map containing the entries that appear in both maps; that is, the intersection of the two maps
     */
    Map<K, V> entriesInCommon();

    
    /**
     * Returns a map describing keys that appear in both maps, but with different values.
     * 
     * @return map describing keys that appear in both maps, but with different values.
     */
    Map<K, IValueDifference<V>> entriesDiffering();
}
