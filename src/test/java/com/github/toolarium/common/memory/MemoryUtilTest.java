/*
 * MemoryUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * Test the {@link MemoryUtil} without the toolarium-java-agent (graceful degradation path).
 *
 * @author patrick
 */
public class MemoryUtilTest {

    /**
     * Agent is not loaded in the regular test JVM.
     */
    @Test
    public void testNotEnabled() {
        assertFalse(MemoryUtil.getInstance().isEnabled());
    }


    /**
     * null input always returns 0L regardless of agent presence.
     */
    @Test
    public void testMemoryUsageOfNull() {
        assertEquals(Long.valueOf(0L), MemoryUtil.getInstance().memoryUsageOf(null));
    }


    /**
     * Without agent, any non-null object returns null.
     */
    @Test
    public void testMemoryUsageOfObjectNotEnabled() {
        assertNull(MemoryUtil.getInstance().memoryUsageOf(new Object()));
        assertNull(MemoryUtil.getInstance().memoryUsageOf("hello"));
    }


    /**
     * Calculator is always set (defaults to {@link MemoryUsageCalculator}).
     */
    @Test
    public void testDefaultCalculator() {
        assertNotNull(MemoryUtil.getInstance().getMemoryUsageCalculator());
        assertTrue(MemoryUtil.getInstance().getMemoryUsageCalculator() instanceof MemoryUsageCalculator);
    }


    /**
     * Runtime memory figures are always available (no agent needed).
     */
    @Test
    public void testRuntimeMemoryInformation() {
        MemoryUtil.RuntimeMemoryInformation info = MemoryUtil.getInstance().getRuntimeMemoryInformation();
        assertTrue(info.getFreeMemory() > 0);
        assertTrue(info.getMaxMemory() > 0);
        assertTrue(info.getTotalMemory() > 0);
        assertTrue(info.getUsedMemory() >= 0);
        assertTrue(info.getTotalFreeMemory() > 0);
        assertEquals(info.getTotalMemory() - info.getFreeMemory(), info.getUsedMemory());
        assertEquals(info.getMaxMemory() - info.getUsedMemory(), info.getTotalFreeMemory());
    }
}
