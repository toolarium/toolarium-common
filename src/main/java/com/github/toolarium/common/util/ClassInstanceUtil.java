/*
 * ClassInstanceUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.lang.reflect.InvocationTargetException;


/**
 * Utility class for loading classes and creating instances via reflection.
 *
 * <p><b>Security Warning:</b> Methods like {@link #newInstance(String)} and {@link #getClassObject(String)}
 * load and instantiate arbitrary classes by name. If class names originate from untrusted input,
 * arbitrary code could be executed (e.g. via static initializers). Callers <b>must</b> validate
 * and/or whitelist class names before passing them to these methods.</p>
 *
 * @author patrick
 */
public final class ClassInstanceUtil {
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
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
     * Create a new array with the given component type and length, e.g. <code>createArray(String.class, 2)</code> returns a <code>String[2]</code>.
     * 
     * @param <T> the generic type
     * @param theClass the component type of the array (must not be a primitive type)
     * @param length the length of the array
     * @return the new array with the runtime component type <code>theClass</code>
     * @throws IllegalArgumentException In case of invalid input (null or primitive class, length &lt;= 0)
     */
    public <T> T[] createArray(Class<T> theClass, int length) {
        if (theClass == null || theClass.isPrimitive() || length <= 0) {
            throw new IllegalArgumentException("Invalid data to create an array!");
        }

        @SuppressWarnings("unchecked")
        T[] array = (T[]) java.lang.reflect.Array.newInstance(theClass, length);
        return array;
    }    

    
    /**
     * Check if the given class exists. The class is not initialized, its static initializers are not executed.
     * The same class loaders as in {@link #newInstance(String)} are used: first the context class loader of the
     * current thread, then the class loader of this library.
     *
     * @param className the name of the class to check
     * @return true if the class exists; otherwise false
     */
    public boolean isClassAvailable(String className) {
        if (className == null || className.trim().isEmpty()) {
            return false;
        }

        return ReflectionUtil.getInstance().isClassAvailable(className);
    }

    
    /**
     * Check if a given class implements an interface. The class is not initialized, its static initializers are not executed.
     *
     * @param className the class name
     * @param interfaceClazz the interface
     * @return true if the class implements the interface; false if not or if the class could not be found or not be linked
     */
    public boolean implementsInterface(String className, Class<?> interfaceClazz) {
        if (className == null || className.trim().isEmpty() || interfaceClazz == null) {
            return false;
        }

        Class<?> clazz = ReflectionUtil.getInstance().getClassObjectWithoutInitialization(className);
        if (clazz == null) {
            return false;
        }

        return interfaceClazz.isAssignableFrom(clazz);
    }
}
