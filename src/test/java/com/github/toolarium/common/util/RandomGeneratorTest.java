/*
 * RandomGeneratorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test the {@link RandomGenerator}.
 * 
 * @author patrick
 */
public class RandomGeneratorTest {
    private static final Logger LOG = LoggerFactory.getLogger(RandomGeneratorTest.class);


    /**
     * Test the GUID generator
     */
    @Test
    public void testGUID() {
        Set<String> guids = new TreeSet<String>();

        try {
            LOG.debug("localhost: " + InetAddress.getLocalHost().toString());
        } catch (UnknownHostException e) {
            // NOP
        }

        for (int i = 0; i < 100; i++) {
            String guid = RandomGenerator.getInstance().createGUID();
            LOG.debug("Created guid: " + guid);

            assertNotNull(guid);
            assertTrue(guid.matches("[0-9a-f]{64}"), guid);
            assertTrue(guids.add(guid)); // no duplicates are allowed
        }
    }

    
    /**
     * Test the UUID generator
     */
    @Test
    public void testUUID() {
        Set<String> guids = new TreeSet<String>();

        for (int i = 0; i < 100; i++) {
            String guid = RandomGenerator.getInstance().createUUID();
            LOG.debug("Created guid: " + guid);

            assertTrue(guid.matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"), guid);
            UUID uuid = UUID.fromString(guid);
            assertEquals(4, uuid.version());
            assertEquals(2, uuid.variant());
            assertEquals(guid, uuid.toString());
            assertTrue(guids.add(guid)); // no duplicates are allowed
        }
    }


    /**
     * Test that getInstance with a secure random does not modify the shared instance
     */
    @Test
    public void testGetInstanceWithSecureRandom() {
        RandomGenerator shared = RandomGenerator.getInstance();
        final SecureRandom sharedRandom = shared.getSecureRandom();
        SecureRandom random = new SecureRandom();

        RandomGenerator custom = RandomGenerator.getInstance(random);
        assertNotSame(shared, custom);
        assertSame(random, custom.getSecureRandom());
        assertSame(shared, RandomGenerator.getInstance());
        assertSame(sharedRandom, RandomGenerator.getInstance().getSecureRandom());
        assertNotSame(random, RandomGenerator.getInstance().getSecureRandom());

        RandomGenerator other = RandomGenerator.getInstance(null);
        assertNotNull(other.getSecureRandom());
        assertNotSame(shared, other);
    }


    /**
     * Test that the getters of the valid characters return a copy
     */
    @Test
    public void testValidCharactersGetterReturnsCopy() {
        char[] numbers = RandomGenerator.getInstance().getValidNumberCharacters();
        assertEquals(10, numbers.length);
        numbers[0] = 'x';
        assertEquals('0', RandomGenerator.getInstance().getValidNumberCharacters()[0]);
        assertNotSame(RandomGenerator.getInstance().getValidLetterCharacters(), RandomGenerator.getInstance().getValidLetterCharacters());
        assertEquals(26, RandomGenerator.getInstance().getValidLowerCaseLetterCharacters().length);
        assertEquals(26, RandomGenerator.getInstance().getValidUpperCaseLetterCharacters().length);
        assertEquals(62, RandomGenerator.getInstance().getValidNumberLetterCharacters().length);
    }


    /**
     * Test that the public (deprecated) character constants can not influence the generation and the getters
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testPublicCharacterConstantsAreCopies() {
        char[] backup = RandomGenerator.validNumberLetterCharacters.clone();
        try {
            Arrays.fill(RandomGenerator.validNumberLetterCharacters, 'x');
            String result = RandomGenerator.getInstance().getRandomString(200);
            assertFalse(result.chars().allMatch(c -> c == 'x'));
            assertArrayEquals(backup, RandomGenerator.getInstance().getValidNumberLetterCharacters());
        } finally {
            System.arraycopy(backup, 0, RandomGenerator.validNumberLetterCharacters, 0, backup.length);
        }
    }


    /**
     * Test the random string with a size only
     */
    @Test
    public void testRandomStringSize() {
        for (int size : new int[] {0, 1, 7, 16, 100 }) {
            String result = RandomGenerator.getInstance().getRandomString(size);
            assertEquals(size, result.length());
            assertTrue(result.matches("[0-9a-zA-Z]*"), result);
        }

        assertThrows(IllegalArgumentException.class, () -> RandomGenerator.getInstance().getRandomString(5, new char[0]));
    }


    /**
     * Test the random number bounds
     */
    @Test
    public void testRandomNumber() {
        RandomGenerator randomGenerator = RandomGenerator.getInstance();
        boolean[] seen = new boolean[6];
        boolean[] seenZero = new boolean[6];
        for (int i = 0; i < 5000; i++) {
            long r = randomGenerator.getRandomNumber(5, false);
            assertTrue(r >= 1 && r <= 5, "" + r);
            seen[(int) r] = true;

            long z = randomGenerator.getRandomNumber(5, true);
            assertTrue(z >= 0 && z <= 4, "" + z);
            seenZero[(int) z] = true;

            long n = randomGenerator.getRandomNumber(-5, false);
            assertTrue(n >= 1 && n <= 5, "" + n);
        }

        for (int i = 1; i <= 5; i++) {
            assertTrue(seen[i], "Upper bound not reached: " + i);
        }
        for (int i = 0; i <= 4; i++) {
            assertTrue(seenZero[i], "Value not reached: " + i);
        }

        assertEquals(1L, randomGenerator.getRandomNumber(0, false));
        assertEquals(0L, randomGenerator.getRandomNumber(0, true));
        assertEquals(1L, randomGenerator.getRandomNumber(1, false));
        assertEquals(0L, randomGenerator.getRandomNumber(1, true));

        for (int i = 0; i < 1000; i++) {
            assertTrue(randomGenerator.getRandomNumber(Long.MAX_VALUE, false) >= 1);
            assertTrue(randomGenerator.getRandomNumber(Long.MAX_VALUE, true) >= 0);
            assertTrue(randomGenerator.getRandomNumber(Long.MIN_VALUE, false) >= 1);
        }
    }

    
    /**
     * Test if the randomness of random algorithmus is ok.
     */
    @Test
    public void testChisQuare() {
        RandomGenerator randomGenerator = RandomGenerator.getInstance();
        int n = 100;

        boolean result = false;
        for (int i = 0; i < 3; i++) {
            float chisQuare = randomGenerator.chisQuare(10 * n, n);
            double distance = Math.sqrt(n) * 2;

            LOG.info("The chisQuare is: " + chisQuare + " of the given parameters " + 10 * n + ", " + n);
            result = n - distance < chisQuare && n + distance > chisQuare;
            if (result) {
                break;
            }
        }

        assertTrue(result);
    }

    
    /**
     * Test the random string
     */
    @Test
    public void testRandomString() {
        char[] validCharacters = new char[] {'0', '1', '2', '3', '4', 'a', 'b', 'c' };

        checkRandomString(10, 1000, validCharacters);
        checkRandomString(10, 1000, RandomGenerator.getInstance().getValidUpperCaseLetterCharacters());
        checkRandomString(10, 1000, RandomGenerator.getInstance().getValidLowerCaseLetterCharacters());
        checkRandomString(10, 1000, RandomGenerator.getInstance().getValidNumberCharacters());
        checkRandomString(10, 1000, RandomGenerator.getInstance().getValidLetterCharacters());
        checkRandomString(10, 1000, RandomGenerator.getInstance().getValidNumberLetterCharacters());
    }

    
    /**
     * Check the random string
     * 
     * @param len the length of the generated string
     * @param runs the number of runs
     * @param validCharacters the character setto use
     */
    private void checkRandomString(int len, int runs, char[] validCharacters) {
        Set<String> entries = new HashSet<String>();
        for (int c = 0; c < runs; c++) {
            String result = RandomGenerator.getInstance().getRandomString(len, validCharacters);
            assertFalse(entries.contains(result), "Bad random number (c:" + c + "/" + result + ")!");

            entries.add(result);
            for (int i = 0; i < result.length(); i++) {
                boolean valid = false;
                for (int j = 0; j < validCharacters.length; j++) {
                    if (result.charAt(i) == validCharacters[j]) {
                        valid = true;
                        break;
                    }
                }

                assertTrue(valid);
            }
        }
    }

    /**
     * The chi square must not overflow for large numbers
     */
    @Test
    public void testChisQuareLargeNumbers() {
        float chisQuare = RandomGenerator.getInstance().chisQuare(1_000_000, 50_000);
        assertTrue(chisQuare > 47_000 && chisQuare < 53_000, "Unexpected chi square: " + chisQuare);
    }


    /**
     * Invalid arguments: a negative size gives an empty result, the chi square needs positive arguments
     */
    @Test
    public void testInvalidArguments() {
        assertEquals(0, RandomGenerator.getInstance().nextBytes(-1).length);
        assertEquals(0, RandomGenerator.getInstance().nextBytes(0).length);
        assertEquals(5, RandomGenerator.getInstance().nextBytes(5).length);
        assertEquals("", RandomGenerator.getInstance().getRandomString(-1));
        assertThrows(IllegalArgumentException.class, () -> RandomGenerator.getInstance().chisQuare(0, 5));
        assertThrows(IllegalArgumentException.class, () -> RandomGenerator.getInstance().chisQuare(5, 0));
        assertThrows(IllegalArgumentException.class, () -> RandomGenerator.getInstance().chisQuare(-1, -1));
    }
}
