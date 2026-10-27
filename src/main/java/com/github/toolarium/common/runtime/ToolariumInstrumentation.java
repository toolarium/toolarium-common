/*
 * ToolariumInstrumentation.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.runtime;

import com.github.toolarium.common.util.ReflectionUtil;
import java.lang.instrument.Instrumentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility to optionally access the toolarium-java-agent's {@link Instrumentation} instance
 * when the agent is loaded via {@code -javaagent:}. All methods degrade gracefully when the
 * agent is absent: no hard compile dependency on the agent jar is required.
 *
 * @author patrick
 */
public final class ToolariumInstrumentation {
    private static final String AGENT_CLASS = "com.github.toolarium.java.agent.InstrumentationAgent";
    private static final Logger LOG = LoggerFactory.getLogger(ToolariumInstrumentation.class);
    private final Instrumentation instrumentation;

    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ToolariumInstrumentation INSTANCE = new ToolariumInstrumentation();
    }


    /**
     * Constructor
     */
    private ToolariumInstrumentation() {
        instrumentation = resolveInstrumentation(AGENT_CLASS);
    }


    /**
     * Get the instance
     *
     * @return the instance
     */
    public static ToolariumInstrumentation getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Check if the toolarium-java-agent is available and has provided an {@link Instrumentation} instance.
     *
     * @return true if the agent is present and instrumentation is available; otherwise false
     */
    public boolean isAgentAvailable() {
        return getInstrumentation() != null;
    }


    /**
     * Get the {@link Instrumentation} instance provided by the toolarium-java-agent, or {@code null}
     * if the agent is not loaded.
     *
     * @return the instrumentation instance or {@code null}
     */
    public Instrumentation getInstrumentation() {
        return instrumentation;
    }


    /**
     * Reflectively resolve the {@link Instrumentation} from the agent class. A {@link LinkageError} while loading
     * or initializing the agent class (e.g. {@link NoClassDefFoundError}) is handled as "not available".
     *
     * @param agentClassName the name of the agent class
     * @return the instrumentation or {@code null}
     */
    static Instrumentation resolveInstrumentation(String agentClassName) {
        try {
            if (!ReflectionUtil.getInstance().isClassAvailable(agentClassName)) {
                LOG.debug("toolarium-java-agent not found on classpath, instrumentation not available.");
                return null;
            }

            Object result = ReflectionUtil.getInstance().callMethod(null, agentClassName, "getInstrumentation", null, null);
            if (result instanceof Instrumentation) {
                LOG.debug("toolarium-java-agent instrumentation available.");
                return (Instrumentation) result;
            }
        } catch (LinkageError | Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to access toolarium-java-agent instrumentation: " + e.getMessage(), e);
            }
            LOG.warn("Failed to access toolarium-java-agent instrumentation: " + e.getMessage());
        }
        return null;
    }
}
