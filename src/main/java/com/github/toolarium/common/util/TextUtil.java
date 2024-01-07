/*
 * TextUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.util.StringTokenizer;


/**
 * Text utility
 * 
 * @author patrick
 */
public final class TextUtil {
    /** NL */
    public static final String NL = "\n";

    /** SPACE */
    public static final String SPACE = " ";

    
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
        static final TextUtil INSTANCE = new TextUtil();
    }

    
    /**
     * Constructor
     */
    private TextUtil() {
        // NOP
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static TextUtil getInstance() {
        return HOLDER.INSTANCE;
    }

    
    /**
     * Prepare input string 
     *
     * @param input the input string
     * @return the prepared string
     */
    public String prepareInput(String input) {
        if (input != null && !input.isEmpty()) {
            return input;
        }
        
        return "";
    }

    
    /**
     * Prepare input string 
     *
     * @param input the input string
     * @return the prepared string
     */
    public StringBuilder prepareInput(StringBuilder input) {
        if (input != null && !input.toString().isEmpty() /* TODO: after java 1.8 support !input.isEmpty()*/) {
            return input;
        }
        
        return new StringBuilder();
    }

    
    /**
     * Create a block text
     *
     * @param text the text
     * @param maxLen the max len of the text
     * @return the created block text
     */
    public String blockText(String text, int maxLen) {
        return this.blockText(prepareInput(new StringBuilder(prepareInput(text))), maxLen).toString();
    }

    
    /**
     * Create a block text
     *
     * @param indent the indention
     * @param text the text
     * @param maxLen the max len of the text
     * @return the created block text
     */
    public String blockText(String indent, String text, int maxLen) {
        return this.blockText(prepareInput(new StringBuilder(prepareInput(indent))),
                              prepareInput(new StringBuilder(prepareInput(text))),
                              maxLen).toString();
    }

    
    /**
     * Create a block text
     *
     * @param indent the indention
     * @param title the title
     * @param maxTitelLen the max title len
     * @param text the text
     * @param maxLen the max len of the text
     * @return the created block text
     */
    public String blockText(String indent, String title, int maxTitelLen, String text, int maxLen) {
        return this.blockText(prepareInput(new StringBuilder(prepareInput(indent))),
                              prepareInput(new StringBuilder(prepareInput(title))),
                              maxTitelLen,
                              prepareInput(new StringBuilder(prepareInput(text))),
                              maxLen).toString();
    }

    
    /**
     * Create a block text
     *
     * @param inputText the text
     * @param maxLen the max len of the text
     * @return the created block text
     */
    public StringBuilder blockText(StringBuilder inputText, int maxLen) {
        return this.blockText(null, null, 0, inputText, maxLen);
    }

    
    /**
     * Create a block text
     *
     * @param inputIndent the indention
     * @param inputText the text
     * @param maxLen the max len of the text
     * @return the created block text
     */
    public StringBuilder blockText(StringBuilder inputIndent, StringBuilder inputText, int maxLen) {
        return this.blockText(inputIndent, null, 0, inputText, maxLen);
    }

    
    /**
     * Create a block text
     *
     * @param inputIndent the indention
     * @param inputTitle the title
     * @param maxTitelLen the max title len
     * @param inputText the text
     * @param maxTextLen the max len of the text
     * @return the created block text
     */
    public StringBuilder blockText(StringBuilder inputIndent, StringBuilder inputTitle, int maxTitelLen, StringBuilder inputText, int maxTextLen) {
        StringBuilder titleBuidler = createTitleBuilder(inputTitle, maxTitelLen);
        StringBuilder indentBuilder = prepareInput(inputIndent);
        StringBuilder subIndent = createIndent(indentBuilder, titleBuidler);

        StringBuilder builder = new StringBuilder().append(indentBuilder).append(titleBuidler);
        if (maxTextLen <= 0) {
            return builder;
        }
        
        StringBuilder data = new StringBuilder();
        StringTokenizer st = new StringTokenizer(prepareInput(inputText).toString(), " \t\n\r\f", true);
        while (st.hasMoreTokens()) {
            String d = st.nextToken();
            if ((data.length() + d.length()) >= maxTextLen) {
                if (!data.toString().isEmpty()) {
                    builder.append(data);
                    if (data.length() < maxTextLen) {
                        builder.append(StringUtil.getInstance().newString(SPACE, maxTextLen - data.length()));
                    }
                    builder.append(NL).append(subIndent);
                }
                
                data = new StringBuilder();
                d = StringUtil.getInstance().trimLeft(d);
                
                while (d.length() > maxTextLen) {
                    data.append(d.substring(0, maxTextLen));
                    builder.append(data).append(NL).append(subIndent);
                    data = new StringBuilder();
                    d = d.substring(maxTextLen);
                }
                
                data = new StringBuilder().append(d);
            } else {
                data.append(d);
            }
            
            if (st.hasMoreTokens()) {
                String sep = st.nextToken();
                if (SPACE.equals(sep)) {
                    data.append(sep);
                }
            }
        }
        
        if (!data.toString().isEmpty()) {
            builder.append(data);
            if (data.length() < maxTextLen) {
                builder.append(StringUtil.getInstance().newString(SPACE, maxTextLen - data.length()));
            }
        }
        
        return builder;
    }


    /**
     * Create title
     * 
     * @param inputTitle the title
     * @param maxTitelLen the title max len
     * @return the title
     */
    private StringBuilder createTitleBuilder(StringBuilder inputTitle, int maxTitelLen) {
        if (maxTitelLen <= 0) {
            return new StringBuilder();
        }
        
        StringBuilder titleBuidler = prepareInput(inputTitle);
        if (maxTitelLen < titleBuidler.length()) {
            titleBuidler = new StringBuilder(titleBuidler.substring(0, maxTitelLen));
        } else {
            int len = maxTitelLen - titleBuidler.length();
            if (len > 0) {
                titleBuidler = titleBuidler.append(StringUtil.getInstance().newString(SPACE, len));
            }
        }
        
        return titleBuidler;
    }


    /**
     * Create indentition
     * 
     * @param indentBuilder the indent builder
     * @param titleBuidler the title builder
     * @return the indention
     */
    private StringBuilder createIndent(StringBuilder indentBuilder, StringBuilder titleBuidler) {
        StringBuilder subIndent = null;
        if (titleBuidler.length() + indentBuilder.length() > 0) {
            subIndent = StringUtil.getInstance().newStringBuilder(SPACE, titleBuidler.length() + indentBuilder.length());
        }
        subIndent = prepareInput(subIndent);
        return subIndent;
    }
}
