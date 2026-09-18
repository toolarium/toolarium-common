/*
 * MemoryUsageCalculator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.memory;

import com.github.toolarium.common.runtime.ToolariumInstrumentation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements {@link IMemoryUsageCalculator} by performing a deep object-graph traversal
 * using the JVM {@link java.lang.instrument.Instrumentation} provided by the
 * toolarium-java-agent. Requires the agent to be loaded via {@code -javaagent:}.
 *
 * <p>Traversal is iterative (stack-based) to avoid stack overflow on deep graphs.
 * Each object is measured once using identity-based deduplication via {@link IdentityHashMap}.
 * Primitive fields and static fields are not followed (their cost is already included in
 * the shallow size of the owning object). Primitive arrays are fully measured by their
 * shallow size. Object arrays are followed element by element.</p>
 *
 * @author patrick
 */
public class MemoryUsageCalculator implements IMemoryUsageCalculator {
    private static final Logger LOG = LoggerFactory.getLogger(MemoryUsageCalculator.class);
    private static final Object UNSAFE;
    private static final Method OBJECT_FIELD_OFFSET;
    private static final Method GET_OBJECT;

    static {
        Object u = null;
        Method offsetMethod = null;
        Method getMethod = null;
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            u = f.get(null);
            offsetMethod = unsafeClass.getMethod("objectFieldOffset", Field.class);
            getMethod = unsafeClass.getMethod("getObject", Object.class, long.class);
        } catch (Exception e) {
            LOG.debug("sun.misc.Unsafe not available: {}", e.getMessage());
        }
        UNSAFE = u;
        OBJECT_FIELD_OFFSET = offsetMethod;
        GET_OBJECT = getMethod;
    }


    /**
     * @see IMemoryUsageCalculator#memoryUsageOf(Object)
     */
    @Override
    public Long memoryUsageOf(Object obj) {
        if (obj == null) {
            return 0L;
        }

        if (!ToolariumInstrumentation.getInstance().isAgentAvailable()) {
            return null;
        }

        return deepSizeOf(obj);
    }


    /**
     * Perform the iterative deep traversal.
     *
     * @param root the root object
     * @return total deep size in bytes
     */
    private long deepSizeOf(Object root) {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
        Deque<Object> stack = new ArrayDeque<Object>();
        stack.push(root);
        long total = 0L;
        Instrumentation inst = ToolariumInstrumentation.getInstance().getInstrumentation();

        while (!stack.isEmpty()) {
            Object current = stack.pop();

            if (current == null || !visited.add(current)) {
                continue;
            }

            if (inst != null) {
                long size = inst.getObjectSize(current);
                if (size >= 0) {
                    total += size;
                }
            }

            Class<?> clazz = current.getClass();

            if (clazz.isArray()) {
                if (!clazz.getComponentType().isPrimitive()) {
                    int len = Array.getLength(current);
                    for (int i = 0; i < len; i++) {
                        Object elem = Array.get(current, i);
                        if (elem != null) {
                            stack.push(elem);
                        }
                    }
                }
                continue;
            }

            Class<?> c = clazz;
            while (c != null) {
                for (Field field : c.getDeclaredFields()) {
                    if (field.getType().isPrimitive() || Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    try {
                        field.setAccessible(true);
                        Object value = field.get(current);
                        if (value != null) {
                            stack.push(value);
                        }
                    } catch (Exception e) {
                        // field.setAccessible() fails for module-encapsulated JDK fields on Java 9+;
                        // fall back to sun.misc.Unsafe which bypasses module boundaries entirely.
                        if (UNSAFE != null && OBJECT_FIELD_OFFSET != null && GET_OBJECT != null) {
                            try {
                                long offset = (Long) OBJECT_FIELD_OFFSET.invoke(UNSAFE, field);
                                Object value = GET_OBJECT.invoke(UNSAFE, current, offset);
                                if (value != null) {
                                    stack.push(value);
                                }
                            } catch (Exception e2) {
                                LOG.debug("Could not access field {}.{} via Unsafe: {}", c.getName(), field.getName(), e2.getMessage());
                            }
                        } else {
                            LOG.debug("Could not access field {}.{}: {}", c.getName(), field.getName(), e.getMessage());
                        }
                    }
                }

                c = c.getSuperclass();
            }
        }

        return total;
    }
}
