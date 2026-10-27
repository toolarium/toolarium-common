/*
 * SecuredValueTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * Test {@link SecuredValue}.
 *
 * @author patrick
 */
public class SecuredValueTest {
    private static final String MASK = "***";
    private static final String SECRET = "secret";

    /**
     * The hash code must not depend on the plain value.
     */
    @Test
    public void testHashCodeIndependentOfValue() {
        assertEquals(new SecuredValue<String>("secret1", MASK).hashCode(), new SecuredValue<String>("secret2", MASK).hashCode());
        assertEquals(new SecuredValue<String>("secret1").hashCode(), new SecuredValue<String>("other").hashCode());
        assertEquals(new SecuredValue<String>("a").setValue("x").hashCode(), new SecuredValue<String>("b").setValue("y").hashCode());
    }


    /**
     * Test equals
     */
    @Test
    public void testEquals() {
        assertEquals(new SecuredValue<String>(SECRET, MASK), new SecuredValue<String>(SECRET, MASK));
        assertNotEquals(new SecuredValue<String>(SECRET, MASK), new SecuredValue<String>("secreT", MASK));
        assertNotEquals(new SecuredValue<String>(SECRET, MASK), new SecuredValue<String>("secret1", MASK));
        assertNotEquals(new SecuredValue<String>(SECRET, MASK), new SecuredValue<String>(SECRET, "..."));
        assertNotEquals(new SecuredValue<String>(SECRET, MASK), new SecuredValue<String>(null, MASK));
        assertEquals(new SecuredValue<String>(null, MASK), new SecuredValue<String>(null, MASK));

        assertEquals(new SecuredValue<char[]>("abc".toCharArray()), new SecuredValue<char[]>("abc".toCharArray()));
        assertNotEquals(new SecuredValue<char[]>("abc".toCharArray()), new SecuredValue<char[]>("abd".toCharArray()));
        assertNotEquals(new SecuredValue<char[]>("abc".toCharArray()), new SecuredValue<char[]>("ab".toCharArray()));

        assertEquals(new SecuredValue<byte[]>(new byte[] {1, 2, 3}), new SecuredValue<byte[]>(new byte[] {1, 2, 3}));
        assertNotEquals(new SecuredValue<byte[]>(new byte[] {1, 2, 3}), new SecuredValue<byte[]>(new byte[] {1, 2, 4}));

        assertEquals(new SecuredValue<Integer>(42), new SecuredValue<Integer>(42));
        assertNotEquals(new SecuredValue<Integer>(42), new SecuredValue<Integer>(43));

        SecuredValue<String> v1 = new SecuredValue<String>(SECRET, MASK);
        SecuredValue<String> v2 = new SecuredValue<String>(SECRET, MASK);
        assertEquals(v1.hashCode(), v2.hashCode());
    }


    /**
     * Setting a new value keeps the mask
     */
    @Test
    public void testSetValueKeepsMask() {
        final String mask = "...";
        SecuredValue<String> value = new SecuredValue<String>("secret");
        assertEquals(mask, value.toString());
        value.setValue("newSecret");
        assertEquals(mask, value.toString());
        assertEquals("newSecret", value.getValue());
        // without a mask the plain value is never returned
        assertEquals(mask, new SecuredValue<String>("x", null).setValue("plain").toString());
        assertEquals("", new SecuredValue<String>(null, null).toString());
    }


    /**
     * Destroy zeros char and byte array values and releases the value reference
     */
    @Test
    public void testDestroyWipesArrays() {
        char[] chars = SECRET.toCharArray();
        SecuredValue<char[]> charValue = new SecuredValue<char[]>(chars, MASK);
        charValue.destroy();
        assertTrue(charValue.isDestroyed());
        assertNull(charValue.getValue());
        assertArrayEquals(new char[SECRET.length()], chars);

        byte[] bytes = new byte[] {1, 2, 3};
        SecuredValue<byte[]> byteValue = new SecuredValue<byte[]>(bytes, MASK);
        byteValue.close();
        assertTrue(byteValue.isDestroyed());
        assertNull(byteValue.getValue());
        assertArrayEquals(new byte[3], bytes);

        SecuredValue<String> stringValue = new SecuredValue<String>(SECRET, MASK);
        stringValue.destroy();
        assertTrue(stringValue.isDestroyed());
        assertNull(stringValue.getValue());
        assertEquals(MASK, stringValue.toString());
    }
}
