/*
 * ByteUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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


    /**
     * Test prettyPrintByteSize with negative values: they are scaled like positive values and keep the sign.
     */
    @Test
    public void testPrettyPrintNegativeByteSize() {
        assertEquals("-1B", ByteUtil.getInstance().prettyPrintByteSize(-1));
        assertEquals("-768B", ByteUtil.getInstance().prettyPrintByteSize(-768));
        assertEquals("-1.5K", ByteUtil.getInstance().prettyPrintByteSize(-1536));
        assertEquals("-4.8M", ByteUtil.getInstance().prettyPrintByteSize(-5000000));
        assertEquals("-8E", ByteUtil.getInstance().prettyPrintByteSize(Long.MIN_VALUE));
    }


    /**
     * Test long to bytes conversion (fixed 8 bytes, big-endian)
     */
    @Test
    public void testLongToBytes() {
        assertArrayEquals(new byte[] {0x00, 0x00, 0x01, 0x1C, 0x5C, 0x6C, 0x4B, 0x63 }, ByteUtil.getInstance().toBytes(1221321313123L));
        assertArrayEquals(new byte[8], ByteUtil.getInstance().toBytes(0L));
        assertArrayEquals(new byte[] {-1, -1, -1, -1, -1, -1, -1, -1 }, ByteUtil.getInstance().toBytes(-1L));
        assertArrayEquals(new byte[] {0x7F, -1, -1, -1, -1, -1, -1, -1 }, ByteUtil.getInstance().toBytes(Long.MAX_VALUE));
        assertArrayEquals(new byte[] {(byte) 0x80, 0, 0, 0, 0, 0, 0, 0 }, ByteUtil.getInstance().toBytes(Long.MIN_VALUE));
    }


    /**
     * Test bytes to long conversion
     */
    @Test
    public void testBytesToLong() {
        long[] values = {0L, 1L, -1L, 255L, 1221321313123L, -1221321313123L, Long.MAX_VALUE, Long.MIN_VALUE };
        for (long v : values) {
            assertEquals(v, ByteUtil.getInstance().toLong(ByteUtil.getInstance().toBytes(v)));
        }

        // only the first 8 bytes are used
        assertEquals(1L, ByteUtil.getInstance().toLong(new byte[] {0, 0, 0, 0, 0, 0, 0, 1, 0x7F, 0x7F }));

        // shorter arrays are interpreted as big-endian two's complement
        assertEquals(1221321313123L, ByteUtil.getInstance().toLong(new byte[] {0x01, 0x1C, 0x5C, 0x6C, 0x4B, 0x63 }));
        assertEquals(-1L, ByteUtil.getInstance().toLong(new byte[] {-1 }));
        assertEquals(255L, ByteUtil.getInstance().toLong(new byte[] {0, -1 }));

        // null / empty
        assertEquals(0L, ByteUtil.getInstance().toLong((byte[]) null));
        assertEquals(0L, ByteUtil.getInstance().toLong(new byte[0]));
    }

    /**
     * Test equals block
     */
    @Test
    public void testEqualsBlock() {
        byte[] a = new byte[] {1, 2, 3, 4};
        byte[] b = new byte[] {9, 2, 3, 9};
        assertTrue(ByteUtil.getInstance().equalsBlock(a, 1, b, 1, 2));
        assertFalse(ByteUtil.getInstance().equalsBlock(a, 0, b, 0, 2));
        assertTrue(ByteUtil.getInstance().equalsBlock(a, 4, b, 4, 0));
        assertFalse(ByteUtil.getInstance().equalsBlock(a, 0, b, 0, 999));
        assertFalse(ByteUtil.getInstance().equalsBlock(a, -1, b, 0, 1));
        assertFalse(ByteUtil.getInstance().equalsBlock(a, 0, b, 0, -1));
        assertFalse(ByteUtil.getInstance().equalsBlock(null, 0, b, 0, 1));
        assertTrue(ByteUtil.getInstance().equalsBlock(null, null));
        assertFalse(ByteUtil.getInstance().equalsBlock(a, null));
        assertTrue(ByteUtil.getInstance().equalsBlock(a, a.clone()));
    }

    /**
     * Test an overlapping copy
     */
    @Test
    public void testCopyBlockOverlapping() {
        byte[] d = new byte[] {1, 2, 3, 4, 5};
        ByteUtil.getInstance().copyBlock(d, 0, d, 1, 4);
        assertArrayEquals(new byte[] {1, 1, 2, 3, 4}, d);
    }

    /**
     * Test the preparation of a byte array to a multiple of a length
     */
    @Test
    public void testToBytesWithLength() {
        assertArrayEquals(new byte[] {0, 1, 2, 3}, ByteUtil.getInstance().toBytes(new byte[] {1, 2, 3}, 4));
        byte[] a = new byte[] {1, 2, 3, 4};
        byte[] r = ByteUtil.getInstance().toBytes(a, 4);
        assertArrayEquals(a, r);
        assertNotSame(a, r);
        assertThrows(IllegalArgumentException.class, () -> ByteUtil.getInstance().toBytes(a, 0));
        assertThrows(IllegalArgumentException.class, () -> ByteUtil.getInstance().toBytes(a, -1));
    }

    /**
     * Test to integer with short arrays
     */
    @Test
    public void testToIntegerShortArray() {
        assertEquals(0, ByteUtil.getInstance().toInteger((byte[]) null));
        assertEquals(0, ByteUtil.getInstance().toInteger(new byte[0]));
        assertEquals(5, ByteUtil.getInstance().toInteger(new byte[] {5}));
        assertEquals(513, ByteUtil.getInstance().toInteger(new byte[] {1, 2}));
    }
}
