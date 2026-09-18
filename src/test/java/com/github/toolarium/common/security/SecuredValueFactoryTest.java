/*
 * SecuredValueFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;


/**
 * Test {@link SecuredValueFactory}.
 *
 * @author patrick
 */
public class SecuredValueFactoryTest {
    private static final String MASK_STAR = "*";
    private static final String MASK_STARS = "***";


    /**
     * Test create() for general-purpose masked values.
     */
    @Test
    public void testCreate() {
        assertEquals("...", SecuredValueFactory.getInstance().create("secureValue1").toString());
        assertEquals(MASK_STAR, SecuredValueFactory.getInstance().create("secureValue2", MASK_STAR).getSecuredValue());
        assertEquals("secureValue3", SecuredValueFactory.getInstance().create("secureValue3").getValue());
        assertEquals(5, SecuredValueFactory.getInstance().create(5, "...").getValue());

        assertEquals(SecuredValueFactory.getInstance().create("secureValue"), SecuredValueFactory.getInstance().create("secureValue"));
        assertEquals(SecuredValueFactory.getInstance().create("secureValue", "...."), SecuredValueFactory.getInstance().create("secureValue", "...."));
        assertNotEquals(SecuredValueFactory.getInstance().create("secureValue", MASK_STAR), SecuredValueFactory.getInstance().create("secureValue2", MASK_STAR));
    }


    /**
     * Test createSecret() from byte array.
     *
     * @throws Exception in case of error
     */
    @Test
    public void testCreateSecretFromBytes() throws Exception {
        try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret("myPassword".getBytes())) {
            assertEquals("...", secret.toString());
            assertEquals("...", secret.getSecuredValue());
            assertTrue(secret.hasSecuredValue());
        }
    }


    /**
     * Test createSecret() from byte array with custom mask.
     *
     * @throws Exception in case of error
     */
    @Test
    public void testCreateSecretFromBytesWithMask() throws Exception {
        try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret("myPassword".getBytes(), MASK_STARS)) {
            assertEquals(MASK_STARS, secret.toString());
            assertEquals(MASK_STARS, secret.getSecuredValue());
        }
    }


    /**
     * Test createSecret() from char array.
     *
     * @throws Exception in case of error
     */
    @Test
    public void testCreateSecretFromChars() throws Exception {
        try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret("myPassword".toCharArray())) {
            assertEquals("...", secret.toString());
            assertTrue(secret.hasSecuredValue());
        }
    }


    /**
     * Test createSecret() from char array with custom mask.
     *
     * @throws Exception in case of error
     */
    @Test
    public void testCreateSecretFromCharsWithMask() throws Exception {
        try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret("myPassword".toCharArray(), "****")) {
            assertEquals("****", secret.toString());
        }
    }


    /**
     * Test safe access via use() and useChars().
     */
    @Test
    public void testUseFunction() {
        try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret("secret123".toCharArray())) {
            String result = secret.use(bytes -> new String(bytes));
            assertEquals("secret123", result);

            String charResult = secret.useChars(String::new);
            assertEquals("secret123", charResult);
        }
    }


    /**
     * Test destroy lifecycle.
     */
    @Test
    public void testDestroyLifecycle() {
        ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret("toDestroy".toCharArray());
        assertFalse(secret.isDestroyed());

        secret.destroy();
        assertTrue(secret.isDestroyed());

        assertNull(secret.getValue());

        assertThrows(IllegalStateException.class, () -> secret.use(bytes -> bytes));
        assertThrows(IllegalStateException.class, () -> secret.useChars(chars -> chars));
    }


    /**
     * Test auto-closeable via try-with-resources.
     *
     * @throws Exception in case of error
     */
    @Test
    public void testAutoCloseable() throws Exception {
        try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret("closeable".toCharArray())) {
            assertFalse(secret.isDestroyed());
        }
    }


    /**
     * Test that double destroy is safe.
     */
    @Test
    public void testDoubleDestroy() {
        ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret("test".toCharArray());
        secret.destroy();
        secret.destroy();
        assertTrue(secret.isDestroyed());
    }


    /**
     * Test usage as ISecuredSecretValue.
     *
     * @throws Exception in case of error
     */
    @Test
    public void testAsISecuredValue() throws Exception {
        try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret("hidden".toCharArray(), MASK_STAR)) {
            assertEquals(MASK_STAR, secret.getSecuredValue());
            assertEquals(MASK_STAR, secret.toString());
            assertTrue(secret.hasSecuredValue());
        }
    }


    /**
     * Test that input arrays are zeroed by constructor.
     *
     * @throws Exception in case of error
     */
    @Test
    public void testInputArrayZeroed() throws Exception {
        char[] input = "sensitive".toCharArray();
        try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret(input)) {
            for (char c : input) {
                assertEquals('\0', c);
            }
        }

        byte[] byteInput = "sensitive".getBytes();
        try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret(byteInput)) {
            for (byte b : byteInput) {
                assertEquals(0, b);
            }
        }
    }


    /**
     * Test that the value field is transient and not serialized.
     *
     * @throws Exception in case of error
     */
    @Test
    public void testSerializationExcludesValue() throws Exception {
        ISecuredValue<String> original = SecuredValueFactory.getInstance().create("mySecret", MASK_STARS);
        assertEquals("mySecret", original.getValue());
        assertEquals(MASK_STARS, original.getSecuredValue());

        // serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
        }

        // deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ISecuredValue<?> deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            deserialized = (ISecuredValue<?>) ois.readObject();
        }

        assertNull(deserialized.getValue());
        assertEquals(MASK_STARS, deserialized.getSecuredValue());
    }
}
