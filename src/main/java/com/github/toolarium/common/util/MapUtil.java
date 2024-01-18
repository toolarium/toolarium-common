/*
 * MapUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Map utility.
 * 
 * @author patrick
 */
public final class MapUtil {
    
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     */
    private static class HOLDER {
        static final MapUtil INSTANCE = new MapUtil();
    }

    
    /**
     * Constructor
     */
    private MapUtil() {
        // NOP
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static MapUtil getInstance() {
        return HOLDER.INSTANCE;
    }

    
    /**
     * Create a new map
     *
     * @param <K> the key
     * @param <V> the value
     * @return the created map
     */
    public <K, V> Map<K, V> newMap() {
        return newMap(false);
    }
    
    
    /**
     * Create a new map
     *
     * @param <K> the key
     * @param <V> the value
     * @param sorted true if it is a sorted map
     * @return the created map
     */
    public <K, V> Map<K, V> newMap(boolean sorted) {
        final Map<K, V> result;
        if (sorted) {
            result = new TreeMap<K, V>();
        } else {
            result = new LinkedHashMap<K, V>();
        }
        
        return result;
    }


    /**
     * Add a map
     *
     * @param <K> the key
     * @param <V> the value
     * @param map the map to extend
     * @param mapToAdd the added map
     * @return the extended map
     */
    public <K, V> Map<K, V> add(Map<K, V> map, Map<K, V> mapToAdd) {
        if (map != null) {
            if (mapToAdd != null) {
                map.putAll(mapToAdd);
            }
        }

        return map;
    }    
}
