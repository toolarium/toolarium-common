/*
 * PropertyExpander.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.util.Properties;


/**
 * Property expander
 *
 * <code>
 * EBNF:
 * -----
 * TERM_EXPRESSION      ::= TERM { BLANKS } { TERM }
 * TERM                 ::= ATTRIBUTE | ENV_EXPRESSION
 * ENV_EXPRESSION       ::= ENV_CHARACTER { BRACE_LEFT ATTRIBUTE BRACE_RIGHT } | { ATTRIBUTE }
 * ATTRIBUTE            ::= [a-zA-Z0-9]
 * ENV_CHARACTER        ::= $
 * BRACE_LEFT           ::= {
 * BRACE_RIGHT          ::= }
 * BLANK                ::=
 * </code>
 *
 * @author patrick
 */
public final class PropertyExpander {
    private static PropertyExpander instance = new PropertyExpander();


    /**
     * Constructor
     */
    private PropertyExpander() {
        // NOP
    }


    /**
     * Get the instance
     *
     * @return the instance
     */
    public static PropertyExpander getInstance() {
        return instance;
    }


    /**
     * Expand the expression
     *
     * @param expression the expression
     * @return the expanded expression
     */
    public String expand(String expression) {
        if (expression == null) {
            return null;
        }

        ExpanderContext context = new ExpanderContext(expression);
        return readTermExpression(context);
    }


    /**
     * Read term expression
     *
     * @param context the context
     * @return the term expression
     */
    private String readTermExpression(ExpanderContext context) {
        int lastPosition = -1;
        boolean positionChange = true;

        String result = "";
        while (!context.isEnd() && positionChange) {
            result += readTerm(context);

            if (!context.isEnd()) {
                result += readBlanks(context);
            }

            // loop protection
            positionChange = lastPosition != context.getPosition();
            lastPosition = context.getPosition();
        }

        return result;
    }


    /**
     * Read a term
     *
     * @param context the context
     * @return the term
     */
    private String readTerm(ExpanderContext context) {
        if (context.startEnvExperession()) {
            return readEnvExpression(context);
        }

        return readAttribute(context, true);
    }


    /**
     * Read blanks
     *
     * @param context the context
     * @return the read blanks
     */
    private String readBlanks(ExpanderContext context) {
        String result = "";

        Character c = context.getCurrent();
        while (c != null && c == ExpanderContext.BLANK) {
            result += c;
            c = context.readNext();
        }

        return result;
    }


    /**
     * Read the attribute
     *
     * @param context the context
     * @param ignoreExpandEndCharacter true to ignore expand end character
     * @return the attribute
     */
    private String readAttribute(ExpanderContext context, boolean ignoreExpandEndCharacter) {
        String result = "";

        Character c = context.getCurrent();
        while (c != null
               && c != ExpanderContext.BLANK
               && c != ExpanderContext.START_ENV_CHARACTER
               && (ignoreExpandEndCharacter || (!ignoreExpandEndCharacter && c != ExpanderContext.END_EXPAND_CHARACTER))) {
            result += c;
            c = context.readNext();
        }

        return result;
    }


    /**
     * Read the attribute
     *
     * @param context the context
     * @return the attribute
     */
    private String readEnvAttributeName(ExpanderContext context) {
        String result = "";

        Character c = context.getCurrent();
        while (c != null
               && c != ExpanderContext.BLANK
               && c != ExpanderContext.START_ENV_CHARACTER
               && c != ExpanderContext.START_EXPAND_CHARACTER
               && c != ExpanderContext.END_EXPAND_CHARACTER) {
            result += c;
            c = context.readNext();
        }

        return result;
    }


    /**
     * Read the environment expression
     *
     * @param context the context
     * @return the environment expression
     */
    String readEnvExpression(ExpanderContext context) {
        Character c = context.getCurrent();
        if (c != ExpanderContext.START_ENV_CHARACTER) {
            return readAttribute(context, false);
        }

        String envExpression = "" + c; // append $
        String envTag = "";

        c = context.readNext();
        if (c != null && c == ExpanderContext.START_EXPAND_CHARACTER) {
            envExpression += c; // append {
            c = context.readNext();

            readBlanks(context);
            envTag += readEnvAttributeName(context);
            envExpression += envTag;
            readBlanks(context);

            if (!context.isEnd()) {
                c = context.getCurrent();
            }

            if (c != null) {
                if (c != ExpanderContext.END_EXPAND_CHARACTER) {
                    return envExpression;
                    //throw new IllegalArgumentException("Start but no end character for environment reference found for tag [" + envTag + "], [" + context.toString() + "]");
                }

                envExpression += c;
            }

            if (!context.isEnd()) {
                c = context.readNext();
            }
        } else {
            envTag = readAttribute(context, false);
            envExpression += envTag;
        }

        String s = resolveProperty(envTag, envExpression);
        if (s.equals("" + ExpanderContext.START_ENV_CHARACTER + ExpanderContext.START_EXPAND_CHARACTER + ExpanderContext.END_EXPAND_CHARACTER)) {
            s = "";
        }

        return s;
    }


    /**
     * Resolve a property
     *
     * @param tag the tag to resolve
     * @param defaultValue the default value
     * @return the result
     */
    private String resolveProperty(String tag, String defaultValue) {
        if (tag == null || tag.isEmpty()) {
            return defaultValue;
        }

        String result = defaultValue;

        // try first to resolve context based properties
        String propEnv = null;
        Properties prop = PropertyExpanderContextBasedProperties.get();
        if (prop != null) {
            propEnv = prop.getProperty(tag);
        }

        // try as second option to resolve the the tag from the system properies
        if (propEnv == null || propEnv.isEmpty()) {
            propEnv = System.getProperty(tag);
        }

        // try as third option to resolve the the tag from the operating system environment
        if (propEnv == null || propEnv.isEmpty()) {
            propEnv = System.getenv().get(tag);
        }

        if (propEnv != null && !propEnv.isEmpty()) {
            result = propEnv;
        }

        return result;
    }


    /**
     * The expander context
     *
     * @author pmeier
     * @version $Revision: 1.6 $
     */
    final class ExpanderContext {
        private static final char BLANK = ' ';
        private static final char START_ENV_CHARACTER = '$';
        private static final char START_EXPAND_CHARACTER = '{';
        private static final char END_EXPAND_CHARACTER = '}';

        private String expression;
        private int currentPosition;


        /**
         * Constructor
         *
         * @param expression the expression
         */
        ExpanderContext(String expression) {
            this.expression = expression;
            this.currentPosition = 0;
        }


        /**
         * Check if the end is reached
         *
         * @return true if the end is reached
         */
        boolean isEnd() {
            return (expression == null || expression.length() == currentPosition);
        }


        /**
         * Check if it start with an env expression
         *
         * @return true if it starts as an env expression
         */
        boolean startEnvExperession() {
            return expression.charAt(currentPosition) == START_ENV_CHARACTER;
        }


        /**
         * Read next character
         *
         * @return the next character or null in case it is at the end
         */
        Character getCurrent() {
            if (isEnd()) {
                return null;
            }

            return expression.charAt(currentPosition);
        }


        /**
         * Read next character
         *
         * @return the next character or null in case it is at the end
         */
        Character readNext() {
            if (isEnd()) {
                return null;
            }

            currentPosition++;
            return getCurrent();
        }


        /**
         * Get the current position
         *
         * @return the current position
         */
        int getPosition() {
            return currentPosition;
        }


        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return expression;
        }
    }
}
