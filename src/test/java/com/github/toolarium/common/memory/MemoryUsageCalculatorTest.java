/*
 * MemoryUsageCalculatorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;


/**
 * Test the deep traversal of the {@link MemoryUsageCalculator} without an agent (each object counts 1 byte).
 *
 * @author patrick
 */
public class MemoryUsageCalculatorTest {

    /**
     * Test that runtime infrastructure objects and reference referents are not traversed.
     */
    @Test
    public void testInfrastructureNotTraversed() {
        Holder holder = new Holder();
        final Object referent = holder.reference.get(); // keep the referent strongly reachable from the test only
        Set<Object> measured = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
        long size = new MemoryUsageCalculator().deepSizeOf(holder, obj -> {
            measured.add(obj);
            return 1L;
        });

        assertEquals(measured.size(), size);
        assertTrue(measured.contains(holder));
        assertTrue(measured.contains(holder.text));
        assertTrue(measured.contains(holder.reference));
        assertFalse(measured.contains(holder.clazz));
        assertFalse(measured.contains(holder.thread));
        assertFalse(measured.contains(holder.classLoader));
        assertFalse(measured.contains(holder.module));
        assertFalse(measured.contains(referent));
    }


    /**
     * Test that a root which is a runtime infrastructure object is only measured shallow.
     */
    @Test
    public void testInfrastructureRoot() {
        assertEquals(1L, new MemoryUsageCalculator().deepSizeOf(Thread.currentThread(), obj -> 1L));
        assertEquals(1L, new MemoryUsageCalculator().deepSizeOf(String.class, obj -> 1L));
        assertEquals(0L, new MemoryUsageCalculator().deepSizeOf(null, obj -> 1L));
    }


    /**
     * Test the traversal of JDK classes (module-encapsulated fields) and the per-class cache.
     */
    @Test
    public void testJdkClasses() {
        List<String> list = new ArrayList<String>();
        list.add("value1");
        list.add("value2");
        list.add("value3");

        MemoryUsageCalculator calculator = new MemoryUsageCalculator();
        long size1 = calculator.deepSizeOf(list, obj -> 1L);
        long size2 = calculator.deepSizeOf(list, obj -> 1L);
        assertEquals(size1, size2);

        // at least the list, its element array and the three strings
        assertTrue(size1 >= 5, "Unexpected size: " + size1);

        // object array root: array, 2 distinct strings
        assertTrue(calculator.deepSizeOf(new Object[] {"a", "b", "a"}, obj -> 1L) >= 3);
    }


    /**
     * Test that object arrays (also typed and nested ones) and fields read by method handles are fully traversed.
     */
    @Test
    public void testArraysAndFields() {
        String a = new String("a");
        String b = new String("b");
        String[] typed = new String[] {a, null, b, a};
        Object[][] nested = new Object[][] {typed, new Object[] {typed}};
        ArrayHolder holder = new ArrayHolder(nested, b);

        Set<Object> measured = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
        long size = new MemoryUsageCalculator().deepSizeOf(holder, obj -> {
            measured.add(obj);
            return 1L;
        });

        assertEquals(measured.size(), size);
        assertTrue(measured.contains(holder));
        assertTrue(measured.contains(nested));
        assertTrue(measured.contains(nested[1]));
        assertTrue(measured.contains(typed));
        assertTrue(measured.contains(a));
        assertTrue(measured.contains(b));
    }


    /**
     * Test that a lambda created inside the JDK (hidden class with fields which may not be readable) is measured
     * without an error.
     */
    @Test
    public void testJdkLambda() {
        Comparator<String> comparator = Comparator.comparing(String::length);
        long size = new MemoryUsageCalculator().deepSizeOf(comparator, obj -> 1L);
        assertTrue(size >= 1, "Unexpected size: " + size);
    }


    /**
     * Test the class loader classification which decides where the field accessors are cached: classes of a parent
     * class loader (e.g. JDK classes) must not be cached in a {@link ClassValue}, since it would pin the class loader
     * of the library.
     *
     * @throws IOException in case of an I/O error
     */
    @Test
    public void testClassLoaderClassification() throws IOException {
        ClassLoader own = MemoryUsageCalculator.class.getClassLoader();
        assertTrue(MemoryUsageCalculator.isOwnOrChildClassLoader(own));
        assertFalse(MemoryUsageCalculator.isParentClassLoader(own));

        // JDK classes: bootstrap and platform class loader
        assertFalse(MemoryUsageCalculator.isOwnOrChildClassLoader(String.class.getClassLoader()));
        assertTrue(MemoryUsageCalculator.isParentClassLoader(String.class.getClassLoader()));
        if (own != null && own.getParent() != null) {
            assertFalse(MemoryUsageCalculator.isOwnOrChildClassLoader(own.getParent()));
            assertTrue(MemoryUsageCalculator.isParentClassLoader(own.getParent()));
        }

        try (URLClassLoader child = new URLClassLoader(new URL[0], own);
             URLClassLoader unrelated = new URLClassLoader(new URL[0], null)) {
            assertTrue(MemoryUsageCalculator.isOwnOrChildClassLoader(child));
            assertFalse(MemoryUsageCalculator.isParentClassLoader(child));
            assertFalse(MemoryUsageCalculator.isOwnOrChildClassLoader(unrelated));
            assertFalse(MemoryUsageCalculator.isParentClassLoader(unrelated));
        }
    }


    /**
     * Test holder
     */
    static class Holder {
        private Object clazz = String.class;
        private Object thread = Thread.currentThread();
        private Object classLoader = MemoryUsageCalculatorTest.class.getClassLoader();
        private Object module = String.class.getModule();
        private WeakReference<Object> reference = new WeakReference<Object>(new int[10]);
        private String text = "text";
    }


    /**
     * Holder of an array and a final field
     */
    static class ArrayHolder {
        private final Object array;
        private final String text;


        /**
         * Constructor for ArrayHolder
         *
         * @param array the array
         * @param text the text
         */
        ArrayHolder(Object array, String text) {
            this.array = array;
            this.text = text;
        }
    }
}
