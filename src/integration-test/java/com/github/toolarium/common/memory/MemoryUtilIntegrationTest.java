/*
 * MemoryUtilIntegrationTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Integration test for {@link MemoryUtil} requiring the toolarium-java-agent to be
 * loaded via {@code -javaagent:}. Run via {@code ./gradlew integrationTest}.
 *
 * @author patrick
 */
public class MemoryUtilIntegrationTest {

    /**
     * Agent must be present for these tests to be meaningful.
     */
    @Test
    public void testEnabled() {
        assertTrue(MemoryUtil.getInstance().isEnabled());
    }


    /**
     * null always returns 0L even with agent present.
     */
    @Test
    public void testMemoryUsageOfNull() {
        assertEquals(Long.valueOf(0L), MemoryUtil.getInstance().memoryUsageOf(null));
    }


    /**
     * A plain Object has a small but positive size.
     */
    @Test
    public void testMemoryUsageOfObject() {
        Long size = MemoryUtil.getInstance().memoryUsageOf(new Object());
        assertNotNull(size);
        assertTrue(size > 0, "Expected positive size for Object, got: " + size);
    }


    /**
     * Deep size grows as unique (non-interned) objects are added to a list.
     * Uses {@code new Object()} instances to guarantee identity uniqueness — avoids
     * the JVM string-literal interning that would cause deduplication across calls.
     */
    @Test
    public void testDeepSizeGrowsWithListElements() {
        List<Object> withOne  = new ArrayList<Object>();
        List<Object> withFive = new ArrayList<Object>();

        withOne.add(new Object());
        withFive.add(new Object());
        withFive.add(new Object());
        withFive.add(new Object());
        withFive.add(new Object());
        withFive.add(new Object());

        final List<Object> empty = new ArrayList<Object>();
        Long sizeEmpty    = MemoryUtil.getInstance().memoryUsageOf(empty);
        Long sizeWithOne  = MemoryUtil.getInstance().memoryUsageOf(withOne);
        Long sizeWithFive = MemoryUtil.getInstance().memoryUsageOf(withFive);

        assertNotNull(sizeEmpty);
        assertNotNull(sizeWithOne);
        assertNotNull(sizeWithFive);
        assertTrue(sizeWithOne  > sizeEmpty,   "List with one element should exceed empty list");
        assertTrue(sizeWithFive > sizeWithOne, "List with five elements should exceed list with one");
    }


    /**
     * A composite object holding a reference has a larger deep size than a plain Object.
     * Uses a dedicated inner class to avoid any JVM sharing of field values.
     */
    @Test
    public void testDeepSizeCompositeObject() {
        Long objectSize   = MemoryUtil.getInstance().memoryUsageOf(new Object());
        Long compositeSize = MemoryUtil.getInstance().memoryUsageOf(new ObjectHolder(new Object()));

        assertNotNull(objectSize);
        assertNotNull(compositeSize);
        assertTrue(compositeSize > objectSize,
                "Composite object (holding a reference) should exceed a plain Object, got composite="
                + compositeSize + " vs object=" + objectSize);
    }


    /**
     * Runtime memory information is available and consistent with agent present.
     */
    @Test
    public void testRuntimeMemoryInformation() {
        MemoryUtil.RuntimeMemoryInformation info = MemoryUtil.getInstance().getRuntimeMemoryInformation();
        assertTrue(info.getFreeMemory() > 0);
        assertTrue(info.getMaxMemory() > 0);
        assertTrue(info.getTotalMemory() > 0);
        assertTrue(info.getUsedMemory() >= 0);
        assertTrue(info.getTotalFreeMemory() > 0);
    }


    /**
     * Simple holder used by {@link #testDeepSizeCompositeObject()}.
     * Holds one non-primitive field so its deep size exceeds a plain {@link Object}.
     */
    private static final class ObjectHolder {
        @SuppressWarnings("unused")
        private final Object ref;

        /**
         * Constructor
         *
         * @param ref the object reference to hold
         */
        ObjectHolder(Object ref) {
            this.ref = ref;
        }
    }
}
