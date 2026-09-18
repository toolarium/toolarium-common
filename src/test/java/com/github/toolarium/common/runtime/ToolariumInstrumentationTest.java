/*
 * ToolariumInstrumentationTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;


/**
 * Test the {@link ToolariumInstrumentation}.
 *
 * @author patrick
 */
public class ToolariumInstrumentationTest {

    /**
     * Test graceful degradation when the agent is not loaded (no -javaagent set in test JVM).
     */
    @Test
    public void testAgentNotAvailable() {
        assertFalse(ToolariumInstrumentation.getInstance().isAgentAvailable());
        assertNull(ToolariumInstrumentation.getInstance().getInstrumentation());
    }
}
