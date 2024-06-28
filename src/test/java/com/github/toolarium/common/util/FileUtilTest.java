/*
 * FileUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Simple FileUtil test class
 * 
 * @author patrick
 */
public class FileUtilTest {
    private static final String TEST = "test";


    /**
     * Slashify unix test
     */
    @Test
    void slashifyUnix() {
        assertEquals("", FileUtil.getInstance().slashify(null));
        assertEquals("", FileUtil.getInstance().slashify(""));
        assertEquals(FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.SLASH_STR));
        assertEquals(FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.SLASH_STR + FileUtil.SLASH_STR + FileUtil.SLASH_STR + FileUtil.SLASH_STR + FileUtil.SLASH_STR + FileUtil.SLASH_STR));
        assertEquals(FileUtil.SLASH_STR + TEST, FileUtil.getInstance().slashify(TEST));
        assertEquals(FileUtil.SLASH_STR + TEST, FileUtil.getInstance().slashify(FileUtil.SLASH_STR + TEST));
        assertEquals(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR));
        assertEquals(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR + FileUtil.SLASH_STR + FileUtil.SLASH_STR));
    }


    /**
     * Slashify windows test
     */
    @Test
    void slashifyWindows() {
        assertEquals(FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.BACKSLASH_STR));
        assertEquals(FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.BACKSLASH_STR + FileUtil.BACKSLASH_STR));
        assertEquals(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.SLASH_STR + TEST + FileUtil.BACKSLASH_STR));
        assertEquals(FileUtil.SLASH_STR + TEST, FileUtil.getInstance().slashify(FileUtil.BACKSLASH_STR + TEST));
        assertEquals(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.BACKSLASH_STR + TEST + FileUtil.BACKSLASH_STR));
        assertEquals("c:" + FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR, FileUtil.getInstance().slashify("c:" + FileUtil.BACKSLASH_STR + TEST + FileUtil.BACKSLASH_STR + FileUtil.BACKSLASH_STR));
        assertEquals(FileUtil.SLASH_STR + TEST + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.SLASH_STR + TEST + FileUtil.BACKSLASH_STR + FileUtil.BACKSLASH_STR + FileUtil.BACKSLASH_STR + FileUtil.BACKSLASH_STR));
    }
    

    /**
     * Test slashify directory
     */
    @Test
    void testSlashifyDirectory() {
        assertEquals(FileUtil.SLASH_STR + System.getProperty("user.dir").replace('\\', '/') + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.BACKSLASH_STR + System.getProperty("user.dir")));
        assertEquals(FileUtil.SLASH_STR + System.getProperty("user.dir").replace('\\', '/') + FileUtil.SLASH_STR, FileUtil.getInstance().slashify(FileUtil.BACKSLASH_STR + System.getProperty("user.dir") + FileUtil.BACKSLASH_STR));
        assertEquals("C:/Users/default/.gradle/caches/x.y/workerMain/gradle-worker.jar", FileUtil.getInstance().slashify("C:\\Users\\default\\.gradle\\caches\\x.y\\workerMain\\gradle-worker.jar"));
    }
}
