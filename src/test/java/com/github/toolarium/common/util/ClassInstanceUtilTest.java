/*
 * ClassInstanceUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link ClassInstanceUtil}.
 *
 * @author patrick
 */
public class ClassInstanceUtilTest {

    /**
     * Test create array has the correct runtime component type
     */
    @Test
    public void testCreateArray() {
        String[] array = ClassInstanceUtil.getInstance().createArray(String.class, 2);
        assertEquals(2, array.length);
        assertEquals(String.class, array.getClass().getComponentType());
        assertNull(array[0]);

        Integer[] intArray = ClassInstanceUtil.getInstance().createArray(Integer.class, 3);
        assertEquals(3, intArray.length);
        assertEquals(Integer.class, intArray.getClass().getComponentType());
    }


    /**
     * Test create array with invalid input
     */
    @Test
    public void testCreateArrayInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ClassInstanceUtil.getInstance().createArray(null, 2));
        assertThrows(IllegalArgumentException.class, () -> ClassInstanceUtil.getInstance().createArray(String.class, 0));
        assertThrows(IllegalArgumentException.class, () -> ClassInstanceUtil.getInstance().createArray(int.class, 2));
    }

    /**
     * The availability check must not run static initializers
     */
    @Test
    public void testIsClassAvailableDoesNotInitialize() {
        assertTrue(ClassInstanceUtil.getInstance().isClassAvailable(FailingStaticInit.class.getName()));
    }


    /**
     * The availability check must use the context class loader first, as {@link ClassInstanceUtil#newInstance(String)} does
     */
    @Test
    public void testIsClassAvailableUsesContextClassLoader() {
        final String className = "com.github.toolarium.common.util.NotExistingContextClass";
        final List<String> requestedClassNames = new ArrayList<String>();
        ClassLoader contextClassLoader = new ClassLoader(ClassInstanceUtilTest.class.getClassLoader()) {
            /**
             * @see java.lang.ClassLoader#loadClass(java.lang.String)
             */
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                requestedClassNames.add(name);
                return super.loadClass(name);
            }
        };

        Thread thread = Thread.currentThread();
        ClassLoader previousClassLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(contextClassLoader);
            assertFalse(ClassInstanceUtil.getInstance().isClassAvailable(className));
            assertTrue(requestedClassNames.contains(className));
        } finally {
            thread.setContextClassLoader(previousClassLoader);
        }

        assertFalse(ClassInstanceUtil.getInstance().isClassAvailable(null));
        assertFalse(ClassInstanceUtil.getInstance().isClassAvailable(" "));
    }


    /**
     * The interface check must not run static initializers and must not throw
     */
    @Test
    public void testImplementsInterfaceDoesNotInitialize() {
        assertTrue(ClassInstanceUtil.getInstance().implementsInterface(FailingStaticInitRunnable.class.getName(), Runnable.class));
        assertFalse(ClassInstanceUtil.getInstance().implementsInterface(FailingStaticInitRunnable.class.getName(), AutoCloseable.class));
        assertFalse(ClassInstanceUtil.getInstance().implementsInterface("com.github.toolarium.common.util.NotExistingClass", Runnable.class));
        assertFalse(ClassInstanceUtil.getInstance().implementsInterface(FailingStaticInitRunnable.class.getName(), null));
    }


    /**
     * Class with a failing static initializer
     */
    static final class FailingStaticInit {
        static final int VALUE = fail();


        /**
         * Constructor for FailingStaticInit
         */
        private FailingStaticInit() {
        }


        /**
         * Fails always
         *
         * @return never returns
         * @throws IllegalStateException always
         */
        private static int fail() {
            throw new IllegalStateException("Static initializer must not run!");
        }
    }


    /**
     * Runnable with a failing static initializer
     */
    static final class FailingStaticInitRunnable implements Runnable {
        static final int VALUE = FailingStaticInit.fail();


        /**
         * Constructor for FailingStaticInitRunnable
         */
        private FailingStaticInitRunnable() {
        }


        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            // NOP
        }
    }
}
