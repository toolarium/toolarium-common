/*
 * AssertTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;


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
}
