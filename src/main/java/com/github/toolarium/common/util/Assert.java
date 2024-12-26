/*
 * Assert.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.lang.reflect.Constructor;
import java.util.Collection;
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
    private static Level level = Level.DEBUG;
    private static boolean logStackTrace = false;

    
    /**
     * Private Constructor.
     */
    private Assert() {
        // NOP
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
     * Assert that the input must not be null or empty, throw an exception otherwise.
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
     * Assert that the input must not be null or empty, throw an exception otherwise.
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
        
        if (object instanceof Collection) {
            isTrue((((Collection<?>) object).size() == 0), exceptionClass, exceptionMessage, logMessage);
        } else if (object instanceof String) {
            isTrue((((String) object).trim().length() == 0), exceptionClass, exceptionMessage, logMessage);
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

        if (object instanceof Collection) {
            isFalse((((Collection<?>) object).size() == 0), exceptionClass, exceptionMessage, logMessage);
        } else if (object instanceof String) {
            isFalse((((String) object).trim().length() == 0), exceptionClass, exceptionMessage, logMessage);
        }
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
            isNull(object2, exceptionClass, exceptionMessage, logMessage);
        } else {
            isNotNull(object2, exceptionClass, exceptionMessage, logMessage);
        }
        
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
            LOG.warn("Error ouccred: " + e.getMessage(), e);
            throw new RuntimeException(exceptionMessage);
        }

        if (exception == null) {
            try {
                exception = ClassInstanceUtil.getInstance().newInstance(exceptionClass);
            } catch (Exception e) {
                LOG.warn("Error ouccred: " + e.getMessage(), e);
                throw new RuntimeException(exceptionMessage);
            }
        }
        
        java.lang.StackTraceElement[] orgStackTraceElements = exception.getStackTrace();
        if (orgStackTraceElements != null && orgStackTraceElements.length > 2) {
            int shift = 0;
            for (int i = 0; i < orgStackTraceElements.length; i++) {
                if (Assert.class.getName().equals(orgStackTraceElements[i].getClassName()) && "newInstance".equals(orgStackTraceElements[i].getMethodName())) {
                    shift = i + 2;
                    break;
                }
            }
            
            if (shift > 0) {
                java.lang.StackTraceElement[] newStackTraceElements = new java.lang.StackTraceElement[orgStackTraceElements.length - shift];
                for (int i = 0; i < orgStackTraceElements.length - shift; i++) {
                    newStackTraceElements[i] = orgStackTraceElements[i + shift];
                }
                
                exception.setStackTrace(newStackTraceElements);
            }
        }
        
        return exception;
    }
}
