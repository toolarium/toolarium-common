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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.toolarium.common.util.ByteUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link ByteArray}.
 *  
 * @author patrick
 */
public class ByteArrayTest {
    private static final String TEST_STRING = "12345678901234567890";
    private static final String X_STR = "X";
    private static final String SPACE_STR = " ";
    private static final String ABC_STR = "abc";


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
     * Test that the shared constants can not be modified
     */
    @Test
    @SuppressWarnings("resource")
    public void testConstantsAreReadOnly() {
        ByteArray[] constants = new ByteArray[] {ByteArray.NL, ByteArray.CR, ByteArray.TAB, ByteArray.SPACE, ByteArray.DOT, ByteArray.EMPTY};
        for (ByteArray c : constants) {
            final String before = c.toString();
            assertThrows(UnsupportedOperationException.class, () -> c.append((byte) 'X'));
            assertThrows(UnsupportedOperationException.class, () -> c.append(X_STR));
            assertThrows(UnsupportedOperationException.class, () -> c.append(new StringBuilder(X_STR)));
            assertThrows(UnsupportedOperationException.class, () -> c.append(new StringBuffer(X_STR)));
            assertThrows(UnsupportedOperationException.class, () -> c.append(new byte[] {1}));
            assertThrows(UnsupportedOperationException.class, () -> c.append(new byte[] {1}, 0, 1));
            assertThrows(UnsupportedOperationException.class, () -> c.append(new ByteArray(X_STR)));
            assertThrows(UnsupportedOperationException.class, () -> c.append(ByteBuffer.wrap(new byte[] {1})));
            assertThrows(UnsupportedOperationException.class, () -> c.write(ByteBuffer.wrap(new byte[] {1})));
            assertThrows(UnsupportedOperationException.class, () -> c.read(ByteBuffer.allocate(10)));
            assertThrows(UnsupportedOperationException.class, () -> c.erase());
            assertThrows(UnsupportedOperationException.class, () -> c.chopLeft());
            assertThrows(UnsupportedOperationException.class, () -> c.chopLeft(1));
            assertThrows(UnsupportedOperationException.class, () -> c.chopRight());
            assertThrows(UnsupportedOperationException.class, () -> c.chopRight(1));
            assertThrows(UnsupportedOperationException.class, () -> c.trim((byte) ' '));
            assertThrows(UnsupportedOperationException.class, () -> c.trimLeft((byte) ' '));
            assertThrows(UnsupportedOperationException.class, () -> c.trimRight((byte) ' '));
            assertThrows(UnsupportedOperationException.class, () -> c.replace((byte) ' ', (byte) 'X'));
            assertThrows(UnsupportedOperationException.class, () -> c.set(0, (byte) 'X'));
            assertThrows(UnsupportedOperationException.class, () -> c.close());
            assertTrue(c.toByteBuffer().isReadOnly());
            assertTrue(c.isOpen());
            assertEquals(before, c.toString());
        }

        assertEquals("\n", ByteArray.NL.toString());
        assertEquals("\r", ByteArray.CR.toString());
        assertEquals("\t", ByteArray.TAB.toString());
        assertEquals(SPACE_STR, ByteArray.SPACE.toString());
        assertEquals(".", ByteArray.DOT.toString());
        assertEquals("", ByteArray.EMPTY.toString());
        assertEquals(0, ByteArray.EMPTY.length());
    }


    /**
     * Test that the shared constants can be used normally
     */
    @Test
    public void testConstantsUsage() {
        ByteArray array = new ByteArray("a").append(ByteArray.NL).append(ByteArray.TAB).append(ByteArray.EMPTY).append(ByteArray.DOT);
        assertEquals("a\n\t.", array.toString());
        assertEquals(new ByteArray("\n"), ByteArray.NL);
        assertEquals(ByteArray.NL, new ByteArray("\n"));
        assertEquals(new ByteArray("\n").hashCode(), ByteArray.NL.hashCode());
        assertEquals(new ByteArray(), ByteArray.EMPTY);
        assertEquals(ByteArray.EMPTY, new ByteArray());

        // copies are modifiable and independent
        ByteArray copy = ByteArray.SPACE.getBytes().append(X_STR);
        assertEquals(" X", copy.toString());
        assertEquals(" X", ByteArray.SPACE.clone().append(X_STR).toString());
        assertEquals(" X", ByteArray.SPACE.concat((byte) 'X').toString());
        assertEquals(X_STR, ByteArray.SPACE.replace(ByteArray.SPACE, new ByteArray(X_STR)).append("").toString());
        assertEquals(SPACE_STR, ByteArray.SPACE.toString());
        assertEquals(SPACE_STR, new ByteArray(ByteArray.SPACE).toString());
    }


    /**
     * Test reading as channel
     */
    @Test
    @SuppressWarnings("resource")
    public void testRead() {
        ByteArray array = new ByteArray("0123456789");
        ByteBuffer dst = ByteBuffer.allocate(4);
        assertEquals(4, array.read(dst));
        assertEquals("0123", new String(dst.array(), 0, dst.position()));
        assertEquals(0, array.read(dst)); // no space left
        dst.clear();
        assertEquals(4, array.read(dst));
        assertEquals("4567", new String(dst.array(), 0, dst.position()));
        dst.clear();
        assertEquals(2, array.read(dst));
        assertEquals("89", new String(dst.array(), 0, dst.position()));
        dst.clear();
        assertEquals(-1, array.read(dst));
        assertEquals("0123456789", array.toString()); // content not consumed

        // standard read loop terminates
        array = new ByteArray(TEST_STRING);
        ByteArray result = new ByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(3);
        while (array.read(buffer) != -1) {
            buffer.flip();
            result.write(buffer);
            buffer.clear();
        }
        assertEquals(TEST_STRING, result.toString());

        // empty, erase and close
        assertEquals(-1, new ByteArray().read(ByteBuffer.allocate(3)));
        array.erase().append("ab");
        dst = ByteBuffer.allocate(3);
        assertEquals(2, array.read(dst));
        assertEquals("ab", new String(dst.array(), 0, dst.position()));
        assertEquals(0, new ByteArray("x").read(ByteBuffer.allocate(0)));
        ByteArray closed = new ByteArray("abc");
        closed.close();
        assertEquals(-1, closed.read(ByteBuffer.allocate(3)));
    }


    /**
     * Test toLong
     */
    @Test
    public void testToLong() {
        assertEquals(0, new ByteArray().toLong());
        assertEquals(0, ByteArray.EMPTY.toLong());
        assertEquals(1221321313123L, new ByteArray(ByteUtil.getInstance().toBytes(1221321313123L)).toLong());

        // spare capacity / stale bytes must be ignored
        ByteArray array = new ByteArray();
        array.append((byte) 1);
        assertEquals(1, array.toLong());
        array.append((byte) 2).chopRight();
        assertEquals(1, array.toLong());
        assertEquals(0x0102, new ByteArray(new byte[] {1, 2}).toLong());
        assertEquals(-1, new ByteArray(new byte[] {(byte) 0xFF}).toLong());
        assertEquals(1, new ByteArray(new byte[] {0, 0, 0, 0, 0, 0, 0, 1, (byte) 0xFF}).toLong()); // only the first 8 bytes
    }


    /**
     * Test serialization
     *
     * @throws IOException in case of error
     * @throws ClassNotFoundException in case of error
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        ByteArray array = new ByteArray("abc");
        assertEquals(array, roundTrip(array));
        ByteArray nl = roundTrip(ByteArray.NL);
        assertEquals(ByteArray.NL, nl);
        assertThrows(UnsupportedOperationException.class, () -> nl.append(X_STR));
    }


    /**
     * Removed data is not kept: neither in the serialized form nor in the byte buffer (M04)
     *
     * @throws IOException in case of error
     * @throws ClassNotFoundException in case of error
     */
    @Test
    @SuppressWarnings("resource")
    public void testRemovedDataIsNotExposed() throws IOException, ClassNotFoundException {
        final String secret = "TopSecret123";
        ByteArray array = new ByteArray("password=" + secret);
        array.erase();
        array.append(X_STR);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(array);
        }
        assertFalse(new String(bos.toByteArray(), StandardCharsets.ISO_8859_1).contains(secret));
        assertEquals(array, roundTrip(array));

        ByteBuffer buffer = array.toByteBuffer();
        assertEquals(array.length(), buffer.capacity());

        ByteArray chopped = new ByteArray("abc" + secret);
        chopped.chopRight(secret.length());
        chopped.append(X_STR);
        assertEquals("abc" + X_STR, chopped.toString());
        chopped.chopLeft(1);
        assertEquals("bc" + X_STR, roundTrip(chopped).toString());
    }


    /**
     * A serialized byte array with an invalid state is rejected on deserialization
     *
     * @throws IOException in case of error
     * @throws ReflectiveOperationException in case of error
     */
    @Test
    public void testDeserializationValidation() throws IOException, ReflectiveOperationException {
        // the serialized primitive fields: isOpen (true), length (3) and readPosition (0)
        final byte[] validFields = {1, 0, 0, 0, 3, 0, 0, 0, 0};
        final byte[] stream = serialize(new ByteArray(ABC_STR));
        assertThrows(InvalidObjectException.class, () -> deserialize(replace(stream, validFields, new byte[] {1, 0, 0, 0, 4, 0, 0, 0, 0})));
        assertThrows(InvalidObjectException.class, () -> deserialize(replace(stream, validFields, new byte[] {1, -1, -1, -1, -1, 0, 0, 0, 0})));
        assertThrows(InvalidObjectException.class, () -> deserialize(replace(stream, validFields, new byte[] {1, 0, 0, 0, 3, -1, -1, -1, -1})));
        assertEquals(ABC_STR, deserialize(replace(stream, validFields, new byte[] {1, 0, 0, 0, 3, 0, 0, 0, 3})).toString());

        ByteArray array = new ByteArray(ABC_STR);
        Field readPositionField = ByteArray.class.getDeclaredField("readPosition");
        readPositionField.setAccessible(true);
        readPositionField.setInt(array, 4);
        final byte[] invalidReadPosition = serialize(array);
        assertThrows(InvalidObjectException.class, () -> deserialize(invalidReadPosition));
    }


    /**
     * The growth size of the internal array is calculated from the used length and not from the capacity
     */
    @Test
    public void testGrowthSizeFromLength() {
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            data.append('a');
        }

        ByteArray array = new ByteArray(data.toString());
        array.erase();
        assertEquals(ByteArray.DEFAULT_BLOCK_SIZE, array.getNewDataSize(10));

        array.append(data.substring(0, 100));
        assertEquals(200, array.getNewDataSize(10));
    }


    /**
     * Serialize an object
     *
     * @param object the object
     * @return the serialized object
     * @throws IOException in case of error
     */
    private byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
        }

        return bos.toByteArray();
    }


    /**
     * Deserialize an object
     *
     * @param data the serialized object
     * @return the object
     * @throws IOException in case of error
     * @throws ClassNotFoundException in case of error
     */
    private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return in.readObject();
        }
    }


    /**
     * Replace the only occurrence of a byte sequence
     *
     * @param data the data
     * @param search the byte sequence to search
     * @param replacement the replacement with the same length
     * @return the copy of the data with the replaced sequence
     */
    private byte[] replace(byte[] data, byte[] search, byte[] replacement) {
        int index = -1;
        for (int i = 0; i <= data.length - search.length; i++) {
            if (Arrays.equals(Arrays.copyOfRange(data, i, i + search.length), search)) {
                assertEquals(-1, index, "The byte sequence is not unique");
                index = i;
            }
        }

        assertTrue(index >= 0, "The byte sequence was not found");
        byte[] result = data.clone();
        System.arraycopy(replacement, 0, result, index, replacement.length);
        return result;
    }


    /**
     * Serialize and deserialize a byte array
     *
     * @param array the array
     * @return the deserialized array
     * @throws IOException in case of error
     * @throws ClassNotFoundException in case of error
     */
    private ByteArray roundTrip(ByteArray array) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(array);
        }

        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (ByteArray) in.readObject();
        }
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

        assertEquals(new ByteArray(new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x1C, (byte) 0x5C, (byte) 0x6C, (byte) 0x4B, (byte) 0x63 }), new ByteArray(data));
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


    /**
     * A negative chop number chops nothing
     */
    @Test
    public void testChopNegative() {
        ByteArray a = new ByteArray(ABC_STR);
        a.chopRight(-5);
        assertEquals(new ByteArray(ABC_STR), a);
        a.chopLeft(-1);
        assertEquals(new ByteArray(ABC_STR), a);
    }


    /**
     * A start position beyond the length returns an empty array
     */
    @Test
    public void testStartBeyondLength() {
        ByteArray a = new ByteArray(ABC_STR);
        assertEquals(0, a.getBytes(10, 20).length());
        assertEquals(0, a.toByteArray(10, 20).length());
        assertEquals(new ByteArray("bc"), a.getBytes(1, 20));
    }


    /**
     * Strings are always UTF-8 encoded
     */
    @Test
    public void testUtf8() {
        final String text = "pässwört€";
        ByteArray a = new ByteArray(text);
        assertEquals(new ByteArray(text.getBytes(StandardCharsets.UTF_8)), a);
        assertEquals(text, a.toString());
        assertEquals(text, new ByteArray().append(new StringBuilder(text)).toString());
    }


    /**
     * Many small appends are fast
     */
    @Test
    public void testManySmallAppends() {
        final int size = 4_000_000;
        ByteArray a = new ByteArray();
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            for (int i = 0; i < size; i++) {
                a.append((byte) i);
            }
        });
        assertEquals(size, a.length());
        assertEquals((byte) (size - 1), a.get(size - 1));
    }


    /**
     * Reading a byte buffer does not change the position of the caller's buffer
     */
    @Test
    public void testByteBufferPositionUnchanged() {
        ByteBuffer buffer = ByteBuffer.wrap(ABC_STR.getBytes(StandardCharsets.UTF_8));
        buffer.position(1);
        assertEquals("bc", ByteArray.createByteArray(buffer).toString());
        assertEquals(1, buffer.position());

        assertEquals("xbc", new ByteArray("x").append(buffer).toString());
        assertEquals(1, buffer.position());
        assertEquals(2, buffer.remaining());
    }


    /**
     * Bytes appended after chopping or trimming at the right are read
     */
    @Test
    @SuppressWarnings("resource")
    public void testReadAfterChopRight() {
        ByteArray array = new ByteArray(ABC_STR);
        ByteBuffer dst = ByteBuffer.allocate(10);
        assertEquals(3, array.read(dst));
        array.chopRight(2).append("yz");
        dst.clear();
        assertEquals(2, array.read(dst));
        assertEquals("yz", new String(dst.array(), 0, dst.position(), StandardCharsets.UTF_8));

        array = new ByteArray("a   ");
        dst.clear();
        assertEquals(4, array.read(dst));
        array.trimRight((byte) ' ').append(X_STR);
        dst.clear();
        assertEquals(1, array.read(dst));
        assertEquals(X_STR, new String(dst.array(), 0, dst.position(), StandardCharsets.UTF_8));
    }


    /**
     * Empty instances behave the same, independent of their capacity
     */
    @Test
    public void testEmptyIndependentOfCapacity() {
        ByteArray small = new ByteArray("");
        ByteArray large = new ByteArray();
        ByteArray erased = new ByteArray(ABC_STR).erase();
        assertEquals(small, large);
        assertEquals(large, erased);
        assertEquals(small.indexOf(ByteArray.EMPTY), large.indexOf(ByteArray.EMPTY));
        assertEquals(-1, large.indexOf(ByteArray.EMPTY));
        assertEquals(-1, erased.indexOf(new ByteArray(ABC_STR), 0));
        assertEquals(-1, erased.indexOf((byte) 'a'));
        assertEquals(-1, erased.lastIndexOf((byte) 'a'));
        assertFalse(erased.startsWith((byte) 'a'));
        assertEquals(small.hashCode(), erased.hashCode());
    }
}
