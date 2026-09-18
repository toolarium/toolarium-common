/*
 * ByteUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


/**
 * Test the {@link ByteUtil}.
 *
 * @author patrick
 */
public class ByteUtilTest {

    /**
     * Test prettyPrintByteSize formatting.
     */
    @Test
    public void testPrettyPrintByteSize() {
        // byte range
        assertEquals("0B",   ByteUtil.getInstance().prettyPrintByteSize(0));
        assertEquals("1B",   ByteUtil.getInstance().prettyPrintByteSize(1));
        assertEquals("16B",  ByteUtil.getInstance().prettyPrintByteSize(16));
        assertEquals("512B", ByteUtil.getInstance().prettyPrintByteSize(512));
        assertEquals("768B", ByteUtil.getInstance().prettyPrintByteSize(768));  // boundary: not > 768

        // kilobyte range
        assertEquals("1K",    ByteUtil.getInstance().prettyPrintByteSize(1024));
        assertEquals("1.5K",  ByteUtil.getInstance().prettyPrintByteSize(1536));
        assertEquals("768K",  ByteUtil.getInstance().prettyPrintByteSize(1024L * 768));

        // megabyte range
        assertEquals("1M",   ByteUtil.getInstance().prettyPrintByteSize(1024L * 1024));
        assertEquals("1.5M", ByteUtil.getInstance().prettyPrintByteSize(1024L * 1024 + 512 * 1024));

        // gigabyte range
        assertEquals("1G",   ByteUtil.getInstance().prettyPrintByteSize(1024L * 1024 * 1024));
        assertEquals("1.5G", ByteUtil.getInstance().prettyPrintByteSize(1024L * 1024 * 1024 + 512L * 1024 * 1024));

        // terabyte range
        assertEquals("1T", ByteUtil.getInstance().prettyPrintByteSize(1024L * 1024 * 1024 * 1024));
    }
}
