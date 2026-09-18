/*
 * IMemoryUsageCalculator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.memory;


/**
 * Defines the memory usage calculator.
 *
 * @author patrick
 */
public interface IMemoryUsageCalculator {

    /**
     * Get the deep memory usage of an object, following its entire object graph.
     * Returns {@code 0L} for {@code null}, or {@code null} when instrumentation
     * is not available (no agent loaded).
     *
     * @param obj the object to measure
     * @return the deep memory size in bytes, {@code 0L} for null, or {@code null} if unavailable
     */
    Long memoryUsageOf(Object obj);
}
