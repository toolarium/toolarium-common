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
     * Constructor for PropertyExpanderContextBasedProperties
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
     * Unset. Should be called in a finally block (e.g. in servlet filters) to prevent
     * ThreadLocal leaks when threads are reused by a thread pool.
     */
    public static void unset() {
        contextBasedPropertiesThreadLocal.remove();
    }


    /**
     * Sets the context based properties, executes the runnable, and guarantees cleanup.
     * This is the preferred way to use context properties in thread-pool environments.
     * After the execution the previously bound properties are restored (or removed if there were none),
     * so nested calls do not affect the outer context.
     *
     * @param properties the context based properties
     * @param runnable the code to execute with the properties set
     */
    public static void runWith(Properties properties, Runnable runnable) {
        final Properties previous = get();
        set(properties);
        try {
            runnable.run();
        } finally {
            if (previous == null) {
                unset();
            } else {
                set(previous);
            }
        }
    }
}
