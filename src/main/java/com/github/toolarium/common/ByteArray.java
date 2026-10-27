/*
 * ByteArray.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common;

import com.github.toolarium.common.util.ByteUtil;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;


/**
 * Class used to represent an array of bytes as an Object.
 *
 * <p>The predefined constants ({@link #NL}, {@link #CR}, {@link #TAB}, {@link #SPACE}, {@link #DOT} and {@link #EMPTY})
 * are read-only: all methods which would modify them throw an {@link UnsupportedOperationException}. Methods which
 * return a copy (e.g. {@link #getBytes()}, {@link #clone()}, {@link #concat(ByteArray)}) return a modifiable instance.</p>
 *
 * @author patrick
 */
public class ByteArray 
    implements Serializable, WritableByteChannel, ReadableByteChannel, Cloneable {
    
    /** The new line character as byte array (read-only) */
    public static final ByteArray NL = new ImmutableByteArray("\n");

    /** The cr character as byte array (read-only) */
    public static final ByteArray CR = new ImmutableByteArray("\r");

    /** The tab character as byte array (read-only) */
    public static final ByteArray TAB = new ImmutableByteArray("\t");

    /** The space as byte array (read-only) */
    public static final ByteArray SPACE = new ImmutableByteArray(" ");

    /** The dot character as byte array (read-only) */
    public static final ByteArray DOT = new ImmutableByteArray(".");

    /** The empty byte array (read-only) */
    public static final ByteArray EMPTY = new ImmutableByteArray("");
    
    /** Defines the default byte array size */
    public static final int DEFAULT_BLOCK_SIZE = 96;

    /** The serial version */
    private static final long serialVersionUID = 3907211563424559664L;

    /** The maximum array size, some JVMs reserve header words in an array */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /** The names of the serialized fields */
    private static final String FIELD_BYTES = "bytes";
    private static final String FIELD_LENGTH = "length";
    private static final String FIELD_IS_OPEN = "isOpen";
    private static final String FIELD_READ_POSITION = "readPosition";

    private byte[] bytes;
    private int length = 0;
    private boolean isOpen; //nio
    private int readPosition = 0; //nio


    /**
     * Create a ByteArray with a specific default size
     * 
     * @param size the size of the byte array (default: 96)
     */
    private ByteArray(int size) {
        bytes = new byte[size];
        isOpen = true;
    }    
    
    
    /**
     * Create a ByteArray which takes over the given data without copying it
     *
     * @param data the data
     * @param takeOver only used to distinguish this constructor from the public one
     */
    private ByteArray(byte[] data, boolean takeOver) {
        bytes = data;
        length = data.length;
        isOpen = true;
    }


    /**
     * Create a ByteArray with the default size
     */
    public ByteArray() {
        this(DEFAULT_BLOCK_SIZE);
    }

    
    /**
     * Create a ByteArray from a String
     * 
     * @param data initial data
     */
    public ByteArray(String data) {
        this((data == null) ? DEFAULT_BLOCK_SIZE : data.length()); // CHECKSTYLE IGNORE THIS LINE

        if (data != null) {
            append(data);
        }
    }
    
    /**
     * Create a ByteArray from a String
     * @param data initial data
     */
    public ByteArray(StringBuilder data) {
        this((data == null) ? DEFAULT_BLOCK_SIZE : data.length()); // CHECKSTYLE IGNORE THIS LINE

        if (data != null) {
            append(data);
        }
    }
    
    /**
     * Create a ByteArray from a String
     * 
     * @param data initial data
     */
    public ByteArray(StringBuffer data) {
        this((data == null) ? DEFAULT_BLOCK_SIZE : data.length()); // CHECKSTYLE IGNORE THIS LINE

        if (data != null) {
            append(data);
        }
    }    

    
    /**
     * Copy constructor
     * @param data initial data
     */
    public ByteArray(ByteArray data) {
        this((data == null) ? DEFAULT_BLOCK_SIZE : data.length()); // CHECKSTYLE IGNORE THIS LINE

        if (data != null) {
            append(data);
        }
    }

    
    /**
     * Create a ByteArray from an array of bytes
     * 
     * @param data initial data
     */
    public ByteArray(byte[] data) {
        this((data == null) ? DEFAULT_BLOCK_SIZE : data.length); // CHECKSTYLE IGNORE THIS LINE

        if (data != null) {
            append(data);
        }
    }

    
    /**
     * Creates a byte array with the given size
     * 
     * @param size the size
     * @return the byte array with the given size
     */
    public static ByteArray createByteArrayWithSize(int size) {
        if (size <= 1) {
            return new ByteArray(1);
        }
        
        return new ByteArray(size);
    }
    
    
    /**
     * Creates a byte array with a simple byte
     * 
     * @param b the byte
     * @param size the size of the byte array
     * @return the created byte array
     */
    public static ByteArray createByteArray(byte b, int size) {
        ByteArray newArray;
        if (size <= 1) {
            newArray = new ByteArray(1);
            newArray.bytes[0] = b;
            newArray.length = 1;
            newArray.isOpen = true;
        } else {
            newArray = new ByteArray(size);
            newArray.append(b);
        }

        return newArray;
    }
    
    
    /**
     * Creates a byte array from the remaining bytes of the given buffer. The position of the given buffer is not changed.
     *
     * @param buffer the byte buffer
     * @return the created byte array
     */
    public static ByteArray createByteArray(ByteBuffer buffer) {
        if (buffer == null) {
            return null;
        }

        int count = buffer.remaining();

        if (count <= 0) {
            return null;
        }

        ByteArray newArray = new ByteArray(count);
        newArray.write(buffer.duplicate());
        return newArray;
    }    
    
    

    
    /**
     * Append a byte
     * 
     * @param ch the data
     * @return the byte array (no copy)
     */
    public ByteArray append(byte ch) {
        crowUpDataSize(1);
        bytes[length++] = ch;
        return this;
    }

    
    /**
     * Append a ByteArray
     * 
     * @param data the data
     * @return the byte array (no copy)
     */
    public ByteArray append(ByteArray data) {
        crowUpDataSize(data.length());
        System.arraycopy(data.bytes, 0, bytes, length, data.length());
        length += data.length();
        return this;
    }

    
    /**
     * Append an array of bytes
     * 
     * @param data the data
     * @return the byte array (no copy)
     */
    public ByteArray append(byte[] data) {
        if (data == null) {
            return this;
        }

        crowUpDataSize(data.length);
        System.arraycopy(data, 0, bytes, length, data.length);
        length += data.length;
        return this;
    }

    
    /**
     * Append an array of bytes
     * 
     * @param data the data
     * @param off the offset
     * @param len the length
     * @return the byte array (no copy)
     */
    public ByteArray append(byte[] data, int off, int len) {
        crowUpDataSize(len);
        System.arraycopy(data, off, bytes, length, len);
        length += len;
        return this;
    }

    
    /**
     * Append a String (UTF-8 encoded)
     * 
     * @param data the data
     * @return the byte array (no copy)
     */
    public ByteArray append(String data) {
        if (data != null) {
            append(data.getBytes(StandardCharsets.UTF_8));
        }

        return this;
    }
    
    
    /**
     * Append a StringBuilder
     * 
     * @param data the data
     * @return the byte array (no copy)
     */
    public ByteArray append(StringBuilder data) {
        if (data != null) {
            append(data.toString().getBytes(StandardCharsets.UTF_8));
        }
        
        return this;
    }

    
    /**
     * Append a StringBuffer
     * 
     * @param data the data
     * @return the byte array (no copy)
     */
    public ByteArray append(StringBuffer data) {
        if (data != null) {
            append(data.toString().getBytes(StandardCharsets.UTF_8));
        }

        return this;
    }
    
    
    /**
     * Append the remaining bytes of a buffer. The position of the given buffer is not changed.
     *
     * @param data the data
     * @return the byte array (no copy)
     */
    public ByteArray append(ByteBuffer data) {
        ByteBuffer source = data.duplicate();
        while (source.hasRemaining()) {
            int size = source.remaining();
            byte[] d = new byte[size];
            source.get(d, 0, size);
            append(d);
        }

        return this;
    }    

    
    /**
     * Reverse the data. This byte array is not modified.
     * 
     * @return a new byte array with the reversed data (copy)
     */
    public ByteArray reverse() {
        byte[] reversed = new byte[length];
        for (int i = 0; i < length; i++) {
            reversed[i] = bytes[length - 1 - i];
        }

        ByteArray array = new ByteArray(length);
        System.arraycopy(reversed, 0, array.bytes, 0, length);
        array.length = length;
        return array;
    }
    
    
    /**
     * Trims the data with the given byte
     * 
     * @param ch the trim byte 
     * @return the byte array (no copy)
     */
    public ByteArray trim(byte ch) {
        trimRight(ch);
        trimLeft(ch);
        return this;
    }    

    
    /**
     * Trims the data right with the given byte
     * 
     * @param ch the trim byte 
     * @return the byte array (no copy)
     */
    public ByteArray trimRight(byte ch) {
        if (length == 0) {
            return this;
        }
        
        int count = 0;
        for (int i = length - 1; i >= 0; i--) {
            if (bytes[i] == ch) {
                count++;
            } else {
                break;
            }
        }

        chopRight(count);
        return this;
    }
    
    
    /**
     * Trims the data left with the given byte
     * 
     * @param ch the trim byte
     * @return the byte array (no copy)
     */
    public ByteArray trimLeft(byte ch) {
        if (length == 0) {
            return this;
        }
        
        int count = 0;
        for (int i = 0; i < length; i++) {
            if (bytes[i] == ch) {
                count++;
            } else {
                break;
            }
        }

        if (count == length) {
            shrink(0);
        } else {
            ByteArray trimmed = getBytes(count, length);
            // wipe the old data, it is replaced by the trimmed copy
            Arrays.fill(bytes, 0, length, (byte) 0);
            length = trimmed.length;
            bytes = trimmed.bytes;
        }

        readPosition = Math.max(0, readPosition - count);
        return this;
    }    
    
    
    /**
     * Replaces all occurrences of a given byte with another. This byte array is modified.
     * 
     * @param toReplace the byte to replace
     * @param newByte the new byte
     * @return this byte array (no copy)
     */
    public ByteArray replace(byte toReplace, byte newByte) {
        if (length == 0) {
            return this;
        }

        for (int i = 0; i < length; i++) {
            if (bytes[i] == toReplace) {
                bytes[i] = newByte;
            }
        }

        return this;
    }
    
    
    /**
     * Replaces all occurrences of the given data. This byte array is not modified.
     * 
     * @param replaceData the data to replace
     * @param newData the new data
     * @return a new byte array with the replaced data (copy); this byte array itself if replaceData is null or empty or newData is null
     */
    public ByteArray replace(ByteArray replaceData, ByteArray newData) {
        if ((replaceData == null) || (replaceData.length() == 0)) {
            return this;
        }

        if (newData == null) {
            return this;
        }

        // estimate capacity to reduce reallocations
        ByteArray workData = new ByteArray(length + newData.length);
        int maxLength = length();
        int len = replaceData.length();

        int start = 0;
        int end = ByteUtil.getInstance().indexOf(bytes, 0, length, replaceData.bytes, 0, replaceData.length, 0);
        while (start >= 0 && end >= 0) {
            if (end > start) {
                workData.append(bytes, start, end - start);
            }
            workData.append(newData);
            start = end + len;

            end = ByteUtil.getInstance().indexOf(bytes, 0, length, replaceData.bytes, 0, replaceData.length, start);
        }

        if (start < maxLength) {
            workData.append(bytes, start, maxLength - start);
        }
        return workData;
    }

    
    /**
     * Finds the first occurrence of a byte in the data
     * 
     * @param toFind the byte to find
     * @return the position in the data or -1 if it is not found
     */
    public int indexOf(byte toFind) {
        if (length == 0) {
            return -1;
        }

        for (int i = 0; i < length; i++) {
            if (bytes[i] == toFind) {
                return i;
            }
        }

        return -1;
    }
    
    
    /**
     * Returns the index within this byte array of the first occurrence of the specified sub array.
     * 
     * @param toFind the sub array for which to search.
     * @return the index within this array of the first occurrence of the specified sub array or -1 if it is not found.
     */
    public int indexOf(ByteArray toFind) {
        if (length == 0) {
            return -1;
        }

        return indexOf(toFind, 0);
    }

    
    /**
     * Returns the index within this byte array of the first occurrence of the specified sub array, starting at the specified index.
     * 
     * @param toFind the sub array for which to search.
     * @param fromIndex the index from which to start the search.
     * @return the index within this array of the first occurrence of the specified sub array, starting at the specified index, or -1 if it is not found.
     */
    public int indexOf(ByteArray toFind, int fromIndex) {
        if (length == 0) {
            return -1;
        }
        return ByteUtil.getInstance().indexOf(bytes, 0, length, toFind.bytes, 0, toFind.length, fromIndex);
    }
    
    
    /**
     * Finds the last occurrence of a byte in the data
     * 
     * @param toFind the byte to find
     * @return the position in the data or -1 if it is not found
     */
    public int lastIndexOf(byte toFind) {
        if (length == 0) {
            return -1;
        }
        
        for (int i = length - 1; i >= 0; i--) {
            if (bytes[i] == toFind) {
                return i;
            }
        }

        return -1;
    }
    
    
    /**
     * Test if the array starts with the given prefix
     * 
     * @param prefix the bytes to test
     * @return true if it starts with the prefix
     */
    public boolean startsWith(ByteArray prefix) {
        return startsWith(prefix, 0);
    }    
    
    
    /**
     * Test if the array starts with the given prefix
     * 
     * @param prefix the bytes to test
     * @param toffset the offset to start
     * @return true if it starts with the prefix
     */
    public boolean startsWith(ByteArray prefix, int toffset) {
        if (prefix == null || prefix.length == 0) {
            return false;
        }

        if (length == 0) {
            return false;
        }

        byte[] ta = bytes;
        int to = toffset;
        byte[] pa = prefix.bytes;
        int po = 0;
        int pc = prefix.length;

        // Note: toffset might be near -1>>>1.
        if ((toffset < 0) || (toffset > length - pc)) {
            return false;
        }

        while (--pc >= 0) {
            if (ta[to++] != pa[po++]) {
                return false;
            }
        }

        return true;
    }
    
    
    /**
     * Test if the array starts with the given prefix
     * 
     * @param prefix the bytes to test
     * @return true if it starts with the prefix
     */
    public boolean startsWith(byte prefix) {
        if (length == 0) {
            return false;
        }

        return bytes[0] == prefix;
    }    
    
    
    /**
     * Test how many times the given prefix is contained at the beginning
     * 
     * @param prefix the bytes to test
     * @return how many times the prefix was found at the beginning 
     */
    public int countStartsWith(ByteArray prefix) {
        if (prefix == null || prefix.length == 0) {
            return 0;
        }

        if (length == 0) {
            return 0;
        }

        int idx = prefix.length();
        int counter = 0;
        int offset = 0;

        while (idx > 0 && (startsWith(prefix, offset))) {
            counter++;
            offset += prefix.length;
        }

        return counter;
    }
    
    
    /**
     * Test if the array ends with the given suffix
     * 
     * @param prefix the byte to test
     * @return true if it ends with the suffix
     */
    public boolean endsWith(byte prefix) {
        if (length <= 0) {
            return false;
        }

        return bytes[length - 1] == prefix;
    }
    
    
    /**
     * Test if the array ends with the given suffix
     * 
     * @param prefix the bytes to test
     * @return true if it ends with the suffix
     */
    public boolean endsWith(ByteArray prefix) {
        if (prefix == null || prefix.length == 0) {
            return false;
        }

        if (length <= 0) {
            return false;
        }

        return startsWith(prefix, length - prefix.length);
    }
    
    
    /**
     * Test how many times the given prefix is contained at the end
     * 
     * @param prefix the bytes to test
     * @return how many times the prefix was found at the end 
     */
    public int countEndsWith(ByteArray prefix) {
        if (prefix == null || prefix.length == 0) {
            return 0;
        }

        if (length == 0) {
            return 0;
        }

        int idx = prefix.length();
        int counter = 0;
        int offset = length - prefix.length;

        while (idx > 0 && (startsWith(prefix, offset))) {
            counter++;
            offset -= prefix.length;
        }

        return counter;
    }
    
    
    /**
     * Convert to hex string
     * 
     * @return the byte array as hex string
     */
    public String toHex() {
        StringBuilder hexNumber = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {
            int b = bytes[i] & 0xFF;
            if (b < 0x10) {
                hexNumber.append('0');
            }
            hexNumber.append(Integer.toHexString(b).toUpperCase(Locale.ROOT));
        }

        return hexNumber.toString();
    }

    
    /**
     * Convert to String (UTF-8 decoded)
     * 
     * @return the byte array as String
     */
    @Override
    public String toString() {
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }
    
    
    /**
     * Return the bytes as a copy of the original
     * 
     * @return the bytes (copy)
     */
    public byte[] toBytes() {
        byte[] data = new byte[length];
        System.arraycopy(bytes, 0, data, 0, length);
        return data;
    }    
    
    
    /**
     * Build a long from the first (up to) 8 valid bytes of the array (spare capacity of the internal buffer is ignored).
     * The bytes are interpreted as a big-endian two's-complement number, shorter arrays are sign-extended
     * (consistent with ByteUtil#toLong(byte[])).
     *
     * @return the long value or 0 in case the array is empty.
     */
    public long toLong() {
        if (length == 0) {
            return 0;
        }

        byte[] data = new byte[Math.min(length, 8)];
        System.arraycopy(bytes, 0, data, 0, data.length);
        return new BigInteger(data).longValue();
    }
    
    
    /**
     * Return a byte buffer of the data. The buffer is backed by the internal array (no copy): changes of the buffer
     * are visible in this byte array and vice versa. After the internal array grows, the buffer no longer reflects
     * the data of this byte array. For read only constants, a read only buffer is returned.
     * 
     * @return the buffer
     */
    public ByteBuffer toByteBuffer() {
        // slice: the capacity is the length, so the unused part of the internal array is not accessible
        return ByteBuffer.wrap(bytes, 0, length).slice();
    }
    
    
    /**
     * Return a sub part of the original bytes
     * 
     * @param startPos the start position, a start position beyond the length returns an empty array
     * @param endPos the end position
     * @return the bytes (copy)
     */
    public ByteArray toByteArray(int startPos, int endPos) {
        int start = 0;
        int end = length();

        if (startPos < 0) {
            start = 0;
        }
        
        if (startPos >= 0 && startPos <= length()) {
            start = startPos;
        } else if (startPos > length()) {
            start = length();
        }

        if (endPos < 0) {
            end = 0;
        } else if (endPos >= 0 && endPos <= length()) {
            end = endPos;
        }

        ByteArray array = null;
        if (start < end) {
            int len = end - start;
            array = new ByteArray(len + 1);
            array.isOpen = isOpen;
            array.length = len;
            System.arraycopy(bytes, start, array.bytes, 0, len);
        } else {
            array = new ByteArray();
        }

        return array;
    }    
    
    
    /**
     * Return the bytes as a copy
     * 
     * @return the bytes (copy)
     */
    public ByteArray getBytes() {
        return new ByteArray(toBytes());
    }    
    
    
    /**
     * Return the bytes as a copy
     * 
     * @param startPos the start position, a start position beyond the length returns an empty array
     * @param endPos the end position
     * @return the bytes (copy)
     */
    public ByteArray getBytes(int startPos, int endPos) {
        int start = 0;
        int end = length();

        if (startPos < 0) {
            start = 0;
        }
        
        if (startPos >= 0 && startPos <= length()) {
            start = startPos;
        } else if (startPos > length()) {
            start = length();
        }

        if (endPos < 0) {
            end = 0;
        } else if (endPos >= 0 && endPos <= length()) {
            end = endPos;
        }

        ByteArray array;
        if (start < end) {
            int len = end - start;
            array = new ByteArray(len + 1);

            System.arraycopy(bytes, start, array.bytes, 0, len);
            array.length = len;
        } else {
            array = new ByteArray();
        }

        return array;
    }

    
    /**
     * Concat a given byte and return a new byte array
     * 
     * @param b the byte to append
     * @return the concated byte array (copy)
     */
    public ByteArray concat(byte b) {
        byte[] r = toBytes();
        ByteArray array = new ByteArray(r.length + 1);
        array.append(r);
        array.append(b);
        return array;
    }
    
    
    /**
     * Concat a given byte and return a new byte array
     * @param b the byte to append
     * @return the concated byte array (copy)
     */
    public ByteArray concat(ByteArray b) {
        byte[] r = toBytes();
        ByteArray array = new ByteArray(r.length + b.length());
        array.append(r);
        array.append(b);
        return array;
    }    

    
    /**
     * Return a specific byte from the byte array
     * 
     * @param pos the position in the byte array
     * @return a byte
     * @exception ArrayIndexOutOfBoundsException in case of error
     */
    public byte get(int pos) throws ArrayIndexOutOfBoundsException {
        if (pos < length()) {
            return bytes[pos];
        }
        
        throw new ArrayIndexOutOfBoundsException("Invalid position at " + pos + " (length:" + length + ")!");
    }
    
    
    /**
     * Set a specific byte in the byte array
     * 
     * @param pos the position in the byte array
     * @param b the byte to set in the byte array
     * @return the replaced byte
     * @exception ArrayIndexOutOfBoundsException in case of error
     */
    public byte set(int pos, byte b) throws ArrayIndexOutOfBoundsException {
        if (pos < length()) {
            byte result = bytes[pos];
            bytes[pos] = b;
            return result;
        }
        throw new ArrayIndexOutOfBoundsException("Invalid position at " + pos + "!");
    }

    
    /**
     * Return the number of bytes
     * 
     * @return the number of bytes
     */
    public int length() {
        return length;
    }

    
    /**
     * Erase the byte array
     * 
     * @return the byte array (no copy)
     */
    public ByteArray erase() {
        shrink(0);
        readPosition = 0;
        return this;
    }
    
    
    /**
     * Chop a byte
     * 
     * @return the byte array (no copy)
     */
    public ByteArray chopRight() {
        return chopRight(1);
    }

    
    /**
     * Chop some bytes
     * 
     * @param num number of bytes to chop, a negative number or zero chops nothing
     * @return the byte array (no copy)
     */
    public ByteArray chopRight(int num) {
        if (num <= 0) {
            return this;
        }

        shrink(Math.max(0, length - num));

        // keep the read position inside the data, so bytes appended later are read
        readPosition = Math.min(readPosition, length);
        return this;
    }
    
    
    /**
     * Chop a byte
     * 
     * @return the byte array (no copy)
     */
    public ByteArray chopLeft() {
        return chopLeft(1);
    }

    
    /**
     * Chop some bytes
     * 
     * @param num number of bytes to chop, a negative number or zero chops nothing
     * @return the byte array (no copy)
     */
    public ByteArray chopLeft(int num) {
        if (num <= 0) {
            return this;
        }

        if (num <= length) {
            System.arraycopy(bytes, num, bytes, 0, length - num);
            shrink(length - num);
            readPosition = Math.max(0, readPosition - num);
        } else {
            erase();
        }

        return this;
    }    

    
    /**
     * Appends the remaining bytes of the given buffer. A closed byte array accepts no data and returns 0,
     * check {@link #isOpen()} to detect it.
     *
     * @param src the buffer from which bytes are to be retrieved
     * @return the number of bytes written, 0 if the buffer is null or empty or the channel is closed
     * @see java.nio.channels.WritableByteChannel#write(java.nio.ByteBuffer)
     */
    @Override
    public int write(ByteBuffer src) {
        if (!isOpen || src == null || !src.hasRemaining()) {
            return 0;
        }

        int size = src.remaining();
        if (size <= 0) {
            return 0;
        }

        byte[] d = new byte[size];
        src.get(d);
        append(d);
        return size;
    }

    
    /**
     * @see java.nio.channels.Channel#close()
     */
    @Override
    public void close() {
        isOpen = false;
    }

    
    /**
     * @see java.nio.channels.Channel#isOpen()
     */
    @Override
    public boolean isOpen() {
        return isOpen;
    }


    /**
     * Reads a sequence of bytes from this byte array into the given buffer. The byte array keeps a read position:
     * every call transfers at most {@code dst.remaining()} bytes starting at the current read position and advances it.
     * Once all bytes have been read (or the channel is closed) -1 is returned. The read position is reset by {@link #erase()}.
     *
     * @param dst the buffer into which bytes are to be transferred
     * @return the number of bytes read, possibly zero, or -1 if the end of the data is reached or the channel is closed
     * @see java.nio.channels.ReadableByteChannel#read(java.nio.ByteBuffer)
     */
    @Override
    public int read(ByteBuffer dst) {
        if (!isOpen || readPosition >= length) {
            return -1;
        }

        if (dst == null || !dst.hasRemaining()) {
            return 0;
        }

        int count = Math.min(dst.remaining(), length - readPosition);
        dst.put(bytes, readPosition, count);
        readPosition += count;
        return count;
    }
    
    
    /**
     * Implements the default functionality of the equals method.
     * Subclasses should implements their own equals method like:<br>
     * <pre>public boolean equals( MyObject other )
     * {
     *     if( this==other )
     *         return true;
     *
     *     if( !super.equals( other ) )
     *         return false;
     *
     *     if( x !=((MyObject)other).x )
     *         return false;
     *
     *     ...
     *     return true;
     * }</pre>
     * @param other the object to compare
     * @return true if the objects are equals
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (other == null) {
            return false;
        }

        if (getComparableClass(other) != getComparableClass(this)) {
            return false;
        }

        ByteArray b2 = (ByteArray) other;
        if (length() != b2.length()) {
            return false;
        }

        byte[] bytes2 = b2.bytes;
        for (int i = 0; i < length(); i++) {
            if (bytes[i] != bytes2[i]) {
                return false;
            }
        }
        return true;
    }           
    
    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (length == 0) {
            return 0;
        }
        
        int off = 0;
        int len = length;
        int hash = 0;

        for (int i = 0; i < len; i++) {
            hash = 31 * hash + bytes[off++];
        }

        return hash;
    }    
    
    
    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public ByteArray clone() {
        return new ByteArray(this);
    }
        

    /**
     * Gets the additional size
     * 
     * @param requestedSpace the requested space
     */
    protected void crowUpDataSize(int requestedSpace) {
        if ((bytes.length - length) < requestedSpace) {
            byte[] tmpbytes = bytes;
            bytes = new byte[getNewDataSize(requestedSpace)];
            System.arraycopy(tmpbytes, 0, bytes, 0, length);
        }
    }    
    
    
    /**
     * Gets the new data size: the used length plus the requested space, at least the doubled used length,
     * so that many small appends stay fast
     *
     * @param requestedSpace the requested space
     * @return the new size
     * @throws OutOfMemoryError if the required size exceeds the maximum array size
     */
    protected int getNewDataSize(int requestedSpace) {
        // the size is calculated from the used length and not from the capacity of the internal array
        long size = length;

        // the minimum new space
        long minLen = requestedSpace + size;
        if (minLen > MAX_ARRAY_SIZE) {
            throw new OutOfMemoryError("Required array size too large: " + minLen);
        }

        // allocate in block size
        long newLen = minLen + DEFAULT_BLOCK_SIZE - (minLen % DEFAULT_BLOCK_SIZE);

        // grow at least by doubling
        newLen = Math.max(newLen, size * 2);
        if (newLen > MAX_ARRAY_SIZE) {
            newLen = MAX_ARRAY_SIZE;
        }

        return (int) newLen;
    }


    /**
     * Get the class used for the comparison in the equals method: the read-only constants are equal to a normal byte array with the same content.
     *
     * @param o the object
     * @return the class to compare
     */
    private static Class<?> getComparableClass(Object o) {
        if (o instanceof ImmutableByteArray) {
            return ByteArray.class;
        }

        return o.getClass();
    }


    /**
     * Shrinks the length of the data. The freed bytes are overwritten with 0, so that removed data (e.g. a password)
     * does not stay in the internal array.
     *
     * @param newLength the new length, not greater than the current length
     */
    private void shrink(int newLength) {
        if (newLength < length) {
            Arrays.fill(bytes, newLength, length, (byte) 0);
        }

        length = newLength;
    }


    /**
     * Writes the state, the internal array is written only up to the length, so that the unused part is not serialized.
     *
     * @param out the object output stream
     * @throws IOException if an I/O error occurs
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        final ObjectOutputStream.PutField fields = out.putFields();
        fields.put(FIELD_BYTES, Arrays.copyOf(bytes, length));
        fields.put(FIELD_LENGTH, length);
        fields.put(FIELD_IS_OPEN, isOpen);
        fields.put(FIELD_READ_POSITION, readPosition);
        out.writeFields();
    }


    /**
     * Reads and validates the state: the internal array must exist, the length must be within the internal array
     * and the read position within the length.
     *
     * @param in the object input stream
     * @throws InvalidObjectException if the state is not valid
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the class of a serialized object can not be found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        final ObjectInputStream.GetField fields = in.readFields();
        final Object readBytes = fields.get(FIELD_BYTES, null);
        final int readLength = fields.get(FIELD_LENGTH, 0);
        final boolean readIsOpen = fields.get(FIELD_IS_OPEN, false);
        final int readReadPosition = fields.get(FIELD_READ_POSITION, 0);

        if (!(readBytes instanceof byte[])) {
            throw new InvalidObjectException("Invalid byte array: the data is missing!");
        }

        final byte[] data = (byte[]) readBytes;
        if (readLength < 0 || readLength > data.length) {
            throw new InvalidObjectException("Invalid byte array: the length " + readLength + " is not between 0 and " + data.length + "!");
        }

        if (readReadPosition < 0 || readReadPosition > readLength) {
            throw new InvalidObjectException("Invalid byte array: the read position " + readReadPosition + " is not between 0 and " + readLength + "!");
        }

        bytes = data;
        length = readLength;
        isOpen = readIsOpen;
        readPosition = readReadPosition;
    }


    /**
     * Read-only byte array which is used for the shared constants. All modifying methods throw an {@link UnsupportedOperationException}.
     *
     * @author patrick
     */
    private static final class ImmutableByteArray extends ByteArray {
        private static final long serialVersionUID = -2335627781208740913L;
        private static final String READ_ONLY_MESSAGE = "The byte array is read-only!";


        /**
         * Constructor for ImmutableByteArray
         *
         * @param data the data
         */
        ImmutableByteArray(String data) {
            super(data.getBytes(StandardCharsets.UTF_8), true);
        }


        /**
         * Create the read-only exception.
         *
         * @return the exception
         */
        private static UnsupportedOperationException readOnly() {
            return new UnsupportedOperationException(READ_ONLY_MESSAGE);
        }


        /**
         * @see com.github.toolarium.common.ByteArray#append(byte)
         */
        @Override
        public ByteArray append(byte ch) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#append(com.github.toolarium.common.ByteArray)
         */
        @Override
        public ByteArray append(ByteArray data) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#append(byte[])
         */
        @Override
        public ByteArray append(byte[] data) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#append(byte[], int, int)
         */
        @Override
        public ByteArray append(byte[] data, int off, int len) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#append(java.lang.String)
         */
        @Override
        public ByteArray append(String data) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#append(java.lang.StringBuilder)
         */
        @Override
        public ByteArray append(StringBuilder data) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#append(java.lang.StringBuffer)
         */
        @Override
        public ByteArray append(StringBuffer data) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#append(java.nio.ByteBuffer)
         */
        @Override
        public ByteArray append(ByteBuffer data) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#trim(byte)
         */
        @Override
        public ByteArray trim(byte ch) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#trimRight(byte)
         */
        @Override
        public ByteArray trimRight(byte ch) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#trimLeft(byte)
         */
        @Override
        public ByteArray trimLeft(byte ch) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#replace(byte, byte)
         */
        @Override
        public ByteArray replace(byte toReplace, byte newByte) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#set(int, byte)
         */
        @Override
        public byte set(int pos, byte b) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#erase()
         */
        @Override
        public ByteArray erase() {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#chopRight()
         */
        @Override
        public ByteArray chopRight() {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#chopRight(int)
         */
        @Override
        public ByteArray chopRight(int num) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#chopLeft()
         */
        @Override
        public ByteArray chopLeft() {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#chopLeft(int)
         */
        @Override
        public ByteArray chopLeft(int num) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#write(java.nio.ByteBuffer)
         */
        @Override
        public int write(ByteBuffer src) {
            throw readOnly();
        }


        /**
         * The shared constants keep no read position; read from a copy (e.g. {@link #getBytes()}) instead.
         *
         * @see com.github.toolarium.common.ByteArray#read(java.nio.ByteBuffer)
         */
        @Override
        public int read(ByteBuffer dst) {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#close()
         */
        @Override
        public void close() {
            throw readOnly();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#toByteBuffer()
         */
        @Override
        public ByteBuffer toByteBuffer() {
            return super.toByteBuffer().asReadOnlyBuffer();
        }


        /**
         * @see com.github.toolarium.common.ByteArray#crowUpDataSize(int)
         */
        @Override
        protected void crowUpDataSize(int requestedSpace) {
            throw readOnly();
        }
    }
}
