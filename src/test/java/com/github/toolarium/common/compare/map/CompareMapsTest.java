/*
 * CompareMapsTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.compare.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.toolarium.common.compare.map.impl.ValueDifference;
import com.github.toolarium.common.util.MapUtil;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;


/**
 * Test compare maps 
 * 
 * @author patrick
 */
public class CompareMapsTest {
    private static final String KEY_A = "a";
    private static final String KEY_UPPER_A = "A";
    private static final String KEY_B = "b";

    /**
     * Test null and empty
     */
    @Test
    public void nullAndEmpty() {
        Map<String, Integer> lm = new LinkedHashMap<String, Integer>();
        Map<String, Integer> rm = new LinkedHashMap<String, Integer>();
        IMapDifference<String, Integer> d1 = CompareMaps.getInstance().compare(null, null);
        assertEquals(true, d1.areEqual());

        IMapDifference<String, Integer> d2 = CompareMaps.getInstance().compare(lm, null);
        assertEquals(true, d2.areEqual());

        IMapDifference<String, Integer> d3 = CompareMaps.getInstance().compare(null, rm);
        assertEquals(true, d3.areEqual());
        
        IMapDifference<String, Integer> d4 = CompareMaps.getInstance().compare(lm, rm);
        assertEquals(true, d4.areEqual());
    }


    /**
     * Test compare {@link Map}.
     */
    @Test
    public void compare() {
        compareMap(false);
    }

    
    /**
     * Test compare {@link SortedMap}.
     */
    @Test
    public void compareSorted() {
        compareMap(true);
    }
    

    /**
     * Compare map test case
     *
     * @param sorted true for {@link SortedMap}.
     */
    private void compareMap(boolean sorted) {
        String differingKey = "c39";
        Integer differingValue = 1000;
        Map<String, Integer> lo = newMap("l", 10, 9, sorted);
        Map<String, Integer> ro = newMap("r", 20, 9, sorted);
        Map<String, Integer> cm = newMap("c", 30, 9, sorted);
        Map<String, Integer> differing = new LinkedHashMap<String, Integer>();
        differing.put(differingKey, differingValue);
        Map<String, Integer> lm = MapUtil.getInstance().add(MapUtil.getInstance().add(new LinkedHashMap<String, Integer>(lo), cm), differing); 
        Map<String, Integer> rm = MapUtil.getInstance().add(new LinkedHashMap<String, Integer>(ro), cm); 
        
        IMapDifference<String, Integer> d = CompareMaps.getInstance().compare(lm, rm);
        assertEquals(false, d.areEqual());
        assertEquals(lo, d.entriesOnlyOnLeft());
        assertEquals(ro, d.entriesOnlyOnRight());
        Map<String, Integer> inCommon = new LinkedHashMap<String, Integer>(cm);
        inCommon.remove(differingKey);
        assertEquals(inCommon, d.entriesInCommon());
        
        Map<String, IValueDifference<Integer>> differingMap = MapUtil.getInstance().newMap(); 
        differingMap.put(differingKey, new ValueDifference<Integer>(differingValue, 39));
        assertEquals(differingMap, d.entriesDiffering());
    }

    
    /**
     * Create a new map
     *
     * @param keyPrefix the key refix
     * @param startNumber the start number
     * @param numberOfElements the number of elements
     * @param sorted true to support {@link SortedMap}.
     * @return the new map
     */
    private Map<String, Integer> newMap(String keyPrefix, int startNumber, int numberOfElements, boolean sorted) {
        final Map<String, Integer> result = MapUtil.getInstance().newMap(sorted);
        
        for (int i = (startNumber + numberOfElements); i >= startNumber; i--) {
            result.put(keyPrefix + i, i);
        }

        return result;
    }


    /**
     * A sorted left map compared with a null right map
     */
    @Test
    public void sortedLeftAndNullRight() {
        SortedMap<String, Integer> left = new TreeMap<String, Integer>();
        left.put("a", 1);
        IMapDifference<String, Integer> d = CompareMaps.getInstance().compare(left, null);
        assertEquals(false, d.areEqual());
        assertEquals(left, d.entriesOnlyOnLeft());
        assertEquals(0, d.entriesOnlyOnRight().size());
    }


    /**
     * Null keys are compared with a sorted map in natural order without exception
     */
    @Test
    public void nullKeyWithSortedMap() {
        SortedMap<String, Integer> sorted = new TreeMap<String, Integer>();
        sorted.put(KEY_A, 1);
        Map<String, Integer> unsorted = new LinkedHashMap<String, Integer>();
        unsorted.put(null, 2);
        unsorted.put(KEY_A, 1);

        // unsorted left, sorted right: the null key is only on the left side
        IMapDifference<String, Integer> d1 = CompareMaps.getInstance().compare(unsorted, sorted);
        assertEquals(false, d1.areEqual());
        assertEquals(1, d1.entriesOnlyOnLeft().size());
        assertEquals(Integer.valueOf(2), d1.entriesOnlyOnLeft().get(null));
        assertEquals(0, d1.entriesOnlyOnRight().size());
        assertEquals(1, d1.entriesInCommon().size());

        // sorted left, unsorted right: the null key is only on the right side
        IMapDifference<String, Integer> d2 = CompareMaps.getInstance().compare(sorted, unsorted);
        assertEquals(false, d2.areEqual());
        assertEquals(0, d2.entriesOnlyOnLeft().size());
        assertEquals(1, d2.entriesOnlyOnRight().size());
        assertEquals(Integer.valueOf(2), d2.entriesOnlyOnRight().get(null));
        assertEquals(1, d2.entriesInCommon().size());
    }


    /**
     * A key which the right map (case-insensitive) considers present is compared once and not reported as only on the right side
     */
    @Test
    public void caseInsensitiveRightMap() {
        SortedMap<String, Integer> right = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        right.put(KEY_A, 1);
        right.put(KEY_B, 2);
        Map<String, Integer> left = new LinkedHashMap<String, Integer>();
        left.put(KEY_UPPER_A, 1);

        IMapDifference<String, Integer> d = CompareMaps.getInstance().compare(left, right);
        assertEquals(0, d.entriesOnlyOnLeft().size());
        assertEquals(1, d.entriesInCommon().size());
        assertEquals(Integer.valueOf(1), d.entriesInCommon().get(KEY_UPPER_A));
        assertEquals(0, d.entriesDiffering().size());
        assertEquals(1, d.entriesOnlyOnRight().size());
        assertEquals(Integer.valueOf(2), d.entriesOnlyOnRight().get(KEY_B));

        // the same maps without the additional key are equal
        right.remove(KEY_B);
        assertEquals(true, CompareMaps.getInstance().compare(left, right).areEqual());

        // a differing value is reported once as difference
        left.put(KEY_UPPER_A, 3);
        d = CompareMaps.getInstance().compare(left, right);
        assertEquals(1, d.entriesDiffering().size());
        assertEquals(new ValueDifference<Integer>(3, 1), d.entriesDiffering().get(KEY_UPPER_A));
        assertEquals(0, d.entriesOnlyOnRight().size());

        // case-insensitive left map, case-sensitive right map: the right map decides, the keys are different
        SortedMap<String, Integer> sortedLeft = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        sortedLeft.put(KEY_UPPER_A, 1);
        Map<String, Integer> unsortedRight = new LinkedHashMap<String, Integer>();
        unsortedRight.put(KEY_A, 1);
        d = CompareMaps.getInstance().compare(sortedLeft, unsortedRight);
        assertEquals(1, d.entriesOnlyOnLeft().size());
        assertEquals(1, d.entriesOnlyOnRight().size());
        assertEquals(0, d.entriesDiffering().size());
    }


    /**
     * A null key of the right map is compared with a sorted left map with a custom comparator without exception
     */
    @Test
    public void nullKeyWithCustomComparator() {
        SortedMap<String, Integer> sorted = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        sorted.put(KEY_A, 1);
        Map<String, Integer> unsorted = new LinkedHashMap<String, Integer>();
        unsorted.put(null, 2);
        unsorted.put(KEY_A, 1);

        IMapDifference<String, Integer> d = CompareMaps.getInstance().compare(sorted, unsorted);
        assertEquals(1, d.entriesOnlyOnRight().size());
        assertEquals(Integer.valueOf(2), d.entriesOnlyOnRight().get(null));
        assertEquals(1, d.entriesInCommon().size());
        assertEquals(Integer.valueOf(1), d.entriesInCommon().get(KEY_UPPER_A));
        assertEquals(0, d.entriesOnlyOnLeft().size());
        assertEquals(null, d.entriesOnlyOnRight().keySet().iterator().next());
    }


    /**
     * Keys of an identity map which are equal but not identical are not collapsed
     */
    @Test
    public void identityHashMap() {
        String key1 = new String(KEY_A);
        String key2 = new String(KEY_A);
        Map<String, Integer> left = new IdentityHashMap<String, Integer>();
        left.put(key1, 1);
        left.put(key2, 2);
        Map<String, Integer> right = new IdentityHashMap<String, Integer>();
        right.put(key1, 1);

        IMapDifference<String, Integer> d = CompareMaps.getInstance().compare(left, right);
        assertEquals(1, d.entriesInCommon().size());
        assertEquals(1, d.entriesOnlyOnLeft().size());
        assertEquals(Integer.valueOf(2), d.entriesOnlyOnLeft().get(key2));

        d = CompareMaps.getInstance().compare(right, left);
        assertEquals(1, d.entriesOnlyOnRight().size());
        assertEquals(Integer.valueOf(2), d.entriesOnlyOnRight().get(key2));
    }
}
