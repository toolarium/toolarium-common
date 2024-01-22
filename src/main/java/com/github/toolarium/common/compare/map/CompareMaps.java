/*
 * CompareMaps.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.compare.map;

import com.github.toolarium.common.compare.map.impl.MapDifference;
import com.github.toolarium.common.compare.map.impl.SortedMapDifference;
import com.github.toolarium.common.compare.map.impl.ValueDifference;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Implements the functionality to compare two maps.
 * 
 * @author patrick
 */
public final class CompareMaps {
    
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     */
    private static class HOLDER {
        static final CompareMaps INSTANCE = new CompareMaps();
    }

    
    /**
     * Constructor
     */
    private CompareMaps() {
        // NOP
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static CompareMaps getInstance() {
        return HOLDER.INSTANCE;
    }

    
    /**
     * Compare two maps: The difference is an immutable snapshot of the state of the maps at the time this method is called. It
     * will never change, even if the maps change at a later time.
     *
     * <p>Since this method uses {@code HashMap} instances internally, the keys of
     * the supplied maps must be well-behaved with respect to {@link Object#equals} and {@link Object#hashCode}.
     *
     * @param <K> the key
     * @param <V> the value
     * @param left the map to treat as the "left" map for purposes of comparison
     * @param right the map to treat as the "right" map for purposes of comparison
     * @return the difference between the two maps
     */
    @SuppressWarnings("unchecked")
    public <K, V> IMapDifference<K, V> compare(Map<? extends K, ? extends V> left, Map<? extends K, ? extends V> right) {
        if (left instanceof SortedMap) {
            final SortedMap<K, ? extends V> sortedLeft = (SortedMap<K, ? extends V>) left;
            Comparator<? super K> comparator = sortedLeft.comparator();
            final SortedMap<K, V> onlyOnLeft = new TreeMap<>(comparator);
            final SortedMap<K, V> onlyOnRight = new TreeMap<>(comparator);
            onlyOnRight.putAll(right); // will whittle it down
            final SortedMap<K, V> onBoth = new TreeMap<>(comparator);
            final SortedMap<K, IValueDifference<V>> differences = new TreeMap<>(comparator);
            compare(left, right, onlyOnLeft, onlyOnRight, onBoth, differences);
            return new SortedMapDifference<K, V>(onlyOnLeft, onlyOnRight, onBoth, differences);
        }

        final Map<K, V> onlyOnLeft = new LinkedHashMap<>();
        final Map<K, V> onlyOnRight; 
        if (right != null) {
            onlyOnRight = new LinkedHashMap<K, V>(right); 
            
        } else {
            onlyOnRight = new LinkedHashMap<K, V>(); 
        }
        
        final Map<K, V> onBoth = new LinkedHashMap<>();
        final Map<K, IValueDifference<V>> differences = new LinkedHashMap<>();
        compare(left, right, onlyOnLeft, onlyOnRight, onBoth, differences);
        return new MapDifference<K, V>(onlyOnLeft, onlyOnRight, onBoth, differences);
    }

    
    /**
     * Compare 
     *
     * @param <K> the key
     * @param <V> the value
     * @param left the left map
     * @param right the right map
     * @param onlyOnLeft only on left side
     * @param onlyOnRight only on right side
     * @param onBoth on both side
     * @param differences the differences
     */
    private <K, V> void compare(Map<? extends K, ? extends V> left, Map<? extends K, ? extends V> right, 
                                Map<K, V> onlyOnLeft, Map<K, V> onlyOnRight, Map<K, V> onBoth, Map<K, IValueDifference<V>> differences) {
        if (left == null) {
            return;
        }
        
        for (Entry<? extends K, ? extends V> entry : left.entrySet()) {
            K leftKey = entry.getKey();
            V leftValue = entry.getValue();
            
            if (right != null && right.containsKey(leftKey)) {
                V rightValue = onlyOnRight.remove(leftKey);

                if ((leftValue == null && rightValue == null) || (leftValue != null && leftValue.equals(rightValue))) {
                    onBoth.put(leftKey, leftValue);
                } else {
                    differences.put(leftKey, new ValueDifference<V>(leftValue, rightValue));
                } 
            } else {
                onlyOnLeft.put(leftKey, leftValue);
            }
        }
    }
}
