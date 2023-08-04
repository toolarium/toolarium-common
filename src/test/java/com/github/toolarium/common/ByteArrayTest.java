/*
 * ByteArrayTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.toolarium.common.util.ByteUtil;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link ByteArray}.
 *  
 * @author patrick
 */
public class ByteArrayTest {
    private static final String TEST_STRING = "12345678901234567890";


    /**
     * Test equal
     */
    @Test
    @SuppressWarnings("resource")
    public void testEqual() {
        assertEquals(new ByteArray(""), new ByteArray());
        assertEquals(new ByteArray(), new ByteArray());
        assertEquals(new ByteArray(""), new ByteArray(""));
        assertEquals(new ByteArray("ABCDEFGHIJKLMNOPQRSTUVWXY"), new ByteArray("ABCDEFGHIJKLMNOPQRSTUVWXY"));
        assertEquals(new ByteArray("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), new ByteArray("ABCDEFGHIJKLMNOPQRSTUVWXYZ").toByteArray(0, 26));
        
    }

    
    /**
     * Test equal
     */
    @Test
    @SuppressWarnings("resource")
    public void testToHex() {
        assertEquals("414243", new ByteArray("ABC").toHex());
    }


    /**
     * Test hash
     */
    @Test
    public void testHash() {
        assertEqualsHash(new ByteArray(""), new ByteArray());
        assertEqualsHash(new ByteArray(), new ByteArray());
        assertEqualsHash(new ByteArray(""), new ByteArray(""));
        assertEqualsHash(new ByteArray("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), new ByteArray("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
    }

    
    /**
     * Test trim
     */
    @Test
    @SuppressWarnings("resource")
    public void testTrim() {
        ByteArray array;
        array = new ByteArray();
        array.trimRight((byte) ' ');
        assertEquals("", array.toString());

        array = new ByteArray("");
        array.trimRight((byte) ' ');
        assertEquals("", array.toString());

        array = new ByteArray("   ");
        array.trimRight((byte) ' ');
        assertEquals("", array.toString());

        array = new ByteArray("Test2");
        array.trimRight((byte) ' ');
        assertEquals("Test2", array.toString());

        array = new ByteArray("Test                   ");
        array.trimRight((byte) ' ');
        assertEquals("Test", array.toString());

        array = new ByteArray("                   Test");
        array.trimRight((byte) ' ');
        assertEquals("                   Test", array.toString());

        array = new ByteArray();
        array.trimLeft((byte) ' ');
        assertEquals("", array.toString());

        array = new ByteArray("");
        array.trimLeft((byte) ' ');
        assertEquals("", array.toString());

        array = new ByteArray("   ");
        array.trimLeft((byte) ' ');
        assertEquals("", array.toString());

        array = new ByteArray("Test");
        array.trimLeft((byte) ' ');
        assertEquals("Test", array.toString());

        array = new ByteArray("                   Test");
        array.trimLeft((byte) ' ');
        assertEquals("Test", array.toString());

        array = new ByteArray("Test                   ");
        array.trimLeft((byte) ' ');
        assertEquals("Test                   ", array.toString());
    }


    /**
     * Test replace
     */
    @Test
    @SuppressWarnings("resource")
    public void testReplace() {
        ByteArray array;
        array = new ByteArray();
        array.replace((byte) 'T', (byte) 'B');
        assertEquals("", array.toString());

        array = new ByteArray("");
        array.replace((byte) 'T', (byte) 'B');
        assertEquals("", array.toString());

        array = new ByteArray("   ");
        array.replace((byte) 'T', (byte) 'B');
        assertEquals("   ", array.toString());

        array = new ByteArray("Test");
        array.replace((byte) 'T', (byte) 'B');
        assertEquals("Best", array.toString());

        array = new ByteArray("Test                   ");
        array.replace((byte) 'T', (byte) 'B');
        assertEquals("Best                   ", array.toString());
    }


    /**
     * Test replace
     */
    @Test
    @SuppressWarnings("resource")
    public void testIndexOf() {
        ByteArray array;
        array = new ByteArray();
        assertEquals(-1, array.indexOf((byte) 'T'));

        array = new ByteArray("");
        assertEquals(-1, array.indexOf((byte) 'T'));

        array = new ByteArray("   ");
        assertEquals(-1, array.indexOf((byte) 'T'));

        array = new ByteArray("Test ");
        assertEquals(0, array.indexOf((byte) 'T'));
        assertEquals(3, array.indexOf((byte) 't'));
        assertEquals(2, array.lastIndexOf((byte) 's'));
        assertEquals(3, array.lastIndexOf((byte) 't'));

        assertEquals(true, array.startsWith((byte) 'T'));
        assertEquals(false, array.startsWith((byte) 'e'));

        assertEquals(true, array.endsWith((byte) ' '));
        assertEquals(false, array.endsWith((byte) 't'));

        assertEquals(2, array.indexOf(new ByteArray("st")));
        assertEquals(3, array.indexOf(new ByteArray("t ")));
        assertEquals(0, array.indexOf(new ByteArray("Te")));
    }

    
    /**
     * Test chop
     */
    @Test
    @SuppressWarnings("resource")
    public void testChopRight() {
        ByteArray array = new ByteArray();
        array.chopRight();
        array.chopRight();
        array.chopRight();
        array.chopRight();
        array.chopRight();

        array = new ByteArray("123456");
        array.chopRight();
        assertEquals("12345", array.toString());
        assertEquals("12345", new String(array.toBytes()));
        assertEquals(5, array.length());

        array.chopRight();
        assertEquals("1234", array.toString());
        assertEquals("1234", new String(array.toBytes()));
        assertEquals(4, array.length());

        array.chopRight();
        assertEquals("123", array.toString());
        assertEquals("123", new String(array.toBytes()));
        assertEquals(3, array.length());

        array.chopRight();
        assertEquals("12", array.toString());
        assertEquals("12", new String(array.toBytes()));
        assertEquals(2, array.length());

        array.chopRight();
        assertEquals("1", array.toString());
        assertEquals("1", new String(array.toBytes()));
        assertEquals(1, array.length());

        array.chopRight();
        assertEquals("", array.toString());
        assertEquals("", new String(array.toBytes()));
        assertEquals(0, array.length());

        array.chopRight();
        assertEquals("", array.toString());
        assertEquals("", new String(array.toBytes()));
        assertEquals(0, array.length());

        array = new ByteArray("123456");
        assertEquals(6, array.length());
        array.chopRight(4);
        assertEquals("12", array.toString());
        assertEquals("12", new String(array.toBytes()));
        assertEquals(2, array.length());
        array.chopRight(2);
        assertEquals("", array.toString());
        assertEquals("", new String(array.toBytes()));
        assertEquals(0, array.length());
    }

    
    /**
     * Test chop
     */
    @Test
    @SuppressWarnings("resource")
    public void testChop() {
        ByteArray array = new ByteArray();
        array.chopLeft();
        array.chopLeft();
        array.chopLeft();
        array.chopLeft();
        array.chopLeft();

        array = new ByteArray("123456");
        array.chopLeft();
        assertEquals("23456", array.toString());
        assertEquals("23456", new String(array.toBytes()));
        assertEquals(5, array.length());

        array.chopLeft();
        assertEquals("3456", array.toString());
        assertEquals("3456", new String(array.toBytes()));
        assertEquals(4, array.length());

        array.chopLeft();
        assertEquals("456", array.toString());
        assertEquals("456", new String(array.toBytes()));
        assertEquals(3, array.length());

        array.chopLeft();
        assertEquals("56", array.toString());
        assertEquals("56", new String(array.toBytes()));
        assertEquals(2, array.length());

        array.chopLeft();
        assertEquals("6", array.toString());
        assertEquals("6", new String(array.toBytes()));
        assertEquals(1, array.length());

        array.chopLeft();
        assertEquals("", array.toString());
        assertEquals("", new String(array.toBytes()));
        assertEquals(0, array.length());

        array.chopLeft();
        assertEquals("", array.toString());
        assertEquals("", new String(array.toBytes()));
        assertEquals(0, array.length());

        array = new ByteArray("123456");
        assertEquals(6, array.length());
        array.chopLeft(4);
        assertEquals("56", array.toString());
        assertEquals("56", new String(array.toBytes()));
        assertEquals(2, array.length());
        array.chopLeft(2);
        assertEquals("", array.toString());
        assertEquals("", new String(array.toBytes()));
        assertEquals(0, array.length());
    }

    /**
     * Test append
     */
    @Test
    @SuppressWarnings("resource")
    public void testAppend() {
        ByteArray array = new ByteArray();
        array.append((byte) 0x01);
        array.append((byte) 0x02);
        array.append((byte) 0x03);
        array.append((byte) 0x04);

        assertEquals(array.get(0), (byte) 0x01);
        assertEquals(array.get(1), (byte) 0x02);
        assertEquals(array.get(2), (byte) 0x03);
        assertEquals(array.get(3), (byte) 0x04);
        assertEquals(4, array.length());

        assertNotSame(array.toBytes(), new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
        assertNotSame(array.toString().getBytes(), new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });

        assertEqualsBytes(array.toBytes(), new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
        assertEqualsBytes(array.toString().getBytes(), new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });

        array = new ByteArray();
        array.append(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
        assertEqualsBytes(array.toBytes(), new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
        assertEqualsBytes(array.toString().getBytes(), new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });

        array = ByteArray.createByteArray((byte) 0x01, 1);
        assertEqualsBytes(array.toBytes(), new byte[] {(byte) 0x01 });
        assertEqualsBytes(array.toString().getBytes(), new byte[] {(byte) 0x01 });

        array = ByteArray.createByteArray((byte) 0x02, 1);
        assertEqualsBytes(array.toBytes(), new byte[] {(byte) 0x02 });
        assertEqualsBytes(array.toString().getBytes(), new byte[] {(byte) 0x02 });

        array = new ByteArray(new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
        assertEqualsBytes(array.toBytes(), new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
        assertEqualsBytes(array.toString().getBytes(), new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 });
    }


    /**
     * Test append
     */
    @Test
    @SuppressWarnings("resource")
    public void testGetBytes() {
        final String testString = "1234567890";
        assertEquals(new ByteArray(""), (new ByteArray(testString)).getBytes(-1, -1));
        assertEquals(new ByteArray(""), (new ByteArray(testString)).getBytes(-1, 0));
        assertEquals(new ByteArray(""), (new ByteArray(testString)).getBytes(0, -1));
        assertEquals(new ByteArray(""), (new ByteArray(testString)).getBytes(0, 0));
        assertEquals(new ByteArray("1"), (new ByteArray(testString)).getBytes(0, 1));
        assertEquals(new ByteArray("12"), (new ByteArray(testString)).getBytes(0, 2));
        assertEquals(new ByteArray(testString), (new ByteArray(testString)).getBytes(0, 10));
        assertEquals(new ByteArray(testString), (new ByteArray(testString)).getBytes(0, 20));
    }

    /**
     * Test reverse
     */
    @Test
    @SuppressWarnings("resource")
    public void testReverse() {
        ByteArray array = new ByteArray();
        array.append((byte) 0x01);
        array.append((byte) 0x02);
        array.append((byte) 0x03);
        array.append((byte) 0x04);
        array = array.reverse();

        assertEquals(array.get(0), (byte) 0x04);
        assertEquals(array.get(1), (byte) 0x03);
        assertEquals(array.get(2), (byte) 0x02);
        assertEquals(array.get(3), (byte) 0x01);

        assertNotSame(array.toBytes(), new byte[] {(byte) 0x04, (byte) 0x03, (byte) 0x02, (byte) 0x01 });
        assertNotSame(array.toString().getBytes(), new byte[] {(byte) 0x04, (byte) 0x03, (byte) 0x02, (byte) 0x01 });

        assertEqualsBytes(array.toBytes(), new byte[] {(byte) 0x04, (byte) 0x03, (byte) 0x02, (byte) 0x01 });
        assertEqualsBytes(array.toString().getBytes(), new byte[] {(byte) 0x04, (byte) 0x03, (byte) 0x02, (byte) 0x01 });
    }


    /**
     * Test the array conversion
     */
    @Test
    public void testByteArrayToInteger() {
        int value = 231221139;
        byte[] data = ByteUtil.getInstance().toBytes(value);
        assertNotNull(data);
        assertEquals(new ByteArray(new byte[] {(byte) 0x93, (byte) 0x27, (byte) 0xC8, (byte) 0x0D }), new ByteArray(data));
        assertEquals(value, ByteUtil.getInstance().toInteger(data));
    }


    /**
     * Test the array conversion
     */
    @Test
    public void testByteArrayToLong() {
        long value = 1221321313123L;
        byte[] data = ByteUtil.getInstance().toBytes(value);
        assertNotNull(data);

        assertEquals(new ByteArray(new byte[] {(byte) 0x01, (byte) 0x1C, (byte) 0x5C, (byte) 0x6C, (byte) 0x4B, (byte) 0x63 }), new ByteArray(data));
        assertEquals(value, ByteUtil.getInstance().toLong(data));
    }


    /**
     * Test the array conversion
     */
    @Test
    public void testPreapareByteArray() {
        assertEquals(new ByteArray(new byte[] {(byte)0x00, (byte)0x00, (byte)0x01, (byte)0x1C, (byte)0x5C, (byte)0x6C, (byte)0x4B, (byte)0x63 }),
                     new ByteArray(ByteUtil.getInstance().toBytes(ByteUtil.getInstance().toBytes(1221321313123L), 8)));


        byte[] testDataBefore = new byte[] {(byte)0x01, (byte)0x1C, (byte)0x5C, (byte)0x6C, (byte)0x4B, (byte)0x63 };
        byte[] testDataAfter = new byte[] {(byte)0x00, (byte)0x00, (byte)0x01, (byte)0x1C, (byte)0x5C, (byte)0x6C, (byte)0x4B, (byte)0x63 };
        assertEquals(new ByteArray(testDataAfter), new ByteArray(ByteUtil.getInstance().toBytes(testDataBefore, 8)));

        testDataBefore = new byte[] {(byte)0x01, (byte)0x1C, (byte)0x5C, (byte)0x6C, (byte)0x4B, (byte)0x63 };
        testDataAfter = new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x1C, (byte)0x5C, (byte)0x6C, (byte)0x4B, (byte)0x63 };
        assertEquals(new ByteArray(testDataAfter), new ByteArray(ByteUtil.getInstance().toBytes(testDataBefore, 16)));
    }


    /**
     * Test
     * @throws Exception in case of error
     */
    @Test
    @SuppressWarnings("resource")
    public void testReplaceBytes() throws Exception {
        ByteArray b = new ByteArray();
        assertEquals(b, b.replace(null, null));
        assertEquals(b, b.replace(null, null));
        assertEquals(b, b.replace(new ByteArray(""), null));
        assertEquals(b, b.replace(new ByteArray(""), new ByteArray("")));

        b = new ByteArray(TEST_STRING);
        assertEquals(new ByteArray("A4567890A4567890"), b.replace(new ByteArray("123"), new ByteArray("A")));
        b = new ByteArray("AAAAAAAAA");
        assertEquals(new ByteArray("BBBBBBBBB"), b.replace(new ByteArray("A"), new ByteArray("B")));
        b = new ByteArray("ABABABABABABABABAB");
        assertEquals(new ByteArray("BBBBBBBBB"), b.replace(new ByteArray("AB"), new ByteArray("B")));
        b = new ByteArray("BBBBBBBBB");
        assertEquals(new ByteArray("ABABABABABABABABAB"), b.replace(new ByteArray("B"), new ByteArray("AB")));
    }    
    
    /**
     * Test the grow performance
     */
    @Test
    public void testGrowPerformance() {
        for (int i = 0; i < 10000; i++) {
            @SuppressWarnings("resource")
            ByteArray b = new ByteArray();
            b.append("123456");
            b.append("78901234567890");
            b.append("7890123456789078901234567890789012345678907890123456789078901234567890789012345678907890123456789078901234567890");
            b.append("7");
        }
    }    
    
    /**
     * Test the starts with
     */
    @Test
    @SuppressWarnings("resource")
    public void testStartsWith() {
        assertFalse(new ByteArray().startsWith(null));
        assertFalse(new ByteArray(TEST_STRING).startsWith(null));
        assertEquals(0, new ByteArray().countStartsWith(null));
        
        String prefixStr = "__prefix__";
        ByteArray prefix = new ByteArray(prefixStr);
        assertFalse(new ByteArray(TEST_STRING).startsWith(prefix, -1));
        assertFalse(new ByteArray(TEST_STRING).startsWith(prefix, 128));
        assertFalse(new ByteArray(TEST_STRING).startsWith(prefix));
        assertFalse(new ByteArray().startsWith(prefix));
        assertTrue(new ByteArray(prefixStr).startsWith(prefix));
        assertTrue(new ByteArray(prefixStr + prefixStr).startsWith(prefix));
        assertFalse(new ByteArray(TEST_STRING + prefixStr).startsWith(prefix));
        assertTrue(new ByteArray(prefixStr + TEST_STRING).startsWith(prefix));
        assertTrue(new ByteArray(prefixStr + prefixStr + prefixStr + TEST_STRING).startsWith(prefix));
    }
    
    
    /**
     * Test the count starts with
     */
    @Test
    @SuppressWarnings("resource")
    public void testCountStartsWith() {
        String prefixStr = "__prefix__";
        ByteArray prefix = new ByteArray(prefixStr);

        assertEquals(0, new ByteArray().countStartsWith(null));
        assertEquals(0, new ByteArray(TEST_STRING).countStartsWith(null));
        assertEquals(0, new ByteArray().countStartsWith(prefix));
        assertEquals(1, new ByteArray(prefixStr).countStartsWith(prefix));
        assertEquals(2, new ByteArray(prefixStr + prefixStr).countStartsWith(prefix));
        assertEquals(0, new ByteArray(TEST_STRING + prefixStr).countStartsWith(prefix));
        assertEquals(1, new ByteArray(prefixStr + TEST_STRING).countStartsWith(prefix));
        assertEquals(3, new ByteArray(prefixStr + prefixStr + prefixStr + TEST_STRING).countStartsWith(prefix));
        assertEquals(1, new ByteArray(prefixStr + " " + prefixStr + prefixStr + TEST_STRING).countStartsWith(prefix));
    }

    
    /**
     * Test the ends with
     */
    @Test
    @SuppressWarnings("resource")
    public void testEndsWith() {
        String prefixStr = "__prefix__";
        ByteArray prefix = new ByteArray(prefixStr);

        assertFalse(new ByteArray().endsWith(null));
        assertFalse(new ByteArray(TEST_STRING).endsWith(null));
        assertFalse(new ByteArray(TEST_STRING).endsWith(prefix));
        assertFalse(new ByteArray().endsWith(prefix));
        assertTrue(new ByteArray(prefixStr).endsWith(prefix));
        assertTrue(new ByteArray(prefixStr + prefixStr).endsWith(prefix));
        assertFalse(new ByteArray(prefixStr + TEST_STRING).endsWith(prefix));
        assertTrue(new ByteArray(TEST_STRING + prefixStr).endsWith(prefix));
        assertTrue(new ByteArray(TEST_STRING + prefixStr + prefixStr + prefixStr).endsWith(prefix));
    }
    
    
    /**
     * Test the ends with
     */
    @Test
    @SuppressWarnings("resource")
    public void testCountEndsWith() {
        String prefixStr = "__prefix__";
        ByteArray prefix = new ByteArray(prefixStr);

        assertEquals(0, new ByteArray().countEndsWith(null));
        assertEquals(0, new ByteArray(TEST_STRING).countEndsWith(null));
        assertEquals(0, new ByteArray(TEST_STRING).countEndsWith(prefix));
        assertEquals(0, new ByteArray().countEndsWith(prefix));
        assertEquals(1, new ByteArray(prefixStr).countEndsWith(prefix));
        assertEquals(2, new ByteArray(prefixStr + prefixStr).countEndsWith(prefix));
        assertEquals(0, new ByteArray(prefixStr + TEST_STRING).countEndsWith(prefix));
        assertEquals(1, new ByteArray(TEST_STRING + prefixStr).countEndsWith(prefix));
        assertEquals(3,
                new ByteArray(TEST_STRING + prefixStr + prefixStr + prefixStr).countEndsWith(prefix));
        assertEquals(1,
                new ByteArray(TEST_STRING + prefixStr + prefixStr + " " + prefixStr).countEndsWith(prefix));
    }
    
    
    /**
     * Test the ends with
     */
    @Test
    public void testCountEndsWith2() {
        String prefixStr = "__prefix__";
        ByteArray prefix = new ByteArray(prefixStr);

        assertEquals(0, countEndsWith(new ByteArray(), null));
        assertEquals(0, countEndsWith(new ByteArray(TEST_STRING), null));
        assertEquals(0, countEndsWith(new ByteArray(TEST_STRING), prefix));
        assertEquals(0, countEndsWith(new ByteArray(), prefix));
        assertEquals(1, countEndsWith(new ByteArray(prefixStr), prefix));
        assertEquals(2, countEndsWith(new ByteArray(prefixStr + prefixStr), prefix));
        assertEquals(0, countEndsWith(new ByteArray(prefixStr + TEST_STRING), prefix));
        assertEquals(1, countEndsWith(new ByteArray(TEST_STRING + prefixStr), prefix));
        assertEquals(3, countEndsWith(new ByteArray(TEST_STRING + prefixStr + prefixStr + prefixStr), prefix));
        assertEquals(1, countEndsWith(new ByteArray(TEST_STRING + prefixStr + prefixStr + " " + prefixStr), prefix));
    }

    

    
    /**
     * Test the hash code of the given object
     *
     * @param o1 the first object
     * @param o2 the second object
     */
    private void assertEqualsHash(Object o1, Object o2) {
        assertEquals(o1.hashCode(), o2.hashCode());
    }

    
    /**
     * Test the hash code of the given object
     *
     * @param expected the first object
     * @param actual the second object
     */
    @SuppressWarnings("resource")
    private void assertEqualsBytes(byte[] expected, byte[] actual) {
        if (expected == null && actual == null) {
            return;
        }

        if (ByteUtil.getInstance().equalsBlock(expected, actual)) {
            return;
        }

        fail("Asserttion failed: expected:<" + new ByteArray(expected).toHex() + "> but was:<" + new ByteArray(actual).toHex() + ">\n");
    }


    /**
     * Count ends with
     * 
     * @param data the data
     * @param prefix the prefix
     * @return the number of found prefix at the end
     */
    private int countEndsWith(ByteArray data, ByteArray prefix) {
        if (prefix == null || prefix.length() == 0) {
            return 0;
        }

        if (data == null || data.length() == 0) {
            return 0;
        }

        int idx = prefix.length();
        int counter = 0;
        int offset = data.length() - prefix.length();

        while (idx > 0 && (data.startsWith(prefix, offset))) {
            counter++;
            offset -= prefix.length();
        }

        return counter;
    }
}
