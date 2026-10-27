/*
 * ExceptionWrapperTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;


/**
 * Test the {@link ExceptionWrapper}.
 * 
 * @author patrick
 */
public class ExceptionWrapperTest {

    /**
     * Test
     */
    @Test
    public void testLogWarnException() {
        IllegalArgumentException e = new IllegalArgumentException("My message.");
        Exception ex = ExceptionWrapper.getInstance().convertException(e, java.lang.Exception.class, Level.WARN);
        checkExceptionCheck(e, ex);
    }        

    
    /**
     * Test
     */
    @Test
    public void testConvertException() {
        IllegalArgumentException e = new IllegalArgumentException("My message.");
        Exception ex = ExceptionWrapper.getInstance().convertException(e, java.lang.Exception.class);
        checkExceptionCheck(e, ex);
    }        
    
    
    /**
     * Test
     */
    @Test
    public void testConvertRuntimeException() {
        IllegalArgumentException e = new IllegalArgumentException();
        NumberFormatException ex = ExceptionWrapper.getInstance().convertException(e, java.lang.NumberFormatException.class);
        checkExceptionCheck(e, ex);
    }    

    
    /**
     * Test
     */
    @Test
    public void testConvertExceptionWithMessage() {
        IllegalArgumentException e = new IllegalArgumentException("My message.");
        NumberFormatException ex = ExceptionWrapper.getInstance().convertException(e, java.lang.NumberFormatException.class);
        checkExceptionCheck(e, ex);
    }

    
    /**
     * Test
     */
    @Test
    public void testConvertExceptionIntoRuntimeException() {
        Exception e = new Exception("My message.");
        Exception ex = ExceptionWrapper.getInstance().convertException(e, java.lang.IllegalArgumentException.class);
        checkExceptionCheck(e, ex);
    }
    
    
    /**
     * Test
     */
    @Test
    public void testConvertComplexExceptionIntoRuntimeException() {
        Exception e = new Exception("My message.", new IllegalArgumentException("LL"));
        Throwable ex = ExceptionWrapper.getInstance().convertException(e, java.lang.Throwable.class);
        checkExceptionCheck(e, ex);
    }    
    
    
    /**
     * Test
     */
    @Test
    public void testConvertOwnException() {
        IllegalArgumentException e = new IllegalArgumentException();
        IllegalArgumentException ex = ExceptionWrapper.getInstance().convertException(e, IllegalArgumentException.class);
        checkExceptionCheck(e, ex);
    }    
    
    
    /**
     * Checks a given exception 
     * 
     * @param t the exception
     * @param newEx the exception to compare 
     * @return the new exception
     */
    private Throwable checkExceptionCheck(Throwable t, Throwable newEx) {
        final String newExceptionClass = newEx.getClass().getName();
        assertNotNull(newEx);
        assertEquals(newEx.getMessage(), t.getMessage());
        assertEquals(newEx.getLocalizedMessage(), t.getLocalizedMessage());
        assertSame(t, newEx.getCause());
        assertEquals(newEx.toString(), t.toString().replace(t.getClass().getName(), newExceptionClass));
        assertEquals(newEx.getClass().getName(), newExceptionClass);
        return newEx;
    }


    /**
     * The default exception handler can be created and extended
     *
     * @throws Exception in case of error
     */
    @Test
    public void testCreateDefaultExceptionHandler() throws Exception {
        IllegalStateException e = new IllegalStateException("test");
        IllegalArgumentException ex = new ExceptionWrapper.DefaultExceptionHandler().createExceptionInstance(e, IllegalArgumentException.class);
        checkExceptionCheck(e, ex);
    }


    /**
     * Converting no exception (null) returns a new exception with a message and without a cause
     */
    @Test
    public void testConvertNullException() {
        IllegalStateException ex = ExceptionWrapper.getInstance().convertException(null, IllegalStateException.class);
        assertNotNull(ex);
        assertEquals("No exception given", ex.getMessage());
        assertNull(ex.getCause());

        RuntimeException rex = ExceptionWrapper.getInstance().convertException(null, RuntimeException.class, Level.WARN);
        assertEquals("No exception given", rex.getMessage());
        assertNull(rex.getCause());
    }
}
