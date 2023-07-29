/*
 * SecuredValue.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security;

import java.util.Objects;


/**
 * Implements the {@link ISecuredValue}.
 * 
 * @author patrick
 */
public class SecuredValue<T> implements ISecuredValue<T> {
    private static final long serialVersionUID = -7130513492378872813L;
    private T value;
    private String securedValue;

    
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
     * Sets the value
     *
     * @param value the value
     * @return the secured value
     */
    public SecuredValue<T> setValue(T value) {
        this.value = value;
        this.securedValue = null;
        return this;
    }

    
    /**
     * Sets the value
     *
     * @param value the value
     * @param securedValue the secured value
     * @return the secured value
     */
    public SecuredValue<T> setValue(T value, String securedValue) {
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(securedValue, value);
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
        return Objects.equals(securedValue, other.securedValue) && Objects.equals(value, other.value);
    }
    
    
    /**
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
        
        return "" + value;
    }
}
