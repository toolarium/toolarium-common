/*
 * StackTraceTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.stacktrace;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test the {@link StackTrace}.
 *  
 * @author patrick
 */
public class StackTraceTest {
    private static final Logger LOG = LoggerFactory.getLogger(StackTraceTest.class);
    
    
    /** 
     * Test
     * 
     * @throws Exception in case of error
     */
    @Test
    public void testStackTraceElements() throws Exception {
        checkResults(StackTrace.getStackTraceElements(), SimpleStackTrace.getTrace());
        checkResults(StackTrace.getStackTraceElements(-1), SimpleStackTrace.getTrace());
        checkResults(StackTrace.getStackTraceElements(0), SimpleStackTrace.getTrace());
        checkResults(StackTrace.getStackTraceElements(200), SimpleStackTrace.getTrace());
    }

    
    /**
     * Simple test 
     * 
     * @throws Exception in case of error
     */
    @Test
    public void test2() throws Exception {
        IllegalArgumentException t1 = new IllegalArgumentException("T1");
        IllegalArgumentException t2 = new IllegalArgumentException("T2");
        IllegalArgumentException t3 = new IllegalArgumentException("T3");
        IllegalArgumentException t4 = new IllegalArgumentException("T4");
        t1.initCause(t2);
        t2.initCause(t3);
        t3.initCause(t4);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        t1.printStackTrace(new PrintStream(stream)); // CHECKSTYLE IGNORE THIS LINE
        StackTraceElement[] elements = StackTrace.parseStackTraceElements(t1, 0, -1);

        assertEquals(stream.toString(), StackTrace.getStackTrace(elements, null, "") + System.getProperty("line.separator"));
    }

    
    /**
     * Simple test 
     * 
     * @throws Exception in case of error
     */
    @Test
    public void test3() throws Exception {
        IllegalArgumentException t1 = new IllegalArgumentException("T1");
        IllegalArgumentException t2 = new IllegalArgumentException("T2");
        IllegalArgumentException t3 = new IllegalArgumentException("T3");
        IllegalArgumentException t4 = new IllegalArgumentException("T4");
        t1.initCause(t2);
        t2.initCause(t3);
        t3.initCause(t4);

        List<String> excludes = new ArrayList<String>();
        excludes.add("worker.org.gradle.");

        t1.setStackTrace(StackTrace.filterStackTrace(t1.getStackTrace(), null));
        t2.setStackTrace(StackTrace.filterStackTrace(t2.getStackTrace(), null));
        t3.setStackTrace(StackTrace.filterStackTrace(t3.getStackTrace(), null));
        t4.setStackTrace(StackTrace.filterStackTrace(t4.getStackTrace(), null));
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        t1.printStackTrace(new PrintStream(stream)); // CHECKSTYLE IGNORE THIS LINE
        
        String newline = System.getProperty("line.separator");
        String ref = "java.lang.IllegalArgumentException: T1" + newline 
                      + "\tat com.github.toolarium.common.stacktrace.StackTraceTest.test3(StackTraceTest.java:73)" + newline 
                      + "Caused by: java.lang.IllegalArgumentException: T2" + newline 
                      + "\tat com.github.toolarium.common.stacktrace.StackTraceTest.test3(StackTraceTest.java:74)" + newline 
                      + "\t... 25 more" + newline 
                      + "Caused by: java.lang.IllegalArgumentException: T3" + newline 
                      + "\tat com.github.toolarium.common.stacktrace.StackTraceTest.test3(StackTraceTest.java:75)" + newline 
                      + "\t... 25 more" + newline 
                      + "Caused by: java.lang.IllegalArgumentException: T4" + newline 
                      + "\tat com.github.toolarium.common.stacktrace.StackTraceTest.test3(StackTraceTest.java:76)" + newline 
                      + "\t... 25 more";
        ref = ref.replaceAll("... [0-9]?[0-9]? more", "... xx more");
        
        excludes = new ArrayList<String>(StackTrace.DEFAULT_EXCLUDES);
        excludes.add("java.util.concurrent."); // java 1.8
        excludes.add("java.lang."); // java 1.8
        excludes.add("worker.org.gradle.");

        StackTraceElement[] elements = StackTrace.parseStackTraceElements(t1, 0, -1);
        String stackTrace = StackTrace.getStackTrace(elements, excludes, "");
        stackTrace = stackTrace.replaceAll("... [0-9]?[0-9]? more", "... xx more");
        LOG.debug("1[" + ref + "]");
        LOG.debug("2[" + stackTrace + "]");
        assertEquals(stackTrace, ref);
    }
    
    
    /** 
     * Test
     * 
     * @throws Exception in case of error
     */
    @Test
    public void testLogStackTrace() throws Exception {
        Consumer<String> consumer = LOG::info;
        //Consumer<String> consumer = this::getString;
        //Consumer<String> consumer = System.out::println;
        StackTrace.processStackTrace(consumer);
        StackTrace.processStackTrace(consumer, StackTrace.DEFAULT_EXCLUDES);
        StackTrace.processStackTrace(consumer, StackTrace.DEFAULT_EXCLUDES, "Test1");
        StackTrace.processStackTrace(consumer, null, "Test2", 10);
        StackTrace.processStackTrace(consumer, StackTrace.DEFAULT_EXCLUDES, "Test3", 10);
    }
    
    
    /**
     * Checks the two different stack traces
     * 
     * @param elements the elements
     * @param strElements the string elements
     */
    private void checkResults(StackTraceElement[] elements, String[][] strElements) {
        for (int i = 1; i < elements.length; i++) {
            String stackTrace = "";

            if (strElements[i][4] != null) {
                stackTrace = strElements[i][4].toString() + ".";
            }
            
            if (strElements[i][3] != null) {
                stackTrace += strElements[i][3].toString() + ".";
            }
            
            if (strElements[i][2] != null) {
                stackTrace += strElements[i][2].toString() + "(";
            }
            
            if (strElements[i][0] != null) {
                stackTrace += strElements[i][0].toString();
            }
            
            if (strElements[i][1] != null) {
                stackTrace += ":" + strElements[i][1].toString() + ")";
            } else {
                stackTrace += ")";
            }
            
            assertEquals(stackTrace, elements[i].toString());
        }
    }
}
