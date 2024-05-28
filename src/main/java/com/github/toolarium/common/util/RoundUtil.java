/*
 * RoundUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;


/**
 * Round util class.
 * 
 * @author patrick
 */
public final class RoundUtil {
    
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
        static final RoundUtil INSTANCE = new RoundUtil();
    }

    /**
     * Constructor
     */
    private RoundUtil() {
        // NOP
    }

    /**
     * Get the instance
     *
     * @return the instance
     */
    public static RoundUtil getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Round double values
     * 
     * @param val   the value to round
     * @param scale the next
     * @return the rounded value
     */
    public double round(double val, int scale) {
        double s = Math.pow(10, scale);
        return Math.round(val * s) / s;
    }

    
    /**
     * Round double values
     * 
     * @param val the value to round
     * @return the rounded value
     */
    public int roundToInt(double val) {
        return (int) round(val, 0);
    }

    
    /**
     * Round double values
     * 
     * @param val the value to round
     * @return the rounded value
     */
    public long roundToLong(double val) {
        return (long) round(val, 0);
    }
}
