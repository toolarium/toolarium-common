/*
 * JavaVersion.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import com.github.toolarium.common.version.Version;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * The java version information.
 * See https://javaalmanac.io/bytecode/versions/
 * 
 * @author patrick
 */
public enum JavaVersion {
    /** Java 1.1 (class file version 45). */
    JAVA_1_1(1, 1, 45, 0),

    /** Java 1.2 (class file version 46). */
    JAVA_1_2(1, 2, 46, 0),

    /** Java 1.3 (class file version 47). */
    JAVA_1_3(1, 3, 47, 0),

    /** Java 1.4 (class file version 48). */
    JAVA_1_4(1, 4, 48, 0),

    /** Java 1.5 (class file version 49). */
    JAVA_1_5(1, 5, 49, 0),

    /** Java 1.6 (class file version 50). */
    JAVA_1_6(1, 6, 50, 0),

    /** Java 1.7 (class file version 51). */
    JAVA_1_7(1, 7, 51, 0),

    /** Java 1.8 (class file version 52). */
    JAVA_1_8(1, 8, 52, 0),

    /** Java 9 (class file version 53). */
    JAVA_9(9, 0, 53, 0),

    /** Java 10 (class file version 54). */
    JAVA_10(10, 0, 54, 0),

    /** Java 11 (class file version 55). */
    JAVA_11(11, 0, 55, 0),

    /** Java 12 (class file version 56). */
    JAVA_12(12, 0, 56, 0),

    /** Java 13 (class file version 57). */
    JAVA_13(13, 0, 57, 0),

    /** Java 14 (class file version 58). */
    JAVA_14(14, 0, 58, 0),

    /** Java 15 (class file version 59). */
    JAVA_15(15, 0, 59, 0),

    /** Java 16 (class file version 60). */
    JAVA_16(16, 0, 60, 0),

    /** Java 17 (class file version 61). */
    JAVA_17(17, 0, 61, 0),

    /** Java 18 (class file version 62). */
    JAVA_18(18, 0, 62, 0),

    /** Java 19 (class file version 63). */
    JAVA_19(19, 0, 63, 0),

    /** Java 20 (class file version 64). */
    JAVA_20(20, 0, 64, 0),

    /** Java 21 (class file version 65). */
    JAVA_21(21, 0, 65, 0),

    /** Java 22 (class file version 66). */
    JAVA_22(22, 0, 66, 0),

    /** Java 23 (class file version 67). */
    JAVA_23(23, 0, 67, 0),

    /** Java 24 (class file version 68). */
    JAVA_24(24, 0, 68, 0),

    /** Java 25 (class file version 69). */
    JAVA_25(25, 0, 69, 0),

    /** Java 26 (class file version 70). */
    JAVA_26(26, 0, 70, 0),

    /** Java 27 (class file version 71). */
    JAVA_27(27, 0, 71, 0),

    /** Java 28 (class file version 72), planned general availability March 2027. */
    JAVA_28(28, 0, 72, 0);

    private Version javaVersion;
    private Version classVersion;
    
    /**
     * Constructor for JavaVersion
     * 
     * @param major the java major version
     * @param minor the java minor version
     * @param classMajor the class major version
     * @param classMinor the class minor version
     */
    JavaVersion(int major, int minor, int classMajor, int classMinor) {
        javaVersion = new Version(major, minor, 0);
        classVersion = new Version(classMajor, classMinor, 0);
    }
    

    /**
     * Get the java version
     * 
     * @return the java version
     */
    public Version getJavaVersion() {
        return javaVersion;
    }


    /**
     * Get the class version
     * 
     * @return the class version
     */
    public Version getClassVersion() {
        return classVersion;
    }

    
    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return "" + javaVersion.getMajorNumber() + "." + javaVersion.getMinorNumber();
    }

    
    /**
     * Get the Java version as string
     *
     * @return the java version as string
     */
    public static JavaVersion resolveJavaVersion() {
        return convertJavaVersion(System.getProperties().getProperty("java.version"));
    }

    
    /**
     * Read the java version information
     * 
     * @param classFilename the class filename
     * @return the java version information
     * @throws IOException in case of error
     */
    public static JavaVersion resolveJavaVersion(String classFilename) throws IOException {
        if (classFilename == null || classFilename.isEmpty()) {
            return null;
        }

        if (!FileUtil.getInstance().isReadable(classFilename)) {
            return null;
        }

        try (InputStream fis = new FileInputStream(classFilename)) {
            return resolveJavaVersion(fis);
        }
    }

    
    /**
     * Read the java version information from a class file stream. The stream is closed after reading.
     * 
     * @param classStream the class file stream
     * @return the java version information
     * @throws IOException in case of error
     */
    public static JavaVersion resolveJavaVersion(InputStream classStream) throws IOException {
        if (classStream == null) {
            return null;
        }

        try (DataInputStream in = new DataInputStream(classStream)) {
            int magic = in.readInt();
            if (magic != 0xcafebabe) {
                return null;
            }

            int minor = in.readUnsignedShort();
            int major = in.readUnsignedShort();
            return resolveJavaVersionByClassVersion(major, minor);
        }
    }


    /**
     * Resolve the java version
     * 
     * @param majorInput the major version
     * @param minorInput the minor version
     * @return the java version information
     */
    protected static JavaVersion resolveJavaVersion(int majorInput, int minorInput) {
        int major = 0;
        int minor = 0;

        if (majorInput > 0) {
            major = majorInput;
        }
        
        if (minorInput > 0) {
            minor = minorInput;
        }
        
        for (JavaVersion v : JavaVersion.values()) {
            if (v.getJavaVersion().getMajorNumber() == major && v.getJavaVersion().getMinorNumber() == minor) {
                return v;
            }
        }

        return null;
    }


    /**
     * Resolve the java version by the class file version. The class file major version identifies the java version;
     * the class file minor version is ignored (e.g. preview features use 0xFFFF, Java 1.1 class files use 3).
     *
     * @param classMajor the class file major version
     * @param classMinor the class file minor version (ignored for the resolution)
     * @return the java version information or null if the class file version is unknown
     */
    protected static JavaVersion resolveJavaVersionByClassVersion(int classMajor, int classMinor) {
        for (JavaVersion v : JavaVersion.values()) {
            if (v.getClassVersion().getMajorNumber() == classMajor) {
                return v;
            }
        }

        return null;
    }

    
    /**
     * Convert java version
     * 
     * @param version the version number
     * @return the java version information
     */
    public static JavaVersion convertJavaVersion(String version) {
        if (version == null || version.isEmpty()) {
            return null;
        }

        Version v = new Version(version);
        return resolveJavaVersion(v.getMajorNumber(), v.getMinorNumber());
    }    
}
