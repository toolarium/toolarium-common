/*
 * SecuredValue.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security.impl;

import com.github.toolarium.common.security.ISecuredValue;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;


/**
 * Implements the {@link ISecuredValue}. Setting the value and destroying it are atomic (synchronized on the
 * instance), reading the value is lock free.
 *
 * @param <T> the value type
 * @author patrick
 */
public class SecuredValue<T> implements ISecuredValue<T> {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_MASK = "...";
    private transient volatile T value;
    private volatile String securedValue;
    private transient volatile boolean destroyed = false;


    /**
     * Constructor for SecuredValue
     *
     * @param value the value
     */
    public SecuredValue(T value) {
        setValue(value, DEFAULT_MASK);
    }


    /**
     * Constructor for SecuredValue
     *
     * @param value the value
     * @param securedValue the secured value
     */
    public SecuredValue(T value, String securedValue) {
        setValue(value, securedValue);
    }


    /**
     * @see com.github.toolarium.common.security.ISecuredValue#getValue()
     */
    @Override
    public T getValue() {
        return value;
    }


    /**
     * Sets the value, the current secured (masked) value is kept
     *
     * @param value the value
     * @return the secured value
     */
    public synchronized SecuredValue<T> setValue(T value) {
        this.value = value;
        return this;
    }


    /**
     * Sets the value
     *
     * @param value the value
     * @param securedValue the secured value
     * @return the secured value
     */
    public synchronized SecuredValue<T> setValue(T value, String securedValue) {
        this.value = value;
        this.securedValue = securedValue;
        return this;
    }


    /**
     * @see com.github.toolarium.common.security.ISecuredValue#hasSecuredValue()
     */
    @Override
    public boolean hasSecuredValue() {
        return (securedValue != null);
    }


    /**
     * @see com.github.toolarium.common.security.ISecuredValue#getSecuredValue()
     */
    @Override
    public String getSecuredValue() {
        return securedValue;
    }


    /**
     * Zeros a {@code char[]} or {@code byte[]} value and releases the reference to the value. Other value types
     * (e.g. an immutable {@link String}) can not be zeroed, only the reference is released.
     *
     * @see com.github.toolarium.common.security.ISecuredValue#destroy()
     */
    @Override
    public synchronized void destroy() {
        final Object currentValue = this.value;
        if (currentValue instanceof char[]) {
            Arrays.fill((char[]) currentValue, (char) 0);
        } else if (currentValue instanceof byte[]) {
            Arrays.fill((byte[]) currentValue, (byte) 0);
        }

        this.value = null;
        this.destroyed = true;
    }


    /**
     * @see javax.security.auth.Destroyable#isDestroyed()
     */
    @Override
    public boolean isDestroyed() {
        return destroyed;
    }


    /**
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        destroy();
    }


    /**
     * The hash code is based on the class and the secured (masked) value only, the plain value is not
     * included to not leak any information about it.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(getClass().getName(), securedValue);
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        SecuredValue<T> other = (SecuredValue<T>) obj;
        return Objects.equals(securedValue, other.securedValue) && valueEquals(value, other.value);
    }


    /**
     * Compares two values. Values of type {@link String}, {@code char[]} and {@code byte[]} are compared
     * by content in constant time (only the length may be revealed by timing), other values by {@link Objects#equals(Object, Object)}.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return true if the values are equal
     */
    private static boolean valueEquals(Object value1, Object value2) {
        if (value1 == value2) {
            return true;
        }

        if (value1 == null || value2 == null) {
            return false;
        }

        if (value1 instanceof String && value2 instanceof String) {
            return MessageDigest.isEqual(((String) value1).getBytes(StandardCharsets.UTF_8), ((String) value2).getBytes(StandardCharsets.UTF_8));
        }

        if (value1 instanceof byte[] && value2 instanceof byte[]) {
            return MessageDigest.isEqual((byte[]) value1, (byte[]) value2);
        }

        if (value1 instanceof char[] && value2 instanceof char[]) {
            char[] chars1 = (char[]) value1;
            char[] chars2 = (char[]) value2;
            if (chars1.length != chars2.length) {
                return false;
            }

            int result = 0;
            for (int i = 0; i < chars1.length; i++) {
                result |= chars1[i] ^ chars2[i];
            }
            return result == 0;
        }

        return Objects.equals(value1, value2);
    }


    /**
     * Returns the secured (masked) value. The plain value is never returned: without a secured value the default
     * mask {@code ...} is returned, for a null value an empty string.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (hasSecuredValue()) {
            return securedValue;
        }

        if (value == null) {
            return "";
        }

        return DEFAULT_MASK;
    }
}
