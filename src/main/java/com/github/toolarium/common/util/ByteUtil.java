/*
 * ByteUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import com.github.toolarium.common.ByteArray;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;


/**
 * Byte utitlity.
 * 
 * @author patrick
 */
public final class ByteUtil {
    
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
        static final ByteUtil INSTANCE = new ByteUtil();
    }
    

    /**
     * Constructor
     */
    private ByteUtil() {
        // NOP
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static ByteUtil getInstance() {
        return HOLDER.INSTANCE;
    }
    
    
    /**
     * Check two blocks for equality.
     * The specified sub blocks of the given byte arrays are checked for equality.
     *
     * @param dataA the first byte array to be compared
     * @param offsetDataA the offset indicating the start position of the sub-block of <code>a</code>
     * @param dataB the second byte array to be compared
     * @param offsetDataB the offset indicating the start position of the sub-block of <code>b</code> 
     * @param len the number of bytes to be compared
     * @return <code>true</code>, if the two sub-blocks are equal, 
     *         <code>false</code> otherwise
     */
    public boolean equalsBlock(byte[] dataA, int offsetDataA, byte[] dataB, int offsetDataB, int len) {
        try {
            for (int i = 0; i < len; ++i) {
                if (dataA[offsetDataA + i] != dataB[offsetDataB + i]) {
                    return false;
                }
            }
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

 
    /**
     * Check two blocks for equality.
     *
     * @param a the first byte array to be compared
     * @param b the second byte array to be compared
     * @return <code>true</code>, if the two blocks are equal, 
     *         <code>false</code> otherwise
     */
    public boolean equalsBlock(byte[] a, byte[] b) {
        if (a.length == b.length) {
            return equalsBlock(a, 0, b, 0, a.length);
        }
        return false;
    }

   
    /**
     * Convert the given string with hex values to a byte array. For example "001122" is turned into {0, 0x11, 0x22}. All characters
     * outside the range of '0'-'9', 'a'-'z', and 'A'-'Z' or simply ignored.
     * 
     * @param s the string to convert
     * @return the byte array
     */
    public byte[] toByteArray(String s) {
        int n = s.length();
        int b1 = -1;
        int b2 = -1;

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int i = 0; i < n; i++) {
            if (b1 == -1) {
                b1 = toByte(s.charAt(i), 16);
            } else {
                b2 = toByte(s.charAt(i), 16);
            }

            if ((b1 != -1) && (b2 != -1)) {
                bout.write((b1 << 4) | b2);
                b1 = -1;
                b2 = -1;
            }
        }

        return bout.toByteArray();
    }  

    
    /**
     * Convert the given char to an int. '0' maps to 0, 'a' and 'A' to 10, 'z' and 'Z' to 36, and so on. For all other characters and for
     * resulting values equal to or larger than radix -1 is returned.
     * 
     * @param c the string to convert
     * @param radix the radix
     * @return the converted byte
     */
    public int toByte(char c, int radix) {
        int b;
        if ((c >= '0') && (c <= '9')) {
            b = (c - '0');
        } else if ((c >= 'A') && (c <= 'Z')) {
            b = (c - 'A' + 10);
        } else if ((c >= 'a') && (c <= 'z')) {
            b = (c - 'a' + 10);
        } else {
            b = -1;
        }

        if ((b < 0) || (b >= radix)) {
            return -1;
        }

        return b;
    }

    
    /**
     * Copies the specified byte sequence of the given source array to the specified destination array.
     * Beginning at the specified <code>srcOff</code> position, <code>len</code> bytes
     * of the source array are copied to the given destination array, starting at 
     * <code>dstOff</code>.
     *
     * @param src the source byte array
     * @param srcOff the offset indicating the start position within the first byte array; the 
     *        following <code>len</code> bytes will be copied to the destination array
     * @param dst the destination array to which to copy the bytes
     * @param dstOff the offset indicating the start position within the destination byte 
     *        array, to which the bytes shall be copied
     * @param len the number of bytes to be copied 
     */
    public void copyBlock(byte[] src, int srcOff, byte[] dst, int dstOff, int len) {
        for (int i = 0; i < len; i++) {
            dst[dstOff + i] = src[srcOff + i];
        }
    }

    
    /**
     * Copies one byte block to another.
     * 
     * @param src the source byte array
     * @param dst the destination array to which to copy the bytes
     */
    public void copyBlock(byte[] src, byte[] dst) {
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i];
        }
    }

    
    /**
     * Copies the specified int sequence of the given source array to the specified destination array.
     * Beginning at the specified <code>srcOff</code> position, <code>len</code> ints
     * of the source array are copied to the given destination array, starting at 
     * <code>dstOff</code>.
     *
     * @param src the source int array
     * @param srcOff the offset indicating the start position within the first int array; the 
     *        following <code>len</code> ints will be copied to the destination array
     * @param dst the destination array to which to copy the ints
     * @param dstOff the offset indicating the start position within the destination int 
     *        array, to which the ints shall be copied
     * @param len the number of ints to be copied 
     */
    public void copyBlock(int[] src, int srcOff, int[] dst, int dstOff, int len) {
        for (int i = 0; i < len; i++) {
            dst[dstOff + i] = src[srcOff + i];
        }
    }

    
    /**
     * Copies one int block to another.
     * 
     * @param src the source int array
     * @param dst the destination array to which to copy the ints
     */
    public void copyBlock(int[] src, int[] dst) {
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i];
        }
    }
        
    
    /**
     * Convert an integer value to a 4 byte array 
     * 
     * @param inputSrc the array to convert (must be of size 4)
     * @return the converted byte array
     */    
    public byte[] toBytes(int inputSrc) {
        int src = inputSrc;
        byte[] dest = new byte[4];
        dest[0] = (byte) (src & 0xFF);
        src >>= 8;
        dest[1] = (byte) (src & 0xFF);
        src >>= 8;
        dest[2] = (byte) (src & 0xFF);
        src >>= 8;
        dest[3] = (byte) (src & 0xFF);
        return dest;
    }

    
    /**
     * Convert from a long value to a 8 byte array 
     * 
     * @param src the array to convert (must be of size 8)
     * @return the converted byte array
     */    
    public byte[] toBytes(long src) {
        return BigInteger.valueOf(src).toByteArray();
        /*
        byte[] dest = new byte[ 8 ];
     
        dest[7] = (byte)src;
        src >>>= 8;
        dest[6] = (byte)src;
        src >>>= 8;
        dest[5] = (byte)src;
        src >>>= 8;
        dest[4] = (byte)src;
        src >>>= 8;
        dest[3] = (byte)src;
        src >>>= 8;
        dest[2] = (byte)src;
        src >>>= 8;
        dest[1] = (byte)src;
        src >>>= 8;
        dest[0] = (byte)src;
        return dest;
        */
    }

    
    /**
     * Prepare a byte array to a byte array of a defined size 
     * 
     * @param src the array to prepare
     * @param len the size of the returned byte array
     * @return the prepared byte array with the defined size
     */    
    public byte[] toBytes(byte[] src, int len) {
        if (src.length % len == 0) {
            return src;
        }

        int size = len * ((src.length / len) + 1);
        int diff = size - src.length;

        byte[] result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = 0x00;
        }

        copyBlock(src, 0, result, diff, src.length);
        return result;
    }    
    
        
    /**
     * Build an integer from first 4 bytes of the array.
     *
     * @param src The byte[] to convert.
     * @return the integer value.
     */
    public int toInteger(ByteArray src) {
        if (src == null) {
            return 0;
        }

        return toInteger(src.toBytes());
    }    
    
    
    /**
     * Build an integer from first 4 bytes of the array.
     *
     * @param src The byte[] to convert.
     * @return the integer value.
     */
    public int toInteger(byte[] src) {
        if (src == null) {
            return 0;
        }

        int result = (src[0] & 0xFF);
        result |= ((src[1] & 0xFF) << 8);

        if (src.length > 2) {
            result |= ((src[2] & 0xFF) << 16);

            if (src.length > 3) {
                result |= ((src[3] & 0xFF) << 24);
            }
        }

        return result;
    }    
    
    
    /**
     * Build a long from first 8 bytes of the array.
     *
     * @param src The byte[] to convert.
     * @return the long value.
     */
    public long toLong(byte[] src) {
        if (src == null) {
            return 0;
        }
        
        return (new BigInteger(src)).longValue();
        /*
        return ( ( ( (long) src[7]) & 0xFF )
                + ( ( ( (long) src[6]) & 0xFF ) << 8 )
                + ( ( ( (long) src[5]) & 0xFF ) << 16 )
                + ( ( ( (long) src[4]) & 0xFF ) << 24 )
                + ( ( ( (long) src[3]) & 0xFF ) << 32 )
                + ( ( ( (long) src[2]) & 0xFF ) << 40 )
                + ( ( ( (long) src[1]) & 0xFF ) << 48 )
                + ( ( ( (long) src[0]) & 0xFF ) << 56 ) );
                */
    }

    
    /**
     * Build a long from first 8 bytes of the array.
     *
     * @param src The byte[] to convert.
     * @return the long value.
     */
    public long toLong(ByteArray src) {
        if (src == null) {
            return 0;
        }

        return ByteUtil.getInstance().toLong(src.toBytes());
    }        

    
    /**
     * The source is the byte array being searched, and the target is the string being searched for.
     *
     * @param source the bytes being searched.
     * @param sourceOffset offset of the source array.
     * @param sourceCount count of the source array.
     * @param target the bytes being searched for.
     * @param targetOffset offset of the target array.
     * @param targetCount count of the target array.
     * @param index the index to begin searching from.
     * @return the index within this array of the first occurrence of the specified sub array, starting at the specified index.
     */
    public int indexOf(byte[] source, int sourceOffset, int sourceCount, byte[] target, int targetOffset, int targetCount, int index) {
        int fromIndex = index;
        if (fromIndex >= sourceCount) { // (targetCount == 0 ? sourceCount : -1);
            if (targetCount == 0) {
                return sourceCount;
            } else {
                return -1;
            }
        }

        if (fromIndex < 0) {
            fromIndex = 0;
        }

        if (targetCount == 0) {
            return fromIndex;
        }

        byte first = target[targetOffset];
        int i = sourceOffset + fromIndex;
        int max = sourceOffset + (sourceCount - targetCount);

        startSearchForFirstChar: while (true) {
            /* Look for first character. */
            while (i <= max && source[i] != first) {
                i++;
            }

            if (i > max) {
                return -1;
            }

            /* Found first character, now look at the rest of v2 */
            int j = i + 1;
            int end = j + targetCount - 1;
            int k = targetOffset + 1;
            while (j < end) {
                if (source[j++] != target[k++]) {
                    i++;
                    /* Look for str's first char again. */
                    continue startSearchForFirstChar;
                }
            }

            return i - sourceOffset; /* Found whole string. */
        }
    }
}
