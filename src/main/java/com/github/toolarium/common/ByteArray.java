/*
 * ByteArray.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common;

import com.github.toolarium.common.util.ByteUtil;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;


/**
 * Class used to represent an array of bytes as an Object.
 * 
 * @author patrick
 */
public class ByteArray 
    implements Serializable, WritableByteChannel, ReadableByteChannel, Cloneable {
    
    /** The new line character as byte array */
    public static final ByteArray NL = new ByteArray("\n");
    
    /** The cr character as byte array */
    public static final ByteArray CR = new ByteArray("\r");

    /** The tab character as byte array */
    public static final ByteArray TAB = new ByteArray("\t");
    
    /** The space as byte array */    
    public static final ByteArray SPACE = new ByteArray(" ");

    /** The dot character as byte array */    
    public static final ByteArray DOT = new ByteArray(".");

    /** The dot character as byte array */    
    public static final ByteArray EMPTY = new ByteArray(1);
    
    /** Defines the default byte array size */
    public static final int DEFAULT_BLOCK_SIZE = 96;

    /** The serial version */
    private static final long serialVersionUID = 3907211563424559664L;

    private byte[] bytes;
    private int length = 0;
    private boolean isOpen; //nio

    
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
     * Creates a byte array 
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
        newArray.write(buffer);
        buffer.rewind();
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
     * Append a String
     * 
     * @param data the data
     * @return the byte array (no copy)
     */
    public ByteArray append(String data) {
        if (data != null) {
            append(data.getBytes());
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
            append(data.toString().getBytes());
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
            append(data.toString().getBytes());
        }

        return this;
    }
    
    
    /**
     * Append a buffer
     * 
     * @param data the data
     * @return the byte array (no copy)
     */
    public ByteArray append(ByteBuffer data) {
        while (data.hasRemaining()) {
            int size = data.remaining();
            byte[] d = new byte[size];
            data.get(d, 0, size);
            append(d);
        }

        data.rewind();
        return this;
    }    

    
    /**
     * Reverse the internal array
     * 
     * @return the reversed byte array (no copy)
     */
    public ByteArray reverse() {
        ByteArray array = new ByteArray(length());
        for (int i = length() - 1; i >= 0; i--) {
            array.append(get(i));
        }
        
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
        if (bytes.length == 0) {
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
        if (bytes.length == 0) {
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
            length = 0;
        } else {
            ByteArray trimmed = getBytes(count, length);
            length = trimmed.length;
            bytes = trimmed.bytes;
        }

        return this;
    }    
    
    
    /**
     * Replaces a given byte with another
     * 
     * @param toReplace the byte to replace
     * @param newByte the new byte
     * @return the byte array (no copy)
     */
    public ByteArray replace(byte toReplace, byte newByte) {
        if (bytes.length == 0) {
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
     * Replaces data in a given byte array. 
     * 
     * @param replaceData the data to replace
     * @param newData the new data
     * @return the replaced byte array as a copy (copy)
     */
    public ByteArray replace(ByteArray replaceData, ByteArray newData) {
        if ((replaceData == null) || (replaceData.length() == 0)) {
            return this;
        }

        if (newData == null) {
            return this;
        }

        ByteArray workData = new ByteArray();
        int maxLength = length();
        int len = replaceData.length();
        
        int start = 0;
        int end = indexOf(replaceData);
        while (start >= 0 && end >= 0) {
            workData.append(getBytes(start, end));
            workData.append(newData);
            start = end + len;

            end = indexOf(replaceData, start);
        }

        if (start < maxLength) {
            workData.append(getBytes(start, maxLength));
        }
        return workData;
    }

    
    /**
     * Finds a byte in the data
     * 
     * @param toFind the byte to find
     * @return the position in the data
     */
    public int indexOf(byte toFind) {
        if (bytes.length == 0) {
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
     * Returns the index within this byte array of the first occurrence of the specified substring, starting at the specified index.
     * 
     * @param toFind the sub array for which to search.
     * @return the index within this array of the first occurrence of the specified sub array, starting at the specified index.
     */
    public int indexOf(ByteArray toFind) {
        if (bytes.length == 0) {
            return -1;
        }

        return indexOf(toFind, 0);
    }

    
    /**
     * Returns the index within this byte array of the first occurrence of the specified substring, starting at the specified index.
     * 
     * @param toFind the sub array for which to search.
     * @param fromIndex the index from which to start the search.
     * @return the index within this array of the first occurrence of the specified sub array, starting at the specified index.
     */
    public int indexOf(ByteArray toFind, int fromIndex) {
        if (bytes.length == 0) {
            return -1;
        }
        return ByteUtil.getInstance().indexOf(bytes, 0, length, toFind.bytes, 0, toFind.length, fromIndex);
    }
    
    
    /**
     * Finds a byte in the data
     * 
     * @param toFind the byte to find
     * @return the position in the data
     */
    public int lastIndexOf(byte toFind) {
        if (bytes.length == 0) {
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

        if (bytes.length == 0) {
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
        if (bytes.length == 0) {
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

        if (bytes.length == 0) {
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
     * Test if the array ends with the given prefix
     * 
     * @param prefix the bytes to test
     * @return true if it ends with the prefix
     */
    public boolean endsWith(byte prefix) {
        if (bytes.length == 0 || length <= 0) {
            return false;
        }

        return bytes[length - 1] == prefix;
    }
    
    
    /**
     * Test if the array starts with the given prefix
     * 
     * @param prefix the bytes to test
     * @return true if it starts with the prefix
     */
    public boolean endsWith(ByteArray prefix) {
        if (prefix == null || prefix.length == 0) {
            return false;
        }

        if (bytes.length == 0 || length <= 0) {
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

        if (bytes.length == 0) {
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
        StringBuffer hexNumber = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            String value = Long.toString(bytes[i] & 0xff, 16);
            if (value.length() == 1) {
                value = "0" + value;
            }

            value = value.toUpperCase();
            hexNumber.append(value);
        }

        return hexNumber.toString();        
    }

    
    /**
     * Convert to String
     * 
     * @return the byte array as String
     */
    @Override
    public String toString() {
        return new String(bytes, 0, length);
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
     * Build a long from  the array.
     *
     * @return the long value.
     */
    public long toLong() {
        return ByteUtil.getInstance().toLong(bytes);
    }
    
    
    /**
     * Return a byte buffer of the data
     * 
     * @return the buffer
     */
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(bytes, 0, length);
    }
    
    
    /**
     * Return a sub part of the original bytes
     * 
     * @param startPos the start position
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
     * @param startPos the start position
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
        length = 0;
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
     * @param num number of bytes to chop
     * @return the byte array (no copy)
     */
    public ByteArray chopRight(int num) {
        length -= num;

        if (length < 0) {
            length = 0;
        }

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
     * @param num number of bytes to chop
     * @return the byte array (no copy)
     */
    public ByteArray chopLeft(int num) {
        if (num <= length) {
            System.arraycopy(bytes, num, bytes, 0, length - num);
            length -= num;
        } else {
            erase();
        }

        return this;
    }    

    
    /**
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
     * @see java.nio.channels.ReadableByteChannel#read(java.nio.ByteBuffer)
     */
    @Override
    public int read(ByteBuffer dst) {
        if (!isOpen || dst == null || length() == 0) {
            return 0;
        }

        dst.put(bytes, 0, length); 
        return length;
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

        if (other.getClass() != this.getClass()) {
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
        if (bytes.length == 0) {
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
     * Gets the new data size
     * 
     * @param requestedSpace the requested space
     * @return the new size
     */
    protected int getNewDataSize(int requestedSpace) {
        int size = 0;

        if (bytes != null) {
            size = bytes.length;
        }
        
        // the minimum new space
        int minLen = requestedSpace + size;

        // allocate in block size
        int rest = DEFAULT_BLOCK_SIZE - (minLen % DEFAULT_BLOCK_SIZE);
        return minLen + rest;
    }

    
}
