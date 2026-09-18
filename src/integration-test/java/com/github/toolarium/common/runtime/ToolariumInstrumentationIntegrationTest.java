/*
 * ToolariumInstrumentationIntegrationTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.runtime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * Integration test for {@link ToolariumInstrumentation} requiring the toolarium-java-agent to be
 * loaded via {@code -javaagent:} in the JVM. Run via {@code ./gradlew integrationTest}.
 *
 * @author patrick
 */
public class ToolariumInstrumentationIntegrationTest {

    /**
     * Verify the agent is present and instrumentation is available.
     */
    @Test
    public void testAgentAvailable() {
        assertTrue(ToolariumInstrumentation.getInstance().isAgentAvailable());
        assertNotNull(ToolariumInstrumentation.getInstance().getInstrumentation());
    }


    /**
     * Verify object size is positive for a live object.
     */
    @Test
    public void testGetObjectSize() {
        long size = ToolariumInstrumentation.getInstance().getInstrumentation().getObjectSize(new Object());
        assertTrue(size > 0, "Object size should be positive when agent is available, got: " + size);
    }


    /**
     * Verify a String object size is positive.
     */
    @Test
    public void testGetStringSize() {
        long size = ToolariumInstrumentation.getInstance().getInstrumentation().getObjectSize("Hello, toolarium!");
        assertTrue(size > 0, "String size should be positive when agent is available, got: " + size);
    }

}
