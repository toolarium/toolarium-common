/*
 * EnumUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Simple enum utilities.
 *
 * @author patrick
 */
public final class EnumUtil {

    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final EnumUtil INSTANCE = new EnumUtil();
    }

    
    private final ConcurrentHashMap<Class<?>, Map<String, ?>> enumCache = new ConcurrentHashMap<>();


    /**
     * Constructor
     */
    private EnumUtil() {
        // NOP
    }


    /**
     * Get the instance
     *
     * @return the instance
     */
    public static EnumUtil getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Returns the enum constant of the specified enum type with the
     * specified name.  The name must match exactly an identifier used
     * to declare an enum constant in this type.  (Extraneous whitespace
     * characters are not permitted.)
     *
     * @param <T> the type
     * @param enumType the <tt>Class</tt> object of the enum type from which
     *        to return a constant
     * @param name the name of the constant to return
     * @return the enum constant of the specified enum type with the
     *         specified name
     */
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        if (name == null || name.trim().length() == 0) {
            return null;
        }

        Map<String, T> lookup = (Map<String, T>) enumCache.computeIfAbsent(enumType, k -> {
            Map<String, T> map = new ConcurrentHashMap<>();
            for (T constant : enumType.getEnumConstants()) {
                map.put(constant.name().toUpperCase(), constant);
            }
            return map;
        });

        return lookup.get(name.trim().toUpperCase());
    }
}