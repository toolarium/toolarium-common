/*
 * Version.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test the {@link SemanticVersion}.
 *  
 * @author patrick
 */
public class SemanticVersionTest extends AbstractVersionTest {
    private static final Logger LOG = LoggerFactory.getLogger(SemanticVersionTest.class);
    private static final String T1 = "1";
    private static final String T2 = "2";
    private static final String T3 = "3";
    private static final String V1_0_0 = "1.0.0";
    private static final String V1_0_0_ALPHA_1 = "1.0.0-alpha.1";
    private static final String V1_0_0_ALPHA_BETA = "1.0.0-alpha.beta";
    private static final String V1_0_0_BETA = "1.0.0-beta";
    private static final String V1_0_0_BETA_2 = "1.0.0-beta.2";
    private static final String V1_0_0_BETA_11 = "1.0.0-beta.11";
    private static final String V1_0_0_RC_1 = "1.0.0-rc.1";
    private static final String V1_2_3 = "1.2.3";
    private static final String SHA_0NSFGKJKJSDF = "sha.0nsfgkjkjsdf";
    private static final String BETA = "beta";
    private static final String ELEVEN = "11";

    
    /**
     * Test
     */
    @Test
    public void constructor_with_empty_build_fails() {
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new SemanticVersion("1.0.0+");
        });
        Assertions.assertEquals("Invalid version (build cannot be empty):1.0.0+", thrown.getMessage());
    }

    
    /**
     * Test the default constuctor
     */
    @Test
    public void default_constructor_test_full_version() {
        String versionStr = "1.2.3-beta.11+sha.0nsfgkjkjsdf";
        SemanticVersion version = new SemanticVersion(versionStr);
        assertIsVersion(version, versionStr, 1, 2, 3, new String[]{BETA, ELEVEN }, SHA_0NSFGKJKJSDF);
    }

    
    /**
     * Test
     */
    @Test
    public void default_constructor_test_only_major_and_minor() {
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new SemanticVersion("1.2-beta.11+sha.0nsfgkjkjsdf");
        });
        Assertions.assertEquals("Invalid version (no patch version):1.2-beta.11+sha.0nsfgkjkjsdf", thrown.getMessage());
    }


    /**
     * Test
     */
    @Test
    public void default_constructor_test_only_major() {
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new SemanticVersion("1-beta.11+sha.0nsfgkjkjsdf");
        });
        Assertions.assertEquals("Invalid version (no minor version):1-beta.11+sha.0nsfgkjkjsdf", thrown.getMessage());
    }
    
    
    /**
     * Test
     */
    @Test 
    public void npm_constructor_test_full_version() {
        String versionStr = "1.2.3-beta.11+sha.0nsfgkjkjsdf";
        SemanticVersion version = new SemanticVersion(versionStr, VersionType.NPM);
        assertIsVersion(version, versionStr, 1, 2, 3, new String[]{BETA, ELEVEN}, SHA_0NSFGKJKJSDF);
    }

    
    /**
     * Test
     */
    @Test public void npm_constructor_test_only_major_and_minor() {
        String versionStr = "1.2-beta.11+sha.0nsfgkjkjsdf"; 
        SemanticVersion version = new SemanticVersion(versionStr, VersionType.NPM);
        assertIsVersion(version, versionStr, 1, 2, null, new String[]{BETA, ELEVEN}, SHA_0NSFGKJKJSDF);
    }
    
    
    /**
     * Test
     */
    @Test public void npm_constructor_test_only_major() {
        String versionStr = "1-beta.11+sha.0nsfgkjkjsdf";
        SemanticVersion version = new SemanticVersion(versionStr, VersionType.NPM);
        assertIsVersion(version, versionStr, 1, null, null, new String[]{BETA, ELEVEN}, SHA_0NSFGKJKJSDF);
    }

    
    /**
     * Test
     */
    @Test public void npm_constructor_with_leading_v() {
        SemanticVersion version = new SemanticVersion("v1.2.3-beta.11+sha.0nsfgkjkjsdf", VersionType.NPM);
        assertIsVersion(version, "1.2.3-beta.11+sha.0nsfgkjkjsdf", 1, 2, 3, new String[]{BETA, ELEVEN}, SHA_0NSFGKJKJSDF);
    
        String versionStr = "v 1.2.3-beta.11+sha.0nsfgkjkjsdf";
        SemanticVersion versionWithSpace = new SemanticVersion(versionStr, VersionType.NPM);
        assertIsVersion(versionWithSpace, versionStr.replace('v', ' ').trim(), 1, 2, 3, new String[]{BETA, ELEVEN}, SHA_0NSFGKJKJSDF);
    }
    
    
    /**
     * Test
     */
    @Test
    public void loose_constructor_test_only_major_and_minor() {
        String versionStr = "1.2-beta.11+sha.0nsfgkjkjsdf";
        SemanticVersion version = new SemanticVersion(versionStr, VersionType.LOOSE);
        assertIsVersion(version, versionStr, 1, 2, null, new String[]{BETA, ELEVEN}, SHA_0NSFGKJKJSDF);
    }

    
    /**
     * Test
     */
    @Test
    public void loose_constructor_test_only_major() {
        String versionStr = "1-beta.11+sha.0nsfgkjkjsdf";
        SemanticVersion version = new SemanticVersion(versionStr, VersionType.LOOSE);
        assertIsVersion(version, versionStr, 1, null, null, new String[]{BETA, ELEVEN}, SHA_0NSFGKJKJSDF);
    }
    
    
    /**
     * Test
     */
    @Test public void default_constructor_test_myltiple_hyphen_signs() {
        String versionStr = "1.2.3-beta.1-1.ab-c+sha.0nsfgkjkjs-df";
        SemanticVersion version = new SemanticVersion(versionStr);
        assertIsVersion(version, versionStr, 1, 2, 3, new String[]{BETA, "1-1", "ab-c"}, "sha.0nsfgkjkjs-df");
    }
    
    
    /**
     * Test
     */
    @Test
    public void isGreaterThan_test() {
        // 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0
    
        assertTrue(new SemanticVersion(V1_0_0_ALPHA_1).isGreaterThan("1.0.0-alpha"));
        assertTrue(new SemanticVersion(V1_0_0_ALPHA_BETA).isGreaterThan(V1_0_0_ALPHA_1));
        assertTrue(new SemanticVersion(V1_0_0_BETA).isGreaterThan(V1_0_0_ALPHA_BETA));
        assertTrue(new SemanticVersion(V1_0_0_BETA_2).isGreaterThan(V1_0_0_BETA));
        assertTrue(new SemanticVersion(V1_0_0_BETA_11).isGreaterThan(V1_0_0_BETA_2));
        assertTrue(new SemanticVersion(V1_0_0_RC_1).isGreaterThan(V1_0_0_BETA_11));
        assertTrue(new SemanticVersion(V1_0_0).isGreaterThan(V1_0_0_RC_1));
    
    
        assertFalse(new SemanticVersion("1.0.0-alpha").isGreaterThan(V1_0_0_ALPHA_1));
        assertFalse(new SemanticVersion(V1_0_0_ALPHA_1).isGreaterThan(V1_0_0_ALPHA_BETA));
        assertFalse(new SemanticVersion(V1_0_0_ALPHA_BETA).isGreaterThan(V1_0_0_BETA));
        assertFalse(new SemanticVersion(V1_0_0_BETA).isGreaterThan(V1_0_0_BETA_2));
        assertFalse(new SemanticVersion(V1_0_0_BETA_2).isGreaterThan(V1_0_0_BETA_11));
        assertFalse(new SemanticVersion(V1_0_0_BETA_11).isGreaterThan(V1_0_0_RC_1));
        assertFalse(new SemanticVersion(V1_0_0_RC_1).isGreaterThan(V1_0_0));
    
        assertFalse(new SemanticVersion(V1_0_0).isGreaterThan(V1_0_0));
        assertFalse(new SemanticVersion("1.0.0-alpha.12").isGreaterThan("1.0.0-alpha.12"));
    
        assertFalse(new SemanticVersion("0.0.1").isGreaterThan("5.0.0"));
        assertFalse(new SemanticVersion("1.0.0-alpha.12.ab-c").isGreaterThan("1.0.0-alpha.12.ab-c"));
    }
    
    
    /**
     * Test
     */
    @Test
    public void isLowerThan_test() {
        // 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0
    
        assertFalse(new SemanticVersion(V1_0_0_ALPHA_1).isLowerThan("1.0.0-alpha"));
        assertFalse(new SemanticVersion(V1_0_0_ALPHA_BETA).isLowerThan(V1_0_0_ALPHA_1));
        assertFalse(new SemanticVersion(V1_0_0_BETA).isLowerThan(V1_0_0_ALPHA_BETA));
        assertFalse(new SemanticVersion(V1_0_0_BETA_2).isLowerThan(V1_0_0_BETA));
        assertFalse(new SemanticVersion(V1_0_0_BETA_11).isLowerThan(V1_0_0_BETA_2));
        assertFalse(new SemanticVersion(V1_0_0_RC_1).isLowerThan(V1_0_0_BETA_11));
        assertFalse(new SemanticVersion(V1_0_0).isLowerThan(V1_0_0_RC_1));
    
        assertTrue(new SemanticVersion("1.0.0-alpha").isLowerThan(V1_0_0_ALPHA_1));
        assertTrue(new SemanticVersion(V1_0_0_ALPHA_1).isLowerThan(V1_0_0_ALPHA_BETA));
        assertTrue(new SemanticVersion(V1_0_0_ALPHA_BETA).isLowerThan(V1_0_0_BETA));
        assertTrue(new SemanticVersion(V1_0_0_BETA).isLowerThan(V1_0_0_BETA_2));
        assertTrue(new SemanticVersion(V1_0_0_BETA_2).isLowerThan(V1_0_0_BETA_11));
        assertTrue(new SemanticVersion(V1_0_0_BETA_11).isLowerThan(V1_0_0_RC_1));
        assertTrue(new SemanticVersion(V1_0_0_RC_1).isLowerThan(V1_0_0));
    
        assertFalse(new SemanticVersion(V1_0_0).isLowerThan(V1_0_0));
        assertFalse(new SemanticVersion("1.0.0-alpha.12").isLowerThan("1.0.0-alpha.12"));
        assertFalse(new SemanticVersion("1.0.0-alpha.12.x-yz").isLowerThan("1.0.0-alpha.12.x-yz"));
    }
    
    
    /**
     * Test
     */
    @Test
    public void isEquivalentTo_isEqualTo_and_build() {
        SemanticVersion version = new SemanticVersion("1.0.0+ksadhjgksdhgksdhgfj");
        String version2 = "1.0.0+sdgfsdgsdhsdfgdsfgf";
        assertFalse(version.isEqualTo(version2));
        assertTrue(version.isEquivalentTo(version2));
    }
    

    /**
     * Test
     */
    @Test
    public void withIncMajor_test() {
        SemanticVersion version = new SemanticVersion("1.2.3-Beta.4+SHA123456789");
        assertTrue(version.withIncrementMajor(2).isEqualTo("3.2.3-Beta.4+SHA123456789"));
    }

    
    /**
     * Test
     */
    @Test
    public void withIncMinor_test() {
        SemanticVersion version = new SemanticVersion("1.2.3-Beta.4+SHA123456789");
        assertTrue(version.withIncrementMinor(2).isEqualTo("1.4.3-Beta.4+SHA123456789"));
    }

    
    /**
     * Test
     */
    @Test
    public void withIncPatch_test() {
        SemanticVersion version = new SemanticVersion("1.2.3-Beta.4+SHA123456789");
        assertTrue(version.withIncrementPatch(2).isEqualTo("1.2.5-Beta.4+SHA123456789"));
    }
    

    /**
     * Test
     */
    @Test
    public void withClearedSuffix_test() {
        SemanticVersion version = new SemanticVersion("1.2.3-Beta.4+SHA123456789");
        assertTrue(version.withClearedSuffix().isEqualTo("1.2.3+SHA123456789"));
    }

    
    /**
     * Test
     */
    @Test
    public void withClearedBuild_test() {
        SemanticVersion version = new SemanticVersion("1.2.3-Beta.4+sha123456789");
        assertTrue(version.withClearedBuild().isEqualTo("1.2.3-Beta.4"));
    }

    
    /**
     * Test
     */
    @Test
    public void withClearedBuild_test_multiple_hyphen_signs() {
        SemanticVersion version = new SemanticVersion("1.2.3-Beta.4-test+sha12345-6789");
        assertTrue(version.withClearedBuild().isEqualTo("1.2.3-Beta.4-test"));
    }

    
    /**
     * Test
     */
    @Test
    public void withClearedSuffixAndBuild_test() {
        SemanticVersion version = new SemanticVersion("1.2.3-Beta.4+SHA123456789");
        assertTrue(version.withClearedSuffixAndBuild().isEqualTo(V1_2_3));
    }
    
    
    /**
     * Test
     */
    @Test
    public void withSuffix_test_change_suffix() {
        SemanticVersion version = new SemanticVersion("1.2.3-Alpha.4+SHA123456789");
        SemanticVersion result = version.withSuffix("Beta.1");
    
        assertEquals("1.2.3-Beta.1+SHA123456789", result.toString());
        assertEquals(Arrays.asList("Beta", "1"), result.getSuffixTokens());
    }
    

    /**
     * Test
     */
    @Test
    public void withSuffix_test_add_suffix() {
        SemanticVersion version = new SemanticVersion("1.2.3+SHA123456789");
        SemanticVersion result = version.withSuffix("Beta.1");
    
        assertEquals("1.2.3-Beta.1+SHA123456789", result.toString());
        assertEquals(Arrays.asList("Beta", "1"), result.getSuffixTokens());
    }
    

    /**
     * Test
     */
    @Test
    public void withBuild_test_change_build() {
        SemanticVersion version = new SemanticVersion("1.2.3-Alpha.4+SHA123456789");
        SemanticVersion result = version.withBuild("SHA987654321");
    
        assertEquals("1.2.3-Alpha.4+SHA987654321", result.toString());
        assertEquals("SHA987654321", result.getBuild());
    }

    
    /**
     * Test
     */
    @Test
    public void withBuild_test_add_build() {
        SemanticVersion version = new SemanticVersion("1.2.3-Alpha.4");
        SemanticVersion result = version.withBuild("SHA987654321");
    
        assertEquals("1.2.3-Alpha.4+SHA987654321", result.toString());
        assertEquals("SHA987654321", result.getBuild());
    }
    
    
    /**
     * Test
     */
    @Test
    public void nextMajor_test() {
        SemanticVersion version = new SemanticVersion("1.2.3-beta.4+sha123456789");
        version.nextMajor().isEqualTo("2.0.0");
    }

    
    /**
     * Test
     */
    @Test
    public void nextMinor_test() {
        SemanticVersion version = new SemanticVersion("1.2.3-beta.4+sha123456789");
        version.nextMinor().isEqualTo("1.3.0");
    }

    
    /**
     * Test
     */
    @Test
    public void nextPatch_test() {
        SemanticVersion version = new SemanticVersion("1.2.3-beta.4+sha123456789");
        version.nextPatch().isEqualTo("1.2.4");
    }
    

    /**
     * Test
     */
    @Test
    public void toStrict_test() {
        String[][] versionGroups = new String[][]{
            new String[]{"3.0.0-beta.4+sha123456789", "3.0-beta.4+sha123456789", "3-beta.4+sha123456789"},
            new String[]{"3.0.0+sha123456789", "3.0+sha123456789", "3+sha123456789"},
            new String[]{"3.0.0-beta.4", "3.0-beta.4", "3-beta.4"},
            new String[]{"3.0.0", "3.0", "3"},
        };
    
        VersionType[] types = new VersionType[] {VersionType.NPM, VersionType.IVY, VersionType.LOOSE };
        for (String[] versions : versionGroups) {
            SemanticVersion strict = new SemanticVersion(versions[0]);
            assertEquals(strict, strict.toStrict());
            for (VersionType type : types) {
                for (String version : versions) {
                    SemanticVersion sem = new SemanticVersion(version, type);
                    assertEquals(strict, sem.toStrict());
                }
            }
        }
    }
    
    
    /**
     * Test
     */
    @Test
    public void diff() {
        SemanticVersion version = new SemanticVersion("1.2.3-beta.4+sha899d8g79f87");
        assertEquals(VersionDiff.NONE, version.diff("1.2.3-beta.4+sha899d8g79f87"));
        assertEquals(VersionDiff.MAJOR, version.diff("2.3.4-alpha.5+sha32iddfu987"));
        assertEquals(VersionDiff.MINOR, version.diff("1.3.4-alpha.5+sha32iddfu987"));
        assertEquals(VersionDiff.PATCH, version.diff("1.2.4-alpha.5+sha32iddfu987"));
        assertEquals(VersionDiff.SUFFIX, version.diff("1.2.3-alpha.4+sha32iddfu987"));
        assertEquals(VersionDiff.SUFFIX, version.diff("1.2.3-beta.5+sha32iddfu987"));
        assertEquals(VersionDiff.BUILD, version.diff("1.2.3-beta.4+sha32iddfu987"));
        assertEquals(VersionDiff.BUILD, version.diff("1.2.3-beta.4+sha899-d8g79f87"));
    }
    
    
    /**
     * Test
     */
    @Test
    public void compareTo_test() {
        // GIVEN
        SemanticVersion[] array = new SemanticVersion[] {new SemanticVersion(V1_2_3), new SemanticVersion("1.2.3-rc3"), new SemanticVersion("1.2.3-rc2"),
                                                         new SemanticVersion("1.2.3-rc1"), new SemanticVersion("1.2.2"), new SemanticVersion("1.2.2-rc2"),
                                                         new SemanticVersion("1.2.2-rc1"), new SemanticVersion("1.2.0") };
        int len = array.length;
        List<SemanticVersion> list = new ArrayList<SemanticVersion>(len);
        Collections.addAll(list, array);
    
        // WHEN
        Collections.sort(list);
    
        // THEN
        for (int i = 0; i < list.size(); i++) {
            assertEquals(array[len - 1 - i], list.get(i));
        }
    }

    
    /**
     * Test
     */
    @Test
    public void compareTo_without_path_or_minor() {
        assertTrue(new SemanticVersion(V1_2_3,VersionType.LOOSE).isGreaterThan("1.2"));
        assertTrue(new SemanticVersion("1.3",VersionType.LOOSE).isGreaterThan(V1_2_3));
        assertTrue(new SemanticVersion(V1_2_3,VersionType.LOOSE).isGreaterThan("1"));
        assertTrue(new SemanticVersion("2",VersionType.LOOSE).isGreaterThan(V1_2_3));
    }

    
    /**
     * Test
     */
    @Test
    public void getValue_returns_the_original_value_trimmed_and_with_the_same_case() {
        SemanticVersion version = new SemanticVersion("  1.2.3-BETA.11+sHa.0nSFGKjkjsdf  ");
        assertEquals("1.2.3-BETA.11+sHa.0nSFGKjkjsdf", version.toString());
    }

    
    /**
     * Test
     */
    @Test
    public void compareTo_with_buildNumber() {
        SemanticVersion v3 = new SemanticVersion("1.24.1-rc3+903423.234");
        assertEquals(1, v3.getMajor());
        assertEquals(24, v3.getMinor());
        assertEquals(1, v3.getPatch());
        assertEquals("[rc3]", "" + v3.getSuffixTokens());
        assertEquals("903423.234", v3.getBuild());

        
        SemanticVersion v4 = new SemanticVersion("1.24.1-rc3+903423.235");
        assertEquals(1, v4.getMajor());
        assertEquals(24, v4.getMinor());
        assertEquals(1, v4.getPatch());
        assertEquals("[rc3]", "" + v4.getSuffixTokens());
        assertEquals("903423.235", v4.getBuild());

        assertEquals(0, v3.compareTo(v4));
    }

    
    /**
     * Test
     */
    @Test
    public void isStable_test() {
        assertTrue(new SemanticVersion("1.2.3+sHa.0nSFGKjkjsdf").isStable());
        assertTrue(new SemanticVersion(V1_2_3).isStable());
        assertFalse(new SemanticVersion("1.2.3-BETA.11+sHa.0nSFGKjkjsdf").isStable());
        assertFalse(new SemanticVersion("0.1.2+sHa.0nSFGKjkjsdf").isStable());
        assertFalse(new SemanticVersion("0.1.2").isStable());
    }
    
    
    
    /**
     * Test sort version
     */
    @Test
    public void testSortVersion() {
        List<String> list = getVersionList(true);
        Collections.sort(list, Collections.reverseOrder());
        final String orderedList = list.toString().replace("0.4.5, 0.4.2, 0.4.11-b, 0.4.11,", "0.4.11, 0.4.11-b, 0.4.5, 0.4.2,");
        list.addAll(VERSION_LIST_INVALID);
        Collections.shuffle(list);
        
        List<String> invalidVersionList = new ArrayList<String>();
        List<SemanticVersion> versionList = SemanticVersion.convert(list, invalidVersionList);

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
        List<SemanticVersion> versionList = SemanticVersion.convert(list, invalidVersionList);
        Collections.shuffle(versionList);

        final String firstVerison = "[2.2.1]";
        assertEquals("[]", SemanticVersion.filter(versionList, 0, 0, 0, 0).toString());
        assertEquals(firstVerison, SemanticVersion.filter(versionList, 0, 1, 0, 0).toString());
        assertEquals(firstVerison, SemanticVersion.filter(versionList, 0, 1, 1, 0).toString());
        assertEquals(firstVerison, SemanticVersion.filter(versionList, 0, 1, 1, 1).toString());
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
                     + "0.7.3, 0.7.2, 0.7.1, 0.7.0, 0.6.3, 0.6.2, 0.6.1, 0.6.0, 0.5.33, 0.5.2, 0.5.1, 0.5.0, 0.4.11, 0.4.11-b, 0.4.5, 0.4.2, 0.4.1, 0.4.0, "
                     + "0.1.5, 0.1.4, 0.1.3, 0.1.2, 0.1.1, 0.1.0, 0.0.6, 0.0.5, 0.0.4, 0.0.3, 0.0.2]", SemanticVersion.invertFilter(versionList, 2, 2, 2).toString());
        
        Collections.sort(VERSION_LIST_INVALID);
        assertEquals(VERSION_LIST_INVALID.toString(), invalidVersionList.toString());
    }
    
    
    /**
     * Test filter version
     */
    @Test
    public void testFilterVersionsNewRelease() {
        List<String> list = getVersionList(true);
        list.addAll(parseVersionList("3.0.0, 3.0.1, 3.0.2, 3.0.3"));
        list.addAll(VERSION_LIST_INVALID);
        Collections.shuffle(list);
        
        List<SemanticVersion> versionList = SemanticVersion.convert(list, new ArrayList<String>());
        Collections.shuffle(versionList);
        assertEquals("[3.0.3, 3.0.2, 2.2.1, 2.2.0]", SemanticVersion.filter(versionList, 0, 2, 2, 2, 1, 2).toString());
        
        //
        
        list.addAll(parseVersionList("3.1.0, 3.1.1, 3.1.2"));
        list.addAll(VERSION_LIST_INVALID);
        Collections.shuffle(list);
        
        versionList = SemanticVersion.convert(list, new ArrayList<String>());
        Collections.shuffle(versionList);
        assertEquals("[3.1.2, 3.1.1, 3.0.3, 3.0.2, 2.2.1, 2.2.0]", SemanticVersion.filter(versionList, 0, 2, 2, 2, 1, 2).toString());
    }

    
    /**
     * Test filter version
     */
    @Test
    public void testFilterMaxVersions() {
        List<String> list = getVersionList(true);
        list.addAll(VERSION_LIST_INVALID);
        Collections.shuffle(list);
        
        List<String> invalidVersionList = new ArrayList<String>();
        List<SemanticVersion> versionList = SemanticVersion.convert(list, invalidVersionList);
        Collections.shuffle(versionList);

        // same test cases
        assertEquals("[2.2.1]", SemanticVersion.filter(versionList, 0, 1, 0, 0).toString());
        assertEquals("[2.2.1]", SemanticVersion.filter(versionList, 1, 1, 0, 0).toString());
        
        assertEquals("[2.2.1, 2.1.2, 1.3.4, 1.2.4]", SemanticVersion.filter(versionList, 0, 2, 2, 0).toString());
        assertEquals("[2.2.1]", SemanticVersion.filter(versionList, 1, 2, 2, 0).toString());
        
        assertEquals("[2.2.1]", SemanticVersion.filter(versionList, 1, 2, 2, 1).toString());
        assertEquals("[2.2.1, 2.2.0]", SemanticVersion.filter(versionList, 1, 2, 2, 2).toString());

        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1]", SemanticVersion.filter(versionList, 2, 2, 2, 2).toString());
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1, 1.3.4, 1.3.3]", SemanticVersion.filter(versionList, 3, 2, 2, 2).toString());
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1, 1.3.4, 1.3.3, 1.2.4, 1.2.3]", SemanticVersion.filter(versionList, 4, 2, 2, 2).toString());
        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1, 1.3.4, 1.3.3, 1.2.4, 1.2.3]", SemanticVersion.filter(versionList, 4, 2, 2, 2, 2, 2).toString());

        assertEquals("[2.2.1, 2.2.0, 2.1.2, 2.1.1, 1.3.4, 1.3.3]", SemanticVersion.filter(versionList, 3, 2, 2, 2).toString());
        assertEquals("[2.1.0, 2.0.2, 2.0.1, 2.0.0, 1.3.2, 1.3.1, 1.3.0, 1.2.2, 1.2.1, 1.2.0, 1.1.4, 1.1.3, 1.1.2, 1.1.1, 1.1.0, 1.0.3, 1.0.2, 1.0.1, 1.0.0, "
                     + "0.7.3, 0.7.2, 0.7.1, 0.7.0, 0.6.3, 0.6.2, 0.6.1, 0.6.0, 0.5.33, 0.5.2, 0.5.1, 0.5.0, 0.4.11, 0.4.11-b, 0.4.5, 0.4.2, 0.4.1, 0.4.0, "
                     + "0.1.5, 0.1.4, 0.1.3, 0.1.2, 0.1.1, 0.1.0, 0.0.6, 0.0.5, 0.0.4, 0.0.3, 0.0.2]", SemanticVersion.invertFilter(versionList, 2, 2, 2).toString());
        
        Collections.sort(VERSION_LIST_INVALID);
        assertEquals(VERSION_LIST_INVALID.toString(), invalidVersionList.toString());
    }

    
    /**
     * Test
     * 
     * @param version rge version
     * @param value the value
     * @param major the major number
     * @param minor the minor number
     * @param patch the major patch
     * @param suffixTokens the suffix tokens
     * @param build the build
     */
    private static void assertIsVersion(SemanticVersion version, String value, Integer major, Integer minor, Integer patch, String[] suffixTokens, String build) {
        assertEquals(value, version.toString());
        assertEquals(major, version.getMajor());
        assertEquals(minor, version.getMinor());
        assertEquals(patch, version.getPatch());
        assertEquals(suffixTokens.length, version.getSuffixTokens().size());
        for (int i = 0; i < suffixTokens.length; i++) {
            assertEquals(suffixTokens[i], version.getSuffixTokens().get(i));
        }
        assertEquals(build, version.getBuild());
    }


    /**
     * Test that filter does not mutate the input list
     */
    @Test
    public void testFilterDoesNotMutateInputList() {
        List<SemanticVersion> input = new ArrayList<>();
        input.add(new SemanticVersion("1.0.0"));
        input.add(new SemanticVersion("2.0.0"));
        input.add(new SemanticVersion("0.5.0"));

        List<SemanticVersion> copy = new ArrayList<>(input);
        SemanticVersion.filter(input, 0, 1, 1, 1);

        assertEquals(copy.size(), input.size());
        for (int i = 0; i < copy.size(); i++) {
            assertEquals(copy.get(i).toString(), input.get(i).toString());
        }
    }


    /**
     * Test isGreaterThan with equal major versions
     */
    @Test
    public void testIsGreaterThanEqualMajor() {
        SemanticVersion v1 = new SemanticVersion("1.0.0");
        SemanticVersion v2 = new SemanticVersion("1.0.0");
        assertFalse(v1.isGreaterThan(v2));
        assertFalse(v2.isGreaterThan(v1));

        SemanticVersion v3 = new SemanticVersion("2.0.0");
        assertTrue(v3.isGreaterThan(v1));
        assertFalse(v1.isGreaterThan(v3));
    }


    /**
     * Test that SemanticVersion.filter and .invertFilter produce the exact same results
     * as the cb-version-filter CLI tool. Each test case was validated against the CLI output.
     *
     * <p>Note: Java's filter(list, majorMinorMax, major, minor, patch) passes minor/patch
     * as previousMajorMinor/PatchThreshold too. When comparing with the CLI, explicit
     * --previousMajorMinorThreshold and --previousMajorPatchThreshold must be set to match.
     */
    @Test
    public void testFilterMatchesCbVersionFilterCli() {
        List<SemanticVersion> versionList = createCliVersionList();

        // cb-version-filter --majorThreshold 1 --minorThreshold 1 --patchThreshold 1
        //   --previousMajorMinorThreshold 1 --previousMajorPatchThreshold 1
        assertFilterResult(versionList, 0, 1, 1, 1,
                "2.2.1");

        // cb-version-filter --majorThreshold 1 --minorThreshold 3 --patchThreshold 3
        //   --previousMajorMinorThreshold 3 --previousMajorPatchThreshold 3
        assertFilterResult(versionList, 0, 1, 3, 3,
                "2.2.1, 2.2.0, 2.1.2, 2.1.1, 2.1.0, 2.0.2, 2.0.1, 2.0.0");

        // cb-version-filter --majorThreshold 2 --minorThreshold 1 --patchThreshold 1
        //   --previousMajorMinorThreshold 1 --previousMajorPatchThreshold 1
        assertFilterResult(versionList, 0, 2, 1, 1,
                "2.2.1, 1.3.4");

        // cb-version-filter --majorThreshold 2 --minorThreshold 2 --patchThreshold 2
        //   --previousMajorMinorThreshold 2 --previousMajorPatchThreshold 2
        assertFilterResult(versionList, 0, 2, 2, 2,
                "2.2.1, 2.2.0, 2.1.2, 2.1.1, 1.3.4, 1.3.3, 1.2.4, 1.2.3");

        // cb-version-filter --majorThreshold 2 --minorThreshold 1 --patchThreshold 2
        //   --previousMajorMinorThreshold 1 --previousMajorPatchThreshold 1
        assertFilterResult(versionList, 0, 2, 1, 2, 1, 1,
                "2.2.1, 2.2.0, 1.3.4");

        // cb-version-filter --majorThreshold 2 --minorThreshold 1 --patchThreshold 2
        //   --previousMajorMinorThreshold 2 --previousMajorPatchThreshold 2
        assertFilterResult(versionList, 0, 2, 1, 2, 2, 2,
                "2.2.1, 2.2.0, 1.3.4, 1.3.3, 1.2.4, 1.2.3");

        // cb-version-filter --majorMinorMax 1 --majorThreshold 2 --minorThreshold 2 --patchThreshold 2
        //   --previousMajorMinorThreshold 2 --previousMajorPatchThreshold 2
        assertFilterResult(versionList, 1, 2, 2, 2,
                "2.2.1, 2.2.0");

        // cb-version-filter --majorMinorMax 2 --majorThreshold 2 --minorThreshold 2 --patchThreshold 2
        //   --previousMajorMinorThreshold 2 --previousMajorPatchThreshold 2
        assertFilterResult(versionList, 2, 2, 2, 2,
                "2.2.1, 2.2.0, 2.1.2, 2.1.1");

        // cb-version-filter --majorMinorMax 3 --majorThreshold 2 --minorThreshold 2 --patchThreshold 2
        //   --previousMajorMinorThreshold 2 --previousMajorPatchThreshold 2
        assertFilterResult(versionList, 3, 2, 2, 2,
                "2.2.1, 2.2.0, 2.1.2, 2.1.1, 1.3.4, 1.3.3");
    }


    /**
     * Test that SemanticVersion.invertFilter produces the exact same results
     * as cb-version-filter --invertFilter. Each test case was validated against the CLI output.
     */
    @Test
    public void testInvertFilterMatchesCbVersionFilterCli() {
        List<SemanticVersion> versionList = createCliVersionList();

        // cb-version-filter --majorThreshold 2 --minorThreshold 2 --patchThreshold 2
        //   --previousMajorMinorThreshold 2 --previousMajorPatchThreshold 2 --invertFilter
        assertEquals("[2.1.0, 2.0.2, 2.0.1, 2.0.0, "
                        + "1.3.2, 1.3.1, 1.3.0, 1.2.2, 1.2.1, 1.2.0, "
                        + "1.1.4, 1.1.3, 1.1.2, 1.1.1, 1.1.0, 1.0.3, 1.0.2, 1.0.1, 1.0.0, "
                        + "0.7.3, 0.7.2, 0.7.1, 0.7.0, 0.6.3, 0.6.2, 0.6.1, 0.6.0, "
                        + "0.5.33, 0.5.2, 0.5.1, 0.5.0, 0.4.11, 0.4.5, 0.4.2, 0.4.1, 0.4.0, "
                        + "0.1.5, 0.1.4, 0.1.3, 0.1.2, 0.1.1, 0.1.0, 0.0.6, 0.0.5, 0.0.4, 0.0.3, 0.0.2]",
                SemanticVersion.invertFilter(versionList, 2, 2, 2).toString());

        // cb-version-filter --majorThreshold 2 --minorThreshold 1 --patchThreshold 2
        //   --previousMajorMinorThreshold 2 --previousMajorPatchThreshold 2 --invertFilter
        assertEquals("[2.1.2, 2.1.1, 2.1.0, 2.0.2, 2.0.1, 2.0.0, "
                        + "1.3.2, 1.3.1, 1.3.0, 1.2.2, 1.2.1, 1.2.0, "
                        + "1.1.4, 1.1.3, 1.1.2, 1.1.1, 1.1.0, 1.0.3, 1.0.2, 1.0.1, 1.0.0, "
                        + "0.7.3, 0.7.2, 0.7.1, 0.7.0, 0.6.3, 0.6.2, 0.6.1, 0.6.0, "
                        + "0.5.33, 0.5.2, 0.5.1, 0.5.0, 0.4.11, 0.4.5, 0.4.2, 0.4.1, 0.4.0, "
                        + "0.1.5, 0.1.4, 0.1.3, 0.1.2, 0.1.1, 0.1.0, 0.0.6, 0.0.5, 0.0.4, 0.0.3, 0.0.2]",
                SemanticVersion.invertFilter(versionList, 2, 1, 2, 2, 2).toString());
    }


    /**
     * Test filter and invertFilter by executing cb-version-filter on the command line
     * and comparing the output. Skipped if cb-version-filter is not available.
     */
    @Test
    public void testFilterAgainstLiveCbVersionFilter() {
        if (!isCbVersionFilterAvailable()) {
            LOG.info("cb-version-filter not available on PATH, skipping live CLI comparison test.");
            return;
        }

        String versions = createCliVersionInput();
        List<SemanticVersion> versionList = createCliVersionList();

        // filter: --majorThreshold 2 --minorThreshold 2 --patchThreshold 2
        //   --previousMajorMinorThreshold 2 --previousMajorPatchThreshold 2
        String cliResult = runCbVersionFilter(versions,
                "--majorThreshold", T2, "--minorThreshold", T2, "--patchThreshold", T2,
                "--previousMajorMinorThreshold", T2, "--previousMajorPatchThreshold", T2);
        String javaResult = toSpaceSeparated(SemanticVersion.filter(versionList, 0, 2, 2, 2));
        assertEquals(cliResult, javaResult);

        // filter: --majorThreshold 2 --minorThreshold 1 --patchThreshold 2
        //   --previousMajorMinorThreshold 1 --previousMajorPatchThreshold 1
        cliResult = runCbVersionFilter(versions,
                "--majorThreshold", T2, "--minorThreshold", T1, "--patchThreshold", T2,
                "--previousMajorMinorThreshold", T1, "--previousMajorPatchThreshold", T1);
        javaResult = toSpaceSeparated(SemanticVersion.filter(versionList, 0, 2, 1, 2, 1, 1));
        assertEquals(cliResult, javaResult);

        // filter: --majorMinorMax 3 --majorThreshold 2 --minorThreshold 2 --patchThreshold 2
        //   --previousMajorMinorThreshold 2 --previousMajorPatchThreshold 2
        cliResult = runCbVersionFilter(versions,
                "--majorMinorMax", T3, "--majorThreshold", T2, "--minorThreshold", T2, "--patchThreshold", T2,
                "--previousMajorMinorThreshold", T2, "--previousMajorPatchThreshold", T2);
        javaResult = toSpaceSeparated(SemanticVersion.filter(versionList, 3, 2, 2, 2));
        assertEquals(cliResult, javaResult);

        // invertFilter: --majorThreshold 2 --minorThreshold 2 --patchThreshold 2
        //   --previousMajorMinorThreshold 2 --previousMajorPatchThreshold 2 --invertFilter
        cliResult = runCbVersionFilter(versions,
                "--majorThreshold", T2, "--minorThreshold", T2, "--patchThreshold", T2,
                "--previousMajorMinorThreshold", T2, "--previousMajorPatchThreshold", T2, "--invertFilter");
        javaResult = toSpaceSeparated(SemanticVersion.invertFilter(versionList, 2, 2, 2));
        assertEquals(cliResult, javaResult);

        // filter: --majorThreshold 1 --minorThreshold 1 --patchThreshold 1
        //   --previousMajorMinorThreshold 1 --previousMajorPatchThreshold 1
        cliResult = runCbVersionFilter(versions,
                "--majorThreshold", T1, "--minorThreshold", T1, "--patchThreshold", T1,
                "--previousMajorMinorThreshold", T1, "--previousMajorPatchThreshold", T1);
        javaResult = toSpaceSeparated(SemanticVersion.filter(versionList, 0, 1, 1, 1));
        assertEquals(cliResult, javaResult);
    }


    /**
     * Check if cb-version-filter is available on the PATH
     *
     * @return true if available
     */
    private boolean isCbVersionFilterAvailable() {
        try {
            Process process = new ProcessBuilder("cb-version-filter", "--help")
                    .redirectErrorStream(true).start();
            process.getInputStream().readAllBytes();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Create the space-separated version input string for the CLI
     *
     * @return the version input
     */
    private String createCliVersionInput() {
        return "2.2.1 2.2.0 2.1.2 2.1.1 2.1.0 2.0.2 2.0.1 2.0.0 "
                + "1.3.4 1.3.3 1.3.2 1.3.1 1.3.0 1.2.4 1.2.3 1.2.2 1.2.1 1.2.0 "
                + "1.1.4 1.1.3 1.1.2 1.1.1 1.1.0 1.0.3 1.0.2 1.0.1 1.0.0 "
                + "0.7.3 0.7.2 0.7.1 0.7.0 0.6.3 0.6.2 0.6.1 0.6.0 "
                + "0.5.33 0.5.2 0.5.1 0.5.0 0.4.11 0.4.5 0.4.2 0.4.1 0.4.0 "
                + "0.1.5 0.1.4 0.1.3 0.1.2 0.1.1 0.1.0 0.0.6 0.0.5 0.0.4 0.0.3 0.0.2";
    }


    /**
     * Run cb-version-filter with the given arguments and return the output as a space-separated string
     *
     * @param input the version input to pipe
     * @param args the CLI arguments
     * @return the output versions as space-separated string
     * @throws RuntimeException if cb-version-filter execution fails
     */
    private String runCbVersionFilter(String input, String... args) {
        try {
            List<String> command = new ArrayList<>();
            command.add("bash");
            command.add("-c");

            StringBuilder cmd = new StringBuilder("echo '");
            cmd.append(input).append("' | cb-version-filter");
            for (String arg : args) {
                cmd.append(' ').append(arg);
            }
            command.add(cmd.toString());

            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines()
                        .filter(line -> !line.trim().isEmpty())
                        .collect(Collectors.joining(" "));
            }
            process.waitFor();
            return output.trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to run cb-version-filter: " + e.getMessage(), e);
        }
    }


    /**
     * Convert a version list to space-separated string (matching CLI output format)
     *
     * @param versions the version list
     * @return space-separated version string
     */
    private String toSpaceSeparated(List<SemanticVersion> versions) {
        return versions.stream().map(SemanticVersion::toString).collect(Collectors.joining(" "));
    }


    /**
     * Create the version list used for CLI comparison tests
     *
     * @return the version list
     */
    private List<SemanticVersion> createCliVersionList() {
        return SemanticVersion.convert(Arrays.asList(
                "2.2.1", "2.2.0", "2.1.2", "2.1.1", "2.1.0", "2.0.2", "2.0.1", "2.0.0",
                "1.3.4", "1.3.3", "1.3.2", "1.3.1", "1.3.0", "1.2.4", "1.2.3", "1.2.2", "1.2.1", "1.2.0",
                "1.1.4", "1.1.3", "1.1.2", "1.1.1", "1.1.0", "1.0.3", "1.0.2", "1.0.1", "1.0.0",
                "0.7.3", "0.7.2", "0.7.1", "0.7.0", "0.6.3", "0.6.2", "0.6.1", "0.6.0",
                "0.5.33", "0.5.2", "0.5.1", "0.5.0", "0.4.11", "0.4.5", "0.4.2", "0.4.1", "0.4.0",
                "0.1.5", "0.1.4", "0.1.3", "0.1.2", "0.1.1", "0.1.0", "0.0.6", "0.0.5", "0.0.4", "0.0.3", "0.0.2"),
                null);
    }


    /**
     * Assert filter result with 4-param filter (previousMajor thresholds default to minor/patch)
     *
     * @param versionList the version list
     * @param majorMinorMax the major minor max
     * @param majorThreshold the major threshold
     * @param minorThreshold the minor threshold
     * @param patchThreshold the patch threshold
     * @param expected the expected comma-separated version string
     */
    private void assertFilterResult(List<SemanticVersion> versionList, int majorMinorMax,
                                    int majorThreshold, int minorThreshold, int patchThreshold, String expected) {
        assertEquals("[" + expected + "]",
                SemanticVersion.filter(versionList, majorMinorMax, majorThreshold, minorThreshold, patchThreshold).toString());
    }


    /**
     * Assert filter result with 6-param filter (explicit previousMajor thresholds)
     *
     * @param versionList the version list
     * @param majorMinorMax the major minor max
     * @param majorThreshold the major threshold
     * @param minorThreshold the minor threshold
     * @param patchThreshold the patch threshold
     * @param prevMinor the previous major minor threshold
     * @param prevPatch the previous major patch threshold
     * @param expected the expected comma-separated version string
     */
    private void assertFilterResult(List<SemanticVersion> versionList, int majorMinorMax,
                                    int majorThreshold, int minorThreshold, int patchThreshold,
                                    int prevMinor, int prevPatch, String expected) {
        assertEquals("[" + expected + "]",
                SemanticVersion.filter(versionList, majorMinorMax, majorThreshold, minorThreshold, patchThreshold, prevMinor, prevPatch).toString());
    }
}