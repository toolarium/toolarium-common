/*
 * StackTrace.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.stacktrace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Consumer;


/**
 * The StackTrace class is a collection of static methods to allow users to quickly determine the call stack.
 * This class makes it possible to determine what class called a method and what text files and line numbers
 * resulted in stack calls. The StackTrace calls will not appear in the result.
 * By calling the method {@link #getStackTraceElements()} an array of {@link StackTraceElement} is returned.
 * 
 * @author patrick
 */
public final class StackTrace {
    
    /** Default Stacktrace exclues */
    public static final List<String> DEFAULT_EXCLUDES;
    private static String where;

    static {
        List<String> excludes = new ArrayList<String>();
        excludes.add("java.base/");
        excludes.add("jdk.internal.");
        excludes.add("sun.");
        excludes.add("com.sun.");
        excludes.add("java.lang.reflect.");
        excludes.add("org.testng.");
        excludes.add("junit.");
        excludes.add("org.junit.");
        excludes.add("org.gradle.");
        excludes.add("org.eclipse.jdt.internal.");
        DEFAULT_EXCLUDES = Collections.unmodifiableList(excludes);
        where = StackTrace.class.getName();
    }

    
    /**
     * Constructor
     */
    private StackTrace() {
    }

    
    /**
     * Parse the stack trace
     *  
     * @return an array with stack trace elements.
     */
    public static StackTraceElement[] getStackTraceElements() {
        java.lang.StackTraceElement[] javaElements = Thread.currentThread().getStackTrace();
        return convertStackTraceElements(javaElements, 2, -1);
    }    

    
    /**
     * Parse the stack trace
     * 
     * @param numberOfLastElements the number of the last elements in stack trace
     * @return an array with stack trace elements.
     */
    public static StackTraceElement[] getStackTraceElements(int numberOfLastElements) {
        return getStackTraceElements(3, numberOfLastElements);
    }

    
    /**
     * Parse the stack trace
     * 
     * @param startIndex the start index 
     * @param numberOfLastElements the number of the last elements in stack trace
     * @return an array with stack trace elements.
     */
    public static StackTraceElement[] getStackTraceElements(int startIndex, int numberOfLastElements) {
        java.lang.StackTraceElement[] javaElements = Thread.currentThread().getStackTrace();
        return convertStackTraceElements(javaElements, startIndex, numberOfLastElements);
    }

        
    /**
     * Create the current stack trace
     * 
     * @return the stack trace
     */
    public static String getStackTrace() {
        return getStackTrace(-1);
    }    

    
    /**
     * Create the current stack trace
     * 
     * @param traceSize the size of the stack trace (default -1)
     * @return the stack trace
     */
    public static String getStackTrace(int traceSize) {
        // the own frames are filtered first, so that the trace size counts only the frames of the caller
        StackTraceElement[] elements = getStackTraceElements(-1);
        StringBuilder buffer = new StringBuilder();

        boolean addNewline = false;
        int count = 0;
        for (int i = 0; i < elements.length && (traceSize <= 0 || count < traceSize); i++) {
            StackTraceElement element = elements[i];

            if ((element != null) && !isOwnFrame(element)) {
                count++;
                if (addNewline) {
                    buffer.append(System.getProperty("line.separator"));
                } else {
                    addNewline = true;
                }
                
                buffer.append(element.toString());
            }
        }

        return buffer.toString();
    }


    
    /**
     * Prepares the given trace elements as stack trace
     * 
     * @param elements the elements
     * @return the prepared stack trace
     */
    public static String getStackTrace(StackTraceElement[] elements) {
        return getStackTrace(elements, DEFAULT_EXCLUDES);
    }

    
    /**
     * Prepares the given trace elements as stack trace
     * 
     * @param elements the elements
     * @param filteredClassNames a list of element names which should be filtered
     * @return the prepared stack trace
     */
    public static String getStackTrace(StackTraceElement[] elements, List<String> filteredClassNames) {
        return getStackTrace(elements, filteredClassNames, null);
    }

    
    /**
     * Prepares the given trace elements as stack trace
     * 
     * @param elements the elements
     * @param filteredClassNames a list of element names which should be filtered
     * @param header the header
     * @return the prepared stack trace
     */
    public static String getStackTrace(StackTraceElement[] elements, List<String> filteredClassNames, String header) {
        return getStackTrace(elements, filteredClassNames, header, System.getProperty("line.separator"));
    }

    
    /**
     * Prepares the given trace elements as stack trace
     * 
     * @param elements the elements
     * @param filteredClassNames a list of element names which should be filtered
     * @param header the header
     * @param newline the newline
     * @return the prepared stack trace
     */
    public static String getStackTrace(StackTraceElement[] elements, List<String> filteredClassNames, String header, String newline) {
        StringBuilder buffer = new StringBuilder();
        if (elements == null || elements.length == 0) {
            return buffer.toString();
        }
        
        String indentElement = null;
        if (header != null) {
            indentElement = "\tat ";
            
            if (!header.isEmpty()) {
                buffer.append(header);
                buffer.append(newline);
            }
        }
        
        boolean addNewline = false;
        for (int i = 0; i < elements.length; i++) {
            StackTraceElement element = elements[i];
            if (element != null && !filterStackTraceElement(filteredClassNames, element)) {
                if (addNewline) {
                    buffer.append(newline);
                } else {
                    addNewline = true;
                }

                if (element.getHeader() == null && indentElement != null && !indentElement.isEmpty()) {
                    buffer.append(indentElement);
                }
                
                buffer.append(element.toString());
            }
        }

        return buffer.toString();
    }

    
    /**
     * Process the current stack trace
     * 
     * @param consumer the consumer
     */
    public static void processStackTrace(Consumer<String> consumer) {
        consumer.accept(getStackTrace(getStackTraceElements(3, -1)));
    }

    
    /**
     * Create the current stack trace
     * 
     * @param consumer the consumer
     * @param filteredClassNames a list of element names which should be filtered
     */
    public static void processStackTrace(Consumer<String> consumer, List<String> filteredClassNames) {
        consumer.accept(getStackTrace(getStackTraceElements(3, -1), filteredClassNames));
    }

    
    /**
     * Process the current stack trace
     * 
     * @param consumer the consumer
     * @param traceSize the size of the stack trace (default -1)
     */
    public static void processStackTrace(Consumer<String> consumer, int traceSize) {
        consumer.accept(getStackTrace(getStackTraceElements(3, traceSize)));
    }

    
    /**
     * Create the current stack trace
     * 
     * @param consumer the consumer
     * @param filteredClassNames a list of element names which should be filtered
     * @param header the header
     */
    public static void processStackTrace(Consumer<String> consumer, List<String> filteredClassNames, String header) {
        consumer.accept(getStackTrace(getStackTraceElements(3, -1), filteredClassNames, header));
    }

    
    /**
     * Create the current stack trace
     * 
     * @param consumer the consumer
     * @param filteredClassNames a list of element names which should be filtered
     * @param header the header
     * @param traceSize the size of the stack trace (default -1)
     */
    public static void processStackTrace(Consumer<String> consumer, List<String> filteredClassNames, String header, int traceSize) {
        consumer.accept(getStackTrace(getCallerStackTraceElements(traceSize), filteredClassNames, header));
    }

    
    /**
     * Create the current stack trace
     * 
     * @param consumer the consumer
     * @param filteredClassNames a list of element names which should be filtered
     * @param header the header
     * @param newline the newline
     * @param traceSize the size of the stack trace (default -1)
     */
    public static void processStackTrace(Consumer<String> consumer, List<String> filteredClassNames, String header, String newline, int traceSize) {
        consumer.accept(getStackTrace(getCallerStackTraceElements(traceSize), filteredClassNames, header, newline));
    }

    
    /**
     * Get the stack trace elements of the caller: the frames of this class (and of the thread) are removed before
     * the trace size is applied.
     *
     * @param traceSize the size of the stack trace (default -1)
     * @return the stack trace elements of the caller
     */
    private static StackTraceElement[] getCallerStackTraceElements(int traceSize) {
        StackTraceElement[] elements = getStackTraceElements(1, -1);
        List<StackTraceElement> result = new ArrayList<StackTraceElement>();
        for (int i = 0; i < elements.length && (traceSize <= 0 || result.size() < traceSize); i++) {
            StackTraceElement element = elements[i];
            if (element != null && !isOwnFrame(element)) {
                result.add(element);
            }
        }

        return result.toArray(new StackTraceElement[0]);
    }


    /**
     * Check if the stack trace element is a frame of this class (or one of its inner classes). A prefix of the class name
     * like the module or the class loader name (e.g. <code>module@version/</code> or <code>app//</code>) is ignored.
     *
     * @param element the stack trace element
     * @return true if it is a frame of this class
     */
    static boolean isOwnFrame(StackTraceElement element) {
        String className = element.getClassName();
        if (className == null) {
            return false;
        }

        // remove the module or class loader prefix, a class name never contains a slash
        int idx = className.lastIndexOf('/');
        if (idx >= 0) {
            className = className.substring(idx + 1);
        }

        return className.equals(where) || className.startsWith(where + "$");
    }


    /**
     * Filter the given trace elements as stack trace
     * 
     * @param elements the elements
     * @return the prepared stack trace
     */
    public static java.lang.StackTraceElement[] filterStackTrace(java.lang.StackTraceElement[] elements) {
        return filterStackTrace(elements, DEFAULT_EXCLUDES);
    }
    
    
    /**
     * Filter the given trace elements as stack trace
     * 
     * @param elements the elements
     * @param filteredClassNames a list of element names which should be filtered
     * @return the prepared stack trace
     */
    public static java.lang.StackTraceElement[] filterStackTrace(java.lang.StackTraceElement[] elements, List<String> filteredClassNames) {
        if (elements == null || elements.length == 0) {
            return elements;
        }

        List<java.lang.StackTraceElement> list = new ArrayList<java.lang.StackTraceElement>();
        for (java.lang.StackTraceElement element : elements) {
            if (!filterStackTraceElement(filteredClassNames, element)) {
                list.add(element);
            }
        }

        return list.toArray(new java.lang.StackTraceElement[list.size()]);
    }
    
    
    /**
     * Parse the stack trace of the given exception into stack trace elements
     * 
     * @param t the exception
     * @param startTrace the start of the trace (default 2)
     * @param traceSize the stack trace size (default -1)
     * @return the parsed stack trace elements or null if the exception is null.
     *         Each element represents a call on the stack and is structured like below:<BR>
     *         <UL>
     *             <LI>{@link StackTraceElement#getFileName()} : name of text file from which call was made
     *             <LI>{@link StackTraceElement#getLineNumber()} : line number of text file from which call was made (-1 if unknown)
     *             <LI>{@link StackTraceElement#getMethodName()} : method name of calling class
     *             <LI>{@link StackTraceElement#getClassName()} : full qualified name of calling class (package and class name)
     *             <LI>{@link StackTraceElement#getHeader()} : the line itself in case it is not a call, e.g. "Caused by ..." or "... 5 more"
     *         </UL>
     * @exception IOException in case of error
     */
    public static StackTraceElement[] parseStackTraceElements(Throwable t, int startTrace, int traceSize) throws IOException {
        if (t == null) {
            return null;
        }
        
        StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter)); // CHECKSTYLE IGNORE THIS LINE
        return parseStackTraceElements(stringWriter.toString(), startTrace, traceSize);
    }    

    
    /**
     * Parse the given stack trace text into stack trace elements
     * 
     * @param callStack the stack trace to parse
     * @param startIndex the start of the trace (default 2)
     * @param traceSize the maximum number of returned elements (default -1 for all)
     * @return the parsed stack trace elements or null if the call stack is null. Lines which can not be parsed are skipped.
     *         Each element represents a call on the stack and is structured like below:<BR>
     *         <UL>
     *             <LI>{@link StackTraceElement#getFileName()} : name of text file from which call was made
     *             <LI>{@link StackTraceElement#getLineNumber()} : line number of text file from which call was made (-1 if unknown)
     *             <LI>{@link StackTraceElement#getMethodName()} : method name of calling class
     *             <LI>{@link StackTraceElement#getClassName()} : full qualified name of calling class (package and class name)
     *             <LI>{@link StackTraceElement#getHeader()} : the line itself in case it is not a call, e.g. "Caused by ..." or "... 5 more"
     *         </UL>
     */
    public static StackTraceElement[] parseStackTraceElements(String callStack, int startIndex, int traceSize) {
        if (callStack == null) {
            return null;
        }

        List<String> stackLines = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new StringReader(callStack))) {
            String line = reader.readLine();
            while (line != null) {
                stackLines.add(line);
                line = reader.readLine();
            }
        } catch (IOException exc) {
            // quit
        }

        // first line is always java.lang.Throwable so we will not include that
        // the second line will always be getTraceElements so we will not include
        // that either
        int startTrace = 2;
        if ((startIndex >= 0) && (startIndex < stackLines.size())) {
            startTrace = startIndex;
        }

        if (startTrace > stackLines.size()) {
            startTrace = 0;
        }

        // parse each line of the stack trace, lines which can not be parsed are skipped
        // and at most traceSize elements are returned (all if traceSize is not positive)
        List<StackTraceElement> result = new ArrayList<StackTraceElement>();
        for (int i = startTrace; i < stackLines.size() && (traceSize <= 0 || result.size() < traceSize); i++) {
            StackTraceElement e = parseStackTraceElement(stackLines.get(i));
            if (e != null) {
                result.add(e);
            }
        }

        return result.toArray(new StackTraceElement[result.size()]);
    }

    
    /**
     * Break a line from the stack trace into the format:
     * member 0 : name of text file from which call was made
     * member 1 : line number of text file from which call was made
     * member 2 : method name of calling class
     * member 3 : name of calling class
     * member 4 : full package name of calling class
     * 
     * @param line a line from the stack trace
     * @return a stack trace element
     */
    public static StackTraceElement parseStackTraceElement(String line) {
        String packageName = null;
        String declaringClass = null;
        String header = null;
        String fileName = null;
        String methodName = null;
        int lineNumber = -1;

        if (line.startsWith("Caused by")) {
            header = line;
        } else if (line.indexOf("...") > 0 && line.indexOf(" more") > 0) {
            header = line;
        } else if (!isStackTraceFrameLine(line)) {
            header = line;
        } else {
            String[] stackTraceLine = parseStackTraceLine(line);

            if (stackTraceLine == null) {
                return null;
            }
            
            packageName = stackTraceLine[4];
            if ((packageName != null) && (packageName.length() > 0)) {
                declaringClass = packageName + "." + stackTraceLine[3];
            } else {
                declaringClass = stackTraceLine[3];
            }
            
            fileName = stackTraceLine[0];
            methodName = stackTraceLine[2];
            lineNumber = -1;

            try {
                lineNumber = Integer.parseInt(stackTraceLine[1]);
            } catch (NumberFormatException e) {
                // NOP
            }
        }
        
        StackTraceElement element = new StackTraceElement(header, declaringClass, fileName, methodName, lineNumber);
        return element;
    }
    
    
    /**
     * Break a line from the stack trace into the format:
     * member 0 : name of text file from which call was made
     * member 1 : line number of text file from which call was made
     * member 2 : method name of calling class
     * member 3 : name of calling class
     * member 4 : full package name of calling class
     * 
     * @param line a line from the stack trace
     * @return a formated stack trace array or null if the line is not a stack trace frame (its trimmed form must start with "at "):
     *         <UL>
     *             <LI>member 0 : name of text file from which call was made
     *             <LI>member 1 : line number of text file from which call was made
     *             <LI>member 2 : method name of calling class
     *             <LI>member 3 : name of calling class
     *             <LI>member 4 : full package name of calling class
     *         </UL>
     */
    public static String[] parseStackTraceLine(String line) {
        String[] returnString = new String[5];

        if (line == null || line.trim().length() == 0) {
            return returnString;
        }

        if (!isStackTraceFrameLine(line)) {
            return null;
        }

        // get the package.class.method string
        int start = line.indexOf("at ") + 3;
        int end = line.indexOf("(", start);

        if (start < 3 || end < 0) {
            return null;
        }

        // get the file name and the line number, only the text inside the parentheses is used
        // to ignore a suffix like " ~[?:?]" which is added by some logging frameworks
        int close = line.indexOf(")", end + 1);
        String location;
        if (close >= 0) {
            location = line.substring(end + 1, close);
        } else {
            location = line.substring(end + 1);
        }

        int colon = location.lastIndexOf(':');
        if (colon < 0) {
            returnString[0] = location;
        } else {
            if (colon > 0) {
                returnString[0] = location.substring(0, colon);
            }
            returnString[1] = location.substring(colon + 1);
        }

        // use tokenizer to count the periods to find the package name size
        StringTokenizer st = new StringTokenizer(line.substring(start, end), ".");

        // number of words in package is 2 less than the number of tokens
        int packageLength = st.countTokens() - 2;

        // get the package name and store it in the return string
        // there may be no package name in which case package name will be a empty string
        returnString[4] = "";

        if (packageLength > 0) {
            StringBuilder packageBuilder = new StringBuilder();
            for (int i = 0; i < packageLength; i++) {
                if (packageBuilder.length() > 0) {
                    packageBuilder.append('.');
                }
                packageBuilder.append(st.nextToken());
            }
            returnString[4] = packageBuilder.toString();
        }

        // get the class name
        if (st.hasMoreTokens()) {
            returnString[3] = st.nextToken();
        }

        // get the method name
        if (st.hasMoreTokens()) {
            returnString[2] = st.nextToken();
        }
        
        return returnString;
    }


    /**
     * Checks if a line is a stack trace frame line, i.e. its trimmed form starts with "at ".
     *
     * @param line the line
     * @return true if it is a stack trace frame line
     */
    private static boolean isStackTraceFrameLine(String line) {
        return line != null && line.trim().startsWith("at ");
    }

    
    /**
     * Convert java stack trace elements to toolarium stack trace elements
     *
     * @param javaElements the java stack trace elements
     * @param startIndex the start index
     * @param traceSize the trace size (default -1 for all)
     * @return the converted stack trace elements
     */
    private static StackTraceElement[] convertStackTraceElements(java.lang.StackTraceElement[] javaElements, int startIndex, int traceSize) {
        int start = Math.max(0, startIndex);
        if (start >= javaElements.length) {
            return new StackTraceElement[0];
        }

        int end = javaElements.length;
        if (traceSize > 0 && (start + traceSize) < end) {
            end = start + traceSize;
        }

        StackTraceElement[] result = new StackTraceElement[end - start];
        for (int i = start; i < end; i++) {
            java.lang.StackTraceElement je = javaElements[i];
            String declaringClass = je.getClassName();
            String moduleName = je.getModuleName();
            if (moduleName != null && !moduleName.isEmpty()) {
                declaringClass = moduleName + "/" + declaringClass;
            }
            result[i - start] = new StackTraceElement(null, declaringClass, je.getFileName(), je.getMethodName(), je.getLineNumber());
        }
        return result;
    }


    /**
     * Check if a given stack trace element should be filtered
     *
     * @param filteredElementNames the filer
     * @param e the element
     * @return true if it should be filtered
     */
    private static boolean filterStackTraceElement(List<String> filteredElementNames, StackTraceElement e) {
        if (filteredElementNames == null) {
            return false;
        }
        
        for (String s : filteredElementNames) {
            if (matchesFilter(s, e.getClassName(), null)) {
                return true;
            }
        }

        return false;
    }

    
    /**
     * Check if a given stack trace element should be filtered
     * 
     * @param filteredElementNames the filer
     * @param e the element
     * @return true if it should be filtered
     */
    private static boolean filterStackTraceElement(List<String> filteredElementNames, java.lang.StackTraceElement e) {
        if (filteredElementNames == null) {
            return false;
        }
        
        for (String s : filteredElementNames) {
            if (matchesFilter(s, e.getClassName(), e.getModuleName())) {
                return true;
            }
        }

        return false;
    }


    /**
     * Check if a class name matches a filter. A filter with a module prefix (e.g. <code>java.base/</code>) also matches
     * a class name without module prefix in case the module is known or the package of the class belongs to that
     * module of the boot layer.
     *
     * @param filter the filter
     * @param className the class name, optional with a module prefix
     * @param moduleName the module name or null if it is unknown
     * @return true if the class name matches the filter
     */
    static boolean matchesFilter(String filter, String className, String moduleName) {
        if (filter == null || className == null) {
            return false;
        }

        if (className.startsWith(filter)) {
            return true;
        }

        int filterIdx = filter.indexOf('/');
        if (filterIdx <= 0 || className.indexOf('/') >= 0) {
            return false;
        }

        // the filter has a module prefix but the class name has none
        String filterModuleName = filter.substring(0, filterIdx);
        if (!className.startsWith(filter.substring(filterIdx + 1))) {
            return false;
        }

        if (moduleName != null && !moduleName.isEmpty()) {
            return filterModuleName.equals(moduleName);
        }

        int packageIdx = className.lastIndexOf('.');
        if (packageIdx <= 0) {
            return false;
        }

        String packageName = className.substring(0, packageIdx);
        return ModuleLayer.boot().findModule(filterModuleName).map(m -> m.getPackages().contains(packageName)).orElse(Boolean.FALSE).booleanValue();
    }
}
