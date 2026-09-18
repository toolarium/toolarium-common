/*
 * MemoryUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.memory;

import com.github.toolarium.common.runtime.ToolariumInstrumentation;


/**
 * Singleton facade for measuring the memory footprint of objects. Delegates deep
 * object-graph traversal to an {@link IMemoryUsageCalculator}. All methods degrade
 * gracefully when the toolarium-java-agent is not loaded: {@link #isEnabled()} returns
 * {@code false} and {@link #memoryUsageOf(Object)} returns {@code null} for non-null
 * input. Runtime memory statistics ({@link #getRuntimeMemoryInformation()}) are always
 * available regardless of agent presence.
 *
 * @author patrick
 */
public final class MemoryUtil {
    private volatile IMemoryUsageCalculator memoryUsageCalculator;

    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final MemoryUtil INSTANCE = new MemoryUtil();
    }


    /**
     * Constructor
     */
    private MemoryUtil() {
        memoryUsageCalculator = new MemoryUsageCalculator();
    }


    /**
     * Get the instance
     *
     * @return the instance
     */
    public static MemoryUtil getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Check if memory measurement is enabled (i.e. the toolarium-java-agent is loaded).
     *
     * @return true if the agent is available; otherwise false
     */
    public boolean isEnabled() {
        return ToolariumInstrumentation.getInstance().isAgentAvailable();
    }


    /**
     * Replace the memory usage calculator. Pass {@code null} to disable deep measurement.
     *
     * @param memoryUsageCalculator the new calculator, or {@code null}
     * @return the previous calculator
     */
    public IMemoryUsageCalculator setMemoryUsageCalculator(IMemoryUsageCalculator memoryUsageCalculator) {
        IMemoryUsageCalculator previous = this.memoryUsageCalculator;
        this.memoryUsageCalculator = memoryUsageCalculator;
        return previous;
    }


    /**
     * Get the current memory usage calculator.
     *
     * @return the calculator
     */
    public IMemoryUsageCalculator getMemoryUsageCalculator() {
        return memoryUsageCalculator;
    }


    /**
     * Get the deep memory usage of an object. Returns {@code 0L} for {@code null},
     * {@code null} when the agent is not loaded, and the deep object-graph size otherwise.
     *
     * @param obj the object to measure
     * @return the deep memory size in bytes, {@code 0L} for null input, or {@code null} if unavailable
     */
    public Long memoryUsageOf(Object obj) {
        if (memoryUsageCalculator != null) {
            return memoryUsageCalculator.memoryUsageOf(obj);
        }
        return null;
    }


    /**
     * Get a snapshot of the current JVM runtime memory figures.
     *
     * @return runtime memory information
     */
    public RuntimeMemoryInformation getRuntimeMemoryInformation() {
        return new RuntimeMemoryInformation();
    }


    /**
     * Snapshot of JVM heap memory figures derived from {@link Runtime#getRuntime()}.
     * Always available regardless of agent presence.
     *
     * @author patrick
     */
    public final class RuntimeMemoryInformation {

        /**
         * Constructor
         */
        RuntimeMemoryInformation() {
        }


        /**
         * Get the currently free memory within the allocated heap.
         *
         * @return free memory in bytes
         */
        public long getFreeMemory() {
            return Runtime.getRuntime().freeMemory();
        }


        /**
         * Get the maximum heap size (equivalent to {@code -Xmx}).
         *
         * @return max memory in bytes
         */
        public long getMaxMemory() {
            return Runtime.getRuntime().maxMemory();
        }


        /**
         * Get the total heap currently allocated by the JVM process.
         *
         * @return total memory in bytes
         */
        public long getTotalMemory() {
            return Runtime.getRuntime().totalMemory();
        }


        /**
         * Get the heap memory currently in use.
         *
         * @return used memory in bytes
         */
        public long getUsedMemory() {
            return getTotalMemory() - getFreeMemory();
        }


        /**
         * Get the remaining memory available before hitting {@code -Xmx}.
         *
         * @return total free memory in bytes
         */
        public long getTotalFreeMemory() {
            return getMaxMemory() - (getTotalMemory() - getFreeMemory());
        }
    }
}
