/*
 * CompareMaps.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.compare.map;

import com.github.toolarium.common.compare.map.impl.MapDifference;
import com.github.toolarium.common.compare.map.impl.SortedMapDifference;
import com.github.toolarium.common.compare.map.impl.ValueDifference;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * Implements the functionality to compare two maps.
 * 
 * @author patrick
 */
public final class CompareMaps {
    
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     */
    private static final class HOLDER {
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
     * A key which one map can not hold (e.g. a {@code null} key and a {@code TreeMap} with natural ordering) is
     * treated as not present in that map. Whether a key of the left map is present in the right map is always decided
     * by the lookup of the right map (e.g. a case-insensitive {@code TreeMap}), a key of the right map is only on the
     * right side if no key of the left map matched it. In case the left map is a {@code SortedMap} the result maps
     * are sorted by its comparator, {@code null} keys are sorted first. In case one of the maps is an
     * {@code IdentityHashMap} (and the left map is not sorted) the result maps compare the keys by identity.
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
            Comparator<? super K> comparator = nullSafeComparator(sortedLeft.comparator());
            final SortedMap<K, V> onlyOnLeft = new TreeMap<>(comparator);
            final SortedMap<K, V> onlyOnRight = new TreeMap<>(comparator);
            final SortedMap<K, V> onBoth = new TreeMap<>(comparator);
            final SortedMap<K, IValueDifference<V>> differences = new TreeMap<>(comparator);
            compare(left, right, onlyOnLeft, onlyOnRight, onBoth, differences);
            return new SortedMapDifference<K, V>(onlyOnLeft, onlyOnRight, onBoth, differences);
        }

        if (left instanceof IdentityHashMap || right instanceof IdentityHashMap) {
            // keys which are equal but not identical must not collapse in the result
            final Map<K, V> onlyOnLeft = new IdentityHashMap<>();
            final Map<K, V> onlyOnRight = new IdentityHashMap<>();
            final Map<K, V> onBoth = new IdentityHashMap<>();
            final Map<K, IValueDifference<V>> differences = new IdentityHashMap<>();
            compare(left, right, onlyOnLeft, onlyOnRight, onBoth, differences);
            return new MapDifference<K, V>(onlyOnLeft, onlyOnRight, onBoth, differences);
        }

        final Map<K, V> onlyOnLeft = new LinkedHashMap<>();
        final Map<K, V> onlyOnRight = new LinkedHashMap<>();
        final Map<K, V> onBoth = new LinkedHashMap<>();
        final Map<K, IValueDifference<V>> differences = new LinkedHashMap<>();
        compare(left, right, onlyOnLeft, onlyOnRight, onBoth, differences);
        return new MapDifference<K, V>(onlyOnLeft, onlyOnRight, onBoth, differences);
    }

    
    /**
     * Compare: the presence of a left key is checked with the lookup of the right map and the matched right keys are
     * tracked with the same semantics, so a key pair is either matched or reported on its own side, never both.
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
        if (right == null) {
            if (left != null) {
                onlyOnLeft.putAll(left);
            }
            return;
        }

        final Set<Object> matchedRightKeys = newKeySet(right);
        if (left != null) {
            for (Entry<? extends K, ? extends V> entry : left.entrySet()) {
                K leftKey = entry.getKey();
                V leftValue = entry.getValue();
                
                if (containsKey(right, leftKey)) {
                    matchedRightKeys.add(leftKey);
                    V rightValue = right.get(leftKey);
    
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

        for (Entry<? extends K, ? extends V> entry : right.entrySet()) {
            if (!matchedRightKeys.contains(entry.getKey())) {
                onlyOnRight.put(entry.getKey(), entry.getValue());
            }
        }
    }


    /**
     * Create an empty key set which uses the same key semantics as the given map, e.g. the comparator of a
     * {@link SortedMap} or the identity of an {@link IdentityHashMap}.
     *
     * @param map the map
     * @return the new empty key set
     */
    @SuppressWarnings("unchecked")
    private static Set<Object> newKeySet(Map<?, ?> map) {
        if (map instanceof SortedMap) {
            return new TreeSet<Object>((Comparator<Object>) ((SortedMap<?, ?>) map).comparator());
        }

        if (map instanceof IdentityHashMap) {
            return Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
        }

        return new HashSet<Object>();
    }


    /**
     * Check if the given map contains the key. In case the map can not hold the key (e.g. a {@code null} key in a
     * {@code TreeMap} with natural ordering or a key of an incompatible type), the key is treated as not present.
     *
     * @param map the map
     * @param key the key
     * @return true if the map contains the key
     */
    private boolean containsKey(Map<?, ?> map, Object key) {
        try {
            return map.containsKey(key);
        } catch (NullPointerException | ClassCastException e) {
            // the map can not hold this key, so it is not present
            return false;
        }
    }


    /**
     * Get a comparator for the sorted result maps. A comparator is returned which sorts {@code null} keys first and all
     * other keys by the given comparator (or by natural ordering in case of a {@code null} comparator), so that
     * {@code null} keys of the other map can be held.
     *
     * @param <K> the key
     * @param comparator the comparator of the sorted map or null for natural ordering
     * @return the comparator to use
     */
    @SuppressWarnings("unchecked")
    private static <K> Comparator<? super K> nullSafeComparator(Comparator<? super K> comparator) {
        if (comparator != null) {
            return Comparator.nullsFirst(comparator);
        }

        Comparator<Object> naturalOrder = (a, b) -> ((Comparable<Object>) a).compareTo(b);
        return Comparator.nullsFirst(naturalOrder);
    }
}
