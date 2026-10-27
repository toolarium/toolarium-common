/*
 * AssertTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;


/**
 * Test Asset
 *  
 * @author patrick
 */
public class AssertTest {
    private static final String LOG_MESSAGE = "log message";
    private static final String MY_MESSAGE = "my message";
    private static final long LONG_1 = 1;
    private static final long LONG_2 = 2;
    private List<String> notEmptylist = new ArrayList<String>();
    private List<String> emptyList = new ArrayList<String>();


    /**
     * Constructor for AssertTest
     */
    public AssertTest() {
        notEmptylist.add("Text");
    }

    
    /**
     * Test isTrue
     * 
     * @throws IOException in case of error
     */
    @Test
    public void testIsTrue() throws IOException {
        // positive tests
        Assert.isTrue(true, IOException.class, MY_MESSAGE);
        Assert.isTrue(true, IOException.class, MY_MESSAGE, LOG_MESSAGE);

        // negative tests
        try {
            Assert.isTrue(false, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isTrue(false, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
    }
    
    
    /**
     * Test isFalse
     * 
     * @throws IOException in case of error
     */
    @Test
    public void testIsFalse() throws IOException {
        // positive tests
        Assert.isFalse(false, IOException.class, MY_MESSAGE);
        Assert.isFalse(false, IOException.class, MY_MESSAGE, LOG_MESSAGE);

        // negative tests
        try {
            Assert.isFalse(true, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isFalse(true, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
    }

    
    /**
     * Test isNull
     * 
     * @throws IOException in case of error
     */
    @Test
    public void testIsNull() throws IOException {
        // positive tests
        Assert.isNull(null, IOException.class, MY_MESSAGE);
        Assert.isNull(null, IOException.class, MY_MESSAGE, LOG_MESSAGE);

        // negative tests
        try {
            Assert.isNull("", IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNull("", IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
    }

    
    /**
     * Test isNotNull
     * 
     * @throws IOException in case of error
     */
    @Test
    public void testIsNotNull() throws IOException {
        // positive tests
        Assert.isNotNull("", IOException.class, MY_MESSAGE);
        Assert.isNotNull("", IOException.class, MY_MESSAGE, LOG_MESSAGE);

        // negative tests
        try {
            Assert.isNotNull(null, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotNull(null, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
    }

    
    /**
     * Test isNotNull
     * 
     * @throws IOException in case of error
     */
    @Test
    public void testIsEmpty() throws IOException {
        // positive tests
        Assert.isEmpty(null, IOException.class, MY_MESSAGE);
        Assert.isEmpty(null, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isEmpty("", IOException.class, MY_MESSAGE);
        Assert.isEmpty("", IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isEmpty(emptyList, IOException.class, MY_MESSAGE);
        Assert.isEmpty(emptyList, IOException.class, MY_MESSAGE, LOG_MESSAGE);

        // negative tests
        try {
            Assert.isEmpty("fddf", IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEmpty("fddf", IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEmpty(notEmptylist, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEmpty(notEmptylist, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
    }

    
    /**
     * Test isNotNull
     * 
     * @throws IOException in case of error
     */
    @Test
    public void testIsNotEmpty() throws IOException {
        // positive tests
        Assert.isNotEmpty("tt", IOException.class, MY_MESSAGE);
        Assert.isNotEmpty("tt", IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isNotEmpty(notEmptylist, IOException.class, MY_MESSAGE);
        Assert.isNotEmpty(notEmptylist, IOException.class, MY_MESSAGE, LOG_MESSAGE);

        // negative tests
        try {
            Assert.isNotEmpty(null, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEmpty(null, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEmpty("", IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEmpty("", IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEmpty(emptyList, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEmpty(emptyList, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
    }


    /**
     * Test isEmpty / isNotEmpty with map, array and char sequence
     *
     * @throws IOException in case of error
     */
    @Test
    public void testIsEmptyMapArrayCharSequence() throws IOException {
        Map<String, String> emptyMap = new HashMap<String, String>();
        Map<String, String> notEmptyMap = new HashMap<String, String>();
        notEmptyMap.put("key", "value");

        // positive tests
        Assert.isEmpty(emptyMap, IOException.class, MY_MESSAGE);
        Assert.isEmpty(new String[0], IOException.class, MY_MESSAGE);
        Assert.isEmpty(new int[0], IOException.class, MY_MESSAGE);
        Assert.isEmpty(new StringBuilder("  "), IOException.class, MY_MESSAGE);
        Assert.isNotEmpty(notEmptyMap, IOException.class, MY_MESSAGE);
        Assert.isNotEmpty(new String[] {"a"}, IOException.class, MY_MESSAGE);
        Assert.isNotEmpty(new int[] {1}, IOException.class, MY_MESSAGE);
        Assert.isNotEmpty(new StringBuilder("a"), IOException.class, MY_MESSAGE);

        // negative tests
        try {
            Assert.isEmpty(notEmptyMap, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }

        try {
            Assert.isEmpty(new String[] {"a"}, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }

        try {
            Assert.isEmpty(new StringBuilder("a"), IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }

        try {
            Assert.isNotEmpty(emptyMap, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }

        try {
            Assert.isNotEmpty(new int[0], IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }

        try {
            Assert.isNotEmpty(new StringBuilder(" "), IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
    }


    /**
     * Test isEqual
     * 
     * @throws IOException in case of error
     */
    @Test
    public void testIsEqual() throws IOException {
        // positive tests
        Assert.isEqual(true, true, IOException.class, MY_MESSAGE);
        Assert.isEqual(true, true, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isEqual((short) 1, (short) 1, IOException.class, MY_MESSAGE);
        Assert.isEqual((short) 1, (short) 1, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isEqual('k', 'k', IOException.class, MY_MESSAGE);
        Assert.isEqual('k', 'k', IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isEqual((byte) 0x33, (byte) 0x33, IOException.class, MY_MESSAGE);
        Assert.isEqual((byte) 0x33, (byte) 0x33, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isEqual(1, 1, IOException.class, MY_MESSAGE);
        Assert.isEqual(1, 1, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isEqual(LONG_1, LONG_1, IOException.class, MY_MESSAGE);
        Assert.isEqual(LONG_1, LONG_1, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isEqual(42.1f, 42.1f, IOException.class, MY_MESSAGE);
        Assert.isEqual(42.1f, 42.1f, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isEqual(42.3d, 42.3d, IOException.class, MY_MESSAGE);
        Assert.isEqual(42.3d, 42.3d, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isEqual("A", "A", IOException.class, MY_MESSAGE);
        Assert.isEqual("B", "B", IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isEqual(notEmptylist, notEmptylist, IOException.class, MY_MESSAGE);
        Assert.isEqual(notEmptylist, notEmptylist, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isEqual(emptyList, emptyList, IOException.class, MY_MESSAGE);
        Assert.isEqual(emptyList, emptyList, IOException.class, MY_MESSAGE, LOG_MESSAGE);

        // negative tests
        try {
            Assert.isEqual(true, false, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(true, false, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(false, true, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(false, true, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual((short) 1, (short) 2, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual((short) 1, (short) 2, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual('k', 'h', IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual('k', 'h', IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual((byte) 0x33, (byte) 0x34, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual((byte) 0x33, (byte) 0x34, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(1, 2, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(1, 2, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(LONG_1, LONG_2, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(LONG_1, LONG_2, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(42.1f, 42.2f, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(42.1f, 42.2f, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(42.3d, 42.4d, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(42.3d, 42.4d, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual("C", "C1", IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual("D", "D1", IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(emptyList, notEmptylist, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isEqual(emptyList, notEmptylist, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
    }


    /**
     * Test isEqual
     * 
     * @throws IOException in case of error
     */
    @Test
    public void testIsNotEqual() throws IOException {
        // positive tests
        Assert.isNotEqual(true, false, IOException.class, MY_MESSAGE);
        Assert.isNotEqual(true, false, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isNotEqual(false, true, IOException.class, MY_MESSAGE);
        Assert.isNotEqual(false, true, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isNotEqual((short) 1, (short) 2, IOException.class, MY_MESSAGE);
        Assert.isNotEqual((short) 1, (short) 2, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isNotEqual('k', 'h', IOException.class, MY_MESSAGE);
        Assert.isNotEqual('k', 'h', IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isNotEqual((byte) 0x33, (byte) 0x34, IOException.class, MY_MESSAGE);
        Assert.isNotEqual((byte) 0x33, (byte) 0x34, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isNotEqual(1, 2, IOException.class, MY_MESSAGE);
        Assert.isNotEqual(1, 2, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isNotEqual(LONG_1, LONG_2, IOException.class, MY_MESSAGE);
        Assert.isNotEqual(LONG_1, LONG_2, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isNotEqual(42.1f, 42.2f, IOException.class, MY_MESSAGE);
        Assert.isNotEqual(42.1f, 42.2f, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isNotEqual(42.3d, 42.4d, IOException.class, MY_MESSAGE);
        Assert.isNotEqual(42.3d, 42.4d, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isNotEqual("E", "E1", IOException.class, MY_MESSAGE);
        Assert.isNotEqual("F", "F1", IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isNotEqual(notEmptylist, emptyList, IOException.class, MY_MESSAGE);
        Assert.isNotEqual(notEmptylist, emptyList, IOException.class, MY_MESSAGE, LOG_MESSAGE);
        Assert.isNotEqual(emptyList, notEmptylist, IOException.class, MY_MESSAGE);
        Assert.isNotEqual(emptyList, notEmptylist, IOException.class, MY_MESSAGE, LOG_MESSAGE);

        // negative tests
        try {
            Assert.isNotEqual(true, true, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(true, true, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual((short) 1, (short) 1, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual((short) 1, (short) 1, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual('k', 'k', IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual('k', 'k', IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual((byte) 0x33, (byte) 0x33, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual((byte) 0x33, (byte) 0x33, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(1, 1, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(1, 1, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(LONG_1, LONG_1, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(LONG_1, LONG_1, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(42.1f, 42.1f, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(42.1f, 42.1f, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(42.3d, 42.3d, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(42.3d, 42.3d, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual("G", "G", IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual("H", "H", IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(emptyList, emptyList, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(emptyList, emptyList, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(notEmptylist, notEmptylist, IOException.class, MY_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
        
        try {
            Assert.isNotEqual(notEmptylist, notEmptylist, IOException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IOException e) { /* NOP */ }
    }

    
    /**
     * Test own exception
     */
    @Test
    public void testIllegalArgumentException() {
        try {
            Assert.isEqual(emptyList, notEmptylist, IllegalArgumentException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (IllegalArgumentException e) {
            // NOP
        }
    }

    
    /**
     * Test own exception
     */
    @Test
    public void testTestException() {
        try {
            Assert.isEqual(emptyList, notEmptylist, TestException.class, MY_MESSAGE, LOG_MESSAGE);
            fail();
        } catch (TestException e) {
            // NOP
        }
    }


    /**
     * Test the log configuration of a failed assertion
     */
    @Test
    public void testLogConfiguration() {
        try {
            Assert.setLogLevel(Level.WARN);
            Assert.setLogStackTrace(true);
            assertThrows(TestException.class, () -> Assert.isTrue(false, TestException.class, MY_MESSAGE, LOG_MESSAGE));
        } finally {
            Assert.setLogLevel(null);
            Assert.setLogStackTrace(false);
        }
    }


    /**
     * Two null objects are equal
     */
    @Test
    public void testIsEqualObjectBothNull() {
        assertDoesNotThrow(() -> Assert.isEqual((Object) null, (Object) null, TestException.class, MY_MESSAGE, LOG_MESSAGE));
        assertThrows(TestException.class, () -> Assert.isEqual((Object) null, (Object) MY_MESSAGE, TestException.class, MY_MESSAGE, LOG_MESSAGE));
        assertThrows(TestException.class, () -> Assert.isEqual((Object) MY_MESSAGE, (Object) null, TestException.class, MY_MESSAGE, LOG_MESSAGE));
    }


    /**
     * The cause must be kept in case the exception can not be created
     */
    @Test
    public void testExceptionCreationFailureKeepsCause() {
        RuntimeException e = assertThrows(RuntimeException.class, () -> Assert.isTrue(false, FailingException.class, MY_MESSAGE, LOG_MESSAGE));
        assertEquals(MY_MESSAGE, e.getMessage());
        assertNotNull(e.getCause());
    }


    /**
     * The stack trace of the thrown exception starts at the caller of the assertion, without internal frames
     */
    @Test
    public void testStackTraceStartsAtCaller() {
        TestException e1 = assertThrows(TestException.class, () -> Assert.isTrue(false, TestException.class, MY_MESSAGE));
        assertStackTraceStartsAtCaller(e1);

        TestException e2 = assertThrows(TestException.class, () -> Assert.isEqual((Object) null, (Object) MY_MESSAGE, TestException.class, MY_MESSAGE, LOG_MESSAGE));
        assertStackTraceStartsAtCaller(e2);

        // exception without a message constructor
        NoMessageException e3 = assertThrows(NoMessageException.class, () -> Assert.isTrue(false, NoMessageException.class, MY_MESSAGE));
        assertStackTraceStartsAtCaller(e3);
    }


    /**
     * Asserts that the stack trace of an exception starts at this class and contains no element of {@link Assert}
     *
     * @param e the exception
     */
    private static void assertStackTraceStartsAtCaller(Exception e) {
        StackTraceElement[] elements = e.getStackTrace();
        assertEquals(AssertTest.class.getName(), elements[0].getClassName());
        for (StackTraceElement element : elements) {
            assertFalse(Assert.class.getName().equals(element.getClassName()), "Unexpected element: " + element);
        }
    }


    /**
     * Exception without a message constructor
     */
    public static class NoMessageException extends Exception {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for NoMessageException
         */
        public NoMessageException() {
            super();
        }
    }


    /**
     * Exception which can not be created
     */
    public static class FailingException extends Exception {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for FailingException
         *
         * @param message the message
         * @throws IllegalStateException always
         */
        public FailingException(String message) {
            super(message);
            throw new IllegalStateException("Can not be created!");
        }
    }
}
