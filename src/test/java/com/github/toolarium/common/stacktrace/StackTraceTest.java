/*
 * StackTraceTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.stacktrace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                      + "\tat com.github.toolarium.common.stacktrace.StackTraceTest.test3(StackTraceTest.java:76)" + newline 
                      + "Caused by: java.lang.IllegalArgumentException: T2" + newline 
                      + "\tat com.github.toolarium.common.stacktrace.StackTraceTest.test3(StackTraceTest.java:77)" + newline 
                      + "\t... 25 more" + newline 
                      + "Caused by: java.lang.IllegalArgumentException: T3" + newline 
                      + "\tat com.github.toolarium.common.stacktrace.StackTraceTest.test3(StackTraceTest.java:78)" + newline 
                      + "\t... 25 more" + newline 
                      + "Caused by: java.lang.IllegalArgumentException: T4" + newline 
                      + "\tat com.github.toolarium.common.stacktrace.StackTraceTest.test3(StackTraceTest.java:79)" + newline 
                      + "\t... 25 more";
        ref = ref.replaceAll("... [0-9]?[0-9]? more", "... xx more");
        
        excludes = new ArrayList<String>(StackTrace.DEFAULT_EXCLUDES);
        excludes.add("java.util.concurrent."); // java 1.8
        excludes.add("java.lang."); // java 1.8
        excludes.add("jdk.proxy"); // java 21
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
     * Test that lines which contain "at " but are not stack trace frames are not parsed as frames.
     *
     * @throws Exception in case of error
     */
    @Test
    public void testMessageContainingAt() throws Exception {
        String header = "java.lang.NumberFormatException: Invalid float (x)";
        assertNull(StackTrace.parseStackTraceLine(header));
        assertNull(StackTrace.parseStackTraceLine("Caused by: java.lang.IllegalStateException: what (at all)"));

        StackTraceElement element = StackTrace.parseStackTraceElement(header);
        assertNotNull(element);
        assertNull(element.getMethodName());

        String[] frame = StackTrace.parseStackTraceLine("\tat com.github.toolarium.Test.method(Test.java:12)");
        assertEquals("Test.java", frame[0]);
        assertEquals("12", frame[1]);
        assertEquals("method", frame[2]);
        assertEquals("Test", frame[3]);
        assertEquals("com.github.toolarium", frame[4]);

        frame = StackTrace.parseStackTraceLine("at Test(Test.java:12)");
        assertEquals("Test", frame[3]);
        assertNull(frame[2]);

        NumberFormatException ex = new NumberFormatException("Invalid float (x)");
        StackTraceElement[] elements = StackTrace.parseStackTraceElements(ex, 0, -1);
        assertNotNull(elements);
        assertEquals(header, elements[0].toString().trim());
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


    /**
     * The trace size counts only the frames of the caller
     */
    @Test
    public void testTraceSize() {
        String trace = StackTrace.getStackTrace(2);
        String[] lines = trace.split(System.getProperty("line.separator"));
        assertEquals(2, lines.length, trace);
        assertTrue(lines[0].contains(StackTraceTest.class.getName()), trace);

        final String newline = "\n";
        List<String> result = new ArrayList<String>();
        StackTrace.processStackTrace(result::add, null, null, newline, 2);
        assertEquals(2, result.get(0).split(newline).length, result.get(0));
        assertTrue(result.get(0).startsWith(StackTraceTest.class.getName()), result.get(0));
    }


    /**
     * No empty line in case the first element is filtered
     */
    @Test
    public void testNoEmptyLineIfFirstElementIsFiltered() {
        StackTraceElement[] elements = new StackTraceElement[] {
            new StackTraceElement(null, "org.junit.Foo", "Foo.java", "run", 1),
            new StackTraceElement(null, "com.x.Bar", "Bar.java", "call", 2)};
        final String header = "Header";
        final String newline = "\n";
        assertEquals(header + newline + "\tat com.x.Bar.call(Bar.java:2)", StackTrace.getStackTrace(elements, StackTrace.DEFAULT_EXCLUDES, header, newline));
    }

    /**
     * A native frame prints only (Native Method)
     */
    @Test
    public void testNativeMethodToString() {
        assertEquals("java.lang.Thread.sleep(Native Method)", new StackTraceElement(null, "java.lang.Thread", null, "sleep", -2).toString());
    }


    /**
     * The own frames are also detected on the module path (class name with a module or class loader prefix).
     * The imports are not changed to keep the line numbers of the test3 assertions.
     */
    @Test
    public void testIsOwnFrameWithModulePrefix() {
        final String className = StackTrace.class.getName();
        final String fileName = "StackTrace.java";
        final String methodName = "getStackTrace";
        assertTrue(StackTrace.isOwnFrame(new StackTraceElement(null, className, fileName, methodName, 1)));
        assertTrue(StackTrace.isOwnFrame(new StackTraceElement(null, "toolarium.common/" + className, fileName, methodName, 1)));
        assertTrue(StackTrace.isOwnFrame(new StackTraceElement(null, "toolarium.common@1.2.1/" + className, fileName, methodName, 1)));
        assertTrue(StackTrace.isOwnFrame(new StackTraceElement(null, "app//" + className + "$Inner", fileName, methodName, 1)));
        org.junit.jupiter.api.Assertions.assertFalse(StackTrace.isOwnFrame(new StackTraceElement(null, "toolarium.common/" + className + "Test", fileName, methodName, 1)));
        org.junit.jupiter.api.Assertions.assertFalse(StackTrace.isOwnFrame(new StackTraceElement(null, "com.x.Bar", "Bar.java", "call", 2)));
        org.junit.jupiter.api.Assertions.assertFalse(StackTrace.isOwnFrame(new StackTraceElement("Caused by: x", null, null, null, -1)));
    }


    /**
     * The parsed stack trace elements respect the trace size and contain no null elements.
     */
    @Test
    public void testParseStackTraceElementsSize() {
        final String text = "java.lang.IllegalStateException: x\n"
                + "\tat com.x.A.a(A.java:1)\n"
                + "\tat broken\n"
                + "\tat com.x.B.b(B.java:2)\n"
                + "\tat com.x.C.c(C.java:3)\n";
        assertNull(StackTrace.parseStackTraceElements((String) null, 0, -1));

        StackTraceElement[] elements = StackTrace.parseStackTraceElements(text, 0, 2);
        assertEquals(2, elements.length);
        assertEquals("com.x.A", elements[1].getClassName());

        elements = StackTrace.parseStackTraceElements(text, 1, Integer.MAX_VALUE);
        assertEquals(3, elements.length);
        for (StackTraceElement e : elements) {
            assertNotNull(e);
        }
        assertEquals("com.x.C", elements[2].getClassName());

        assertEquals(4, StackTrace.parseStackTraceElements(text, 0, -1).length);
    }


    /**
     * A suffix after the closing parenthesis (e.g. added by a logging framework) does not corrupt the file name.
     */
    @Test
    public void testParseStackTraceLineWithSuffix() {
        String[] frame = StackTrace.parseStackTraceLine("\tat com.x.Foo.bar(Foo.java:12) ~[?:?]");
        assertEquals("Foo.java", frame[0]);
        assertEquals("12", frame[1]);
        assertEquals("bar", frame[2]);

        frame = StackTrace.parseStackTraceLine("\tat com.x.Foo.bar(Native Method) ~[?:?]");
        assertEquals("Native Method", frame[0]);
        assertNull(frame[1]);
    }


    /**
     * The module exclude also filters frames of the module without module prefix.
     */
    @Test
    public void testFilterFramesWithoutModulePrefix() {
        final String barClassName = "com.x.Bar";
        java.lang.StackTraceElement[] javaElements = new java.lang.StackTraceElement[] {
            new java.lang.StackTraceElement("java.lang.Thread", "run", "Thread.java", 1),
            new java.lang.StackTraceElement(barClassName, "call", "Bar.java", 2)};
        java.lang.StackTraceElement[] filtered = StackTrace.filterStackTrace(javaElements);
        assertEquals(1, filtered.length);
        assertEquals(barClassName, filtered[0].getClassName());

        StackTraceElement[] elements = new StackTraceElement[] {
            new StackTraceElement(null, "java.lang.Thread", "Thread.java", "run", 1),
            new StackTraceElement(null, barClassName, "Bar.java", "call", 2)};
        final String header = "Header";
        final String newline = "\n";
        assertEquals(header + newline + "\tat com.x.Bar.call(Bar.java:2)", StackTrace.getStackTrace(elements, StackTrace.DEFAULT_EXCLUDES, header, newline));
        org.junit.jupiter.api.Assertions.assertFalse(StackTrace.matchesFilter("java.base/", barClassName, null));
        assertTrue(StackTrace.matchesFilter("java.base/", barClassName, "java.base"));
    }
}
