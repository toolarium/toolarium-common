/*
 * MemoryUsageCalculator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.memory;

import com.github.toolarium.common.runtime.ToolariumInstrumentation;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToLongFunction;
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
 * <p>The accessible fields are resolved once per class and cached. The cache never pins the class loader of this
 * library from a class of a parent or an unrelated class loader (e.g. JDK classes): classes of the own class loader
 * (or a child of it) are cached in a {@link ClassValue}, classes of a parent class loader in a map of this class
 * and classes of an unrelated class loader only during a single traversal. Instances of {@link Class},
 * {@link ClassLoader}, {@link Thread} and {@link Module} as well as the referents of
 * {@link Reference} instances are not followed, since they are shared runtime infrastructure.</p>
 *
 * <p>Limitation: the fields of classes which can neither be made accessible by reflection nor be read by
 * sun.misc.Unsafe can not be followed. This is the case for the fields of JDK hidden classes, e.g. the class of a
 * lambda created inside the JDK such as {@code Comparator.comparing(...)}: the objects captured by such a lambda are
 * not measured, only the shallow size of the lambda itself. There is no supported way to read these fields; the
 * affected class is logged once at debug level when its fields are resolved.</p>
 *
 * @author patrick
 */
public class MemoryUsageCalculator implements IMemoryUsageCalculator {
    private static final Logger LOG = LoggerFactory.getLogger(MemoryUsageCalculator.class);
    private static final Object UNSAFE;
    private static final Method OBJECT_FIELD_OFFSET;
    private static final MethodHandle GET_OBJECT;
    private static final MethodType GETTER_TYPE = MethodType.methodType(Object.class, Object.class);
    private static final ClassLoader OWN_CLASS_LOADER = MemoryUsageCalculator.class.getClassLoader();
    private static final Map<Class<?>, FieldAccessor[]> PARENT_LOADER_FIELD_ACCESSORS = new ConcurrentHashMap<Class<?>, FieldAccessor[]>();
    private static final ClassValue<FieldAccessor[]> FIELD_ACCESSORS = new ClassValue<FieldAccessor[]>() {
        /**
         * @see java.lang.ClassValue#computeValue(java.lang.Class)
         */
        @Override
        protected FieldAccessor[] computeValue(Class<?> type) {
            return resolveFieldAccessors(type);
        }
    };

    static {
        Object u = null;
        Method offsetMethod = null;
        MethodHandle getHandle = null;
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Object unsafe = f.get(null);
            Method offset = unsafeClass.getMethod("objectFieldOffset", Field.class);

            // (Object, long) -> Object, bound to the Unsafe instance
            MethodHandle get = MethodHandles.lookup().unreflect(unsafeClass.getMethod("getObject", Object.class, long.class)).bindTo(unsafe);
            u = unsafe;
            offsetMethod = offset;
            getHandle = get;
        } catch (Exception e) {
            LOG.debug("sun.misc.Unsafe not available: {}", e.getMessage());
        }
        UNSAFE = u;
        OBJECT_FIELD_OFFSET = offsetMethod;
        GET_OBJECT = getHandle;
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
        final Instrumentation inst = ToolariumInstrumentation.getInstance().getInstrumentation();
        return deepSizeOf(root, obj -> {
            if (inst == null) {
                return 0L;
            }
            return inst.getObjectSize(obj);
        });
    }


    /**
     * Perform the iterative deep traversal. Instances of {@link Class}, {@link ClassLoader}, {@link Thread} and
     * {@link Module} as well as the referents of {@link Reference} instances are not followed; if the root itself
     * is such an instance, only its shallow size is measured.
     *
     * @param root the root object
     * @param shallowSizeFunction the function to get the shallow size of an object
     * @return total deep size in bytes
     */
    long deepSizeOf(Object root, ToLongFunction<Object> shallowSizeFunction) {
        if (root == null) {
            return 0L;
        }

        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
        Map<Class<?>, FieldAccessor[]> traversalFieldAccessors = new IdentityHashMap<Class<?>, FieldAccessor[]>();
        Deque<Object> stack = new ArrayDeque<Object>();
        stack.push(root);
        long total = 0L;

        while (!stack.isEmpty()) {
            Object current = stack.pop();

            if (!visited.add(current)) {
                continue;
            }

            long size = shallowSizeFunction.applyAsLong(current);
            if (size >= 0) {
                total += size;
            }

            if (isNotTraversable(current)) {
                continue;
            }

            Class<?> clazz = current.getClass();
            if (clazz.isArray()) {
                if (!clazz.getComponentType().isPrimitive()) {
                    // every array with a non-primitive component type is an Object[]
                    for (Object element : (Object[]) current) {
                        push(stack, element);
                    }
                }
                continue;
            }

            FieldAccessor[] accessors = traversalFieldAccessors.get(clazz);
            if (accessors == null) {
                accessors = getFieldAccessors(clazz);
                traversalFieldAccessors.put(clazz, accessors);
            }

            for (FieldAccessor accessor : accessors) {
                push(stack, accessor.get(current));
            }
        }

        return total;
    }


    /**
     * Gets the field accessors of a class. They are cached depending on the class loader of the class, so that
     * the class loader of this library is not pinned by a class which may outlive it:
     * <ul>
     *   <li>own class loader or a child of it: cached in a {@link ClassValue} of the class</li>
     *   <li>bootstrap or a parent class loader (they outlive this class): cached in a map of this class</li>
     *   <li>unrelated class loader: not cached globally, only during the current traversal</li>
     * </ul>
     *
     * @param clazz the class
     * @return the field accessors
     */
    private static FieldAccessor[] getFieldAccessors(Class<?> clazz) {
        ClassLoader loader = clazz.getClassLoader();
        if (isOwnOrChildClassLoader(loader)) {
            return FIELD_ACCESSORS.get(clazz);
        }

        if (isParentClassLoader(loader)) {
            return PARENT_LOADER_FIELD_ACCESSORS.computeIfAbsent(clazz, MemoryUsageCalculator::resolveFieldAccessors);
        }

        return resolveFieldAccessors(clazz);
    }


    /**
     * Checks if a class loader is the class loader of this class or a child (descendant) of it.
     *
     * @param loader the class loader, null for the bootstrap class loader
     * @return true if it is the own or a child class loader
     */
    static boolean isOwnOrChildClassLoader(ClassLoader loader) {
        if (OWN_CLASS_LOADER == null) {
            return true; // loaded by the bootstrap class loader, which is never unloaded
        }

        ClassLoader l = loader;
        while (l != null) {
            if (l == OWN_CLASS_LOADER) {
                return true;
            }
            l = l.getParent();
        }
        return false;
    }


    /**
     * Checks if a class loader is the bootstrap class loader or a parent (ancestor) of the class loader of this class.
     *
     * @param loader the class loader, null for the bootstrap class loader
     * @return true if it is the bootstrap or a parent class loader
     */
    static boolean isParentClassLoader(ClassLoader loader) {
        if (loader == null) {
            return true;
        }

        ClassLoader l = OWN_CLASS_LOADER;
        while (l != null) {
            l = l.getParent();
            if (l == loader) {
                return true;
            }
        }
        return false;
    }


    /**
     * Pushes a value on the stack if it is not null and should be followed.
     *
     * @param stack the stack
     * @param value the value
     */
    private static void push(Deque<Object> stack, Object value) {
        if (value != null && !isNotTraversable(value)) {
            stack.push(value);
        }
    }


    /**
     * Checks if an object is part of the runtime infrastructure and should not be traversed.
     *
     * @param obj the object
     * @return true if the object should not be traversed
     */
    private static boolean isNotTraversable(Object obj) {
        return obj instanceof Class || obj instanceof ClassLoader || obj instanceof Thread || obj instanceof Module;
    }


    /**
     * Resolves the field accessors of a class (including all super classes): primitive and static fields are not
     * included, neither are the fields declared by {@link Reference} (referent, queue). Fields which can not be
     * accessed at all (e.g. fields of JDK hidden classes) are ignored and logged once per resolved class at debug level.
     *
     * @param clazz the class
     * @return the field accessors
     */
    private static FieldAccessor[] resolveFieldAccessors(Class<?> clazz) {
        List<FieldAccessor> result = new ArrayList<FieldAccessor>();
        List<String> unreadableFields = null;
        Class<?> c = clazz;
        while (c != null) {
            if (c != Reference.class) {
                Field[] fields;
                try {
                    fields = c.getDeclaredFields();
                } catch (RuntimeException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Could not get declared fields of " + c.getName() + ": " + e.getMessage());
                    }
                    fields = new Field[0];
                }

                for (Field field : fields) {
                    if (!field.getType().isPrimitive() && !Modifier.isStatic(field.getModifiers())) {
                        FieldAccessor accessor = createFieldAccessor(field);
                        if (accessor != null) {
                            result.add(accessor);
                        } else {
                            if (unreadableFields == null) {
                                unreadableFields = new ArrayList<String>();
                            }
                            unreadableFields.add(getFieldName(field));
                        }
                    }
                }
            }

            c = c.getSuperclass();
        }

        if (unreadableFields != null && LOG.isDebugEnabled()) {
            LOG.debug("Fields of class " + clazz.getName() + " can not be read, the objects referenced by them are not measured: " + unreadableFields);
        }

        return result.toArray(new FieldAccessor[0]);
    }


    /**
     * Creates a field accessor: by a method handle if the field can be made accessible, otherwise by
     * sun.misc.Unsafe (module-encapsulated JDK fields on Java 9+).
     *
     * @param field the field
     * @return the field accessor or null if the field can not be accessed
     */
    private static FieldAccessor createFieldAccessor(Field field) {
        try {
            if (field.trySetAccessible()) {
                return new FieldAccessor(getFieldName(field), MethodHandles.lookup().unreflectGetter(field).asType(GETTER_TYPE));
            }
        } catch (IllegalAccessException | RuntimeException e) {
            // e.g. SecurityException: try Unsafe
            if (LOG.isTraceEnabled()) {
                LOG.trace("Could not set field " + getFieldName(field) + " accessible: " + e.getMessage());
            }
        }

        if (UNSAFE != null && OBJECT_FIELD_OFFSET != null && GET_OBJECT != null) {
            try {
                long offset = (Long) OBJECT_FIELD_OFFSET.invoke(UNSAFE, field);
                return new FieldAccessor(getFieldName(field), MethodHandles.insertArguments(GET_OBJECT, 1, offset).asType(GETTER_TYPE));
            } catch (Exception e) {
                // e.g. UnsupportedOperationException for the fields of a hidden class
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Could not access field " + getFieldName(field) + " via Unsafe: " + e.getMessage());
                }
            }
        }

        return null;
    }


    /**
     * Gets the qualified name of a field.
     *
     * @param field the field
     * @return the declaring class name and the field name
     */
    private static String getFieldName(Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }


    /**
     * Reads the value of an object field by a method handle with the type {@code (Object)Object}: either a
     * field getter or a sun.misc.Unsafe read bound to the field offset.
     */
    private static final class FieldAccessor {
        private final String name;
        private final MethodHandle getter;


        /**
         * Constructor for FieldAccessor
         *
         * @param name the qualified field name
         * @param getter the getter with the type {@code (Object)Object}
         */
        FieldAccessor(String name, MethodHandle getter) {
            this.name = name;
            this.getter = getter;
        }


        /**
         * Gets the field value of an object.
         *
         * @param obj the object
         * @return the field value or null if it can not be read
         */
        Object get(Object obj) {
            try {
                return (Object) getter.invokeExact(obj);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Could not read field " + name + ": " + e.getMessage());
                }
                return null;
            }
        }
    }
}
