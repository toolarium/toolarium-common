/*
 * JavaVersionTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.github.toolarium.common.version.Version;
import java.io.IOException;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link JavaVersion}.
 * 
 * @author patrick
 */
public class JavaVersionTest {
    private static final String V_1_1 = "1.1";
    private static final String V_1_2 = "1.2";


    /**
     * Test convertJavaVersion
     * 
     * @throws ParseException in case of error
     */
    @Test
    public void testConvertJavaVersion() {
        assertEquals(JavaVersion.JAVA_1_1.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_1).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_2.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_1).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_3.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_1).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_4.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_1).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_5.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_1).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_6.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_1).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_7.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_1).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_8.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_1).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_9.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_1).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_10.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_1).getJavaVersion());

        assertEquals(JavaVersion.JAVA_1_2.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_2).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_1.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_2).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_3.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_2).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_4.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_2).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_5.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_2).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_6.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_2).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_7.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_2).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_1_8.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_2).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_9.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_2).getJavaVersion());
        assertNotEquals(JavaVersion.JAVA_10.getJavaVersion(), JavaVersion.convertJavaVersion(V_1_2).getJavaVersion());

        assertEquals(JavaVersion.JAVA_1_3.getJavaVersion(), JavaVersion.convertJavaVersion("1.3").getJavaVersion());
        assertEquals(JavaVersion.JAVA_1_4.getJavaVersion(), JavaVersion.convertJavaVersion("1.4").getJavaVersion());
        assertEquals(JavaVersion.JAVA_1_5.getJavaVersion(), JavaVersion.convertJavaVersion("1.5").getJavaVersion());
        assertEquals(JavaVersion.JAVA_1_6.getJavaVersion(), JavaVersion.convertJavaVersion("1.6").getJavaVersion());
        assertEquals(JavaVersion.JAVA_1_7.getJavaVersion(), JavaVersion.convertJavaVersion("1.7").getJavaVersion());
        assertEquals(JavaVersion.JAVA_1_8.getJavaVersion(), JavaVersion.convertJavaVersion("1.8").getJavaVersion());
        assertEquals(JavaVersion.JAVA_9.getJavaVersion(), JavaVersion.convertJavaVersion("9").getJavaVersion());
        assertEquals(JavaVersion.JAVA_10.getJavaVersion(), JavaVersion.convertJavaVersion("10").getJavaVersion());

        assertEquals("1.1.0", JavaVersion.convertJavaVersion(V_1_1).getJavaVersion().toString());
        assertEquals("1.2.0", JavaVersion.convertJavaVersion(V_1_2).getJavaVersion().toString());
        assertEquals("1.3.0", JavaVersion.convertJavaVersion("1.3").getJavaVersion().toString());
        assertEquals("1.4.0", JavaVersion.convertJavaVersion("1.4").getJavaVersion().toString());
        assertEquals("1.5.0", JavaVersion.convertJavaVersion("1.5").getJavaVersion().toString());
        assertEquals("1.6.0", JavaVersion.convertJavaVersion("1.6").getJavaVersion().toString());
        assertEquals("1.7.0", JavaVersion.convertJavaVersion("1.7").getJavaVersion().toString());
        assertEquals("1.8.0", JavaVersion.convertJavaVersion("1.8").getJavaVersion().toString());
        assertEquals("9.0.0", JavaVersion.convertJavaVersion("9").getJavaVersion().toString());
        assertEquals("10.0.0", JavaVersion.convertJavaVersion("10").getJavaVersion().toString());
        
        assertEquals(JavaVersion.JAVA_10.compareTo(JavaVersion.JAVA_11), -1);
        assertEquals(JavaVersion.JAVA_10.compareTo(JavaVersion.JAVA_9), 1);
        assertEquals(JavaVersion.JAVA_10.compareTo(JavaVersion.JAVA_12), -2);
        assertEquals(JavaVersion.JAVA_11.compareTo(JavaVersion.JAVA_10), 1);
        assertEquals(JavaVersion.JAVA_11.compareTo(JavaVersion.JAVA_11), 0);

        assertEquals(JavaVersion.JAVA_1_8.compareTo(JavaVersion.JAVA_10), -2);
        assertEquals(JavaVersion.JAVA_1_8.compareTo(JavaVersion.JAVA_1_8), 0);
        assertEquals(JavaVersion.JAVA_1_8.compareTo(JavaVersion.JAVA_1_7), 1);
    }

    
    /**
     * Test resolve current java version
     */
    @Test
    public void testResolveCurrentJavaVersion() {
        Version version = new Version(System.getProperties().getProperty("java.version"));
        Version resolvedVersion = JavaVersion.resolveJavaVersion().getJavaVersion();
        assertEquals(version.getMajorNumber(), resolvedVersion.getMajorNumber());
        assertEquals(version.getMinorNumber(), resolvedVersion.getMinorNumber());
    }
    
    
    /**
     * Test resolve java version
     * 
     * @throws IOException in case of error
     */
    @Test
    public void testResolveJavaVersion() throws IOException {
        Version version = new Version("21.0");
        Version resolvedVersion = JavaVersion.convertJavaVersion("21.0").getJavaVersion();
        assertEquals(version.getMajorNumber(), resolvedVersion.getMajorNumber());
        assertEquals(version.getMinorNumber(), resolvedVersion.getMinorNumber());
    }
}
