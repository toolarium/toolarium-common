/*
 * PropertyExpander.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import java.util.Properties;


/**
 * Property expander: replaces references like <code>$NAME</code> or <code>${NAME}</code> by the value of the property.
 * The value is resolved from the {@link PropertyExpanderContextBasedProperties}, then from the system properties and
 * at last from the environment variables. An empty value is a valid value. A reference which can not be resolved
 * or which is not well-formed (e.g. an unterminated <code>${NAME</code> or <code>${NAME other}</code>) is kept as it is.
 *
 * <pre>
 * EBNF:
 * -----
 * TERM_EXPRESSION      ::= TERM { BLANKS TERM }
 * TERM                 ::= ENV_EXPRESSION | ATTRIBUTE
 * ENV_EXPRESSION       ::= ENV_CHARACTER ( BRACE_LEFT { BLANK } NAME { BLANK } BRACE_RIGHT | SIMPLE_NAME )
 * ATTRIBUTE            ::= any character except BLANK and ENV_CHARACTER { any character except BLANK and ENV_CHARACTER }
 * NAME                 ::= any character except BLANK, ENV_CHARACTER, BRACE_LEFT and BRACE_RIGHT { ... }
 * SIMPLE_NAME          ::= SIMPLE_NAME_CHAR { SIMPLE_NAME_CHAR }
 * SIMPLE_NAME_CHAR     ::= letter | digit | '_' | '.' | '-'
 * ENV_CHARACTER        ::= '$'
 * BRACE_LEFT           ::= '{'
 * BRACE_RIGHT          ::= '}'
 * BLANKS               ::= BLANK { BLANK }
 * BLANK                ::= ' '
 * </pre>
 *
 * @author patrick
 */
public final class PropertyExpander {
    private static final PropertyExpander INSTANCE = new PropertyExpander();


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
        return INSTANCE;
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

        StringBuilder result = new StringBuilder();
        while (!context.isEnd() && positionChange) {
            result.append(readTerm(context));

            if (!context.isEnd()) {
                result.append(readBlanks(context));
            }

            // loop protection
            positionChange = lastPosition != context.getPosition();
            lastPosition = context.getPosition();
        }

        return result.toString();
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
        StringBuilder result = new StringBuilder();

        Character c = context.getCurrent();
        while (c != null && c == ExpanderContext.BLANK) {
            result.append(c);
            c = context.readNext();
        }

        return result.toString();
    }


    /**
     * Read the attribute
     *
     * @param context the context
     * @param ignoreExpandEndCharacter true to ignore expand end character
     * @return the attribute
     */
    private String readAttribute(ExpanderContext context, boolean ignoreExpandEndCharacter) {
        StringBuilder result = new StringBuilder();

        Character c = context.getCurrent();
        while (c != null
               && c != ExpanderContext.BLANK
               && c != ExpanderContext.START_ENV_CHARACTER
               && (ignoreExpandEndCharacter || c != ExpanderContext.END_EXPAND_CHARACTER)) {
            result.append(c);
            c = context.readNext();
        }

        return result.toString();
    }


    /**
     * Read the attribute
     *
     * @param context the context
     * @return the attribute
     */
    private String readEnvAttributeName(ExpanderContext context) {
        StringBuilder result = new StringBuilder();

        Character c = context.getCurrent();
        while (c != null
               && c != ExpanderContext.BLANK
               && c != ExpanderContext.START_ENV_CHARACTER
               && c != ExpanderContext.START_EXPAND_CHARACTER
               && c != ExpanderContext.END_EXPAND_CHARACTER) {
            result.append(c);
            c = context.readNext();
        }

        return result.toString();
    }


    /**
     * Read the name of a reference without braces: it consists of letters, digits, '_', '.' and '-' and ends at
     * any other character.
     *
     * @param context the context
     * @return the name, empty if there is no name
     */
    private String readSimpleName(ExpanderContext context) {
        StringBuilder result = new StringBuilder();

        Character c = context.getCurrent();
        while (c != null && isSimpleNameCharacter(c)) {
            result.append(c);
            c = context.readNext();
        }

        return result.toString();
    }


    /**
     * Check if a character is part of a name of a reference without braces.
     *
     * @param c the character
     * @return true if it is a letter, a digit, '_', '.' or '-'
     */
    private static boolean isSimpleNameCharacter(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '-';
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

        final int startPosition = context.getPosition();
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
                    // not well-formed: keep the original text as it is
                    return context.substring(startPosition, context.getPosition());
                    //throw new IllegalArgumentException("Start but no end character for environment reference found for tag [" + envTag + "], [" + context.toString() + "]");
                }

                envExpression += c;
            }

            if (!context.isEnd()) {
                c = context.readNext();
            }
        } else {
            envTag = readSimpleName(context);
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

        // try first to resolve context based properties
        String propEnv = null;
        Properties prop = PropertyExpanderContextBasedProperties.get();
        if (prop != null) {
            propEnv = prop.getProperty(tag);
        }

        // try as second option to resolve the the tag from the system properies (an empty value is a valid value)
        if (propEnv == null) {
            propEnv = System.getProperty(tag);
        }

        // try as third option to resolve the the tag from the operating system environment
        if (propEnv == null) {
            propEnv = System.getenv().get(tag);
        }

        String result = defaultValue;
        if (propEnv != null) {
            result = propEnv;
        }

        return result;
    }


    /**
     * The expander context
     *
     * @author pmeier
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
         * Get a part of the expression
         *
         * @param start the start position (inclusive)
         * @param end the end position (exclusive)
         * @return the part of the expression
         */
        String substring(int start, int end) {
            return expression.substring(start, end);
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
