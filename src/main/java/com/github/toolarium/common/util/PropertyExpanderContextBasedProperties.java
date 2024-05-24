/*
 * PropertyExpanderContextBasedProperties.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.util.Properties;


/**
 * Context based properties bind to the local thread.
 *
 * @author patrick
 */
public final class PropertyExpanderContextBasedProperties {
    private static final ThreadLocal<Properties> contextBasedPropertiesThreadLocal = new ThreadLocal<Properties>();


    /**
     * Constructor for LogInformationHolder
     */
    private PropertyExpanderContextBasedProperties() {
    }

    
    /**
     * Gets the context based properties bound on current thread.
     *
     * @return the context based properties bound on current thread.
     */
    public static Properties get() {
        return contextBasedPropertiesThreadLocal.get();
    }

    
    /**
     * Sets the context based properties bound on current thread.
     *
     * @param properties the context based properties bound on current thread.
     */
    public static void set(Properties properties) {
        contextBasedPropertiesThreadLocal.set(properties);
    }


    /**
     * Unset
     */
    public static void unset() {
        contextBasedPropertiesThreadLocal.remove();
    }
}
