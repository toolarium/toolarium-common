/*
 * MapUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;


/**
 * Test {@link MapUtil}.
 *
 * @author patrick
 */
public class MapUtilTest {

    /**
     * Test new map creation
     */
    @Test
    public void testNewMap() {
        Map<String, String> map = MapUtil.getInstance().newMap();
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }


    /**
     * Test sorted map creation
     */
    @Test
    public void testNewSortedMap() {
        Map<String, String> map = MapUtil.getInstance().newMap(true);
        assertNotNull(map);
        map.put("b", "2");
        map.put("a", "1");
        assertEquals("[a, b]", map.keySet().toString());
    }


    /**
     * Test add maps
     */
    @Test
    public void testAdd() {
        Map<String, String> map = MapUtil.getInstance().newMap();
        map.put("a", "1");

        Map<String, String> toAdd = MapUtil.getInstance().newMap();
        toAdd.put("b", "2");

        Map<String, String> result = MapUtil.getInstance().add(map, toAdd);
        assertEquals(2, result.size());
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("b"));
    }


    /**
     * Test add with null mapToAdd is safe
     */
    @Test
    public void testAddNullMapToAdd() {
        Map<String, String> map = MapUtil.getInstance().newMap();
        map.put("a", "1");
        Map<String, String> result = MapUtil.getInstance().add(map, null);
        assertEquals(1, result.size());
    }


    /**
     * Test add with null target map throws exception
     */
    @Test
    public void testAddNullTargetMap() {
        Map<String, String> toAdd = MapUtil.getInstance().newMap();
        toAdd.put("a", "1");
        assertThrows(IllegalArgumentException.class, () -> MapUtil.getInstance().add(null, toAdd));
    }
}
