/*
 * AbstractDifferenceFormatter.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.formatter;

import com.github.toolarium.common.util.TextUtil;
import java.text.DecimalFormat;


/**
 * 
 * @author patrick
 */
public abstract class AbstractDifferenceFormatter  {
    /** The internal decimal formatter */
    private DecimalFormat formatter;
    
    /** Defines if after a value follows a space */
    private boolean spaceAfterValue = false;

    
    /**
     * Constructor
     * 
     * @param pattern the format pattern
     * @param spaceAfterValue true to have a space after the value
     */
    public AbstractDifferenceFormatter(String pattern, boolean spaceAfterValue) {
        formatter = new DecimalFormat(pattern);
        formatter.setMinimumFractionDigits(0);
        formatter.setGroupingUsed(false);
        this.spaceAfterValue = spaceAfterValue;
    }
    
    
    /**
     * Formats the given value to a well formated string
     * 
     * @param value the value difference
     * @param len the size of the string
     * @return the formated string 
     */
    public String formatAsString(double value, int len) {
        String t = formatAsString(value);

        if (len > 0) {
            return TextUtil.getInstance().blockText(t, len);
        }
        return t;
    }    
    
    
    /**
     * Formats the given value to a well formated string
     * 
     * @param value the value difference
     * @param len the size of the string
     * @return the formated string 
     */
    public String formatAsString(long value, int len) {
        String t = formatAsString(value);

        if (len > 0) {
            return TextUtil.getInstance().blockText(t, len);
        }

        return t;
    }
    
    
    /**
     * Formats the given value to a well formated string
     * @param value the value difference
     * @return the formated string 
     */
    public String formatAsString(double value) {
        return formatAsString(Math.round(value));
    }    
    
    
    /**
     * Formats the given value to a well formated string
     * @param value the value difference
     * @return the formated string 
     */
    public abstract String formatAsString(long value);
    
    /** 
     * Formats a given number
     * 
     * @param number the number to format
     * @param trailer the tailer
     * @param space add space between number and trailer
     * @return the formated number
     */
    public String formatNumber(double number, String trailer, boolean space) {
        String t = "";

        if (trailer != null) {
            t = trailer;
        }

        if (space) {
            return formatter.format(number) + " " + t;
        }
        
        return formatter.format(number) + t;
    }
    
    
    /** 
     * Formats a given number
     * 
     * @param number the number to format
     * @param trailer the tailer
     * @param space add space between number and trailer
     * @return the formated number
     */
    protected String formatNumber(long number, String trailer, boolean space) {
        String t = "";

        if (trailer != null) {
            t = trailer;
        }
        
        if (space) {
            return formatter.format(number) + " " + t;
        }
        
        return formatter.format(number) + t;
    }
    
    
    /**
     * Gets a prepared string which contains the result and additional the given
     * value
     * 
     * @param value the value
     * @param trailer the trailer  
     * @param result the current result
     * @return the prepared string
     */
    protected String addResult(long value, String trailer, String result) {
        String t = result;

        if (value > 0) {
            if (result != null && result.length() > 0) {
                t = formatNumber(value, trailer, spaceAfterValue) + " " + result;
            } else {
                t = formatNumber(value, trailer, spaceAfterValue);
            }
        }

        return t;
    }
    
    
    /**
     * Get the decimal formatter
     *
     * @return the decimal formatter
     */
    protected DecimalFormat getDecimalFormatter() {
        return formatter;
    }
    
    
    /**
     * Defines if after a value follows a space
     * 
     * @return true to add space after the value
     */
    protected boolean spaceAfterValue() {
        return spaceAfterValue;
    }
}
