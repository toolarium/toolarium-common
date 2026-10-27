/*
 * ExceptionWrapper.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
 *      IllegalArgumentException ex = new IllegalArgumentException(e.getMessage());
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
 *      throw ExceptionWrapper.getInstance().convertException(e, IllegalArgumentException.class);
 * }
 * </pre>
 * <br><br>
 * To support logging in the exception case add the log level. There are
 * additional parameters to manipulate the log behavior. 
 * <pre>
 * try {
 *     ...
 * } catch(XYException e) {
 *      throw ExceptionWrapper.getInstance().convertException(e, IllegalArgumentException.class, org.slf4j.event.Level.WARN);
 * }
 * </pre>
 * <br><br>
 * To change the behavior how an exception will be converted just implement
 * your own {@link ExceptionHandler} and set it with
 * <code>ExceptionWrapper.getInstance().setExceptionHandler(...)</code>
 * 
 * @author patrick
 */
public final class ExceptionWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionWrapper.class);
    private static final String NO_EXCEPTION_MESSAGE = "No exception given";
    private volatile ExceptionHandler exceptionHandler;

    
    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
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
     * This method converts a given exception into an another exception. In case no exception is given (null), a new
     * instance of the new exception class with the message <code>No exception given</code> (and no cause) is returned.
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
        String logMessage = NO_EXCEPTION_MESSAGE;
        if (t != null) {
            logMessage = "Exception " + t.getClass().getName() + " occured: " + t.getMessage();
        }

        return convertException(t, newExceptionClass, logLevel, logMessage);
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
     * @throws IllegalArgumentException in case no exception is given and the new exception class can not be instantiated
     */
    public <T extends Throwable> T convertException(Throwable t, Class<T> newExceptionClass, Level logLevel, String logMessage) {
        String msg = "";
        
        if (logMessage != null) {
            msg = logMessage;
        }

        if (logLevel != null) {
            LOG.atLevel(logLevel).log(msg, t);
        }
        
        if (t == null) {
            return createExceptionWithoutCause(newExceptionClass);
        }


        try {
            return exceptionHandler.createExceptionInstance(t, newExceptionClass);
        } catch (Exception e) {
            String newExceptionClassName = null;
            if (newExceptionClass != null) {
                newExceptionClassName = newExceptionClass.getName();
            }

            final String errorMessage = "Could not convert exception from " + t.getClass().getName() + " to " + newExceptionClassName + ": " + e.getMessage();
            if (LOG.isDebugEnabled()) {
                LOG.debug(errorMessage, e);
            }

            LOG.error(errorMessage);
        }

        // if there is an error we throw a runtime exception, it contains the original exception as cause
        RuntimeException re = new RuntimeException(t.getMessage(), t);
        re.setStackTrace(t.getStackTrace());
        throw re;
    }


    /**
     * Creates a new exception instance without a cause, used in case no exception is given. The message is
     * <code>No exception given</code> if the exception class has a constructor with a message parameter.
     *
     * @param <T> the generic type
     * @param newExceptionClass the new exception class
     * @return the new exception
     * @throws IllegalArgumentException in case the new exception class can not be instantiated
     */
    private <T extends Throwable> T createExceptionWithoutCause(Class<T> newExceptionClass) {
        if (newExceptionClass == null) {
            throw new IllegalArgumentException(NO_EXCEPTION_MESSAGE + " and invalid exception class!");
        }

        try {
            return newExceptionClass.getConstructor(String.class).newInstance(NO_EXCEPTION_MESSAGE);
        } catch (NoSuchMethodException e) {
            // no message constructor: use the default constructor
            if (LOG.isDebugEnabled()) {
                LOG.debug("No message constructor found in " + newExceptionClass.getName() + ".", e);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            final String errorMessage = "Could not create exception " + newExceptionClass.getName() + ": " + e.getMessage();
            if (LOG.isDebugEnabled()) {
                LOG.debug(errorMessage, e);
            }
            LOG.warn(errorMessage);
        }

        try {
            return ClassInstanceUtil.getInstance().newInstance(newExceptionClass);
        } catch (ReflectiveOperationException | RuntimeException e) {
            final String errorMessage = "Could not create exception " + newExceptionClass.getName() + ": " + e.getMessage();
            if (LOG.isDebugEnabled()) {
                LOG.debug(errorMessage, e);
            }
            LOG.warn(errorMessage);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }
    
    
    /**
     * Defines the exception handler API
     * 
     * @author Patrick Meier
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
     */
    public static class DefaultExceptionHandler implements ExceptionHandler {
        private static final String MSG_THROWABLE = "msg+throwable";
        private static final String MSG_ONLY = "msg";
        private static final String THROWABLE_ONLY = "throwable";
        private static final String NO_ARG = "noarg";
        private final ClassValue<Map<String, Optional<Constructor<?>>>> constructorCache = new ClassValue<Map<String, Optional<Constructor<?>>>>() {
            /**
             * @see java.lang.ClassValue#computeValue(java.lang.Class)
             */
            @Override
            protected Map<String, Optional<Constructor<?>>> computeValue(Class<?> type) {
                return new ConcurrentHashMap<String, Optional<Constructor<?>>>();
            }
        };

        /**
         * @see com.github.toolarium.common.util.ExceptionWrapper.ExceptionHandler#createExceptionInstance(java.lang.Throwable, java.lang.Class)
         */
        @Override
        public <T extends Throwable> T createExceptionInstance(Throwable t, Class<T> newExceptionClazz)
                throws InstantiationException, IllegalAccessException, InvocationTargetException {
            T newEx = null;

            // try to get the message constructor, the original exception is kept as cause
            Constructor<T> ct = getCachedConstructor(newExceptionClazz, MSG_THROWABLE);
            if (ct != null && ct.getParameterTypes()[1].isInstance(t)) {
                newEx = ct.newInstance(t.getMessage(), t);
            } else {
                Constructor<T> ctm = getCachedConstructor(newExceptionClazz, MSG_ONLY);
                Constructor<T> ctt = getCachedConstructor(newExceptionClazz, THROWABLE_ONLY);

                if (ctm != null) {
                    newEx = initCause(ctm.newInstance(t.getMessage()), t);
                } else if (ctt != null) {
                    newEx = ctt.newInstance(t);
                }
            }

            if (newEx == null) {
                // create the new class
                newEx = initCause(ClassInstanceUtil.getInstance().newInstance(newExceptionClazz), t);
            }

            // set the stack trace properly
            newEx.setStackTrace(t.getStackTrace());
            return newEx;
        }


        /**
         * Gets a cached constructor for the given exception class and type
         *
         * @param <T> the generic type
         * @param clazz the exception class
         * @param type the constructor type key
         * @return the constructor or null
         */
        @SuppressWarnings("unchecked")
        protected <T> Constructor<T> getCachedConstructor(Class<T> clazz, String type) {
            // the cache is bound to the class itself, a missing constructor is cached too
            Optional<Constructor<?>> cached = constructorCache.get(clazz).computeIfAbsent(type, k -> Optional.ofNullable(lookupConstructor(clazz, k)));
            return (Constructor<T>) cached.orElse(null);
        }


        /**
         * Sets the cause of a new exception if it has none
         *
         * @param <T> the generic type
         * @param newEx the new exception
         * @param cause the cause
         * @return the new exception
         */
        private <T extends Throwable> T initCause(T newEx, Throwable cause) {
            if (newEx != null && newEx.getCause() == null) {
                try {
                    newEx.initCause(cause);
                } catch (IllegalStateException | IllegalArgumentException e) {
                    // the cause is already set or can not be set
                }
            }

            return newEx;
        }


        /**
         * Looks up a constructor by type
         *
         * @param <T> the generic type
         * @param clazz the exception class
         * @param type the constructor type
         * @return the constructor or null
         */
        private <T> Constructor<T> lookupConstructor(Class<T> clazz, String type) {
            switch (type) {
                case MSG_ONLY:
                    try {
                        return clazz.getConstructor(String.class);
                    } catch (Exception e) {
                        return null;
                    }
                case THROWABLE_ONLY:
                    try {
                        return clazz.getConstructor(Throwable.class);
                    } catch (Exception e) {
                        return null;
                    }
                case MSG_THROWABLE:
                    return getMessageAndThrowableConstructor(clazz);
                default:
                    return null;
            }
        }


        /**
         * Gets the message+throwable constructor if any is defined
         *
         * @param <T> the generic type
         * @param newExceptionClazz the current class
         * @return the constructor or null if no constructor was found
         */
        protected <T> Constructor<T> getMessageAndThrowableConstructor(Class<T> newExceptionClazz) {
            try {
                return newExceptionClazz.getConstructor(String.class, Throwable.class);
            } catch (Exception e) {
                // NOP
            }

            try {
                return newExceptionClazz.getConstructor(String.class, Exception.class);
            } catch (Exception e) {
                // NOP
            }

            try {
                return newExceptionClazz.getConstructor(String.class, RuntimeException.class);
            } catch (Exception e) {
                // NOP
                return null;
            }
        }
    }
}
