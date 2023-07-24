/*
 * RandomGeneratorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
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

            guids.add(guid); // no duplicates are allowed
        }
    }

    
    /**
     * Test the GUID generator
     */
    @Test
    public void testUUID() {
        Set<String> guids = new TreeSet<String>();

        for (int i = 0; i < 100; i++) {
            String guid = RandomGenerator.getInstance().createUUID();
            LOG.debug("Created guid: " + guid);

            guids.add(guid); // no duplicates are allowed
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
        checkRandomString(10, 1000, RandomGenerator.validUpperCaseLetterCharacters);
        checkRandomString(10, 1000, RandomGenerator.validLowerCaseLetterCharacters);
        checkRandomString(10, 1000, RandomGenerator.validNumberCharacters);
        checkRandomString(10, 1000, RandomGenerator.validLetterCharacters);
        checkRandomString(10, 1000, RandomGenerator.validNumberLetterCharacters);
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
}

