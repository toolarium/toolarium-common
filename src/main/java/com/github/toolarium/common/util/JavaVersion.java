/*
 * JavaVersionUtil.java
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
    JAVA_1_1(1, 1, 45, 0),
    JAVA_1_2(1, 2, 46, 0),
    JAVA_1_3(1, 3, 47, 0),
    JAVA_1_4(1, 4, 48, 0),
    JAVA_1_5(1, 5, 49, 0),
    JAVA_1_6(1, 6, 50, 0),
    JAVA_1_7(1, 7, 51, 0),
    JAVA_1_8(1, 8, 52, 0),
    JAVA_9(9, 0, 53, 0),
    JAVA_10(10, 0, 54, 0),
    JAVA_11(11, 0, 55, 0),
    JAVA_12(12, 0, 56, 0),
    JAVA_13(13, 0, 57, 0),
    JAVA_14(14, 0, 58, 0),
    JAVA_15(15, 0, 59, 0),
    JAVA_16(16, 0, 60, 0),
    JAVA_17(17, 0, 61, 0),
    JAVA_18(18, 0, 62, 0),
    JAVA_19(19, 0, 63, 0),
    JAVA_20(20, 0, 64, 0),
    JAVA_21(21, 0, 65, 0),
    JAVA_22(22, 0, 66, 0),
    JAVA_23(23, 0, 67, 0),
    JAVA_24(24, 0, 68, 0),
    JAVA_25(25, 0, 69, 0),
    JAVA_26(26, 0, 70, 0);

    private Version javaVersion;
    private Version classVersion;
    
    /**
     * Constructor for JavaVersionInformation
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
        return "" + javaVersion.getMajorNumber() + "." + javaVersion.getMajorNumber();
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

        return resolveJavaVersion(new FileInputStream(classFilename));
    }

    
    /**
     * Read the java version information
     * 
     * @param classStream the class file stream
     * @return the java version information
     * @throws IOException in case of error
     */
    public static JavaVersion resolveJavaVersion(InputStream classStream) throws IOException {
        if (classStream == null) {
            return null;
        }

        DataInputStream in = new DataInputStream(classStream);
        int magic = in.readInt();
        if (magic != 0xcafebabe) {
            return null;
        }

        int minor = in.readUnsignedShort();
        int major = in.readUnsignedShort();
        in.close();

        return resolveJavaVersion(minor, major);
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
