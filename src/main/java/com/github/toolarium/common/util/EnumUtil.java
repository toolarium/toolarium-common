/*
 * EnumUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;


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
    private static class HOLDER {
        static final EnumUtil INSTANCE = new EnumUtil();
    }

    
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
    public <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        if (name == null || name.trim().length() == 0) {
            return null;
        }
        
        T[] types = enumType.getEnumConstants();
        String nameToParse = name.trim();

        for (int i = 0; i < types.length; i++) {
            T type = types[i];

            if (type.name().equalsIgnoreCase(nameToParse)) {
                return type;
            }
        }

        return null;
    }
}