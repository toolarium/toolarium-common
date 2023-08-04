/*
 * StackTraceElement.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.stacktrace;

import java.io.Serializable;
import java.util.Objects;

/**
 * 
 * @author patrick
 */
/**
 * An element in a stack trace. Each element represents a single stack frame.
 * All stack frames except for the one at the top of the stack represent
 * a method invocation.  The frame at the top of the stack represents the 
 * the execution point at which the stack trace was generated.  Typically,
 * this is the point at which the throwable corresponding to the stack trace
 * was created.
 *
 * @author Meier Patrick
 */
public final class StackTraceElement implements Serializable {
    /** The serial version */
    private static final long serialVersionUID = 3257846597475053621L;
    private String header;
    private String declaringClass;
    private String methodName;
    private String fileName;
    private int lineNumber;

    
    /**
     * Default constructor for StackTraceElement
     * 
     * @param e the java stack trace element
     */     
    public StackTraceElement(java.lang.StackTraceElement e) {
        this.header = null;
        this.declaringClass = e.getClassName();
        this.fileName = e.getFileName();
        this.methodName = e.getMethodName();
        this.lineNumber = e.getLineNumber();
    }    

    
    /**
     * Default constructor for StackTraceElement
     * 
     * @param header the header
     * @param declaringClass the declaring class
     * @param fileName the file name
     * @param methodName the method name
     * @param lineNumber the line number
     */     
    public StackTraceElement(String header, String declaringClass, String fileName, String methodName, int lineNumber) {
        this.header = header;
        this.declaringClass = declaringClass;
        this.fileName = fileName;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
    }

    
    /**
     * Returns the header 
     *
     * @return the header
     */
    public String getHeader() {
        return header;
    }


    /**
     * Returns the name of the source file containing the execution point
     * represented by this stack trace element. 
     *
     * @return the name of the file containing the execution point
     *         represented by this stack trace element, or <tt>null</tt> if
     *         this information is unavailable.
     */
    public String getFileName() {
        return fileName;
    }


    /**
     * Returns the line number of the source line containing the execution
     * point represented by this stack trace element.  Generally, this is
     * derived from the <tt>LineNumberTable</tt> attribute of the relevant
     * <tt>class</tt> file (as per <i>The Java Virtual Machine
     * Specification</i>, Section 4.7.8).
     *
     * @return the line number of the source line containing the execution
     *         point represented by this stack trace element, or a negative
     *         number if this information is unavailable.
     */
    public int getLineNumber() {
        return lineNumber;
    }


    /**
     * Returns the fully qualified name of the class containing the
     * execution point represented by this stack trace element.
     *
     * @return the fully qualified name of the <tt>Class</tt> containing
     *         the execution point represented by this stack trace element.
     */
    public String getClassName() {
        return declaringClass;
    }


    /**
     * Returns the name of the method containing the execution point
     * represented by this stack trace element.  If the execution point is
     * contained in an instance or class initializer, this method will return
     * the appropriate <i>special method name</i>, <tt>&lt;init&gt;</tt> or
     * <tt>&lt;clinit&gt;</tt>, as per Section 3.9 of <i>The Java Virtual
     * Machine Specification</i>.
     *
     * @return the name of the method containing the execution point
     *         represented by this stack trace element.
     */
    public String getMethodName() {
        return methodName;
    }


    /**
     * Returns true if the method containing the execution point
     * represented by this stack trace element is a native method.
     *
     * @return <tt>true</tt> if the method containing the execution point
     *         represented by this stack trace element is a native method.
     */
    public boolean isNativeMethod() {
        return lineNumber == -2;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(declaringClass, fileName, header, lineNumber, methodName);
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StackTraceElement other = (StackTraceElement) obj;
        return Objects.equals(declaringClass, other.declaringClass) && Objects.equals(fileName, other.fileName)
                && Objects.equals(header, other.header) && lineNumber == other.lineNumber
                && Objects.equals(methodName, other.methodName);
    }

    
    /**
     * Returns a string representation of this stack trace element.  The
     * format of this string depends on the implementation, but the following
     * examples may be regarded as typical:
     * <ul>
     * <li>
     *   <tt>"MyClass.mash(MyClass.java:9)"</tt> - Here, <tt>"MyClass"</tt>
     *   is the <i>fully-qualified name</i> of the class containing the
     *   execution point represented by this stack trace element,
     *   <tt>"mash"</tt> is the name of the method containing the execution
     *   point, <tt>"MyClass.java"</tt> is the source file containing the
     *   execution point, and <tt>"9"</tt> is the line number of the source
     *   line containing the execution point.
     * <li>
     *   <tt>"MyClass.mash(MyClass.java)"</tt> - As above, but the line
     *   number is unavailable.
     * <li>
     *   <tt>"MyClass.mash(Unknown Source)"</tt> - As above, but neither
     *   the file name nor the line  number are available.
     * <li>
     *   <tt>"MyClass.mash(Native Method)"</tt> - As above, but neither
     *   the file name nor the line  number are available, and the method
     *   containing the execution point is known to be a native method.
     * </ul>
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (header != null) {
            builder.append(header);
        }

        if (declaringClass == null) {
            if (header != null) {
                return header;
            }
        } else {
            builder.append(declaringClass);
        }
        
        if (methodName != null) {
            builder.append(".");
            builder.append(methodName);
        }

        if (isNativeMethod()) {
            builder.append("(Native Method)");
        }

        if (fileName != null && lineNumber >= 0) {
            builder.append("(");
            builder.append(fileName);
            builder.append(":");
            builder.append(lineNumber);
            builder.append(")");
        } else if (fileName != null) {
            builder.append("(");
            builder.append(fileName);
            builder.append(")");
        } else {
            builder.append("(Unknown Source)");
        }
        
        return builder.toString();
    }
}
