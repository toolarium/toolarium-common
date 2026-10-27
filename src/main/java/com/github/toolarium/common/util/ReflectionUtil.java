/*
 * ReflectionUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Defines the reflection utility.
 * 
 * @author patrick
 */
public final class ReflectionUtil {
    private volatile boolean defaultDisabledAccessWarnings;
    private volatile boolean disabledAccessWarnings;
    private volatile boolean disableAccessWarningsAttempted;

    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ReflectionUtil INSTANCE = new ReflectionUtil();
    }

    /**
     * Constructor
     */
    private ReflectionUtil() {
        disabledAccessWarnings = false;
        defaultDisabledAccessWarnings = false;
        
        String prop = System.getProperty("disabledAccessWarnings");
        if (prop != null && prop.trim().equalsIgnoreCase("true")) {
            defaultDisabledAccessWarnings = true;
        }
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static ReflectionUtil getInstance() {
        return HOLDER.INSTANCE;
    }

    
    /**
     * Check if the given class exists
     * 
     * @param className the name of the class to check
     * @return true if the class exists; otherwise false, also if the class can not be linked (e.g. compiled for a newer java version)
     * @throws IllegalArgumentException In case of an invalid class name
     */
    public boolean isClassAvailable(String className) throws IllegalArgumentException {
        if (className == null || className.isEmpty() || className.trim().isEmpty()) { // TODO: isBlank()
            throw new IllegalArgumentException("Invalid class name!");
        }
        
        try {
            disableAccessWarnings();
            
            loadClass(className.trim(), false);
            return true;
        } catch (LinkageError | Exception e) {
            // e.g. a class compiled for a newer java version (UnsupportedClassVersionError) is not available
            return false;
        }
    }

    
    /**
     * Load a class by given name
     * 
     * @param <T> the generic type
     * @param className the name of the class
     * @return the loaded class
     * @throws IllegalArgumentException In case of an invalid class name
     * @throws ClassNotFoundException in case of error, also if the class can not be linked or its static initializer
     *         fails (the {@link LinkageError} is kept as cause)
     */
    public <T> Class<T> getClassObject(String className) throws IllegalArgumentException, ClassNotFoundException {
        if (className == null || className.isEmpty() || className.trim().isEmpty()) { // TODO: isBlank()
            throw new IllegalArgumentException("Invalid class name!");
        }

        disableAccessWarnings();

        try {
            @SuppressWarnings("unchecked")
            Class<T> resultCalss = (Class<T>)loadClass(className.trim(), true);
            return resultCalss;
        } catch (LinkageError e) {
            throw new ClassNotFoundException("Could not load class " + className.trim() + ": " + e, e);
        }
    }


    /**
     * Load a class without initializing it, its static initializers are not executed. It can be used if a class
     * is only inspected.
     *
     * @param className the name of the class
     * @return the loaded class or null if the class name is empty, the class could not be found or not be linked
     */
    Class<?> getClassObjectWithoutInitialization(String className) {
        if (className == null || className.trim().isEmpty()) {
            return null;
        }

        try {
            disableAccessWarnings();
            return loadClass(className.trim(), false);
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }


    /**
     * Load a class: first by the context class loader of the current thread, then by the class loader of this library.
     *
     * @param className the name of the class
     * @param initialize true to initialize the class (execute the static initializers)
     * @return the loaded class
     * @throws ClassNotFoundException in case the class could not be found
     */
    private Class<?> loadClass(String className, boolean initialize) throws ClassNotFoundException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            try {
                return Class.forName(className, initialize, contextClassLoader);
            } catch (ClassNotFoundException e) {
                // try the class loader of this library
            }
        }

        return Class.forName(className, initialize, ReflectionUtil.class.getClassLoader());
    }

    
    /**
     * Create a new instance of a given class
     * 
     * @param <T> the generic type
     * @param clazz the class
     * @return the instance
     * @throws IllegalArgumentException In case of an invalid class name
     * @throws SecurityException in case of error
     * @throws InvocationTargetException in case of error
     * @throws IllegalArgumentException in case of error
     * @throws IllegalAccessException in case of error
     * @throws InstantiationException in case of error
     */
    @SuppressWarnings("unchecked")
    public <T> T newInstance(Class<T> clazz) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException {
        if (clazz == null) {
            throw new IllegalArgumentException("Invalid empty class!");
        }
        
        try {
            disableAccessWarnings();

            Constructor<?> constructor;
            constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (InvocationTargetException | NoSuchMethodException e) {
            // keep the real error of the constructor as cause
            Throwable cause = e;
            if (e instanceof InvocationTargetException && e.getCause() != null) {
                cause = e.getCause();
            }

            InstantiationException ex = new InstantiationException(cause.toString());
            ex.initCause(cause);
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        }
    }
    
    
    /**
     * Gets the defined method back
     * 
     * @param className the class name
     * @param methodName the method name
     * @param paramType the parameter type
     * @param <T> the generic type
     * @return the method
     * @throws IllegalArgumentException In case of an invalid class or method name
     * @throws SecurityException in case of error
     * @throws NoSuchMethodException in case of error
     * @throws ClassNotFoundException in case of error
     */
    public <T> Method getMethod(String className, String methodName, Class<?>[] paramType)
            throws SecurityException, NoSuchMethodException, ClassNotFoundException {
        if (className == null) {
            throw new IllegalArgumentException("Invalid empty class name!");
        }

        if (methodName == null) {
            throw new IllegalArgumentException("Invalid empty method name!");
        }

        Class<? extends T> clazz = getClassObject(className);
        return clazz.getMethod(methodName, paramType);
    }    

    
    /**
     * Calls the given method on the given instance
     * 
     * @param classInstance the instance of the class where the method should be
     *                      called
     * @param className the name of the class
     * @param methodName the method name
     * @param paramType the parameter type
     * @param param the parameter values
     * @param <T> the generic type
     * @return the result
     * @throws SecurityException in case of error
     * @throws NoSuchMethodException in case of error
     * @throws InvocationTargetException in case of error
     * @throws IllegalAccessException in case of error
     * @throws IllegalArgumentException in case of error
     * @throws ClassNotFoundException in case of error
     */
    public <T> Object callMethod(Object classInstance, String className, String methodName, Class<?>[] paramType, Object[] param) 
        throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        Method method = getMethod(className, methodName, paramType);
        return method.invoke(classInstance, param);
    }
    
    
    /**
     * Disable access warnings
     */
    public void disableAccessWarnings() {
        if (defaultDisabledAccessWarnings == disabledAccessWarnings || disableAccessWarningsAttempted) {
            return;
        }

        // try only once, e.g. on Java 17+ it is no longer supported and would fail on every call
        disableAccessWarningsAttempted = true;
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);

            Method putObjectVolatile = unsafeClass.getDeclaredMethod("putObjectVolatile", Object.class, long.class, Object.class);
            Method staticFieldOffset = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);
            Class<?> loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field loggerField = loggerClass.getDeclaredField("logger");
            Long offset = (Long) staticFieldOffset.invoke(unsafe, loggerField);
            putObjectVolatile.invoke(unsafe, loggerClass, offset, null);
            disabledAccessWarnings = true;
        } catch (Exception ignored) {
            // NOP
        }
    }
}
