/*
 * StringUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * String util class
 * 
 * @author patrick
 */
public final class StringUtil {
    private static final char[] CAMEL_CASE_STOP_CHARACTERS = new char[] {'.', '_', ' ' };
    private static final char[] hexDigits = "0123456789ABCDEF".toCharArray();
    
    
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final StringUtil INSTANCE = new StringUtil();
    }

    
    /**
     * Constructor
     */
    private StringUtil() {
        // NOP
    }
    

    /**
     * Get the instance
     *
     * @return the instance
     */
    public static StringUtil getInstance() {
        return HOLDER.INSTANCE;
    }

    
    /**
     * New string builder
     * 
     * @param input the input string to repeat
     * @param num the number of repetitions
     * @return the prepared string; empty if num is zero or negative
     * @throws IllegalArgumentException In case the resulting string would be too large
     */
    public StringBuilder newStringBuilder(CharSequence input, int num) {
        if (input == null) {
            return null;
        }

        if (num <= 0) {
            return new StringBuilder();
        }

        long capacity = (long) num * input.length() + 1;
        if (capacity > Integer.MAX_VALUE - 8) {
            throw new IllegalArgumentException("The resulting string is too large (" + num + " x " + input.length() + ")!");
        }

        StringBuilder buffer = new StringBuilder((int) capacity);
        for (int j = 0; j < num; j++) {
            buffer.append(input);
        }

        return buffer;
    }    

    
    /**
     * New string builder with given length and filled up with the given character
     * 
     * @param num the number of repetitions
     * @param character character to fill up the string
     * @return a filled up string; empty if num is zero or negative
     * @throws IllegalArgumentException In case the resulting string would be too large
     */
    public StringBuilder newStringBuilder(char character, int num) {
        if (num <= 0) {
            return new StringBuilder();
        }

        long capacity = (long) num + 1;
        if (capacity > Integer.MAX_VALUE - 8) {
            throw new IllegalArgumentException("The resulting string is too large (" + num + ")!");
        }

        StringBuilder result = new StringBuilder((int) capacity);
        for (int i = 0; i < num; i++) {
            result.append(character);
        }

        return result;
    }

    
    /**
     * New string builder
     * 
     * @param num the number of repetitions
     * @param input the input string to repeat
     * @return the prepared string
     */
    public String newString(CharSequence input, int num) {
        StringBuilder result = newStringBuilder(input, num);
        if (result == null) {
            return null;
        }
        
        return result.toString();
    }    
    
    
    /**
     * New string with given length and filled up with the given character
     * 
     * @param num the number of repetitions
     * @param character character to fill up the string
     * @return a filled up string
     */     
    public String newString(char character, int num) {
        StringBuilder result = newStringBuilder(character, num);
        if (result == null) {
            return null;
        }
        
        return result.toString();
    }

    
    /**
     * Format a given string to a given length and fill up with the given character
     * Example:
     * <pre>
     *   input: 
     *      text:       "fobar"
     *      width:      8
     *   output:
     *      "fobar   "
     * </pre>
     * It fills up with blanks and cuts on the right side if the text is too long.
     *
     * @param text the string to test
     * @param width the width of the string
     * @return the formated string
     */
    public String width(CharSequence text, int width) {
        return width(text, width, ' ', true);
    }
    
    
    /**
     * Format a given string to a given length and fill up with the given character
     * Example:
     * <pre>
     *   input: 
     *      text:       "fobar"
     *      width:      8
     *      fillupChar: '-'
     *      cutRight: false
     *   output:
     *      fobar---
     * </pre>
     * If the text is longer than width, it is cut on the right side (cutRight true) or on the left side (cutRight false).
     *
     * @param text the string to test
     * @param width the width of the string
     * @param fillupChar character to fill up the string
     * @param cutRight if the given string is too long then cut it on the right side
     * @return the formated string
     */
    public String width(CharSequence text, int width, char fillupChar, boolean cutRight) {
        StringBuilder result = new StringBuilder();
        if (width <= 0) {
            return result.toString();
        }

        if (text != null) {
            result.append(text);
        }

        if (result.length() > width) {
            if (cutRight) {
                return result.substring(0, width);
            }
            return result.substring(result.length() - width, result.length());
        }

        result.append(newStringBuilder(fillupChar, width - result.length()));
        return result.toString();
    }

    
    /**
     * Counts a given character in the given String.
     * 
     * @param input the string to count the characters.
     * @param ch the character to count.
     * @return the number of occurrences of the given character in the given string
     */
    public int countCharacters(String input, char ch) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        
        int result = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == ch) {
                result++;
            }
        }

        return result;
    }    
    
    
    /**
     * Count of a string a given ending character how many times it is existing
     * 
     * @param input the input string
     * @param ch the character to test 
     * @return the number of found ch characters at the end
     */
    public int countEndingCharacter(String input, char ch) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        
        int idx = input.length();
        int counter = 0;
        while (idx > 0 && (input.charAt(--idx) == ch)) {
            counter++;
        }

        return counter;
    }

    
    /**
     * Trims a given string from the right side. 
     * 
     * @param data the string to format
     * @return the trimed string
     */
    public String trimRight(String data) {
        return trimRight(data, ' ');
    }
    
    
    /**
     * Trims a given string from the right side. 
     * 
     * @param data the string to format
     * @param ch the character to trim from the string
     * @return the trimed string
     */
    public String trimRight(String data, char ch) {
        if (data == null) {
            return null;
        }

        int len = data.length() - 1;
        int occurrence = 0;
        for (int i = len; (i >= 0) && (data.charAt(i) == ch); i--) {
            occurrence++;
        }
        
        if (occurrence == 0) {
            return data;
        }
        
        return data.substring(0, data.length() - occurrence);
    }
    
    
    /**
     * Trims a given string from the left side. 
     * 
     * @param data the string to format
     * @return the trimed string
     */
    public String trimLeft(String data) {
        return trimLeft(data, ' ');
    }
    
    
    /**
     * Trims a given string from the left side. 
     * 
     * @param data the string to format
     * @param ch the character to trim from the string
     * @return the trimed string
     */
    public String trimLeft(String data, char ch) {
        if (data == null) {
            return null;
        }
        
        int len = data.length();
        int occurrence = 0;
        for (int i = 0; (i < len) && (data.charAt(i) == ch); i++) {
            occurrence++;
        }

        if (occurrence == 0) {
            return data;
        }

        return data.substring(occurrence, data.length());
    }

    
    /**
     * Converts the given string array to a string
     * 
     * @param array the data to parse
     * @return the converted string
     */
    public String toString(String[] array) {
        return toString(array, " ");
    }
    
    
    /**
     * Converts the given string array to a string
     * 
     * @param array the data to parse
     * @param sep the separator
     * @return the converted string
     */
    public String toString(String[] array, String sep) {
        if (array == null) {
            return null;
        }
        
        StringBuilder theData = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (sep != null && i > 0) {
                theData.append(sep);
            }
            
            theData.append(array[i]);
        }

        return theData.toString();
    }
    
    
    /**
     * Converts the given string array to a string
     * 
     * @param array the data to parse
     * @return the converted string
     */
    public String toString(Object[] array) {
        return toString(array, ", ");
    }

    
    /**
     * Converts the given array to a string
     * 
     * @param array the data to parse
     * @param sep the separator
     * @return the converted string
     */
    public String toString(Object[] array, String sep) {
        if (array == null) {
            return null;
        }
        
        StringBuilder theData = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (sep != null && i > 0) {
                theData.append(sep);
            }
            
            if (array[i] != null) {
                theData.append(array[i]);
            } else {
                theData.append("(null)");
            }
        }
        
        return theData.toString();
    }    


    /**
     * Converts a given byte array to a string representation
     * 
     * @param b the bytes
     * @return bytes as string
     */
    public String toString(byte[] b) {
        if (b == null) {
            return "(null)";
        }
        
        return toString(b, 0, b.length);
    }

    
    /**
     * Converts a given byte array to a string representation
     * 
     * @param b the bytes
     * @param ofs the offset
     * @param len the length of the bytes
     * @return bytes as string
     * @throws IllegalArgumentException In case of an invalid offset or length
     */
    public String toString(byte[] b, int ofs, int len) {
        if (b == null) {
            return "(null)";
        }
        
        if (ofs < 0 || len < 0 || ofs > b.length - len) {
            throw new IllegalArgumentException("Invalid offset (" + ofs + ") or length (" + len + ") for an array of length " + b.length + "!");
        }

        StringBuilder sb = new StringBuilder(len * 3);
        for (int i = 0; i < len; i++) {
            int c = b[ofs + i] & 0xff;

            sb.append(hexDigits[c >> 4]);
            sb.append(hexDigits[c & 15]);
            if (i != len - 1) {
                sb.append(':');
            }
        }

        return sb.toString();
    }  


    /**
     * Split a string into a list by length
     * 
     * @param splitString the string to split
     * @param partLength the length into which the input string is to be divided
     * @return the string list or null
     */
    public List<String> splitAsList(String splitString, int partLength) {
        if (splitString == null) {
            return null;
        }

        if (partLength <= 0 || splitString.length() <= partLength) {
            List<String> result = new ArrayList<String>(1);
            result.add(splitString);
            return result;
        }

        int len = splitString.length();
        int nparts = (len + partLength - 1) / partLength;
        List<String> result = new ArrayList<String>(nparts);

        int offset = 0;
        for (int i = 0; i < nparts; i++) {
            result.add(splitString.substring(offset, Math.min(offset + partLength, len)));
            offset += partLength;
        }

        return result;
    }

    
    /**
     * Split a string into a list. Empty parts between two separators are kept and an expression which starts
     * with the separator results in a leading empty part (e.g. <code>"/a/b"</code> results in <code>["", "a", "b"]</code>).
     * A single trailing separator does not produce a trailing empty part (e.g. <code>"a/"</code> results in <code>["a"]</code>).
     *
     * @param expression the expression to split
     * @param splitString the string, whereby the expression string is split
     * @return the string list or null
     */
    public List<String> splitAsList(String expression, String splitString) {
        if (expression == null) {
            return null;
        }

        List<String> result = new ArrayList<String>();
        if (splitString == null) {
            result.add(expression);
        } else if (splitString.length() == 0) {
            for (int i = 0; i < expression.length(); i++) {
                result.add("" + expression.charAt(i));
            }
        } else {
            int start = 0;
            int splitLength = splitString.length();
            int i = expression.indexOf(splitString, start);
            while (i >= 0) {
                result.add(expression.substring(start, i));
                start = i + splitLength;
                i = expression.indexOf(splitString, start);
            }

            if (start < expression.length() || result.isEmpty()) {
                result.add(expression.substring(start));
            }
        }

        return result;
    }

    
    /**
     * Split a string into an array by length
     * 
     * @param splitString the string to split
     * @param partLength the length into which the input string is to be divided
     * @return the array or null
     */
    public String[] splitAsArray(String splitString, int partLength) {
        List<String> result = splitAsList(splitString, partLength);
        if (result == null) {
            return null;
        }

        String[] resultArr = new String[result.size()];
        resultArr = result.toArray(resultArr);
        return resultArr;
    }

    
    /**
     * Split a string into an array 
     * 
     * @param expression the expression to split
     * @param splitString the string, whereby the expression string is split
     * @return the array or null
     */
    public String[] splitAsArray(String expression, String splitString) {
        List<String> result = splitAsList(expression, splitString);
        if (result == null) {
            return null;
        }

        String[] resultArr = new String[result.size()];
        resultArr = result.toArray(resultArr);
        return resultArr;
    }

    
    /**
     * Changes the first letter of the given String to an upper case letter.
     * 
     * @param name A name.
     * @return The first letter of the given String to an upper case letter.
     */
    public String changeFirstLetterToUpperCase(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }

        StringBuilder changedName = new StringBuilder();
        changedName.append(name.substring(0, 1).toUpperCase(Locale.ROOT));
        changedName.append(name.substring(1, name.length()));

        return changedName.toString();
    }
    
    
    /**
     * Changes the first letter of the given String to a lower case letter.
     * 
     * @param name A name.
     * @return The first letter of the given String to a lower case letter.
     */
    public String changeFirstLetterToLowerCase(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }

        StringBuilder changedName = new StringBuilder();
        changedName.append(name.substring(0, 1).toLowerCase(Locale.ROOT));
        changedName.append(name.substring(1, name.length()));

        return changedName.toString();
    }    
    
    
    /**
     * Convert a given string to CamelCase formated string (lower/upper or pascal case).
     * Example: 
     *  <li>input: This is an example
     *  <li>output: ThisIsAnExample
     * 
     * @param data the string to format
     * @param startWithLowerCase true to start with lower case (lower camel case); otherwise false (upper camel or pascal case)
     * @return the formated string
     */
    public String toCamelCase(final String data, boolean startWithLowerCase) {
        if (data == null) {
            return null;
        }

        final String d = data.trim();
        if (d.length() == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; buffer != null; i++) {
            if (!appendString(buffer, d, i, CAMEL_CASE_STOP_CHARACTERS)) {
                if (buffer != null && buffer.length() > 0) {
                    String txt = buffer.toString();
                    buffer = new StringBuilder();
                    if (txt.toUpperCase(Locale.ROOT).equals(txt)) {
                        txt = txt.toLowerCase(Locale.ROOT);
                    }
    
                    if (result.length() == 0) {
                        if (startWithLowerCase) {
                            result.append(txt.toLowerCase(Locale.ROOT));
                        } else {
                            result.append(changeFirstLetterToUpperCase(txt));
                        }
                    } else {
                        result.append(changeFirstLetterToUpperCase(txt));
                    }
                } else if (i >= d.length()) {
                    buffer = null;
                }
            }
        }

        return result.toString();
    }

    
    /**
     * Convert a given string from CamelCase notation to upper case formated string.
     * Example: 
     *  input: ThisIsAnExample
     *  output: THIS_IS_AN_EXAMPLE
     * 
     * @param data the string to format
     * @param toLowerCase true to lower snake case; otherwise to upper snake case
     * @return the formated string
     */
    public String fromCamelCaseToSnakeCase(final String data, boolean toLowerCase) {
        if (data == null) {
            return null;
        }
        
        final String d = data.trim();
        if (d.length() == 0) {
            return "";
        }

        // only an upper case letter starts a new word; a space, dot or underscore separates words; separators are never doubled
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            final char ch = d.charAt(i);
            if (Character.isSpaceChar(ch) || ch == '.' || ch == '_') {
                appendSnakeCaseSeparator(result);
            } else {
                if (i > 0 && Character.isUpperCase(ch)) {
                    appendSnakeCaseSeparator(result);
                }

                if (toLowerCase) {
                    result.append(Character.toLowerCase(ch));
                } else {
                    result.append(Character.toUpperCase(ch));
                }
            }
        }

        return result.toString();
    }


    /**
     * Append the snake case separator (underscore) if the result does not already end with it
     *
     * @param result the result
     */
    private void appendSnakeCaseSeparator(StringBuilder result) {
        if (result.length() == 0 || result.charAt(result.length() - 1) != '_') {
            result.append('_');
        }
    }

    
    /**
     * Convert a given string to SnakeCase formated string.
     * Example: 
     *  input: This is an example
     *  output: This_is_an_example
     * 
     * @param data the string to format
     * @return the formated string
     */
    public String toSnakeCase(final String data) {
        if (data == null) {
            return null;
        }
        
        final String d = data.trim();
        if (d.length() == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; buffer != null; i++) {
            if (!appendString(buffer, d, i, CAMEL_CASE_STOP_CHARACTERS)) {
                if (buffer != null && buffer.length() > 0) {
                    String txt = buffer.toString();
                    buffer = new StringBuilder();
                    if (txt != null && txt.length() > 0) {
                        if (result.length() > 0) {
                            result.append('_');
                        }
        
                        result.append(txt);
                    }
                } else if (i >= d.length()) {
                    buffer = null;
                }
            }
        }

        return result.toString();
    }
    

    /**
     * Append string to the buffer
     *
     * @param buffer the buffer
     * @param input the input string
     * @param i the current index inside the input string
     * @param characters the characters to stop
     * @return true if characters from the input string could be appended
     */
    private boolean appendString(StringBuilder buffer, String input, int i, char[] characters) {
        if (input == null || input.length() == 0 || i < 0) {
            return false;
        }
        
        if (buffer != null && i < input.length()) {
            char ch = input.charAt(i);
            boolean existStopCharacter = false;
            for (int j = 0; j < characters.length; j++) {
                if (ch == characters[j]) {
                    existStopCharacter = true;
                    break;
                }
            }
            
            if (!existStopCharacter) {
                buffer.append(ch);
                return true;
            }
        }
        
        return false;
    }
}
