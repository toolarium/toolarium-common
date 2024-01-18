/*
 * CompareMapsTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.compare.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.toolarium.common.compare.map.impl.ValueDifference;
import com.github.toolarium.common.util.MapUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import org.junit.jupiter.api.Test;


/**
 * Test compare maps 
 * 
 * @author patrick
 */
public class CompareMapsTest {

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
}
