/*
 * RandomGenerator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import com.github.toolarium.common.ByteArray;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;


/**
 * This class implements some helper methods for random numbers. It works internal with the SecureRandom class.
 * All methods are thread safe, they rely on the thread safety of {@link SecureRandom} and do not block each other.
 *
 * @author patrick
 */
public final class RandomGenerator {
    
    /**
     * Defines valid number characters.
     *
     * @deprecated the content of the array can be changed by everybody, use {@link #getValidNumberCharacters()}
     */
    @Deprecated
    public static final char[] validNumberCharacters = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    /**
     * Defines valid lower case letter characters.
     *
     * @deprecated the content of the array can be changed by everybody, use {@link #getValidLowerCaseLetterCharacters()}
     */
    @Deprecated
    public static final char[] validLowerCaseLetterCharacters = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    /**
     * Defines valid upper case letter characters.
     *
     * @deprecated the content of the array can be changed by everybody, use {@link #getValidUpperCaseLetterCharacters()}
     */
    @Deprecated
    public static final char[] validUpperCaseLetterCharacters = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

    /**
     * Defines valid letter characters.
     *
     * @deprecated the content of the array can be changed by everybody, use {@link #getValidLetterCharacters()}
     */
    @Deprecated
    public static final char[] validLetterCharacters = combineValidCharacters(validLowerCaseLetterCharacters, validUpperCaseLetterCharacters);

    /**
     * Defines valid number and letter characters.
     *
     * @deprecated the content of the array can be changed by everybody, use {@link #getValidNumberLetterCharacters()}
     */
    @Deprecated
    public static final char[] validNumberLetterCharacters = combineValidCharacters(validNumberCharacters, validLetterCharacters);

    /** The miliseconds of a day */
    public static final long ONE_DAY = 24 * 60 * 60 * 1000;

    /** Private copies of the characters, so that changes of the public (deprecated) arrays have no effect */
    private static final char[] NUMBER_CHARACTERS = validNumberCharacters.clone();
    private static final char[] LOWER_CASE_LETTER_CHARACTERS = validLowerCaseLetterCharacters.clone();
    private static final char[] UPPER_CASE_LETTER_CHARACTERS = validUpperCaseLetterCharacters.clone();
    private static final char[] LETTER_CHARACTERS = validLetterCharacters.clone();
    private static final char[] NUMBER_LETTER_CHARACTERS = validNumberLetterCharacters.clone();

    private final SecureRandom secureRandom;
    private final String sid;


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final RandomGenerator INSTANCE = new RandomGenerator();
    }


    /**
     * Constructor
     */
    private RandomGenerator() {
        this(new SecureRandom());
    }


    /**
     * Constructor
     *
     * @param secureRandom the secure random instance to use
     */
    private RandomGenerator(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;

        // random instance id instead of the host name: no (possibly blocking) DNS lookup is needed
        this.sid = Long.toHexString(secureRandom.nextLong()) + Long.toHexString(secureRandom.nextLong());
    }


    /**
     * Get the (shared) instance
     *
     * @return the instance
     */
    public static RandomGenerator getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Creates a new, independent (non-singleton) random generator which uses the given secure random instance.
     * The shared instance returned by {@link #getInstance()} is not modified.
     *
     * @param random the secure random instance to use, in case of null a new {@link SecureRandom} is used.
     * @return a new random generator instance
     */
    public static RandomGenerator getInstance(SecureRandom random) {
        if (random == null) {
            return new RandomGenerator();
        }

        return new RandomGenerator(random);
    }


    /**
     * Returns the secure random instance
     *
     * @return secure random instance
     */
    public SecureRandom getSecureRandom() {
        return secureRandom;
    }


    /**
     * Get the valid number characters.
     *
     * @return a copy of the valid number characters
     */
    public char[] getValidNumberCharacters() {
        return NUMBER_CHARACTERS.clone();
    }


    /**
     * Get the valid lower case letter characters.
     *
     * @return a copy of the valid lower case letter characters
     */
    public char[] getValidLowerCaseLetterCharacters() {
        return LOWER_CASE_LETTER_CHARACTERS.clone();
    }


    /**
     * Get the valid upper case letter characters.
     *
     * @return a copy of the valid upper case letter characters
     */
    public char[] getValidUpperCaseLetterCharacters() {
        return UPPER_CASE_LETTER_CHARACTERS.clone();
    }


    /**
     * Get the valid letter characters.
     *
     * @return a copy of the valid letter characters
     */
    public char[] getValidLetterCharacters() {
        return LETTER_CHARACTERS.clone();
    }


    /**
     * Get the valid number and letter characters.
     *
     * @return a copy of the valid number and letter characters
     */
    public char[] getValidNumberLetterCharacters() {
        return NUMBER_LETTER_CHARACTERS.clone();
    }


    /**
     * Returns a random number as a Boolean
     * 
     * @return random Boolean
     */
    public Boolean getBooleanRandom() {
        return Boolean.valueOf(secureRandom.nextBoolean());
    }
    
    
    /**
     * Returns a random number as a Integer
     * 
     * @return random Integer
     */
    public Integer getIntegerRandom() {
        return Integer.valueOf(secureRandom.nextInt());
    }
    
    
    /**
     * Returns a random number as a Long
     * 
     * @return random Long
     */
    public Long getLongRandom() {
        return Long.valueOf(secureRandom.nextLong());
    }    
    
    
    /**
     * Returns a random number as a Float
     * 
     * @return random Float
     */
    public Float getFloatRandom() {
        return Float.valueOf(secureRandom.nextFloat());
    }

    
    /**
     * Returns a random number as a Double
     * 
     * @return random Double
     */
    public Double getDoubleRandom() {
        return Double.valueOf(secureRandom.nextDouble());
    }

    
    /**
     * Returns a random byte array
     * @param size the size of the byte array
     * @return random byte array
     */
    public ByteArray getRandomByteArray(int size) {
        return new ByteArray(nextBytes(size));
    }


    /**
     * Returns a random string of exactly <code>size</code> characters, which consists of numbers and letters ([0-9a-zA-Z]).
     *
     * @param size the size of the string
     * @return random string
     */
    public String getRandomString(int size) {
        return getRandomString(size, NUMBER_LETTER_CHARACTERS);
    }

    
    /**
     * Returns a random string
     * 
     * @param size the size of the string
     * @param validCharacters the valid characters for the random string
     * @return random string
     * @throws IllegalArgumentException in case the valid characters are null or empty
     */
    public String getRandomString(int size, char[] validCharacters) {
        if (validCharacters == null || validCharacters.length == 0) {
            throw new IllegalArgumentException("Invalid empty valid characters!");
        }

        StringBuilder randomString = new StringBuilder(Math.max(size, 0));
        while (randomString.length() < size) {
            randomString.append(validCharacters[secureRandom.nextInt(validCharacters.length)]);
        }
        return randomString.toString();
    }

    
    /**
     * Gets random bytes
     * 
     * @param size the size of the random bytes, in case of a negative size an empty array is returned
     * @return the instance
     */
    public byte[] nextBytes(int size) {
        if (size <= 0) {
            return new byte[0];
        }

        byte[] bytes = new byte[size];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    
    /**
     * Generates a GUID: GUIDs are guaranteed to be globally unique by using
     * ethernet MACs, IP addresses, time elements, and sequential numbers. GUIDs
     * are not expected to be random and most often are easy/possible to guess
     * given a sample from a given generator.
     * GUIDs can be used as security devices to hide things such as files within
     * a filesystem where listings are unavailable (e.g. files that are served
     * up from a Web server with indexing turned off). This may be desireable
     * in cases where standard authentication is not appropriate. In this
     * scenario, the RandomGUIDs are used as directories.
     * Another example is the use of GUIDs for primary keys in a database where
     * you want to ensure that the keys are secret. Random GUIDs can then be
     * used in a URL to prevent hackers (or users) from accessing records by
     * guessing or simply by incrementing sequential numbers. There are many
     * other possiblities of using GUIDs in the realm of security and
     * encryption where the element of randomness is important.
     * This method was written for these purposes but can also be used
     * as a general purpose GUID generator as well. It generates truly
     * random GUIDs by using a random instance id (created once), system time in
     * milliseconds (as an integer), and a very large random number joined
     * together in a single String that is passed through a SHA-256 hash. The instance
     * id and system time make the SHA-256 seed globally unique and the random
     * number guarantees that the generated GUIDs will have no discernable
     * pattern and cannot be guessed given any number of previously generated
     * GUIDs. It is generally not possible to access the seed information (instance id,
     * time, random number) from the resulting GUIDs as the SHA-256 hash algorithm
     * provides one way encryption.
     *
     * @return a unique GUID as lower case hex string (64 characters)
     * @throws IllegalStateException in case the SHA-256 algorithm is not available
     */
    public String createGUID() {
        StringBuilder guid = new StringBuilder();
        guid.append(sid);
        guid.append(":");
        guid.append(Long.toString(System.currentTimeMillis()));
        guid.append(":");
        guid.append(Long.toString(getLongRandom()));

        final MessageDigest msgDigest;
        try {
            msgDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not create GUID: " + e.getMessage(), e);
        }

        byte[] array = msgDigest.digest(guid.toString().getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder(array.length * 2);
        for (int j = 0; j < array.length; ++j) {
            int b = array[j] & 0xFF;
            if (b < 0x10) {
                result.append('0');
            }
            result.append(Integer.toHexString(b));
        }

        return result.toString();
    }


    /**
     * Creates a random UUID (RFC 4122 version 4) in the format 8-4-4-4-12 (lower case hex), e.g. <code>3f2b8c1e-7d4a-4f0e-9b6c-1a2b3c4d5e6f</code>.
     *
     * @return the uuid
     */
    public String createUUID() {
        byte[] data = nextBytes(16);
        data[6] &= 0x0f; // clear version
        data[6] |= 0x40; // set to version 4
        data[8] &= 0x3f; // clear variant
        data[8] |= (byte) 0x80; // set to IETF variant

        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (data[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }

        return new UUID(msb, lsb).toString();
    }


    /**
     * Get a date in the future
     * 
     * @param dayRange the range of days
     * @return the date
     */
    public Date getRandomDayInFuture(int dayRange) {
        return new Date(new Date().getTime() + (getRandomNumber(dayRange, false) * ONE_DAY));
    }

    
    /**
     * Get a date in the future
     * 
     * @param milisecondRange the range in miliseconds
     * @return the date
     */
    public Date getRandomDateInFuture(long milisecondRange) {
        return new Date(new Date().getTime() + (getRandomNumber(milisecondRange, false)));
    }

    
    /**
     * Get a date in the future
     * 
     * @param dayRange the range of days
     * @return the date
     */
    public Date getRandomDayInPast(int dayRange) {
        return new Date(new Date().getTime() - (getRandomNumber(dayRange, false) * ONE_DAY));
    }

    /**
     * Get a date in the future
     * 
     * @param milisecondRange the range in miliseconds
     * @return the date
     */
    public Date getRandomDateInPast(long milisecondRange) {
        return new Date(new Date().getTime() - (getRandomNumber(milisecondRange, false)));
    }

    
    /**
     * Gets a random long number. The sign of the range is ignored. The distribution is uniform (no modulo bias).
     *
     * @param range the range of the long. As example the range 5 means a random number between 1 and 5 (both inclusive).
     *        The range {@link Long#MIN_VALUE} is handled as {@link Long#MAX_VALUE}.
     *        In case of range 0 the result is 1, respective 0 if zero is allowed.
     * @param allowZero true if zero should also included into the range. As example the range 5 means a random number between 0 and 4 (both inclusive).
     * @return the random number
     */
    public long getRandomNumber(long range, boolean allowZero) {
        long bound = range;
        if (bound == Long.MIN_VALUE) {
            bound = Long.MAX_VALUE;
        } else if (bound < 0) {
            bound = -bound;
        }

        if (bound == 0) {
            if (!allowZero) {
                return 1;
            }
            return 0;
        }

        long r = nextLong(bound);
        if (!allowZero) {
            r++;
        }

        return r;
    }


    /**
     * Gets a uniform distributed random number between 0 (inclusive) and the bound (exclusive) by using rejection sampling.
     *
     * @param bound the bound, must be positive
     * @return the random number
     */
    private long nextLong(long bound) {
        long bits;
        long val;
        do {
            bits = secureRandom.nextLong() >>> 1;
            val = bits % bound;
        } while (bits - val + (bound - 1) < 0); // reject values of the incomplete last block (overflow check)

        return val;
    }


    /**
     * Method to test the randomness of the random algorithm.
     *
     * @param number numbers of positiv numbers
     * @param size the numbers are less the size
     * @return the chis-quare. If chis-quare is in the range from 2*sqr(size) to size, then the algorithm has a good randomness in case the number is 10*size.
     * @throws IllegalArgumentException in case the number or the size is not positive
     */
    public float chisQuare(int number, int size) {
        if (number <= 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid number or size, both must be positive: " + number + ", " + size);
        }

        long t = 0;
        int[] f = new int[size];
        byte[] init = {1, 2, 3, 4, 5, 6, 7 };
        SecureRandom x = new SecureRandom(init);

        for (int i = 0; i < number; i++) {
            f[x.nextInt(size)]++;
        }

        for (int i = 0; i < size; i++) {
            t += (long) f[i] * f[i];
        }

        return (float) (((double) size * t / number) - number);
    }

    
    /**
     * Helper methods to combines two character arrays
     *
     * @param input the input
     * @param input2 the input two
     * @return the combined arrays
     */
    public static char[] combineValidCharacters(char[] input, char[] input2) {
        char[] result = new char[input.length + input2.length];

        int pos = 0;
        for (int i = 0; i < input.length; i++) {
            result[pos++] = input[i];
        }

        for (int i = 0; i < input2.length; i++) {
            result[pos++] = input2[i];
        }

        return result;
    }
}
