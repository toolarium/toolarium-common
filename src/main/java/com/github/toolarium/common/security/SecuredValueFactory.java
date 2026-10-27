/*
 * SecuredValueFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security;

import com.github.toolarium.common.security.impl.SecuredSecretValue;


/**
 * Singleton factory for creating {@link ISecuredValue} instances.
 *
 * <p>Use {@link #create(Object)} for general-purpose masked values and
 * {@link #createSecret(byte[])} / {@link #createSecret(char[])} for secrets
 * that require off-heap memory protection.
 *
 * <pre>{@code
 * // general masked value
 * ISecuredValue<String> token = SecuredValueFactory.getInstance().create(apiToken);
 *
 * // off-heap secret — zeroed on close()
 * try (ISecuredSecretValue pw = SecuredValueFactory.getInstance().createSecret(passwordChars)) {
 *     SecretKeySpec key = pw.getValue().use(bytes -> new SecretKeySpec(bytes, "AES"));
 *     // ...
 * }
 * }</pre>
 *
 * @author patrick
 */
public final class SecuredValueFactory {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final SecuredValueFactory INSTANCE = new SecuredValueFactory();
    }


    /**
     * Private constructor
     */
    private SecuredValueFactory() {
    }


    /**
     * Get the instance
     *
     * @return the instance
     */
    public static SecuredValueFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Create a general-purpose {@link ISecuredValue} that masks the value in logs and
     * {@code toString()} with the default mask {@code "..."}.
     *
     * @param <T> the value type
     * @param value the value to wrap
     * @return a new {@link ISecuredValue}
     */
    public <T> ISecuredValue<T> create(T value) {
        return new com.github.toolarium.common.security.impl.SecuredValue<>(value);
    }


    /**
     * Create a general-purpose {@link ISecuredValue} with a custom masked display string.
     *
     * @param <T> the value type
     * @param value the value to wrap
     * @param maskedDisplay the string returned by {@code toString()} and {@code getSecuredValue()}
     * @return a new {@link ISecuredValue}
     */
    public <T> ISecuredValue<T> create(T value, String maskedDisplay) {
        return new com.github.toolarium.common.security.impl.SecuredValue<>(value, maskedDisplay);
    }


    /**
     * Create a secret-backed {@link ISecuredValue} from a byte array. The array is zeroed
     * immediately; the secret lives in off-heap memory until {@link ISecuredValue#close()} is
     * called. Mask defaults to {@code "..."}.
     *
     * @param secret the sensitive bytes; zeroed by this call
     * @return a new {@link ISecuredValue}
     * @throws NullPointerException if {@code secret} is {@code null}
     */
    public ISecuredSecretValue createSecret(byte[] secret) {
        return new SecuredSecretValue(secret);
    }


    /**
     * Create a secret-backed {@link ISecuredValue} from a byte array with a custom mask.
     *
     * @param secret the sensitive bytes; zeroed by this call
     * @param maskedDisplay the string returned by {@code toString()} and {@code getSecuredValue()}
     * @return a new {@link ISecuredValue}
     * @throws NullPointerException if {@code secret} is {@code null}
     */
    public ISecuredSecretValue createSecret(byte[] secret, String maskedDisplay) {
        return new SecuredSecretValue(secret, maskedDisplay);
    }


    /**
     * Create a secret-backed {@link ISecuredValue} from a char array. The array is zeroed
     * immediately; the secret lives in off-heap memory until {@link ISecuredValue#close()} is
     * called. Mask defaults to {@code "..."}.
     *
     * @param secret the sensitive characters; zeroed by this call
     * @return a new {@link ISecuredValue}
     * @throws NullPointerException if {@code secret} is {@code null}
     */
    public ISecuredSecretValue createSecret(char[] secret) {
        return new SecuredSecretValue(secret);
    }


    /**
     * Create a secret-backed {@link ISecuredValue} from a char array with a custom mask.
     *
     * @param secret the sensitive characters; zeroed by this call
     * @param maskedDisplay the string returned by {@code toString()} and {@code getSecuredValue()}
     * @return a new {@link ISecuredValue}
     * @throws NullPointerException if {@code secret} is {@code null}
     */
    public ISecuredSecretValue createSecret(char[] secret, String maskedDisplay) {
        return new SecuredSecretValue(secret, maskedDisplay);
    }
}
