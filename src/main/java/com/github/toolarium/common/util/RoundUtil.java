/*
 * RoundUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Round util class.
 * 
 * @author patrick
 */
public final class RoundUtil {
    private static final double[] POWERS_OF_TEN = new double[16];
    /** Above this magnitude every double is an integer, rounding cannot change the value anymore (2^52). */
    private static final double MAX_FRACTION_MAGNITUDE = 4503599627370496.0;

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
     * Round double values (half up, away from zero, see {@link RoundingMode#HALF_UP}). The value is rounded based on its
     * decimal representation (see {@link BigDecimal#valueOf(double)}), e.g. 1.005 rounded to 2 fraction digits is 1.01.
     * NaN and infinite values are returned unchanged.
     * In case the value is too large to have any fraction at the given scale, it is returned unchanged as well
     * (no saturation at {@link Long#MAX_VALUE}).
     *
     * @param val   the value to round
     * @param scale the number of fraction digits
     * @return the rounded value
     */
    public double round(double val, int scale) {
        if (Double.isNaN(val) || Double.isInfinite(val)) {
            return val;
        }

        double s = powerOfTen(scale);
        if (s == 0.0) {
            // very large negative scale: 10^scale underflows, every finite value rounds to zero
            return 0.0;
        }

        double scaled = val * s;
        if (Double.isNaN(scaled) || Double.isInfinite(scaled) || Math.abs(scaled) >= MAX_FRACTION_MAGNITUDE) {
            return val;
        }

        // round the decimal representation, the binary value of e.g. 1.005 is slightly below 1.005
        return BigDecimal.valueOf(val).setScale(scale, RoundingMode.HALF_UP).doubleValue();
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
