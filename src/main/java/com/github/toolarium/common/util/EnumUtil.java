/*
 * EnumUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;


/**
 * Simple enum utilities.
 *
 * <p>The enum constants are not cached by this class: a cache would pin the class loader of this library
 * from an enum type which may outlive it (e.g. a JDK enum after a redeploy).</p>
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
     * specified name. The name is trimmed and compared with the identifiers used to declare the enum constants
     * in this type: an exact match is preferred, otherwise the name is compared case insensitive. In case several
     * constants differ only in case, the first declared constant is returned by the case insensitive comparison.
     *
     * @param <T> the type
     * @param enumType the <tt>Class</tt> object of the enum type from which
     *        to return a constant
     * @param name the name of the constant to return
     * @return the enum constant of the specified enum type with the
     *         specified name or null if the name is null, empty or unknown
     */
    public <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        if (name == null || name.trim().length() == 0) {
            return null;
        }

        String trimmedName = name.trim();
        T caseInsensitiveMatch = null;
        for (T constant : enumType.getEnumConstants()) {
            if (constant.name().equals(trimmedName)) {
                return constant;
            }

            if (caseInsensitiveMatch == null && constant.name().equalsIgnoreCase(trimmedName)) {
                caseInsensitiveMatch = constant;
            }
        }

        return caseInsensitiveMatch;
    }
}
