/*
 * ISecuredValue.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security;

import java.io.Serializable;
import javax.security.auth.Destroyable;


/**
 * Defines a secured value
 *
 * @param <T> the type of the secured value
 * @author patrick
 */
public interface ISecuredValue<T> extends Serializable, AutoCloseable, Destroyable {

    /**
     * Get the value
     *
     * @return the value
     */
    T getValue();
    
    
    /**
     * Check if it has a secured value
     *
     * @return true if it has a secured value
     */
    boolean hasSecuredValue();
    
    
    /**
     * Get the secured value, e.g. to write into a log or a console
     *
     * @return the secured value
     */
    String getSecuredValue();


    /**
     * Zeros and releases any sensitive data held by this instance.
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    void close();


    /**
     * Zeros and releases any sensitive data held by this instance.
     *
     * @see javax.security.auth.Destroyable#destroy()
     */
    @Override
    void destroy();
}