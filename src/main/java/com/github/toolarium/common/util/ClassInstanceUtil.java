/*
 * ClassInstanceUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.lang.reflect.InvocationTargetException;


/**
 * 
 * @author patrick
 */
public final class ClassInstanceUtil {
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
        static final ClassInstanceUtil INSTANCE = new ClassInstanceUtil();
    }

    
    /**
     * Constructor
     */
    private ClassInstanceUtil() {
        // NOP
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static ClassInstanceUtil getInstance() {
        return HOLDER.INSTANCE;
    }

    
    /**
     * Load a class by given name
     * 
     * @param <T> the generic type
     * @param className the name of the class
     * @return the loaded class
     * @exception ClassNotFoundException in case of error
     */
    public <T> Class<T> getClassObject(String className) throws ClassNotFoundException {
        return ReflectionUtil.getInstance().getClassObject(className);
    }

    
    /**
     * Create a new instance of a given class
     * 
     * @param <T> the generic type
     * @param className the name of the class
     * @return the loaded class
     * @exception ClassNotFoundException in case of error
     * @exception InstantiationException in case of error
     * @exception IllegalAccessException in case of error
     * @throws SecurityException in case of error
     */
    public <T> T newInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException {
        Class<T> resultClass = getClassObject(className);
        T obj = null;

        if (resultClass != null) {
            obj = newInstance(resultClass);
        }

        if (obj == null) {
            throw new ClassNotFoundException("Could not load class " + className);
        }

        return obj;
    }

    
    /**
     * Create a new instance of a given class
     * 
     * @param <T> the generic type
     * @param clazz the class
     * @return the instance
     * @throws SecurityException in case of error
     * @throws InvocationTargetException in case of error
     * @throws IllegalArgumentException in case of error
     * @throws IllegalAccessException in case of error
     * @throws InstantiationException in case of error
     */
    public <T> T newInstance(Class<T> clazz) throws SecurityException, InstantiationException, IllegalAccessException {
        return ReflectionUtil.getInstance().newInstance(clazz);
    }    
    
    
    /**
     * Load a class by given name
     * 
     * @param <T> the generic type
     * @param theClass the class 
     * @param length the length of the array
     * @return the loaded class
     * @throws IllegalArgumentException In case of invalid input
     */
    public <T> T[] createArray(Class<T> theClass, int length) {
        if (theClass == null || length <= 0) {
            throw new IllegalArgumentException("Invalid data to create an array!");
        }

        @SuppressWarnings("unchecked")
        T[] array = (T[]) new Object[length];
        return array;
    }    

    
    /**
     * Check if the given class exists
     * 
     * @param className the name of the class to check
     * @return true if the class exists; otherwise false
     */
    public boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (NoClassDefFoundError | Exception e) {
            return false;
        }
    }

    
    /**
     * Check if a given class implements an interface
     * 
     * @param className the class name
     * @param interfaceClazz the interface
     * @return true if the class implements the interface
     */
    public boolean implementsInterface(String className, Class<?> interfaceClazz) {
        if (className == null || className.trim().isEmpty()) {
            return false;
        }

        try {
            Class<?> clazz = getClassObject(className);
            if (clazz == null) {
                return false;
            }

            return interfaceClazz.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
