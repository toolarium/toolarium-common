/*
 * StackTrace.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.stacktrace;

import com.github.toolarium.common.util.StringUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.function.Consumer;


/**
 * The StackInfo class is a collection of static methods to allow users to quickly determine the call stack. 
 * This class makes it possible to determine what class called a method and what text files and line numbers 
 * resulted in stack calls. The StackInfo calls will not appear in return string. 
 * By calling the method getTrace a 2D-String-Array is returned.
 * 
 * @author patrick
 */
public final class StackTrace {
    
    /** Default Stacktrace exclues */
    public static final List<String> DEFAULT_EXCLUDES = new ArrayList<String>();
    private static String where;

    static {
        DEFAULT_EXCLUDES.add("java.base/");
        DEFAULT_EXCLUDES.add("jdk.internal.");
        DEFAULT_EXCLUDES.add("sun.");
        DEFAULT_EXCLUDES.add("com.sun.");
        DEFAULT_EXCLUDES.add("java.lang.reflect.");
        DEFAULT_EXCLUDES.add("org.testng.");
        DEFAULT_EXCLUDES.add("junit.");
        DEFAULT_EXCLUDES.add("org.junit.");
        DEFAULT_EXCLUDES.add("org.gradle.");
        DEFAULT_EXCLUDES.add("org.eclipse.jdt.internal.");
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
        Throwable stackThrower = new Throwable();
        StringWriter stringWriter = new StringWriter();
        stackThrower.printStackTrace(new PrintWriter(stringWriter));  // CHECKSTYLE IGNORE THIS LINE
        return parseStackTraceElements(stringWriter.toString(), 2, -1);
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
        Throwable stackThrower = new Throwable();
        StringWriter stringWriter = new StringWriter();
        stackThrower.printStackTrace(new PrintWriter(stringWriter));  // CHECKSTYLE IGNORE THIS LINE
        return parseStackTraceElements(stringWriter.toString(), startIndex, numberOfLastElements);
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
        StackTraceElement[] elements = getStackTraceElements(traceSize);
        StringBuilder buffer = new StringBuilder();

        boolean addNewline = false;

        for (int i = 0; i < elements.length; i++) {
            StackTraceElement element = elements[i];

            if ((element != null) && (element.getClassName() != null && !element.getClassName().startsWith(where))) {
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
        
        for (int i = 0; i < elements.length; i++) {
            StackTraceElement element = elements[i];
            if (element != null && !filterStackTraceElement(filteredClassNames, element)) {
                if (i > 0) {
                    buffer.append(newline);
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
        consumer.accept(getStackTrace(getStackTraceElements(3, traceSize), filteredClassNames, header));
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
        consumer.accept(getStackTrace(getStackTraceElements(3, traceSize), filteredClassNames, header, newline));
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
     * Parse the stack trace into the desired two dimensional string
     * 
     * @param t the exception
     * @param startTrace the start of the trace (default 2)
     * @param traceSize the stack trace size (default -1)
     * @return a formated two dimensional string:
     *         Each row representate a call on the stack and the columns
     *         are formated like below:<BR>
     *         <UL>
     *             <LI>member 0 : name of text file from which call was made
     *             <LI>member 1 : line number of text file from which call was made
     *             <LI>member 2 : method name of calling class
     *             <LI>member 3 : name of calling class
     *             <LI>member 4 : full package name of calling class
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
     * Parse the stack trace into the desired two dimensional string
     * 
     * @param callStack the stack trace to parse
     * @param startIndex the start of the trace (default 2)
     * @param traceSize the stack trace size (default -1)
     * @return a formated two dimensional string:
     *         Each row representate a call on the stack and the columns
     *         are formated like below:<BR>
     *         <UL>
     *             <LI>member 0 : name of text file from which call was made
     *             <LI>member 1 : line number of text file from which call was made
     *             <LI>member 2 : method name of calling class
     *             <LI>member 3 : name of calling class
     *             <LI>member 4 : full package name of calling class
     *         </UL>
     */
    public static StackTraceElement[] parseStackTraceElements(String callStack, int startIndex, int traceSize) {
        Vector<String> stackVector = new Vector<String>(); // vector which holds string lines
        try (BufferedReader reader = new BufferedReader(new StringReader(callStack))) {
            String line = reader.readLine();
            while (line != null) {
                stackVector.add(line);
                line = reader.readLine();
            }
        } catch (IOException exc) {
            // quit
        }
        
        // the size of the stack is now know so the returnString can be initialized
        // first line is always java.lang.Throwable so we will not include that
        // the second line will always be getTraceElements so we will not include
        // that either
        int startTrace = 2;
        if ((startIndex >= 0) && (startIndex < stackVector.size())) {
            startTrace = startIndex;
        }
        
        int size = stackVector.size();
        if ((traceSize > 0) && ((traceSize + startTrace + 1) <= stackVector.size())) {
            size = traceSize + startTrace + 1;
        }

        if (startTrace > size) {
            startTrace = 0;
        }
        
        StackTraceElement[] stackTraceElements = new StackTraceElement[ size - startTrace ];

        // parse each line of the stack trace and put it in the desired format &
        // order remember that we are skipping the first line
        int endTrace = size;
        int pos = startTrace;
        for (int i = startTrace; i < endTrace; i++) {
            StackTraceElement e = parseStackTraceElement(stackVector.elementAt(i));
            if (e != null) {
                stackTraceElements[pos - startTrace] = e;
                pos++;
            }
        }

        return stackTraceElements;
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
        } else if (line.indexOf("at ") < 0) {
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
     * @return a formated stack trace array:
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
        
        int start;
        int end;

        // get the file name
        start = line.indexOf("(") + 1;
        end = line.indexOf(":");

        if (start <= 1 || end < 0) {
            end = line.lastIndexOf(")");

            if (start < 0) {
                start = 0;
            }
            if (end < 0) {
                end = 0;
            }
            returnString[0] = line.substring(start, end);
        } else {
            if (start < 0) {
                start = 0;
            }
            if (end < 0) {
                end = 0;
            }
            if (start < end) {
                returnString[0] = line.substring(start, end);
            }
            
            // get the line number in the file
            start = line.indexOf(":") + 1;
            end = line.indexOf(")", start);

            if (start < 0 || end < 0) {
                returnString[1] = "";
            } else {
                returnString[1] = line.substring(start, end);
            }
        }

        // get the package.class.method string
        start = line.indexOf("at ") + 3;
        end = line.indexOf("(", start);

        if (start < 0 || end < 0) {
            return null;
        }

        // use tokenizer to count the periods to find the package name size
        StringTokenizer st = new StringTokenizer(line.substring(start, end), ".");

        // number of words in package is 2 less than the number of tokens
        int packageLength = st.countTokens() - 2;

        // get the package name and store it in the return string
        // there may be no package name in which case package name will be a empty string
        returnString[4] = "";

        if (packageLength != 0) {
            for (int i = 0; i < packageLength; i++) {
                returnString[4] += st.nextToken() + ".";
            }

            if (returnString[4].length() > 0) {
                // strip the last period off the package name
                returnString[4] = returnString[4].substring(0, returnString[4].length() - 1);
            } else {
                returnString[4] = StringUtil.getInstance().trimRight(returnString[4]);
            }
        }

        // get the class name
        returnString[3] = st.nextToken();

        // get the method name
        try {
            returnString[2] = st.nextToken();
        } catch (NoSuchElementException e) {
            // NOP
        }
        
        return returnString;
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
            if (e.getClassName() != null && e.getClassName().startsWith(s)) {
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
            if (e.getClassName() != null && e.getClassName().startsWith(s)) {
                return true;
            }
        }

        return false;
    }
}

