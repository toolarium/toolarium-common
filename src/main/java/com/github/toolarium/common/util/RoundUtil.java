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
    private static final double[] POWERS_OF_TEN = new double[16];

    static {
        for (int i = 0; i < POWERS_OF_TEN.length; i++) {
            POWERS_OF_TEN[i] = Math.pow(10, i);
        }
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
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
        double s = powerOfTen(scale);
        return Math.round(val * s) / s;
    }


    /**
     * Get the power of ten, using cached values for common scales
     *
     * @param scale the scale
     * @return 10^scale
     */
    private static double powerOfTen(int scale) {
        if (scale >= 0 && scale < POWERS_OF_TEN.length) {
            return POWERS_OF_TEN[scale];
        }
        return Math.pow(10, scale);
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
