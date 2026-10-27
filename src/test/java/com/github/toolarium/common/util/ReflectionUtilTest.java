/*
 * ReflectionUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.common.statistic.StatisticCounter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Test the {@link ReflectionUtil}.
 * 
 * @author patrick
 */
public class ReflectionUtilTest {

    /**
     * Test
     *
     * @throws SecurityException In case of a security exceptiom
     * @throws InstantiationException In case the class can not be instantiated
     * @throws IllegalAccessException In case of illegal access
     */
    @Test
    public void test() throws SecurityException, InstantiationException, IllegalAccessException {
        ReflectionUtil.getInstance().disableAccessWarnings();
        ReflectionUtil.getInstance().newInstance(StatisticCounter.class);
    }

    /**
     * Class names are trimmed in both methods
     *
     * @throws ClassNotFoundException in case the class could not be found
     */
    @Test
    public void testClassNameIsTrimmed() throws ClassNotFoundException {
        String name = " " + StatisticCounter.class.getName() + " ";
        assertTrue(ReflectionUtil.getInstance().isClassAvailable(name));
        assertEquals(StatisticCounter.class, ReflectionUtil.getInstance().getClassObject(name));
    }


    /**
     * The error of the constructor is kept as cause
     */
    @Test
    public void testNewInstanceKeepsCause() {
        InstantiationException e = assertThrows(InstantiationException.class, () -> ReflectionUtil.getInstance().newInstance(FailingConstructor.class));
        assertTrue(e.getCause() instanceof IllegalStateException);
        assertTrue(e.getMessage().contains(FailingConstructor.MESSAGE), e.getMessage());
    }


    /**
     * A failing static initializer is reported as class not found and the error is kept as cause
     */
    @Test
    public void testGetClassObjectFailingStaticInitializer() {
        final String className = FailingStaticInit.class.getName();
        assertEquals(FailingStaticInit.class, ReflectionUtil.getInstance().getClassObjectWithoutInitialization(className));

        ClassNotFoundException e = assertThrows(ClassNotFoundException.class, () -> ReflectionUtil.getInstance().getClassObject(className));
        assertTrue(e.getCause() instanceof ExceptionInInitializerError, String.valueOf(e.getCause()));

        // the class is now in an erroneous state: the jvm reports a NoClassDefFoundError
        e = assertThrows(ClassNotFoundException.class, () -> ReflectionUtil.getInstance().getClassObject(className));
        assertTrue(e.getCause() instanceof NoClassDefFoundError, String.valueOf(e.getCause()));

        assertNull(ReflectionUtil.getInstance().getClassObjectWithoutInitialization("com.github.toolarium.common.util.NotExistingClass"));
        assertNull(ReflectionUtil.getInstance().getClassObjectWithoutInitialization(" "));
    }


    /**
     * A class with a failing constructor
     */
    static class FailingConstructor {
        static final String MESSAGE = "config missing";

        /**
         * Constructor
         *
         * @throws IllegalStateException always
         */
        FailingConstructor() {
            throw new IllegalStateException(MESSAGE);
        }
    }


    /**
     * A class with a failing static initializer
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
            throw new IllegalStateException("Static initializer failed!");
        }
    }


    /**
     * A class compiled for a newer java version is not available, no error is thrown (M07)
     *
     * @param tempDir the temp directory
     * @throws IOException in case of an I/O error
     */
    @Test
    public void testClassOfNewerJavaVersionIsNotAvailable(@TempDir Path tempDir) throws IOException {
        // class file header with major version 99, the JVM rejects it with an UnsupportedClassVersionError
        final String className = "Future99";
        Files.write(tempDir.resolve(className + ".class"), new byte[] {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, 0, 0, 0, 99, 0, 0});

        final ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader loader = new URLClassLoader(new URL[] {tempDir.toUri().toURL()}, null)) {
            Thread.currentThread().setContextClassLoader(loader);
            assertFalse(ReflectionUtil.getInstance().isClassAvailable(className));
            assertFalse(ClassInstanceUtil.getInstance().isClassAvailable(className));
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }
}
