/*
 * VersionTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link Version}.
 * 
 * @author patrick
 */
public class VersionTest extends AbstractVersionTest {
    private static final String V1 = "1";
    private static final String V1A = "1a";
    private static final String V1_3 = "1.3";
    private static final String V1_3_1 = "1.3.1";
    private static final String V2 = "2";
    private static final String QUARKUS_VERSION_3_2_2_FINAL = "3.2.2.Final";


    /**
     * Test
     */
    @Test
    public void testMajorVersion() {
        assertTrue(new Version(V2).isGreaterThan(V1));
        assertEquals(-1, new Version(V1).compareTo(new Version(V2)));
        assertTrue(new Version(V1).isLowerThan(V2));
        assertEquals(1, new Version(V2).compareTo(new Version(V1)));
        
        assertEquals(0, compareVersion((Version) null, (Version) null));
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Version("");
        });
        assertEquals("Invalid version (no major version):", thrown.getMessage());
        assertEquals(0, compareVersion(V1, V1));
        assertEquals(new Version(V1), new Version(V1));
        assertEquals(new Version(V1), new Version(V1));
        assertTrue(new Version(V1).isEqualTo(V1));
        assertTrue(new Version(V1).isGreaterThanOrEqualTo(V1));
        assertFalse(new Version(V1).isGreaterThan(V1));
        assertTrue(new Version(V2).isGreaterThan(V1));
        assertTrue(new Version(V2).isGreaterThanOrEqualTo(V1));
        assertEquals(-1, compareVersion(V1, V2));
        assertEquals(1, compareVersion(V2, V1));
        assertEquals(0, compareVersion(V2, V2));
        assertEquals(1, compareVersion("1bc", V1));
        assertEquals(-1, compareVersion(V1, "1bc"));
        assertEquals(0, compareVersion("1bc", "1bc"));
        assertEquals(-1, compareVersion(V1A, "1b"));
        assertEquals(1, compareVersion("1b_c1", "1b_c0"));
    }


    /**
     * Test
     */
    @Test
    public void testMinorVersion() {
        assertEquals(0, compareVersion((Version) null, (Version) null));
        assertEquals(0, compareVersion(V1_3, V1_3));
        assertEquals(new Version(V1_3), new Version(V1_3));
        assertTrue(new Version(V1_3).isEqualTo(V1_3));
        assertTrue(new Version(V1_3).isGreaterThanOrEqualTo(V1_3));
        assertFalse(new Version(V1_3).isGreaterThan(V1_3));

        assertEquals(-1, compareVersion(V1, V1_3));
        assertEquals(1, compareVersion(V1_3, V1));
        assertEquals(-1, compareVersion(V1_3, "1.4"));
        assertEquals(1, compareVersion("1.4", V1_3));
        assertEquals(1, compareVersion("1.3bc", V1_3));
        assertEquals(-1, compareVersion(V1_3, "1.3bc"));
        assertEquals(0, compareVersion("1.3bc", "1.3bc"));
        assertEquals(-1, compareVersion("1.3a", "1.3b"));
        assertEquals(1, compareVersion("1.3b_c1", "1.3b_c0"));
    }

    /**
     * Test
     */
    @Test
    public void testPatchVersion() {
        assertEquals(0, compareVersion(V1_3_1, V1_3_1));
        assertEquals(new Version(V1_3_1), new Version(V1_3_1));
        assertTrue(new Version(V1_3_1).isEqualTo(V1_3_1));
        assertTrue(new Version(V1_3_1).isGreaterThanOrEqualTo(V1_3_1));
        assertFalse(new Version(V1_3_1).isGreaterThan(V1_3_1));

        assertEquals(1, new Version("1a-b2.3h-sdf_g.4.5.6.7").getMajorNumber());
        assertEquals("a-b2", new Version("1a-b2.3h-sdf_g.4.5.6.7").getMajorSuffix());
        assertEquals(3, new Version("1a-b2.3h-sdf_g.4.5.6.7").getMinorNumber());
        assertEquals("h-sdf_g", new Version("1a-b2.3h-sdf_g.4.5.6.7").getMinorSuffix());
        assertEquals(4, new Version("1a-b2.3h-sdf_g.4.5.6.7").getPatchNumber());
        assertEquals("5-h.6j.7", new Version("1a-b2.3h-sdf_g.4.5-h.6j.7").getPatchSuffix());

        assertEquals(-1, compareVersion(V1_3, V1_3_1));
        assertEquals(1, compareVersion(V1_3_1, V1_3));
        assertEquals(-1, compareVersion(V1_3_1, "1.3.2"));
        assertEquals(1, compareVersion("1.3.2", V1_3_1));
        assertEquals(1, compareVersion("1.3.1bc", V1_3_1));
        assertEquals(-1, compareVersion(V1_3_1, "1.3.1bc"));
        assertEquals(0, compareVersion("1.3.1bc", "1.3.1bc"));
        assertEquals(-1, compareVersion("1.3.1a", "1.3.1b"));
        assertEquals(1, compareVersion("1.3.1b_c1", "1.3.1b_c0"));

        assertEquals(1, compareVersion("1.4", V1_3_1));
        assertEquals(1, compareVersion("1.4.1", "1.3.2"));

        assertEquals(-1, compareVersion(V1_3_1, "1.4"));
        assertEquals(-1, compareVersion("1.3.2", "1.4.1"));
        assertEquals(-1, compareVersion("1.8.255", "11.0.6"));
        
        assertEquals(0, compareVersion(QUARKUS_VERSION_3_2_2_FINAL, QUARKUS_VERSION_3_2_2_FINAL));
        assertEquals(3, new Version(QUARKUS_VERSION_3_2_2_FINAL).getMajorNumber());
        assertNull(new Version(QUARKUS_VERSION_3_2_2_FINAL).getMajorSuffix());
        assertEquals(2, new Version(QUARKUS_VERSION_3_2_2_FINAL).getMinorNumber());
        assertNull(new Version(QUARKUS_VERSION_3_2_2_FINAL).getMinorSuffix());
        assertEquals(2, new Version(QUARKUS_VERSION_3_2_2_FINAL).getPatchNumber());
        assertEquals("Final", new Version(QUARKUS_VERSION_3_2_2_FINAL).getPatchSuffix());
    }

    
    /**
     * Test
     */
    @Test
    public void isStable_test() {
        assertTrue(new Version("1.2.3+sHa.0nSFGKjkjsdf").isStable());
        assertTrue(new Version("1.2.3").isStable());
        assertFalse(new Version("1.2.3-BETA.11+sHa.0nSFGKjkjsdf").isStable());
        assertFalse(new Version("1.2.3-alpha").isStable());
        assertFalse(new Version("1.2.3-rc1").isStable());
        assertFalse(new Version("1.2.3-my-Rc1").isStable());
        assertFalse(new Version("1.2.3-SNAPSHOT").isStable());
        assertFalse(new Version("1.2.3-snapshot").isStable());
        assertFalse(new Version("0.1.2+sHa.0nSFGKjkjsdf").isStable());
        assertFalse(new Version("0.1.2").isStable());
    }

    
    /**
     * Test sort version
     */
    @Test
    public void testSortVersion() {
        List<String> list = getVersionList(false);
        Collections.sort(list, Collections.reverseOrder());
        final String orderedList = list.toString().replace("0.4.5, 0.4.2, 0.4.11-b, 0.4.11,", "0.4.11-b, 0.4.11, 0.4.5, 0.4.2,");
        
        list.addAll(VERSION_LIST_INVALID);
        Collections.shuffle(list);

        List<String> invalidVersionList = new ArrayList<String>();
        List<Version> versionList = Version.convert(list, invalidVersionList);
        
        assertEquals(orderedList, versionList.toString());
        Collections.sort(VERSION_LIST_INVALID);
        assertEquals(VERSION_LIST_INVALID.toString(), invalidVersionList.toString());
    }

    
    /**
     * Test filter version
     */
    @Test
    public void testFilterVersions() {
        List<String> list = getVersionList(true);
        list.addAll(VERSION_LIST_INVALID);
        Collections.shuffle(list);
        
        List<String> invalidVersionList = new ArrayList<String>();
        List<Version> versionList = Version.convert(list, invalidVersionList);
        Collections.shuffle(versionList);

        assertEquals("[]", SemanticVersion.filter(versionList, 0, 0, 0, false).toString());
        assertEquals("[2.2.1]", SemanticVersion.filter(versionList, 1, 0, 0, false).toString());
        assertEquals("[2.2.1]", SemanticVersion.filter(versionList, 1, 1, 0, false).toString());
        assertEquals("[2.2.1]", SemanticVersion.filter(versionList, 1, 1, 1, false).toString());
        assertEquals("[2.2.1, 2.2.0]", SemanticVersion.filter(versionList, 1, 1, 2, false).toString());
        assertEquals("[2.2.1, 2.1.2]", SemanticVersion.filter(versionList, 1, 2, 1, false).toString());
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1]", SemanticVersion.filter(versionList, 1, 2, 2, false).toString());
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1, 2.1.0]", SemanticVersion.filter(versionList, 1, 2, 3, false).toString());
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1, 2.1.0, 2.0.2, 2.0.1, 2.0.0]", SemanticVersion.filter(versionList, 1, 3, 3, false).toString());
        
        // test previous major settings
        assertEquals("[2.2.1, 1.3.4]", SemanticVersion.filter(versionList, 2, 1, 1, false).toString());
        assertEquals("[2.2.1, 2.2.0, 1.3.4]", SemanticVersion.filter(versionList, 2, 1, 2, 1, 1, false).toString());
        assertEquals("[2.2.1, 2.2.0, 1.3.4, 1.3.3]", SemanticVersion.filter(versionList, 2, 1, 2, 1, 2, false).toString());
        assertEquals("[2.2.1, 2.2.0, 1.3.4, 1.2.4]", SemanticVersion.filter(versionList, 2, 1, 2, 2, 1, false).toString());
        assertEquals("[2.2.1, 2.2.0, 1.3.4, 1.3.3, 1.2.4, 1.2.3]", SemanticVersion.filter(versionList, 2, 1, 2, 2, 2, false).toString());
        
        // same test cases
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1, 1.3.4, 1.3.3, 1.2.4, 1.2.3]", SemanticVersion.filter(versionList, 2, 2, 2, false).toString());
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1, 1.3.4, 1.3.3, 1.2.4, 1.2.3]", SemanticVersion.filter(versionList, 2, 2, 2, 2, 2, false).toString());
        assertEquals("[2.1.0, 2.0.2, 2.0.1, 2.0.0, 1.3.2, 1.3.1, 1.3.0, 1.2.2, 1.2.1, 1.2.0, 1.1.4, 1.1.3, 1.1.2, 1.1.1, 1.1.0, 1.0.3, 1.0.2, 1.0.1, 1.0.0, "
                     + "0.7.3, 0.7.2, 0.7.1, 0.7.0, 0.6.3, 0.6.2, 0.6.1, 0.6.0, 0.5.33, 0.5.2, 0.5.1, 0.5.0, 0.4.11-b, 0.4.11, 0.4.5, 0.4.2, 0.4.1, 0.4.0, "
                     + "0.1.5, 0.1.4, 0.1.3, 0.1.2, 0.1.1, 0.1.0, 0.0.6, 0.0.5, 0.0.4, 0.0.3, 0.0.2]", SemanticVersion.filter(versionList, 2, 2, 2, true).toString());

        Collections.sort(VERSION_LIST_INVALID);
        assertEquals(VERSION_LIST_INVALID.toString(), invalidVersionList.toString());
    }

    
    /**
     * Test next version
     */
    @Test
    public void testWithIncrement() {
        Version v = new Version("1a.2b.3v");
        assertEquals("1.2.3", "" + v.withClearedSuffix());
        assertEquals("2.0.0", "" + v.withIncrementMajor());
        assertEquals("1a.3.0", "" + v.withIncrementMinor());
        assertEquals("1a.2b.4", "" + v.withIncrementPatch());

        v = new Version("1a.2b");
        assertEquals("1.2", "" + v.withClearedSuffix());
        assertEquals("2.0", "" + v.withIncrementMajor());
        assertEquals("1a.3", "" + v.withIncrementMinor());
        assertEquals("1a.2b.1", "" + v.withIncrementPatch());

        v = new Version(V1A);
        assertEquals("1", "" + v.withClearedSuffix());
        assertEquals(V2, "" + v.withIncrementMajor());
        assertEquals("1a.1", "" + v.withIncrementMinor());
        assertEquals("1a.0.1", "" + v.withIncrementPatch());
    }
    

    /**
     * Test next version
     */
    @Test
    public void testNextVersion() {
        Version v = new Version("1a.2b.3v");
        assertEquals(V1A, "" + v.getMajor());
        assertEquals(1, v.getMajor().getNumber());
        assertEquals("a", "" + v.getMajor().getSuffix());
        assertEquals("2b", "" + v.getMinor());
        assertEquals(2, v.getMinor().getNumber());
        assertEquals("b", "" + v.getMinor().getSuffix());
        assertEquals("3v", "" + v.getPatch());
        assertEquals(3, v.getPatch().getNumber());
        assertEquals("v", "" + v.getPatch().getSuffix());
        assertEquals("2.0.0", v.nextMajor().toString());
        assertEquals("1a.3.0", v.nextMinor().toString());
        assertEquals("1a.2b.4", v.nextPatch().toString());

        v = new Version("1a.2b");
        assertEquals(V1A, "" + v.getMajor());
        assertEquals(1, v.getMajor().getNumber());
        assertEquals("a", "" + v.getMajor().getSuffix());
        assertEquals("2b", "" + v.getMinor());
        assertEquals(2, v.getMinor().getNumber());
        assertEquals("b", "" + v.getMinor().getSuffix());
        assertNull(v.getPatch());
        assertEquals("2.0", v.nextMajor().toString());
        assertEquals("1a.3", v.nextMinor().toString());
        assertEquals("1a.2b.1", v.nextPatch().toString());

        v = new Version(V1A);
        assertEquals(V1A, "" + v.getMajor());
        assertEquals(1, v.getMajor().getNumber());
        assertEquals("a", "" + v.getMajor().getSuffix());
        assertNull(v.getMinor());
        assertEquals(V2, v.nextMajor().toString());
        assertEquals("1a.1", v.nextMinor().toString());
        assertEquals("1a.0.1", v.nextPatch().toString());
    }
    
    
    /**
     * Test
     */
    @Test
    public void diff() {
        Version version = new Version("1.2.3-beta.4+sha899d8g79f87");
        assertEquals(VersionDiff.NONE, version.diff("1.2.3-beta.4+sha899d8g79f87"));
        assertEquals(VersionDiff.MAJOR, version.diff("2.3.4-alpha.5+sha32iddfu987"));
        assertEquals(VersionDiff.MINOR, version.diff("1.3.4-alpha.5+sha32iddfu987"));
        assertEquals(VersionDiff.PATCH, version.diff("1.2.4-alpha.5+sha32iddfu987"));
        assertEquals(VersionDiff.PATCH, version.diff("1.2.3-alpha.4+sha32iddfu987"));
        assertEquals(VersionDiff.PATCH, version.diff("1.2.3-beta.5+sha32iddfu987"));
        assertEquals(VersionDiff.PATCH, version.diff("1.2.3-beta.4+sha32iddfu987"));
        assertEquals(VersionDiff.PATCH, version.diff("1.2.3-beta.4+sha899-d8g79f87"));
    }

    
    /**
     * Get the Java version as string
     * @param version1 the version one
     * @param version2 the version two
     *
     * @return the java version as string
     */
    protected int compareVersion(String version1, String version2) {
        return compareVersion(new Version(version1), new Version(version2));
    }
    
    
    /**
     * Compares two versions
     *
     * @param v1 the first version
     * @param v2 the second version
     * @return ==0 is the versions are equals; &gt;1 if v1 is bigger than v2 or
     *         &lt;1 if v1 is less than v2
     */
    protected int compareVersion(Version v1, Version v2) {
        int result = -1;

        if (v1 != null) {
            result = v1.compareTo(v2);
        } else if (v2 == null) {
            return 0;
        }

        if (result == 0) {
            assertEquals(v1, v2);
            assertEquals(v2, v1);
        } else if (result > 0) {
            assertTrue(v2.isLowerThan(v1));
        } else if (result < 0) {
            assertTrue(v2.isGreaterThan(v1));
        }

        return result;
    }
}
