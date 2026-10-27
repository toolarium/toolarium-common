/*
 * Assert.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;


/**
 * Utility class for assertion of input parameters. 
 * 
 * @author patrick
 */
public final class Assert {
    private static final String LOG_MESSAGE_HEADER = "ASSERT FAILED | ";
    private static final Logger LOG = LoggerFactory.getLogger(Assert.class);
    private static volatile Level level = Level.DEBUG;
    private static volatile boolean logStackTrace = false;

    
    /**
     * Private Constructor.
     */
    private Assert() {
        // NOP
    }


    /**
     * Set the log level which is used to log a failed assertion. The default is {@link Level#DEBUG}.
     *
     * @param logLevel the log level; null resets it to the default {@link Level#DEBUG}
     */
    public static void setLogLevel(Level logLevel) {
        if (logLevel == null) {
            level = Level.DEBUG;
        } else {
            level = logLevel;
        }
    }


    /**
     * Define if the stack trace of the exception is logged in case of a failed assertion. The default is false.
     *
     * @param logStackTraceOfException true to log the stack trace
     */
    public static void setLogStackTrace(boolean logStackTraceOfException) {
        logStackTrace = logStackTraceOfException;
    }

    
    /**
     * Assert that the input object must be true, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param expression the the expression
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isTrue(boolean expression, Class<T> exceptionClass, String exceptionMessage) throws T {
        isTrue(expression, exceptionClass, exceptionMessage, exceptionMessage);
    }

    
    /**
     * Assert that the input object must be true, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param expression the the expression
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isTrue(boolean expression, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        if (!expression) {
            T ex = newInstance(exceptionClass, exceptionMessage);
            
            if (logStackTrace) {
                LOG.atLevel(level).log(LOG_MESSAGE_HEADER + logMessage, ex);
            } else {
                LOG.atLevel(level).log(LOG_MESSAGE_HEADER + logMessage);
            }

            throw ex;
        }
    }

    
    /**
     * Assert that the input object must be false, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param expression the the expression
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isFalse(boolean expression, Class<T> exceptionClass, String exceptionMessage) throws T {
        isFalse(expression, exceptionClass, exceptionMessage, exceptionMessage);
    }

    
    /**
     * Assert that the input object must be false, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param expression the the expression
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isFalse(boolean expression, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(!expression, exceptionClass, exceptionMessage, logMessage);
    }

    
    /**
     * Assert that the input object is null, throw an exception otherwise.
     *
     * @param <T> the generic exception type
     * @param object the object to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNull(Object object, Class<T> exceptionClass, String exceptionMessage) throws T {
        isNull(object, exceptionClass, exceptionMessage, exceptionMessage);
    }


    /**
     * Assert that the input object must be null, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object the object to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNull(Object object, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isFalse((object != null), exceptionClass, exceptionMessage, logMessage);
    }


    /**
     * Assert that the input object is not null, throw an exception otherwise.
     *
     * @param <T> the generic exception type
     * @param object the object to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotNull(Object object, Class<T> exceptionClass, String exceptionMessage) throws T {
        isNotNull(object, exceptionClass, exceptionMessage, exceptionMessage);
    }


    /**
     * Assert that the input object must not be null, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object the object to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotNull(Object object, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isFalse((object == null), exceptionClass, exceptionMessage, logMessage);
    }

    
    /**
     * Assert that the input is null or empty, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object the object to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEmpty(Object object, Class<T> exceptionClass, String exceptionMessage) throws T {
        isEmpty(object, exceptionClass, exceptionMessage, exceptionMessage);
    }
   
    
    /**
     * Assert that the input is null or empty, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object the object to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEmpty(Object object, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        if (object == null) {
            return;
        }
        
        if (isEmptyCheckSupported(object)) {
            isTrue(isEmptyValue(object), exceptionClass, exceptionMessage, logMessage);
        }
    }

    
    /**
     * Assert that the input must not be null or empty, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object the object to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEmpty(Object object, Class<T> exceptionClass, String exceptionMessage) throws T {
        isNotEmpty(object, exceptionClass, exceptionMessage, exceptionMessage);
    }
     
    
    /**
     * Assert that the input must not be null or empty, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object the object to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEmpty(Object object, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isNotNull(object, exceptionClass, exceptionMessage, logMessage);

        if (isEmptyCheckSupported(object)) {
            isFalse(isEmptyValue(object), exceptionClass, exceptionMessage, logMessage);
        }
    }    

    
    /**
     * Check if the empty check is supported for the given object: {@link Collection}, {@link Map}, arrays and {@link CharSequence}.
     *
     * @param object the object to check
     * @return true if the empty check is supported
     */
    private static boolean isEmptyCheckSupported(Object object) {
        return object instanceof Collection || object instanceof Map || object instanceof CharSequence || (object != null && object.getClass().isArray());
    }

    
    /**
     * Check if the given object is empty. A char sequence which contains only whitespaces is considered as empty.
     *
     * @param object the object to check, see {@link #isEmptyCheckSupported(Object)}
     * @return true if the object is empty
     */
    private static boolean isEmptyValue(Object object) {
        if (object instanceof Collection) {
            return ((Collection<?>) object).isEmpty();
        } else if (object instanceof Map) {
            return ((Map<?, ?>) object).isEmpty();
        } else if (object instanceof CharSequence) {
            return object.toString().trim().isEmpty();
        } else if (object != null && object.getClass().isArray()) {
            return Array.getLength(object) == 0;
        }
        
        return false;
    }

    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(boolean object1, boolean object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(boolean object1, boolean object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(object1 == object2, exceptionClass, exceptionMessage, logMessage);
    }

    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(short object1, short object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(short object1, short object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(object1 == object2, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T  in case of error 
     */
    public static <T extends Exception> void isEqual(char object1, char object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }

        
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(char object1, char object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(object1 == object2, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(byte object1, byte object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(byte object1, byte object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(object1 == object2, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(int object1, int object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(int object1, int object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(object1 == object2, exceptionClass, exceptionMessage, logMessage);
    }

    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(long object1, long object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(long object1, long object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(object1 == object2, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error
     */
    public static <T extends Exception> void isEqual(float object1, float object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }

        
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(float object1, float object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(Float.compare(object1, object2) == 0, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(double object1, double object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
    
        
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(double object1, double object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(Double.compare(object1, object2) == 0, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(Object object1, Object object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isEqual(Object object1, Object object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        if (object1 == null) {
            // both null is equal
            isNull(object2, exceptionClass, exceptionMessage, logMessage);
            return;
        }

        isNotNull(object2, exceptionClass, exceptionMessage, logMessage);
        isTrue(object1.equals(object2), exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(boolean object1, boolean object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isNotEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(boolean object1, boolean object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(object1 != object2, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(short object1, short object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isNotEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(short object1, short object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(object1 != object2, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(char object1, char object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isNotEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(char object1, char object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(object1 != object2, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(byte object1, byte object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isNotEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(byte object1, byte object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(object1 != object2, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(int object1, int object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isNotEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(int object1, int object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(object1 != object2, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(long object1, long object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isNotEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(long object1, long object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(object1 != object2, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(float object1, float object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isNotEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error
     */
    public static <T extends Exception> void isNotEqual(float object1, float object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(Float.compare(object1, object2) != 0, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(double object1, double object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isNotEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(double object1, double object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        isTrue(Double.compare(object1, object2) != 0, exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(Object object1, Object object2, Class<T> exceptionClass, String exceptionMessage) throws T {
        isNotEqual(object1, object2, exceptionClass, exceptionMessage, exceptionMessage);
    }
        
    
    /**
     * Assert that the two input objects are not equal, throw an exception otherwise.
     * 
     * @param <T> the generic exception type
     * @param object1 the object 1 to validate
     * @param object2 the object 2 to validate
     * @param exceptionClass the exception class to throw if it fail
     * @param exceptionMessage the exception message
     * @param logMessage the message to log
     * @throws T in case of error 
     */
    public static <T extends Exception> void isNotEqual(Object object1, Object object2, Class<T> exceptionClass, String exceptionMessage, String logMessage) throws T {
        if (object1 == null) {
            isNotNull(object2, exceptionClass, exceptionMessage, logMessage);
            return;
        }

        isFalse(object1.equals(object2), exceptionClass, exceptionMessage, logMessage);
    }
    
    
    /**
     * Create a new exception instance
     *
     * @param <T> the generic exception type
     * @param exceptionClass the exception class
     * @param exceptionMessage the exception message
     * @return the exception instance
     * @throws RuntimeException In case of a runtime error
     */
    private static <T extends Exception> T newInstance(Class<T> exceptionClass, String exceptionMessage) {
        T exception = null;

        try {
            Class<?>[] constructorParameterTypes = new Class[1];
            constructorParameterTypes[0] = String.class;
            
            Constructor<T> ct = exceptionClass.getConstructor(constructorParameterTypes);
            if (ct != null) {
                Object[] arglist = new Object[1];
                arglist[0] = exceptionMessage;
                exception = ct.newInstance(arglist);
            }
        } catch (NoSuchMethodException e) {
            // NOP
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error occurred: " + e.getMessage(), e);
            }
            LOG.warn("Error occurred: " + e.getMessage());
            throw new RuntimeException(exceptionMessage, e);
        }

        if (exception == null) {
            try {
                exception = ClassInstanceUtil.getInstance().newInstance(exceptionClass);
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Error occurred: " + e.getMessage(), e);
                }
                LOG.warn("Error occurred: " + e.getMessage());
                throw new RuntimeException(exceptionMessage, e);
            }
        }
        
        removeInternalStackTraceElements(exception);
        return exception;
    }


    /**
     * Removes the leading stack trace elements of the exception construction (reflection, class instance utility)
     * and of this class, so that the stack trace of the exception starts at the caller of the assertion.
     *
     * @param exception the exception
     */
    private static void removeInternalStackTraceElements(Exception exception) {
        StackTraceElement[] elements = exception.getStackTrace();
        if (elements == null || elements.length == 0) {
            return;
        }

        // find the first element of this class
        int index = 0;
        while (index < elements.length && !isAssertStackTraceElement(elements[index])) {
            index++;
        }

        if (index >= elements.length) {
            return; // no element of this class found: keep the stack trace as it is
        }

        // skip all following elements of this class
        while (index < elements.length && isAssertStackTraceElement(elements[index])) {
            index++;
        }

        StackTraceElement[] newElements = new StackTraceElement[elements.length - index];
        System.arraycopy(elements, index, newElements, 0, newElements.length);
        exception.setStackTrace(newElements);
    }


    /**
     * Checks if a stack trace element belongs to this class (or an inner class of it).
     *
     * @param element the stack trace element
     * @return true if it belongs to this class
     */
    private static boolean isAssertStackTraceElement(StackTraceElement element) {
        final String className = element.getClassName();
        return Assert.class.getName().equals(className) || className.startsWith(Assert.class.getName() + "$");
    }
}
