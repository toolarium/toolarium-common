/*
 * TimeDifferenceUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.formatter;

/**
 * 
 * @author patrick
 */
public class TimeDifferenceFormatter extends AbstractDifferenceFormatter {
    private boolean shortFormat;
    private String msText;
    private String secText;
    private String minText;
    private String hourText;
    private String dayText;
    
    
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
     * Format a duration 
     * 
     * @param duration the duration
     * @return the formated string
    public String formatAsString( Duration duration )
    {
        if( duration==null || duration.getEnd()==null )
            return formatAsString( 0 );

        if( duration==null || duration.getStart()==null )
            return formatAsString( 0 );

        return formatAsString( duration.getEnd().getTime() - duration.getStart().getTime() );
    }
     */

    
    /**
     * @see jptools.util.formatter.AbstractDifferenceFormatter#formatAsString(long)
     */
    @Override
    public String formatAsString(long time) {
        boolean isNagativeValue = false;
        long diffTime = time;
        if (diffTime < 0) {
            diffTime = -1 * diffTime;
            isNagativeValue = true;
        }

        String result = "";
        long min = 1;
        String endStr = msText;

        if (diffTime < 1000 && shortFormat) {
            result = "<";
            diffTime = 1000;
        }

        if (diffTime >= 1000) {
            long rest = (diffTime % 1000);
            if (shortFormat) {
                if (rest > 500) { 
                    diffTime++;
                }
            } else if (rest > min) {
                result = formatNumber(rest, msText, spaceAfterValue());
            }

            diffTime /= 1000;
            endStr = secText;

            if (diffTime > 60) {
                rest = (diffTime % 60);
                result = addResult(rest, secText, result);

                diffTime /= 60;
                endStr = minText;

                if (diffTime > 60) {
                    rest = (diffTime % 60);
                    result = addResult(rest, minText, result);

                    diffTime /= 60;
                    endStr = hourText;

                    if (diffTime > 24) {
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
