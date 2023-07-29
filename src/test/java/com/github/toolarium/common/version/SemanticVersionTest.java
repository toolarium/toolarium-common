/*
 * Version.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Test the {@link SemanticVersion}.
 *  
 * @author patrick
 */
public class SemanticVersionTest {
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
        
    
}