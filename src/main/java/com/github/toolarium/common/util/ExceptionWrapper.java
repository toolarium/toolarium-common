/*
 * ExceptionWrapper.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;


/**
 * This class implements the mechanism to wrap exceptions. The aim of this helper class is to minimize exception handling code blocks.
 * The following code can be simplified by this class from:
 * <pre>
 * try {
 *     ...
 * } catch(XYException e) {
 *      IllegalargumentException ex = new IllegalargumentException(e. getMessage());
 *      ex.setStackTrace(e.getStackTrace());
 *      
 *      // optional logging
 *      log.error("Exception " + e.getClass().getName() + " occured: " + e.getMessage(), e);
 *      
 *      throw ex;
 * }
 * </pre>
 * <br><br>
 * to:
 * <pre>
 * try {
 *     ...
 * } catch(XYException e) {
 *      throw (IllegalArgumentException)
 *          ExceptionWrapper.convertException(e, IllegalArgumentException.class.getName());
 * }
 * </pre>
 * <br><br>
 * To support logging in the exception case add the log level. There are
 * additional parameters to manipulate the log behavior. 
 * <pre>
 * try {
 *     ...
 * } catch(XYException e) {
 *      throw (IllegalArgumentException)
 *          ExceptionWrapper.convertException(e, IllegalArgumentException.class.getName(), org.slf4j.event.Level.WARN);
 * }
 * </pre>
 * <br><br>
 * To change the behavior how an exception will be converted just implement
 * your own ExceptionWrapper.ExceptionHandler and set it with 
 * <code>ExceptionWrapper.getInstance().setExceptionHandler(...)</code>
 * 
 * @author patrick
 */
public final class ExceptionWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionWrapper.class);
    private ExceptionHandler exceptionHandler;

    
    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
        static final ExceptionWrapper INSTANCE = new ExceptionWrapper();
    }

    
    /**
     * Constructor
     */
    private ExceptionWrapper() {
        exceptionHandler = new DefaultExceptionHandler();
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static ExceptionWrapper getInstance() {
        return HOLDER.INSTANCE;
    }

        
    /**
     * Sets the exception handler
     * 
     * @param exceptionHandler the exception handler
     * @throws IllegalArgumentException In case of an invalid exception handler
     */
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        if (exceptionHandler == null) {
            throw new IllegalArgumentException("Invalid ExceptionHandler!");
        }
        
        this.exceptionHandler = exceptionHandler;
    }
    
    
    /**
     * This method converts a given exception into an another exception
     * 
     * @param <T> the generic type
     * @param t the exception to wrap
     * @param newExceptionClass the new exception class
     * @return the wrapped exception
     */
    public <T extends Throwable> T convertException(Throwable t, Class<T> newExceptionClass) {
        return convertException(t, newExceptionClass, null, null);
    }
    
    
    /**
     * This method converts a given exception into an another exception
     * 
     * @param <T> the generic type
     * @param t the exception to wrap
     * @param newExceptionClass the new exception class
     * @param logLevel the log level
     * @return the wrapped exception
     */
    public <T extends Throwable> T convertException(Throwable t, Class<T> newExceptionClass, Level logLevel) {
        return convertException(t, 
                                newExceptionClass, 
                                logLevel, 
                                "Exception " + t.getClass().getName() + " occured: " + t.getMessage());
    }
    
    
    /**
     * This method converts a given exception into an another exception
     * 
     * @param <T> the generic type
     * @param t the exception to wrap
     * @param newExceptionClass the new exception class
     * @param logLevel the log level
     * @param logMessage the log message
     * @return the wrapped exception
     */
    public <T extends Throwable> T convertException(Throwable t, Class<T> newExceptionClass, Level logLevel, String logMessage) {
        String msg = "";
        
        if (logMessage != null) {
            msg = logMessage;
        }

        if (logLevel != null) {
            LOG.atLevel(logLevel).log(msg, t);
        }
        

        try {
            return exceptionHandler.createExceptionInstance(t, newExceptionClass);
        } catch (Exception e) {
            if (logLevel == null) {
                LOG.error("==>Can not convert exception (" + msg + "):" + t.getMessage(), t);
            }
            
            LOG.error("Could not convert exception from " + t.getClass().getName() + " to " + newExceptionClass + ":" + e.getMessage(), e);
        }

        // if there is an error we throw a runtime exception
        RuntimeException re = new RuntimeException(t.getMessage(), t);
        re.setStackTrace(t.getStackTrace());
        throw re;
    }
    
    
    /**
     * Defines the exception handler API
     * 
     * @author Patrick Meier
     * @version $Revision: 1.9 $
     */
    public interface ExceptionHandler {
        
        /**
         * Creates the new exception 
         * 
         * @param <T> the generic type
         * @param t the current exception 
         * @param newExceptionClazz the new exception class
         * @return the new instance
         * @throws InstantiationException in case of error
         * @throws IllegalAccessException in case of error
         * @throws InvocationTargetException in case of error
         */
        <T extends Throwable> T createExceptionInstance(Throwable t, Class<T> newExceptionClazz)
                throws InstantiationException, IllegalAccessException, InvocationTargetException;
    }

    
    /**
     * This class implements the default exception handler behavior
     * 
     * @author Patrick Meier
     * @version $Revision: 1.9 $
     */
    public class DefaultExceptionHandler implements ExceptionHandler {

        /**
         * @see com.github.toolarium.common.util.ExceptionWrapper.ExceptionHandler#createExceptionInstance(java.lang.Throwable, java.lang.Class)
         */
        @Override
        public <T extends Throwable> T createExceptionInstance(Throwable t, Class<T> newExceptionClazz)
                throws InstantiationException, IllegalAccessException, InvocationTargetException {
            T newEx = null;
            
            // try to get the message constructor
            Constructor<T> ct = getMessageAndThrowableConstructor(newExceptionClazz);
            if (ct == null) {
                Constructor<T> ctm = getMessageConstructor(newExceptionClazz);
                Constructor<T> ctt = getThrowableConstructor(newExceptionClazz);

                if (ctm != null) {
                    // create class with constructor
                    Object[] args = new Object[1];
                    args[0] = t.getMessage();
                    newEx = ctm.newInstance(args);
                } else if (ctt != null) {
                    // create class with constructor
                    Object[] args = new Object[1];
                    args[0] = t.getCause();
                    newEx = ctt.newInstance(args);
                }
            } else {
                // create class with constructor
                Object[] args = new Object[2];
                args[0] = t.getMessage();
                args[1] = t.getCause();
                newEx = ct.newInstance(args);
            }
            
            if (newEx == null) {
                // create the new class
                newEx = ClassInstanceUtil.getInstance().newInstance(newExceptionClazz);
            }
    
            // set the stack trace properly
            newEx.setStackTrace(t.getStackTrace());
            return newEx;
        }
        
        
        /**
         * Gets the default message constructor if is any is defined 
         * 
         * @param <T> the generic type
         * @param newExceptionClazz the current class
         * @return the constructor or null if no constructor was found
         */
        protected <T> Constructor<T> getMessageConstructor(Class<T> newExceptionClazz) {
            try {
                return newExceptionClazz.getConstructor(String.class);
            } catch (Exception e) {
                return null;
            }
        }    
        
        
        /**
         * Gets the default throwable constructor if is any is defined
         *  
         * @param <T> the generic type
         * @param newExceptionClazz the current class
         * @return the constructor or null if no constructor was found
         */
        protected <T> Constructor<T> getThrowableConstructor(Class<T> newExceptionClazz) {
            try {
                return newExceptionClazz.getConstructor(Throwable.class);
            } catch (Exception e) {
                return null;
            }
        }

        
        /**
         * Gets the default throwable constructor if is any is defined 
         * 
         * @param <T> the generic type
         * @param newExceptionClazz the current class
         * @return the constructor or null if no constructor was found
         */
        protected <T> Constructor<T> getMessageAndThrowableConstructor(Class<T> newExceptionClazz) {
            try {
                Class<?>[] param = new Class<?>[2];
                param[0] = String.class;
                param[1] = Throwable.class;
                return newExceptionClazz.getConstructor(param);
            } catch (Exception e) {
                // NOP
            }
        
            try {
                Class<?>[] param = new Class<?>[2];
                param[0] = String.class;
                param[1] = Exception.class;
                return newExceptionClazz.getConstructor(param);
            } catch (Exception e) {
                // NOP
            }
        
            try {
                Class<?>[] param = new Class<?>[2];
                param[0] = String.class;
                param[1] = RuntimeException.class;
                return newExceptionClazz.getConstructor(param);
            } catch (Exception e) {
                // NOP
                return null;
            }
        }
    }
}
