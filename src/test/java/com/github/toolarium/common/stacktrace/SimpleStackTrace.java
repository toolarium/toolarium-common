/*
 * SimpleStackTrace.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.stacktrace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a simple stack trace catcher
 *  
 * @author patrick
 */
public final class SimpleStackTrace {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleStackTrace.class);

    
    /**
     * Constructor
     */
    private SimpleStackTrace() {
        // NOP
    }


    /**
     * Parse the stack trace into the desired two dimensional string
     * 
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
    public static String[][] getTrace() {
        Throwable stackThrower = new Throwable();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        String[][] stackContents = null;

        // output the stack trace to the print writer
        stackThrower.printStackTrace(printWriter); // CHECKSTYLE IGNORE THIS LINE

        try {
            stackContents = parseTraceString(stringWriter.toString());
        } catch (IOException e) {
            LOG.error("Error in determining the call stack!", e);
        }

        return stackContents;
    }
    
    
    /**
     * Get the x-te call from the stack trace into the desired string array
     * 
     * @param nr the last numbers in stack trace
     * @return a formated string array:
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
    public static String[] getTrace(int nr) {
        String[][] fullStackContents = getTrace();
        int call = 0;

        /*
        for( int j=0; j<fullStackContents.length; j++ )
        {
            if( fullStackContents[j]!=null )
            {
                for( int i=0; i<fullStackContents[j].length; i++ )
                {
                    if( fullStackContents[j][i]!=null )
                        BootstrapLog.log( StackTrace.class, Level.DEBUG, "==>"+fullStackContents[j][i] );
                }
            }
        }
        */

        if (nr <= 0) {
            call = fullStackContents.length - 1 + nr;
        } else {
            call = nr;
        }
        
        if (call < 0 || call > fullStackContents.length - 1) {
            return null;
        }

        return fullStackContents[call];
    }

    
    /**
     * Get the last call from the stack trace into the desired string array
     * 
     * @return a formated string array:
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
    public static String[] getLastTrace() {
        String[][] fullStackContents = getTrace();
        int lastCall = fullStackContents.length - 1;

        if (lastCall < 0) {
            return null;
        }
        
        return fullStackContents[lastCall];
    }


    
    /**
     * Parse the stack trace into the desired two dimensional string
     * 
     * @param callStack the stack trace to parse
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
     * @exception IOException In case of a parse error
     */
    private static String[][] parseTraceString(String callStack) throws IOException {
        String line;
        String[][] returnString;
        Vector<String> stackVector = new Vector<String>(); // vector which holds string lines

        // speed up string parsing with a buffered reader
        BufferedReader br = new BufferedReader(new StringReader(callStack));

        // read in each string to a vector member
        while ((line = br.readLine()) != null) {
            stackVector.add(line);
        }

        // the size of the stack is now know so the returnString can be initialized
        // first line is always java.lang.Throwable so we will not include that
        // the second line will always be StackInfo.getTrace so we will not include
        // that either
        returnString = new String[stackVector.size() - 2][5];

        // parse each line of the stack trace and put it in the desired format &
        // order remember that we are skipping the first line
        for (int i = 2; i < stackVector.size(); i++) {
            // BootstrapLog.log( StackTrace.class, Level.DEBUG, "parseing stack trace / ["+i+"]:"+(String)stackVector.elementAt(i) );
            returnString[i - 2] = StackTrace.parseStackTraceLine(stackVector.elementAt(i));
        }

        return returnString;
    }
}
