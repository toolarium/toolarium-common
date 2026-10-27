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

import java.io.IOException;
import java.time.Duration;
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
    private static final String V1_2_3 = "1.2.3";
    private static final String V1_2_3_SNAPSHOT = "1.2.3-SNAPSHOT";
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
        assertTrue(new Version(V1_2_3).isStable());
        assertFalse(new Version("1.2.3-BETA.11+sHa.0nSFGKjkjsdf").isStable());
        assertFalse(new Version("1.2.3-alpha").isStable());
        assertFalse(new Version("1.2.3-rc1").isStable());
        assertFalse(new Version("1.2.3-my-Rc1").isStable());
        assertFalse(new Version(V1_2_3_SNAPSHOT).isStable());
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

        assertEquals("[]", SemanticVersion.filter(versionList, 0, 0, 0, 0).toString());
        assertEquals("[2.2.1]", SemanticVersion.filter(versionList, 0, 1, 0, 0).toString());
        assertEquals("[2.2.1]", SemanticVersion.filter(versionList, 0, 1, 1, 0).toString());
        assertEquals("[2.2.1]", SemanticVersion.filter(versionList, 0, 1, 1, 1).toString());
        assertEquals("[2.2.1, 2.2.0]", SemanticVersion.filter(versionList, 0, 1, 1, 2).toString());
        assertEquals("[2.2.1, 2.1.2]", SemanticVersion.filter(versionList, 0, 1, 2, 1).toString());
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1]", SemanticVersion.filter(versionList, 0, 1, 2, 2).toString());
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1, 2.1.0]", SemanticVersion.filter(versionList, 0, 1, 2, 3).toString());
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1, 2.1.0, 2.0.2, 2.0.1, 2.0.0]", SemanticVersion.filter(versionList, 0, 1, 3, 3).toString());
        
        // test previous major settings
        assertEquals("[2.2.1, 1.3.4]", SemanticVersion.filter(versionList, 0, 2, 1, 1).toString());
        assertEquals("[2.2.1, 2.2.0, 1.3.4]", SemanticVersion.filter(versionList, 0, 2, 1, 2, 1, 1).toString());
        assertEquals("[2.2.1, 2.2.0, 1.3.4, 1.3.3]", SemanticVersion.filter(versionList, 0, 2, 1, 2, 1, 2).toString());
        assertEquals("[2.2.1, 2.2.0, 1.3.4, 1.2.4]", SemanticVersion.filter(versionList, 0, 2, 1, 2, 2, 1).toString());
        assertEquals("[2.2.1, 2.2.0, 1.3.4, 1.3.3, 1.2.4, 1.2.3]", SemanticVersion.filter(versionList, 0, 2, 1, 2, 2, 2).toString());
        
        // same test cases
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1, 1.3.4, 1.3.3, 1.2.4, 1.2.3]", SemanticVersion.filter(versionList, 0, 2, 2, 2).toString());
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1, 1.3.4, 1.3.3, 1.2.4, 1.2.3]", SemanticVersion.filter(versionList, 0, 2, 2, 2, 2, 2).toString());
        assertEquals("[2.1.0, 2.0.2, 2.0.1, 2.0.0, 1.3.2, 1.3.1, 1.3.0, 1.2.2, 1.2.1, 1.2.0, 1.1.4, 1.1.3, 1.1.2, 1.1.1, 1.1.0, 1.0.3, 1.0.2, 1.0.1, 1.0.0, "
                     + "0.7.3, 0.7.2, 0.7.1, 0.7.0, 0.6.3, 0.6.2, 0.6.1, 0.6.0, 0.5.33, 0.5.2, 0.5.1, 0.5.0, 0.4.11-b, 0.4.11, 0.4.5, 0.4.2, 0.4.1, 0.4.0, "
                     + "0.1.5, 0.1.4, 0.1.3, 0.1.2, 0.1.1, 0.1.0, 0.0.6, 0.0.5, 0.0.4, 0.0.3, 0.0.2]", SemanticVersion.invertFilter(versionList, 2, 2, 2).toString());

        Collections.sort(VERSION_LIST_INVALID);
        assertEquals(VERSION_LIST_INVALID.toString(), invalidVersionList.toString());
    }

    
    /**
     * Test next version
     */
    @Test
    public void testWithIncrement() {
        Version v = new Version("1a.2b.3v");
        assertEquals(V1_2_3, "" + v.withClearedSuffix());
        assertEquals("2.0.0", "" + v.withIncrementMajor());
        assertEquals("1a.3.0", "" + v.withIncrementMinor());
        assertEquals("1a.2b.4", "" + v.withIncrementPatch());

        v = new Version("1a.2b");
        assertEquals("1.2", "" + v.withClearedSuffix());
        assertEquals("2.0", "" + v.withIncrementMajor());
        assertEquals("1a.3", "" + v.withIncrementMinor());
        assertEquals("1a.2b.1", "" + v.withIncrementPatch());

        v = new Version(V1A);
        assertEquals(V1, "" + v.withClearedSuffix());
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
     * Test equals and hashCode are consistent with compareTo
     */
    @Test
    public void testEqualsConsistentWithCompareTo() {
        assertEquals(0, compareVersion("1.02.3", V1_2_3));
        assertEquals(new Version("1.02.3"), new Version(V1_2_3));
        assertEquals(new Version("1.02.3").hashCode(), new Version(V1_2_3).hashCode());
        assertTrue(new Version("1.02.3").isEqualTo(V1_2_3));
        assertFalse(new Version("1.02.3").isLowerThan(V1_2_3));
        assertEquals(new Version("1-2-3", "-"), new Version(V1_2_3));
        assertFalse(new Version("1.2.3a").equals(new Version(V1_2_3)));
        assertFalse(new Version("1.2").equals(new Version("1.2.0")));
        assertFalse(new Version(V1_2_3).equals(new SemanticVersion(V1_2_3)));
        assertFalse(new SemanticVersion(V1_2_3).equals(new Version(V1_2_3)));

        String[] versions = {"1", "1.0", "1.0.0", "01.0.0", "1.0.0a", "1.0.0-rc1", "1.2", "1.02", "2.0.0.Final", "2.0.0..Final"};
        for (String v1 : versions) {
            for (String v2 : versions) {
                Version a = new Version(v1);
                Version b = new Version(v2);
                assertEquals(a.compareTo(b) == 0, a.equals(b), v1 + " <> " + v2);
                assertEquals(Integer.signum(a.compareTo(b)), -Integer.signum(b.compareTo(a)), v1 + " <> " + v2);
                if (a.equals(b)) {
                    assertEquals(a.hashCode(), b.hashCode());
                }
            }
        }
    }


    /**
     * Test set number keeps the other parts
     */
    @Test
    public void testSetNumber() {
        assertEquals("5.2.3.4", new Version("1.2.3.4").setMajorNumber("5").toString());
        assertEquals("1.5.3.4", new Version("1.2.3.4").setMinorNumber("5").toString());
        assertEquals("1.2.5", new Version("1.2.3.4").setPatchNumber("5").toString());
        assertEquals("1.2.5.Final", new Version(QUARKUS_VERSION_3_2_2_FINAL).setMajorNumber(V1).setMinorNumber("2").setPatchNumber("5.Final").toString());
        assertEquals("3.2.2.Final", "" + new Version(QUARKUS_VERSION_3_2_2_FINAL).setMajorNumber("3"));
        assertEquals("2", new Version(V1).setMajorNumber(V2).toString());
        assertEquals("2.3", new Version(V1_3).setMajorNumber(V2).toString());
        assertEquals("1.4", new Version(V1_3).setMinorNumber("4").toString());
        assertEquals("1.3.1-SNAPSHOT", new Version(V1_3_1 + "-SNAPSHOT").setMajorNumber(V1).toString());
        assertEquals("2.Final", "" + new Version(QUARKUS_VERSION_3_2_2_FINAL).getPatch());
        assertEquals("Final", new Version(QUARKUS_VERSION_3_2_2_FINAL).getPatch().getSuffix());
    }


    /**
     * Test serialization
     *
     * @throws IOException in case of error
     * @throws ClassNotFoundException in case of error
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        for (String v : new String[] {V1, V1A, V1_3, V1_3_1, QUARKUS_VERSION_3_2_2_FINAL, "1a-b2.3h-sdf_g.4.5-h.6j.7"}) {
            Version version = new Version(v);
            Version copy = (Version) SemanticVersionTest.roundTrip(version);
            assertEquals(version, copy);
            assertEquals(version.toString(), copy.toString());
            assertEquals(version.getType(), copy.getType());
            assertEquals(version.getMajor(), copy.getMajor());
            assertEquals(version.getMinor(), copy.getMinor());
            assertEquals(version.getPatch(), copy.getPatch());
            assertEquals(version.nextPatch(), copy.nextPatch());
        }

        Version version = new Version(1, 2, 3);
        assertEquals(version, SemanticVersionTest.roundTrip(version));
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


    /**
     * A suffix on the major or minor number is not stable
     */
    @Test
    public void testIsStableWithMinorOrMajorSuffix() {
        assertTrue(new Version(V1_2_3).isStable());
        assertFalse(new Version(V1_2_3_SNAPSHOT).isStable());
        assertFalse(new Version("1.2-SNAPSHOT").isStable());
        assertFalse(new Version("1-beta").isStable());
    }


    /**
     * An increment of 0 keeps the version
     */
    @Test
    public void testIncrementZero() {
        final String version = V1_2_3;
        assertEquals(version, new Version(version).withIncrementMajor(0).toString());
        assertEquals(version, new Version(version).withIncrementMinor(0).toString());
        assertEquals("1.3.0", new Version(version).withIncrementMinor().toString());
        assertEquals("2.0.0", new Version(version).withIncrementMajor().toString());
    }


    /**
     * The separator is used literally, an empty or null separator uses the default
     */
    @Test
    public void testSeparator() {
        Version v = new Version("1d2d3", "d");
        assertEquals(1, v.getMajorNumber());
        assertEquals(2, v.getMinorNumber());
        assertEquals(3, v.getPatchNumber());
        assertEquals(2, new Version("1|2|3", "|").getMinorNumber());
        assertEquals(3, new Version(V1_2_3, "").getPatchNumber());
        assertEquals(3, new Version(V1_2_3, (String) null).getPatchNumber());
    }


    /**
     * Only the ASCII digits 0 to 9 are numbers
     */
    @Test
    public void testNonAsciiDigits() {
        final String arabicOne = String.valueOf((char) 0x0661);
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Version(arabicOne + ".2.3"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Version("1." + arabicOne + ".3"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Version("1.2." + arabicOne));
    }


    /**
     * The setters and the element constructor reject null elements
     */
    @Test
    public void testNullElements() {
        final Version version = new Version(V1_2_3, VersionType.STRICT, null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> version.setMajorNumber(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> version.setMinorNumber(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> version.setPatchNumber(null));

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Version(null, version.getMinor(), version.getPatch(), VersionType.STRICT, null));
        assertEquals("Invalid version (no major version):null.2.3", thrown.getMessage());
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Version(version.getMajor(), null, version.getPatch(), VersionType.LOOSE, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Version(version.getMajor(), version.getMinor(), null, VersionType.STRICT, null));
        assertEquals("1.2", new Version(version.getMajor(), version.getMinor(), null, VersionType.LOOSE, null).toString());
    }


    /**
     * A multi character separator is stripped completely from the suffix
     */
    @Test
    public void testMultiCharSeparatorSuffix() {
        final String separator = "::";
        final Version version = new Version("1::2::3::beta", separator);
        assertEquals(3, version.getPatchNumber());
        assertEquals("beta", version.getPatchSuffix());
        assertEquals("3::beta", version.getPatch().toString());
        assertEquals(version, new Version("1::2::3beta", separator));
    }


    /**
     * The unstable qualifiers are matched as whole tokens and not as part of a word
     */
    @Test
    public void testIsStableQualifierTokens() {
        assertTrue(new Version("1.2.3-sources").isStable());
        assertTrue(new Version("1.2.3-force").isStable());
        assertTrue(new Version("1.2.3-alphabet").isStable());
        assertFalse(new Version("1.2.3-RC1").isStable());
        assertFalse(new Version("1.2.3-beta2").isStable());
        assertFalse(new Version("1.2.3.rc.2").isStable());
    }


    /**
     * The early access, preview and milestone qualifiers are not stable
     */
    @Test
    public void testIsStableMoreQualifiers() {
        assertFalse(new Version("21.0.0-ea").isStable());
        assertFalse(new Version("1.0.0-preview").isStable());
        assertFalse(new Version("1.0.0-preview2").isStable());
        assertFalse(new Version("1.0.0-milestone").isStable());
        assertFalse(new Version("1.0.0-M1").isStable());
        assertFalse(new Version("1.0.0-m12").isStable());
        assertTrue(new Version("1.0.0-m").isStable());
        assertTrue(new Version("1.0.0-mx1").isStable());
        assertTrue(new Version("1.0.0-each").isStable());
    }


    /**
     * A suffix with an unstable qualifier ranks below the version without suffix, other suffixes above;
     * numbers in a suffix are compared numerically
     */
    @Test
    public void testSuffixOrdering() {
        assertTrue(new Version(V1_2_3_SNAPSHOT).isLowerThan(V1_2_3));
        assertTrue(new Version("1.2.3-rc1").isLowerThan(V1_2_3));
        assertTrue(new Version("1.2.3-M1").isLowerThan(V1_2_3));
        assertTrue(new Version("1.2.3.Final").isGreaterThan(V1_2_3));
        assertTrue(new Version("1.2.3-rc10").isGreaterThan("1.2.3-rc9"));
        assertTrue(new Version(V1_2_3_SNAPSHOT).isGreaterThan("1.2.2"));
        assertTrue(new Version("1.2.3-rc1").isLowerThan("1.2.3.Final"));
        assertEquals(1, compareVersion(V1_2_3, "1.2.3-beta"));
        assertEquals(-1, compareVersion("1.2.3-beta", V1_2_3));
        assertEquals(-1, compareVersion("1.2.3-rc9", "1.2.3-rc10"));
        assertEquals(1, compareVersion("1.2.3-rc010", "1.2.3-rc9"));
        assertFalse(new Version("1.2.3-rc01").equals(new Version("1.2.3-rc1")));

        List<Version> list = new ArrayList<Version>();
        for (String v : new String[] {"1.2.3.Final", V1_2_3, "1.2.3-rc10", V1_2_3_SNAPSHOT, "1.2.3-rc9", "1.2.2", "1.2.4-alpha"}) {
            list.add(new Version(v));
        }
        Collections.sort(list);
        assertEquals("[1.2.2, 1.2.3-SNAPSHOT, 1.2.3-rc9, 1.2.3-rc10, 1.2.3, 1.2.3.Final, 1.2.4-alpha]", list.toString());

        String[] versions = {V1_2_3, "1.2.3-rc1", "1.2.3-rc01", "1.2.3-RC1", V1_2_3_SNAPSHOT, "1.2.3a", "1.2.3.Final", "1.2.3-beta2", "1.2.3-b2"};
        for (String v1 : versions) {
            for (String v2 : versions) {
                Version a = new Version(v1);
                Version b = new Version(v2);
                assertEquals(a.compareTo(b) == 0, a.equals(b), v1 + " <> " + v2);
                assertEquals(Integer.signum(a.compareTo(b)), -Integer.signum(b.compareTo(a)), v1 + " <> " + v2);
            }
        }
    }


    /**
     * Negative numbers and increments are rejected, compare to null throws a NullPointerException
     */
    @Test
    public void testNegativeNumbersAndNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Version(-1, 0, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Version(1, -1, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Version(1, 0, -1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Version(V1_2_3).withIncrementMajor(-1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Version(V1_2_3).withIncrementMinor(-1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Version(V1_2_3).withIncrementPatch(-1));
        Assertions.assertThrows(NullPointerException.class, () -> new Version(V1_2_3).compareTo(null));
        assertEquals("0.0.0", new Version(0, 0, 0).toString());
    }


    /**
     * An empty minor element is 0 in LOOSE mode and invalid in STRICT mode
     */
    @Test
    public void testEmptyMinorElement() {
        final String version = "1..2";
        final Version loose = new Version(version);
        assertEquals(1, loose.getMajorNumber());
        assertEquals(0, loose.getMinorNumber());
        assertEquals(2, loose.getPatchNumber());
        assertEquals(new Version("1.0.2"), loose);
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Version(version, VersionType.STRICT, null));
    }


    /**
     * Long suffixes and many separators are processed in linear time (M08, M09)
     */
    @Test
    public void testLongInputIsFast() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            assertTrue(new Version("1.0.0-" + V1.repeat(40_000) + "a").isStable());
            assertFalse(new Version("1.0.0-rc" + V1.repeat(40_000)).isStable());
            assertEquals(3, new Version("1.2.3" + ".".repeat(200_000) + "x").getPatchNumber());
        });
    }
}
