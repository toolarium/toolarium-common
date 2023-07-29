/*
 * ISecuredValue.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security;

import java.io.Serializable;


/**
 * Defines a secured value
 * 
 * @author patrick
 */
public interface ISecuredValue<T> extends Serializable {

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
}