/*
 * RegularExpressionUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Utility class for compiling regular expression patterns with an LRU cache.
 * Instances can be created via {@link #getInstance()} for a shared singleton or
 * via the constructor for a dedicated cache per consumer.
 *
 * @author patrick
 */
public final class RegularExpressionUtil {
    /** The default maximum cache size */
    public static final int DEFAULT_MAX_SIZE = 64;

    private final Map<String, Pattern> cache;


    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final RegularExpressionUtil INSTANCE = new RegularExpressionUtil();
    }


    /**
     * Constructor. Creates a new instance with the default maximum cache size.
     */
    public RegularExpressionUtil() {
        this(DEFAULT_MAX_SIZE);
    }


    /**
     * Constructor. Creates a new instance with the given maximum cache size.
     * When the cache is full, the least recently used entry is evicted.
     *
     * @param maxSize the maximum number of compiled patterns to cache
     * @throws IllegalArgumentException if maxSize is less than 1
     */
    public RegularExpressionUtil(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("Max cache size must be at least 1!");
        }

        cache = new LruCache<>(maxSize);
    }


    /**
     * Get the shared singleton instance.
     *
     * @return the shared instance
     */
    public static RegularExpressionUtil getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Compiles the given regular expression pattern. Results are cached so that
     * repeated compilations of the same pattern string are avoided.
     * On cache hit the entry is promoted to most recently used.
     *
     * @param regex the regular expression to compile
     * @return the compiled pattern
     * @throws IllegalArgumentException if the regex is null, empty, or has invalid syntax
     */
    public Pattern compile(String regex) {
        if (regex == null || regex.trim().isEmpty()) {
            throw new IllegalArgumentException("Regular expression must not be null or empty!");
        }

        String trimmed = regex.trim();

        synchronized (cache) {
            Pattern pattern = cache.get(trimmed);
            if (pattern != null) {
                return pattern;
            }

            try {
                pattern = Pattern.compile(trimmed);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid regex pattern: " + e.getMessage(), e);
            }

            cache.put(trimmed, pattern);
            return pattern;
        }
    }


    /**
     * Returns the current number of cached patterns.
     *
     * @return the cache size
     */
    public int getCacheSize() {
        synchronized (cache) {
            return cache.size();
        }
    }


    /**
     * Clears the pattern cache.
     */
    public void clearCache() {
        synchronized (cache) {
            cache.clear();
        }
    }


    /**
     * A simple LRU cache backed by {@link LinkedHashMap} with access-order.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    private static final class LruCache<K, V> extends LinkedHashMap<K, V> {
        private static final long serialVersionUID = 1L;
        private final int maxSize;

        /**
         * Constructor
         *
         * @param maxSize the maximum number of entries
         */
        LruCache(int maxSize) {
            super(maxSize, 0.75f, true); // access-order
            this.maxSize = maxSize;
        }

        /**
         * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
         */
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
    }
}
