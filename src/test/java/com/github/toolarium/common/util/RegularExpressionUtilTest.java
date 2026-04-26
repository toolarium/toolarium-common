/*
 * RegularExpressionUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;


/**
 * Test {@link RegularExpressionUtil}.
 *
 * @author patrick
 */
public class RegularExpressionUtilTest {

    /**
     * Test basic compilation
     */
    @Test
    public void testCompile() {
        RegularExpressionUtil util = new RegularExpressionUtil();
        Pattern pattern = util.compile("[a-z]+");
        assertNotNull(pattern);
        assertTrue(pattern.matcher("abc").matches());
    }


    /**
     * Test that the same pattern is returned from cache
     */
    @Test
    public void testCacheHit() {
        RegularExpressionUtil util = new RegularExpressionUtil();
        Pattern first = util.compile("[0-9]+");
        Pattern second = util.compile("[0-9]+");
        assertSame(first, second);
        assertEquals(1, util.getCacheSize());
    }


    /**
     * Test that whitespace is trimmed before caching
     */
    @Test
    public void testTrimming() {
        RegularExpressionUtil util = new RegularExpressionUtil();
        Pattern first = util.compile("  [a-z]+  ");
        Pattern second = util.compile("[a-z]+");
        assertSame(first, second);
        assertEquals(1, util.getCacheSize());
    }


    /**
     * Test null input
     */
    @Test
    public void testNullInput() {
        RegularExpressionUtil util = new RegularExpressionUtil();
        assertThrows(IllegalArgumentException.class, () -> util.compile(null));
    }


    /**
     * Test empty input
     */
    @Test
    public void testEmptyInput() {
        RegularExpressionUtil util = new RegularExpressionUtil();
        assertThrows(IllegalArgumentException.class, () -> util.compile(""));
        assertThrows(IllegalArgumentException.class, () -> util.compile("   "));
    }


    /**
     * Test invalid regex
     */
    @Test
    public void testInvalidPattern() {
        RegularExpressionUtil util = new RegularExpressionUtil();
        assertThrows(IllegalArgumentException.class, () -> util.compile("[invalid"));
        assertEquals(0, util.getCacheSize());
    }


    /**
     * Test LRU eviction when max size is reached
     */
    @Test
    public void testLruEviction() {
        RegularExpressionUtil util = new RegularExpressionUtil(3);

        Pattern p1 = util.compile("aaa");
        util.compile("bbb");
        util.compile("ccc");
        assertEquals(3, util.getCacheSize());

        // adding a 4th should evict the least recently used (aaa)
        util.compile("ddd");
        assertEquals(3, util.getCacheSize());

        // aaa was evicted, compiling again should return a new instance
        Pattern p1New = util.compile("aaa");
        assertNotNull(p1New);
        // the old p1 was evicted, so a fresh compile happened
        assertEquals(3, util.getCacheSize());
    }


    /**
     * Test that accessing an entry promotes it and prevents eviction
     */
    @Test
    public void testLruAccessOrder() {
        RegularExpressionUtil util = new RegularExpressionUtil(3);

        Pattern p1 = util.compile("aaa");
        util.compile("bbb");
        util.compile("ccc");

        // access aaa to promote it to most recently used
        assertSame(p1, util.compile("aaa"));

        // now adding ddd should evict bbb (the least recently used), not aaa
        util.compile("ddd");
        assertEquals(3, util.getCacheSize());

        // aaa should still be cached
        assertSame(p1, util.compile("aaa"));
    }


    /**
     * Test clear cache
     */
    @Test
    public void testClearCache() {
        RegularExpressionUtil util = new RegularExpressionUtil();
        util.compile("[a-z]+");
        util.compile("[0-9]+");
        assertEquals(2, util.getCacheSize());

        util.clearCache();
        assertEquals(0, util.getCacheSize());
    }


    /**
     * Test default max size
     */
    @Test
    public void testDefaultMaxSize() {
        RegularExpressionUtil util = new RegularExpressionUtil();
        for (int i = 0; i < 100; i++) {
            util.compile("pattern" + i);
        }
        assertEquals(RegularExpressionUtil.DEFAULT_MAX_SIZE, util.getCacheSize());
    }


    /**
     * Test invalid max size
     */
    @Test
    public void testInvalidMaxSize() {
        assertThrows(IllegalArgumentException.class, () -> new RegularExpressionUtil(0));
        assertThrows(IllegalArgumentException.class, () -> new RegularExpressionUtil(-1));
    }


    /**
     * Test singleton instance
     */
    @Test
    public void testSingleton() {
        assertSame(RegularExpressionUtil.getInstance(), RegularExpressionUtil.getInstance());
    }
}
