/*
 * SemanticVersionNpmRangeTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.version;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


/**
 * Test npm semantic version range matching via {@link SemanticVersion#satisfies(String)}.
 *
 * @author patrick
 */
public class SemanticVersionNpmRangeTest {
    private static final String VRANGE_1_2_3 = "~1.2.3";
    private static final String V1_2_2 = "1.2.2";
    private static final String V1_2_7 = "1.2.7";
    private static final String V1_2_3 = "1.2.3";
    private static final String V1_2_4 = "1.2.4";
    private static final String V1_9_9 = "1.9.9";
    private static final String V2_0_0 = "2.0.0";
    private static final String V2_0_1 = "2.0.1";
    private static final String V3_0_0 = "3.0.0";
    private static final String V3_1_5 = "3.1.5";
    private static final String V3_1_X = "3.1.x";


    /**
     * Test npm semantic version range.
     *
     * @param version the version string
     * @param range the range expression
     * @param expected the expected result
     */
    @ParameterizedTest(name = "[{index}] {0} ~ \"{1}\"")
    @MethodSource("getParameters")
    public void test(String version, String range, boolean expected) {
        assertEquals(expected, new SemanticVersion(version, VersionType.NPM).satisfies(range), version + " ~ " + range);
    }


    /**
     * Get the parameters
     *
     * @return the test parameters
     */
    private static Object[][] getParameters() {
        return new Object[][] {
            {V1_2_3, V1_2_3, true }, {V1_2_4, V1_2_3, false },

            // Minor versions:
            {V1_2_3, "1.2", true }, {V1_2_4, "1.3", false },

            // Major versions:
            {V1_2_3, "1", true },
            {V1_2_4, "2", false },

            // Hyphen ranges:
            {"1.2.4-beta+exp.sha.5114f85", "1.2.3 - 2.3.4", false }, // CHECKSTYLE IGNORE THIS LINE
            {V1_2_4, "1.2.3 - 2.3.4", true }, {V1_2_3, "1.2.3 - 2.3.4", true },
            {"2.3.4", "1.2.3 - 2.3.4", true }, {"2.3.0-alpha", "1.2.3 - 2.3.0-beta", true },
            {"2.3.4", "1.2.3 - 2.3", true }, {"2.3.4", "1.2.3 - 2", true },
            {"4.4", "3.X - 4.X", true }, {"1.0.0", "1.2.3 - 2.3.4", false },
            {V3_0_0, "1.2.3 - 2.3.4", false }, {"2.4.3", "1.2.3 - 2.3", false },
            {"2.3.0-rc1", "1.2.3 - 2.3.0-beta", false }, {V3_0_0, "1.2.3 - 2", false },

            // Wildcard ranges:
            {V3_1_5, "", true }, {V3_1_5, "*", true },
            {"0.0.0", "*", true }, {"1.0.0-beta", "*", false }, {"1.0.0-beta", "", false }, {"1.0.0-beta", "x", false },
            {"3.1.5-beta", V3_1_X, false }, {"3.1.5-beta+exp.sha.5114f85", V3_1_X, false }, {"3.1.5+exp.sha.5114f85", V3_1_X, true },
            {V3_1_5, V3_1_X, true }, {V3_1_5, "3.1.X", true }, {V3_1_5, "3.x", true },
            {V3_1_5, "3.*", true }, {V3_1_5, "3.1", true }, {V3_1_5, "3", true },
            {"3.2.5", V3_1_X, false }, {"3.0.5", V3_1_X, false }, {"4.0.0", "3.x", false },
            {V2_0_0, "3.x", false }, {"3.2.5", "3.1", false }, {"3.0.5", "3.1", false },
            {"4.0.0", "3", false }, {V2_0_0, "3", false },

            // Tilde ranges:
            {"1.2.4-beta", VRANGE_1_2_3, false }, {"1.2.4-beta+exp.sha.5114f85", VRANGE_1_2_3, false },
            {V1_2_3, VRANGE_1_2_3, true }, {V1_2_7, VRANGE_1_2_3, true },
            {V1_2_2, "~1.2", true }, {"1.2.0", "~1.2", true }, {"1.3.0", "~1", true }, {"1.0.0", "~1", true },
            {V1_2_3, "~1.2.3-beta.2", true }, {"1.2.3-beta.4", "~1.2.3-beta.2", true }, {V1_2_4, "~1.2.3-beta.2", true },
            {"1.3.0", VRANGE_1_2_3, false }, {V1_2_2, VRANGE_1_2_3, false }, {"1.1.0", "~1.2", false }, {"1.3.0", "~1.2", false },
            {V2_0_0, "~1", false }, {"0.0.0", "~1", false }, {"1.2.3-beta.1", "~1.2.3-beta.2", false },

            // Caret ranges:
            {V1_2_3, "^1.2.3", true }, {V1_2_4, "^1.2.3", true }, {"1.3.0", "^1.2.3", true },
            {"0.2.3", "^0.2.3", true }, {"0.2.4", "^0.2.3", true }, {"0.0.3", "^0.0.3", true },
            {"0.0.3+exp.sha.5114f85", "^0.0.3", true }, {"0.0.3", "^0.0.3-beta", true },
            {"0.0.3-pr.2", "^0.0.3-beta", true }, {V1_2_2, "^1.2.3", false }, {V2_0_0, "^1.2.3", false },
            {"0.2.2", "^0.2.3", false }, {"0.3.0", "^0.2.3", false }, {"0.0.4", "^0.0.3", false },
            {"0.0.3-alpha", "^0.0.3-beta", false }, {"0.0.4", "^0.0.3-beta", false },

            // Comparators:
            {V2_0_0, "=2.0.0", true }, {V2_0_0, "=2.0", true }, {V2_0_1, "=2.0", true },
            {V2_0_0, "=2", true }, {V2_0_1, "=2", true }, {V2_0_1, "=2.0.0", false },
            {V1_9_9, "=2.0.0", false }, {V1_9_9, "=2.0", false }, {V1_9_9, "=2", false },

            {V2_0_1, ">2.0.0", true }, {V3_0_0, ">2.0.0", true }, {V3_0_0, ">2.0", true },
            {V3_0_0, ">2", true }, {V2_0_0, ">2.0.0", false }, {V1_9_9, ">2.0.0", false },
            {V2_0_0, ">2.0", false }, {V1_9_9, ">2.0", false }, {V2_0_1, ">2", false },
            {V2_0_0, ">2", false }, {V1_9_9, ">2", false },

            {V1_9_9, "<2.0.0", true }, {V1_9_9, "<2.0", true }, {V1_9_9, "<2", true },
            {V2_0_0, "<2.0.0", false }, {V2_0_1, "<2.0.0", false }, {V3_0_0, "<2.0.0", false },
            {V2_0_0, "<2.0", false }, {V2_0_1, "<2.0", false }, {V3_0_0, "<2.0", false },
            {V2_0_0, "<2", false }, {V2_0_1, "<2", false }, {V3_0_0, "<2", false },

            {V2_0_0, ">=2.0.0", true }, {V2_0_1, ">=2.0.0", true }, {V3_0_0, ">=2.0.0", true },
            {V2_0_0, ">=2.0", true }, {V3_0_0, ">=2.0", true }, {V2_0_0, ">=2", true },
            {V2_0_1, ">=2", true }, {V3_0_0, ">=2", true }, {V1_9_9, ">=2.0.0", false },
            {V1_9_9, ">=2.0", false }, {V1_9_9, ">=2", false },

            {V1_9_9, "<=2.0.0", true }, {V2_0_0, "<=2.0.0", true }, {V1_9_9, "<=2.0", true },
            {V2_0_0, "<=2.0", true }, {V2_0_1, "<=2.0", true }, {V1_9_9, "<=2", true },
            {V2_0_0, "<=2", true }, {V2_0_1, "<=2", true },
            {V2_0_1, "<=2.0.0", false }, {V3_0_0, "<=2.0.0", false },
            {V3_0_0, "<=2.0", false }, {V3_0_0, "<=2", false },

            // AND ranges:
            {V2_0_1, ">2.0.0 <3.0.0", true }, {V2_0_1, ">2.0 <3.0", false },
            {"1.2.0", "1.2 <1.2.8", true }, {V1_2_7, "1.2 <1.2.8", true }, {"1.1.9", "1.2 <1.2.8", false }, {"1.2.9", "1.2 <1.2.8", false },

            // OR ranges:
            {V1_2_3, "1.2.3 || 1.2.4", true }, {V1_2_4, "1.2.3 || 1.2.4", true }, {"1.2.5", "1.2.3 || 1.2.4", false },

            // Complex ranges:
            {V1_2_2, ">1.2.1 <1.2.8 || >2.0.0", true }, {V1_2_7, ">1.2.1 <1.2.8 || >2.0.0", true }, {V2_0_1, ">1.2.1 <1.2.8 || >2.0.0", true },
            {"1.2.1", ">1.2.1 <1.2.8 || >2.0.0", false }, {V2_0_0, ">1.2.1 <1.2.8 || >2.0.0", false },

            {V1_2_2, ">1.2.1 <1.2.8 || >2.0.0 <3.0.0", true }, // CHECKSTYLE IGNORE THIS LINE
            {V1_2_7, ">1.2.1 <1.2.8 || >2.0.0 <3.0.0", true },
            {V2_0_1, ">1.2.1 <1.2.8 || >2.0.0 <3.0.0", true },
            {"2.5.0", ">1.2.1 <1.2.8 || >2.0.0 <3.0.0", true },
            {"1.2.1", ">1.2.1 <1.2.8 || >2.0.0 <3.0.0", false },
            {"1.2.8", ">1.2.1 <1.2.8 || >2.0.0 <3.0.0", false },
            {V2_0_0, ">1.2.1 <1.2.8 || >2.0.0 <3.0.0", false },
            {V3_0_0, ">1.2.1 <1.2.8 || >2.0.0 <3.0.0", false },

            {V1_2_2, "1.2.2 - 1.2.7 || 2.0.1 - 2.9.9", true }, // CHECKSTYLE IGNORE THIS LINE
            {V1_2_7, "1.2.2 - 1.2.7 || 2.0.1 - 2.9.9", true },
            {V2_0_1, "1.2.2 - 1.2.7 || 2.0.1 - 2.9.9", true },
            {"2.5.0", "1.2.2 - 1.2.7 || 2.0.1 - 2.9.9", true },
            {"1.2.1", "1.2.2 - 1.2.7 || 2.0.1 - 2.9.9", false },
            {"1.2.8", "1.2.2 - 1.2.7 || 2.0.1 - 2.9.9", false },
            {V2_0_0, "1.2.2 - 1.2.7 || 2.0.1 - 2.9.9", false },
            {V3_0_0, "1.2.2 - 1.2.7 || 2.0.1 - 2.9.9", false },

            {"1.2.0", "1.2 <1.2.8 || >2.0.0", true }, // CHECKSTYLE IGNORE THIS LINE
            {V1_2_7, "1.2 <1.2.8 || >2.0.0", true },
            {V1_2_7, "1.2 <1.2.8 || >2.0.0", true },
            {V2_0_1, "1.2 <1.2.8 || >2.0.0", true },
            {"1.1.0", "1.2 <1.2.8 || >2.0.0", false },
            {"1.2.9", "1.2 <1.2.8 || >2.0.0", false },
            {V2_0_0, "1.2 <1.2.8 || >2.0.0", false },
        };
    }
}
