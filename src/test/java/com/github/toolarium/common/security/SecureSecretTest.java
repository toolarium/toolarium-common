/*
 * SecureSecretTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link SecureSecret}.
 *
 * @author patrick
 */
public class SecureSecretTest {
    private static final String SECRET = "pässwörd€𝄞";


    /**
     * Test a secret with non ASCII characters: the content is kept and the input is wiped
     */
    @Test
    public void testNonAsciiCharSecret() {
        char[] input = SECRET.toCharArray();
        try (SecureSecret secret = new SecureSecret(input)) {
            assertEquals(SECRET, secret.useChars(String::new));
            assertArrayEquals(SECRET.getBytes(StandardCharsets.UTF_8), secret.use(byte[]::clone));
        }

        assertArrayEquals(new char[SECRET.length()], input);
    }


    /**
     * Test a byte secret: the content is kept and the input is wiped
     */
    @Test
    public void testByteSecret() {
        byte[] expected = SECRET.getBytes(StandardCharsets.UTF_8);
        byte[] input = expected.clone();
        try (SecureSecret secret = new SecureSecret(input)) {
            assertArrayEquals(expected, secret.use(byte[]::clone));
        }

        assertArrayEquals(new byte[expected.length], input);
    }


    /**
     * Test that a char secret with a lone surrogate is rejected and the input is wiped anyway
     */
    @Test
    public void testLoneSurrogateRejected() {
        char[] lowSurrogate = new char[] {'a', '\uDC00', 'b'}; // lone low surrogate
        assertThrows(IllegalArgumentException.class, () -> new SecureSecret(lowSurrogate));
        assertArrayEquals(new char[3], lowSurrogate);

        char[] highSurrogateAtEnd = new char[] {'a', '\uD800'}; // high surrogate without low surrogate
        assertThrows(IllegalArgumentException.class, () -> new SecureSecret(highSurrogateAtEnd));
        assertArrayEquals(new char[2], highSurrogateAtEnd);

        // a valid surrogate pair is accepted
        char[] pair = new char[] {'a', '\uD834', '\uDD1E'}; // musical symbol G clef
        try (SecureSecret secret = new SecureSecret(pair)) {
            assertEquals("a𝄞", secret.useChars(String::new));
        }

        final char[] lowSurrogateByValue = new char[] {'a', (char) 0xDC00, 'b'};
        assertThrows(IllegalArgumentException.class, () -> new SecureSecret(lowSurrogateByValue));
        assertArrayEquals(new char[3], lowSurrogateByValue);

        final char[] highSurrogateAtEndByValue = new char[] {'a', (char) 0xD800};
        assertThrows(IllegalArgumentException.class, () -> new SecureSecret(highSurrogateAtEndByValue));
        assertArrayEquals(new char[2], highSurrogateAtEndByValue);

        // a valid surrogate pair is accepted
        final char[] pairByValue = new char[] {'a', (char) 0xD834, (char) 0xDD1E};
        try (SecureSecret secret = new SecureSecret(pairByValue)) {
            assertEquals("a𝄞", secret.useChars(String::new));
        }
    }
}
