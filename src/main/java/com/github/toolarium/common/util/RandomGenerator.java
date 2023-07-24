/*
 * RandomGenerator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import com.github.toolarium.common.ByteArray;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;


/**
 * This class implements some helper methods for random numbers. It works internal with the SecureRandom class.
 *
 * @author patrick
 */
public final class RandomGenerator {
    
    /** Defines valid number characters */
    public static final char[] validNumberCharacters = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    /** Defines valid lower case letter characters */
    public static final char[] validLowerCaseLetterCharacters = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    /** Defines valid upper case letter characters */
    public static final char[] validUpperCaseLetterCharacters = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

    /** Defines valid letter characters */
    public static final char[] validLetterCharacters = combineValidCharacters(validLowerCaseLetterCharacters, validUpperCaseLetterCharacters);

    /** Defines valid number and letter characters */
    public static final char[] validNumberLetterCharacters = combineValidCharacters(validNumberCharacters, validLetterCharacters);

    /** The miliseconds of a day */
    public static final long ONE_DAY = 24 * 60 * 60 * 1000;

    //private static Logger log = Logger.getLogger( RandomGenerator.class );
    private SecureRandom secureRandom;
    private String sid;

    
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
        static final RandomGenerator INSTANCE = new RandomGenerator();
    }

    /**
     * Constructor
     */
    private RandomGenerator() {
        secureRandom = new SecureRandom();
    }

    /**
     * Get the instance
     *
     * @return the instance
     */
    public static RandomGenerator getInstance() {
        return HOLDER.INSTANCE;
    }

    
    /**
     * Gets a secure random instance
     * 
     * @param random initialize the random generator with the given secure random instance.
     * @return the instance
     */
    public static synchronized RandomGenerator getInstance(SecureRandom random) {
        RandomGenerator instance = getInstance();
        instance.secureRandom = random;
        return instance;
    }

    
    /**
     * Returns the secure random instance
     * 
     * @return secure random instance
     */
    public synchronized SecureRandom getSecureRandom() {
        return secureRandom;
    }


    /**
     * Returns a random number as a Boolean
     * 
     * @return random Boolean
     */
    public synchronized Boolean getBooleanRandom() {
        return Boolean.valueOf(secureRandom.nextBoolean());
    }
    
    
    /**
     * Returns a random number as a Integer
     * 
     * @return random Integer
     */
    public synchronized Integer getIntegerRandom() {
        return Integer.valueOf(secureRandom.nextInt());
    }
    
    
    /**
     * Returns a random number as a Long
     * 
     * @return random Long
     */
    public synchronized Long getLongRandom() {
        return Long.valueOf(secureRandom.nextLong());
    }    
    
    
    /**
     * Returns a random number as a Float
     * 
     * @return random Float
     */
    public synchronized Float getFloatRandom() {
        return Float.valueOf(secureRandom.nextFloat());
    }

    
    /**
     * Returns a random number as a Double
     * 
     * @return random Double
     */
    public synchronized Double getDoubleRandom() {
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
     * Returns a random string
     * 
     * @param size the size of the string
     * @return random string
     */
    public String getRandomString(int size) {
        return new String(nextBytes(size));
    }

    
    /**
     * Returns a random string
     * 
     * @param size the size of the string
     * @param validCharacters the valid characters for the random string
     * @return random string
     */
    public synchronized String getRandomString(int size, char[] validCharacters) {
        StringBuffer randomString = new StringBuffer();
        byte[] bytes = new byte[1];
        while (randomString.length() < size) {
            getRandomNumber(validCharacters.length, true);
            secureRandom.nextBytes(bytes);

            for (int i = 0; i < validCharacters.length; i++) {
                if ((char) bytes[0] == validCharacters[i]) {
                    randomString.append((char) bytes[0]);
                    break;
                }
            }
        }

        return randomString.toString();
    }

    
    /**
     * Gets random bytes
     * 
     * @param size the size of the random bytes
     * @return the instance
     */
    public synchronized byte[] nextBytes(int size) {
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
     * random GUIDs by using the system's IP address (name/IP), system time in
     * milliseconds (as an integer), and a very large random number joined
     * together in a single String that is passed through an MD5 hash. The IP
     * address and system time make the MD5 seed globally unique and the random
     * number guarantees that the generated GUIDs will have no discernable
     * pattern and cannot be guessed given any number of previously generated
     * GUIDs. It is generally not possible to access the seed information (IP,
     * time, random number) from the resulting GUIDs as the MD5 hash algorithm
     * provides one way encryption.
     *
     * @return a unique GUID
     */
    public String createGUID() {
        initializeSID();

        StringBuffer guid = new StringBuffer();
        guid.append(sid);
        guid.append(":");
        guid.append(Long.toString(System.currentTimeMillis()));
        guid.append(":");
        guid.append(Long.toString(getLongRandom()));

        try {
            StringBuffer result = new StringBuffer();
            
            
            MessageDigest msgDigest = MessageDigest.getInstance("sha-256");
            msgDigest.update(guid.toString().getBytes());
            byte[] array = msgDigest.digest();
            for (int j = 0; j < array.length; ++j) {
                int b = array[j] & 0xFF;
                if (b < 0x10) {
                    result.append('0');
                }
                result.append(Integer.toHexString(b));
            }

            return result.toString();
        } catch (Exception e) {
            // NOP
        }

        return null;
    }


    /**
     * Creates a UUID
     *
     * @return the uuid
     */
    public String createUUID() {
        final String guid = createGUID();
        return "" + guid.substring(0, 8) + "-" + guid.substring(8, 12) + "-" + guid.substring(12, 16) + "-" + guid.substring(16, 20) + "-" + guid.substring(20);
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
     * Gets a random long number
     * 
     * @param range the range of the long. As example the range 5 means a random number between 1 and 5.
     * @param allowZero true if zero should also included into the range. As example the range 5 means a random number between 0 and 4.
     * @return the random number
     */
    public long getRandomNumber(long range, boolean allowZero) {
        long s = range;

        if (s < 0) {
            s = s * -1;
        }

        if (range == 0) {
            if (!allowZero) {
                return 1;
            }
            return 0;
        }

        if (range == 1 && !allowZero) {
            return 1;
        }

        long r = -1;

        if (allowZero) {
            s++;
        }

        synchronized (secureRandom) {
            while (r <= 0) {
                r = Math.abs(secureRandom.nextLong() % s);
            }
        }

        if (allowZero) {
            r--;
        }

        return r;
    }

    
    /**
     * Method to test the randomness of the random algorithm.
     *
     * @param number numbers of positiv numbers
     * @param size the numbers are less the size
     * @return the chis-quare. If chis-quare is in the range from 2*sqr(size) to size, then the algorithm has a good randomness in case the number is 10*size.
     */
    public float chisQuare(int number, int size) {
        int t = 0;
        int[] f = new int[size];
        byte[] init = {1, 2, 3, 4, 5, 6, 7 };
        SecureRandom x = new SecureRandom(init);

        for (int i = 0; i < number; i++) {
            f[x.nextInt(size)]++;
        }

        for (int i = 0; i < size; i++) {
            t += f[i] * f[i];
        }

        return ((size * t / number) - number);
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

    
    /**
     * Initialize the sid
     */
    private void initializeSID() {
        if (sid == null) {
            synchronized (this) {
                if (sid == null) {
                    try {
                        sid = InetAddress.getLocalHost().toString();
                    } catch (UnknownHostException e) {
                        // NOP
                    }
                }
            }
        }
    }
}
