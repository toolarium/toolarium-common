/*
 * SecuredSecretValue.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security.impl;

import com.github.toolarium.common.security.ISecuredSecretValue;
import com.github.toolarium.common.security.SecureSecret;
import java.util.Objects;
import java.util.function.Function;


/**
 * A {@link SecuredValue} backed by a {@link SecureSecret} for memory-safe secret storage.
 *
 * <p>This class combines the display-masking behaviour of {@link SecuredValue} with the
 * off-heap memory protection of {@link SecureSecret}. The actual secret is never stored
 * on the Java heap — it lives in native memory and is deterministically zeroed when
 * {@link #close()} or {@link #destroy()} is called.
 *
 * <p>Setting a secret and destroying it are atomic. Once destroyed (by {@link #close()}, {@link #destroy()},
 * {@code setValue(null)} or by destroying the current {@link SecureSecret}), no new secret can be set.
 *
 * <h2>Preferred usage</h2>
 * <pre>{@code
 * try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret(passwordChars)) {
 *     // safe logging: toString() returns the mask
 *     log.info("Credential: {}", secret);
 *
 *     // safe consumption — temporary copy is zeroed automatically
 *     SecretKeySpec key = secret.use(bytes -> new SecretKeySpec(bytes, "AES"));
 * }
 * // off-heap memory is zeroed
 * }</pre>
 *
 * @author patrick
 */
public class SecuredSecretValue extends SecuredValue<SecureSecret> implements ISecuredSecretValue {
    private static final long serialVersionUID = 1L;
    private static final String DESTROYED_MESSAGE = "SecuredSecretValue has been destroyed";


    /**
     * Constructor for SecuredSecretValue
     *
     * @param secret the sensitive bytes to protect; the array is zeroed by this constructor
     * @throws NullPointerException if {@code secret} is {@code null}
     */
    public SecuredSecretValue(byte[] secret) {
        super(new SecureSecret(secret), "...");
    }


    /**
     * Constructor for SecuredSecretValue
     *
     * @param secret the sensitive bytes to protect; the array is zeroed by this constructor
     * @param securedValue the masked display value
     * @throws NullPointerException if {@code secret} is {@code null}
     */
    public SecuredSecretValue(byte[] secret, String securedValue) {
        super(new SecureSecret(secret), securedValue);
    }


    /**
     * Constructor for SecuredSecretValue
     *
     * @param secret the sensitive characters to protect; the array is zeroed by this constructor
     * @throws NullPointerException if {@code secret} is {@code null}
     */
    public SecuredSecretValue(char[] secret) {
        super(new SecureSecret(secret), "...");
    }


    /**
     * Constructor for SecuredSecretValue
     *
     * @param secret the sensitive characters to protect; the array is zeroed by this constructor
     * @param securedValue the masked display value
     * @throws NullPointerException if {@code secret} is {@code null}
     */
    public SecuredSecretValue(char[] secret, String securedValue) {
        super(new SecureSecret(secret), securedValue);
    }


    /**
     * Passes a temporary {@code byte[]} copy of the secret to the given function, then
     * zeros the copy immediately after the function returns.
     *
     * @param <R> the return type of {@code fn}
     * @param fn the function to receive the temporary copy
     * @return the value returned by {@code fn}
     * @throws NullPointerException if {@code fn} is {@code null}
     * @throws IllegalStateException if this secret has already been destroyed
     */
    @Override
    public <R> R use(Function<byte[], R> fn) {
        Objects.requireNonNull(fn, "fn must not be null");
        SecureSecret s = getValue();
        if (s == null) {
            throw new IllegalStateException(DESTROYED_MESSAGE);
        }
        return s.use(fn);
    }


    /**
     * Passes a temporary {@code char[]} copy of the secret to the given function, then
     * zeros the copy immediately after the function returns.
     *
     * @param <R> the return type of {@code fn}
     * @param fn the function to receive the temporary copy
     * @return the value returned by {@code fn}
     * @throws NullPointerException if {@code fn} is {@code null}
     * @throws IllegalStateException if this secret has already been destroyed
     */
    @Override
    public <R> R useChars(Function<char[], R> fn) {
        Objects.requireNonNull(fn, "fn must not be null");
        SecureSecret s = getValue();
        if (s == null) {
            throw new IllegalStateException(DESTROYED_MESSAGE);
        }
        return s.useChars(fn);
    }


    /**
     * Sets a new secret: the previous secret is destroyed (wiped) immediately. A {@code null} secret destroys
     * this value.
     *
     * @param value the new secret
     * @return the secured value
     * @throws IllegalStateException if this value has already been closed or destroyed
     * @see com.github.toolarium.common.security.impl.SecuredValue#setValue(java.lang.Object)
     */
    @Override
    public synchronized SecuredValue<SecureSecret> setValue(SecureSecret value) {
        assertNotDestroyed();
        if (value == null) {
            destroy();
            return this;
        }

        destroyPreviousSecret(value);
        return super.setValue(value);
    }


    /**
     * Sets a new secret: the previous secret is destroyed (wiped) immediately. A {@code null} secret destroys
     * this value.
     *
     * @param value the new secret
     * @param securedValue the masked display value
     * @return the secured value
     * @throws IllegalStateException if this value has already been closed or destroyed
     * @see com.github.toolarium.common.security.impl.SecuredValue#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public synchronized SecuredValue<SecureSecret> setValue(SecureSecret value, String securedValue) {
        assertNotDestroyed();
        if (value == null) {
            destroy();
            return this;
        }

        destroyPreviousSecret(value);
        return super.setValue(value, securedValue);
    }


    /**
     * @see com.github.toolarium.common.security.ISecuredValue#destroy()
     */
    @Override
    public synchronized void destroy() {
        SecureSecret s = getValue();
        if (s != null && !s.isDestroyed()) {
            s.destroy();
        }
        super.destroy();
    }


    /**
     * @see javax.security.auth.Destroyable#isDestroyed()
     */
    @Override
    public boolean isDestroyed() {
        if (super.isDestroyed()) {
            return true;
        }
        SecureSecret s = getValue();
        return s == null || s.isDestroyed();
    }


    /**
     * Zeros and releases the secret by delegating to {@link #destroy()}.
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        destroy();
    }

    /**
     * Ensures that this value has not been closed or destroyed: a destroyed value must not hold a live secret again.
     * The current secret is null only while it is constructed or after it was destroyed.
     *
     * @throws IllegalStateException if this value has already been closed or destroyed
     */
    private void assertNotDestroyed() {
        SecureSecret current = getValue();
        if (super.isDestroyed() || (current != null && current.isDestroyed())) {
            throw new IllegalStateException(DESTROYED_MESSAGE);
        }
    }


    /**
     * Destroys the current secret in case it is replaced by another one.
     *
     * @param newValue the new secret
     */
    private void destroyPreviousSecret(SecureSecret newValue) {
        SecureSecret previous = getValue();
        if (previous != null && previous != newValue && !previous.isDestroyed()) {
            previous.destroy();
        }
    }
}
