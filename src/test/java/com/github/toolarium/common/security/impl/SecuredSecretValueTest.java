/*
 * SecuredSecretValueTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.common.security.SecureSecret;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link SecuredSecretValue}.
 *
 * @author patrick
 */
public class SecuredSecretValueTest {
    private static final String MASK = "...";

    /**
     * The previous secret is destroyed when a new one is set
     */
    @Test
    public void testSetValueDestroysPreviousSecret() {
        try (SecuredSecretValue value = new SecuredSecretValue("old".toCharArray())) {
            SecureSecret previous = value.getValue();
            SecureSecret next = new SecureSecret("new".toCharArray());
            value.setValue(next, MASK);
            assertTrue(previous.isDestroyed());
            assertFalse(next.isDestroyed());
            assertEquals("new", value.useChars(String::new));

            // setting the same secret again must not destroy it
            value.setValue(next);
            assertFalse(next.isDestroyed());
        }
    }


    /**
     * After close a new secret must not be accepted, otherwise a live secret would be held while isDestroyed() returns true
     */
    @Test
    public void testSetValueAfterCloseIsRefused() {
        SecuredSecretValue value = new SecuredSecretValue("old".toCharArray());
        value.close();
        assertTrue(value.isDestroyed());

        SecureSecret next = new SecureSecret("new".toCharArray());
        try {
            assertThrows(IllegalStateException.class, () -> value.setValue(next));
            assertThrows(IllegalStateException.class, () -> value.setValue(next, MASK));
            assertTrue(value.isDestroyed());
            assertNull(value.getValue());
            assertFalse(next.isDestroyed());
        } finally {
            next.destroy();
        }
    }


    /**
     * After setValue(null) the value is destroyed and a new secret must not be accepted
     */
    @Test
    public void testSetValueAfterSetNullIsRefused() {
        SecuredSecretValue value = new SecuredSecretValue("old".toCharArray());
        SecureSecret old = value.getValue();
        value.setValue(null);
        assertTrue(value.isDestroyed());
        assertTrue(old.isDestroyed());

        SecureSecret next = new SecureSecret("new".toCharArray());
        try {
            assertThrows(IllegalStateException.class, () -> value.setValue(next));
            assertThrows(IllegalStateException.class, () -> value.setValue(next, MASK));
            assertNull(value.getValue());
            assertFalse(next.isDestroyed());
        } finally {
            next.destroy();
        }

        // a wrapper whose secret was destroyed directly does not accept a new secret either
        SecuredSecretValue value2 = new SecuredSecretValue("old".toCharArray());
        value2.getValue().destroy();
        SecureSecret next2 = new SecureSecret("new".toCharArray());
        try {
            assertThrows(IllegalStateException.class, () -> value2.setValue(next2));
        } finally {
            next2.destroy();
            value2.close();
        }
    }


    /**
     * A concurrent setValue and destroy must never leave a live secret in a destroyed value
     *
     * @throws InterruptedException in case of an interrupt
     */
    @Test
    public void testConcurrentSetValueAndDestroy() throws InterruptedException {
        for (int round = 0; round < 20; round++) {
            SecuredSecretValue value = new SecuredSecretValue("start".toCharArray());
            List<SecureSecret> accepted = Collections.synchronizedList(new ArrayList<SecureSecret>());
            Thread setter = new Thread(() -> {
                for (int i = 0; i < 1000; i++) {
                    SecureSecret next = new SecureSecret(("s" + i).toCharArray());
                    try {
                        value.setValue(next);
                        accepted.add(next);
                    } catch (IllegalStateException e) {
                        next.destroy();
                        return;
                    }
                }
            });
            setter.start();
            value.destroy();
            setter.join();

            assertTrue(value.isDestroyed());
            assertNull(value.getValue());
            for (SecureSecret secret : accepted) {
                assertTrue(secret.isDestroyed());
            }
        }
    }
}
