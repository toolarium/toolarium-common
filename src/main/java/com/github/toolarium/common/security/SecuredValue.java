/*
 * SecuredValue.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security;


/**
 * Deprecated backward-compatible stub. Use {@link SecuredValueFactory#create(Object)} instead.
 *
 * @param <T> the value type
 * @author patrick
 * @deprecated Use {@link SecuredValueFactory#create(Object)} or {@link SecuredValueFactory#create(Object, String)} instead.
 */
@Deprecated
public class SecuredValue<T> extends com.github.toolarium.common.security.impl.SecuredValue<T> {
    private static final long serialVersionUID = -7130513492378872814L;


    /**
     * Constructor for SecuredValue
     *
     * @param value the value
     */
    public SecuredValue(T value) {
        super(value);
    }


    /**
     * Constructor for SecuredValue
     *
     * @param value the value
     * @param securedValue the secured value
     */
    public SecuredValue(T value, String securedValue) {
        super(value, securedValue);
    }
}
