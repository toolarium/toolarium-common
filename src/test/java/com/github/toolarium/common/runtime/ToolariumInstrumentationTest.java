/*
 * ToolariumInstrumentationTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.instrument.Instrumentation;
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


    /**
     * A {@link LinkageError} while loading the agent class must lead to "not available" (twice, the second
     * load of a class with a failed static initializer throws a {@link NoClassDefFoundError}).
     */
    @Test
    public void testAgentClassLinkageError() {
        assertNull(ToolariumInstrumentation.resolveInstrumentation(FailingAgent.class.getName()));
        assertNull(ToolariumInstrumentation.resolveInstrumentation(FailingAgent.class.getName()));
    }


    /**
     * Agent class with a failing static initializer
     */
    static final class FailingAgent {
        static final int VALUE = fail();


        /**
         * Constructor for FailingAgent
         */
        private FailingAgent() {
        }


        /**
         * Get the instrumentation
         *
         * @return always null (not reached, the static initializer fails)
         */
        public static Instrumentation getInstrumentation() {
            return null;
        }


        /**
         * Fails always
         *
         * @return never returns
         * @throws IllegalStateException always
         */
        private static int fail() {
            throw new IllegalStateException("Static initializer of the agent fails!");
        }
    }
}
