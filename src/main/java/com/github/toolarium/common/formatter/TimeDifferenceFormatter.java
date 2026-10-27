/*
 * TimeDifferenceFormatter.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.formatter;

/**
 * Formats a time difference in milliseconds into a human readable string, e.g. <code>1h 2min 3s</code>.
 *
 * @author patrick
 */
public class TimeDifferenceFormatter extends AbstractDifferenceFormatter {
    private final boolean shortFormat;
    private final String msText;
    private final String secText;
    private final String minText;
    private final String hourText;
    private final String dayText;
    
    
    /**
     * Constructor for the <code>TimeDifferenceFormatter</code>.
     */
    public TimeDifferenceFormatter() {
        this("###########", false, false);
    }
    
    
    /**
     * Constructor for the <code>TimeDifferenceFormatter</code>.
     * 
     * @param shortFormat enable short format
     * @param spaceAfterValue true to have a space after the value
     */
    public TimeDifferenceFormatter(boolean shortFormat, boolean spaceAfterValue) {
        this("00", shortFormat, isShortForm(shortFormat, spaceAfterValue));
    }


    /**
     * Constructor for the <code>TimeDifferenceFormatter</code>.
     * 
     * @param pattern the pattern
     * @param shortFormat enable short format
     * @param spaceAfterValue true to have a space after the value
     */
    public TimeDifferenceFormatter(String pattern, boolean shortFormat, boolean spaceAfterValue) {        
        super(pattern, spaceAfterValue);

        this.shortFormat = shortFormat;
        if (this.shortFormat) {
            msText = "";
            secText = "''";
            minText = "'";
            hourText = "h";
            dayText = "d";
        } else {
            msText = "ms";
            secText = "sec";
            minText = "min";
            hourText = "h";
            dayText = "d";
        }
    }


    /**
     * @see com.github.toolarium.common.formatter.AbstractDifferenceFormatter#formatAsString(long)
     */
    @Override
    public String formatAsString(long time) {
        boolean isNagativeValue = false;
        long diffTime = time;
        if (diffTime < 0) {
            if (diffTime == Long.MIN_VALUE) {
                diffTime = Long.MAX_VALUE; // -Long.MIN_VALUE can not be represented
            } else {
                diffTime = -1 * diffTime;
            }
            isNagativeValue = true;
        }

        String result = "";
        String endStr = msText;

        if (diffTime < 1000 && shortFormat) {
            result = "<";
            diffTime = 1000;
        }

        if (diffTime >= 1000) {
            long rest = (diffTime % 1000);
            if (shortFormat) {
                if (rest >= 500 && diffTime <= Long.MAX_VALUE - 1000) {
                    diffTime += 1000 - rest; // round to the next second
                }
            } else if (rest > 0) {
                result = formatNumber(rest, msText, spaceAfterValue());
            }

            diffTime /= 1000;
            endStr = secText;

            if (diffTime >= 60) {
                rest = (diffTime % 60);
                result = addResult(rest, secText, result);

                diffTime /= 60;
                endStr = minText;

                if (diffTime >= 60) {
                    rest = (diffTime % 60);
                    result = addResult(rest, minText, result);

                    diffTime /= 60;
                    endStr = hourText;

                    if (diffTime >= 24) {
                        rest = (diffTime % 24);
                        result = addResult(rest, hourText, result);

                        diffTime /= 24;
                        endStr = dayText;
                    }
                }
            }
        }

        result = addResult(diffTime, endStr, result);
        if (result.length() == 0) {
            if (shortFormat) {
                result = formatNumber(0, endStr, spaceAfterValue());
            } else {
                if (spaceAfterValue()) {
                    result = "0 " + endStr;
                } else {
                    result = "0" + endStr;
                }
            }
        }

        if (isNagativeValue) {
            return "-" + result;
        }
        return result;
    }


    /**
     * Check if it is short form
     * 
     * @param shortFormat true to use short form
     * @param spaceAfterValue true space after value
     * @return the short form
     */
    private static boolean isShortForm(boolean shortFormat, boolean spaceAfterValue) {
        if (shortFormat) {
            return false;
        } else {
            return spaceAfterValue;
        }
    }
}
