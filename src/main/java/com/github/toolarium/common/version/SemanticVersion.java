/*
 * Version.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * The semantic version, see https://semver.org.
 * TODO: cargo support https://doc.rust-lang.org/cargo/reference/specifying-dependencies.html, https://guides.cocoapods.org/using/the-podfile.html
 * @author patrick
 */
public class SemanticVersion extends AbstractVersion<SemanticVersion> implements Comparable<SemanticVersion>, Serializable {
    private static final long serialVersionUID = 7953136231878785785L;
    private static final String DOT_STR = ".";
    private static final String MINUS_CHARACTER = "-";
    private static final String PLUS_CHARACTER = "+";
    private static final String BACKSLASH_ESCAPED = "\\";
    private static final String PLUS_SPLIT = BACKSLASH_ESCAPED + PLUS_CHARACTER;
    private static final String DOT_SPLIT = BACKSLASH_ESCAPED + DOT_STR;
    private static final String NPM_TILDE = "~";
    private static final String NPM_CARET = "^";
    private static final String NPM_GTE = ">=";
    private static final String NPM_LTE = "<=";
    private static final String NPM_GT = ">";
    private static final String NPM_LT = "<";
    private static final String NPM_EQ = "=";
    private static final String NPM_STAR = "*";
    private static final String NPM_X_WILDCARD = "x";
    private static final String DOT_ZERO = ".0";
    private static final String DOT_ZERO_ZERO = ".0.0";
    private static final Pattern NPM_OR_PATTERN = Pattern.compile("\\s*\\|\\|\\s*");
    private static final Pattern NPM_SPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern NPM_DOT_PATTERN = Pattern.compile(DOT_SPLIT);
    private static final Pattern NPM_MINUS_PATTERN = Pattern.compile(MINUS_CHARACTER);

    private final Integer major;
    private final Integer minor;
    private final Integer patch;
    private final List<String> suffixTokens;
    private String build;


    /**
     * Constructor for Version
     *
     * @param version the version to parse
     */
    public SemanticVersion(String version) {
        this(version, VersionType.STRICT);
    }
    
    
    /**
     * Constructor for Version
     *
     * @param version the version to parse
     * @param inputVersionType the version type
     * @throws IllegalArgumentException the exception which will be thrown of an invalid input
     */
    public SemanticVersion(String version, VersionType inputVersionType) throws IllegalArgumentException {
        super(version, inputVersionType);
        final String[] tokens = splitVersionIntoTokens(super.toString());
        final String[] mainTokens = splitMainTokens(tokens); // split into main tokens -> sets the build attribute as well
        this.major = parseInteger(getType(), mainTokens, 0, "no major version");
        this.minor = parseInteger(getType(), mainTokens, 1, "no minor version");
        this.patch = parseInteger(getType(), mainTokens, 2, "no patch version");
        this.suffixTokens = splitSuffixTokens(tokens); // split into suffixes -> sets the build attribute as well
        throwInvalidVersion(this.minor == null && getType() == VersionType.STRICT, "no minor version");
        throwInvalidVersion(this.patch == null && getType() == VersionType.STRICT, "no patch version");
    }

    
    /**
     * Get the major part of the version.
     * Example: for "1.2.3" = 1
     *
     * @return the major part of the version
     */
    public Integer getMajor() {
        return major;
    }
    
    
    /**
     * @see com.github.toolarium.common.version.AbstractVersion#getMajorNumber()
     */
    @Override
    protected int getMajorNumber() {
        if (major == null) {
            return -1;
        }
        
        return major.intValue();
    }


    /**
     * Get the minor part of the version.
     * Example: for "1.2.3" = 2
     *
     * @return the minor part of the version
     */
    public Integer getMinor() {
        return minor;
    }

    
    /**
     * @see com.github.toolarium.common.version.AbstractVersion#getMinorNumber()
     */
    @Override
    protected int getMinorNumber() {
        if (minor == null) {
            return -1;
        }
        
        return minor.intValue();
    }

    
    /**
     * Get the patch part of the version.
     * Example: for "1.2.3" = 3
     *
     * @return the patch part of the version
     */
    public Integer getPatch() {
        return patch;
    }


    /**
     * @see com.github.toolarium.common.version.AbstractVersion#getPatchNumber()
     */
    @Override
    protected int getPatchNumber() {
        if (patch == null) {
            return -1;
        }
        
        return patch.intValue();
    }

    
    /**
     * Get the suffix of the version.
     * Example: for "1.2.3-beta.4+sha98450956" = {"beta", "4"}
     *
     * @return the suffix of the version
     */
    public List<String> getSuffixTokens() {
        return suffixTokens;
    }

    
    /**
     * Get the build of the version.
     * Example: for "1.2.3-beta.4+sha98450956" = "sha98450956"
     *
     * @return the build of the version
     */
    public String getBuild() {
        return build;
    }

    
    /**
     * Checks if the version is greater than another version
     *
     * @param version the version to compare
     * @return true if the current version is greater than the provided version
     */
    public boolean isGreaterThan(String version) {
        return isGreaterThan(new SemanticVersion(version, getType()));
    }

    
    /**
     * Checks if the version is greater than another version
     *
     * @param version the version to compare
     * @return true if the current version is greater than the provided version
     */
    public boolean isGreaterThan(SemanticVersion version) {
        int thisMajor = 0;
        if (getMajor() != null) {
            thisMajor = getMajor();
        }
        int otherMajor = 0;
        if (version.getMajor() != null) {
            otherMajor = version.getMajor();
        }
        if (thisMajor > otherMajor) {
            return true;
        } else if (thisMajor < otherMajor) {
            return false;
        }
        if (getType() == VersionType.NPM && version.getMinor() == null) {
            return false;
        }
        
        int otherMinor = 0; 
        if (version.getMinor() != null) { 
            otherMinor = version.getMinor();
        }
        if (getMinor() != null && getMinor() > otherMinor) {
            return true;
        } else if (getMinor() != null && getMinor() < otherMinor) {
            return false;
        }
        if (getType() == VersionType.NPM && version.getPatch() == null) {
            return false;
        }

        int otherPatch = 0; 
        if (version.getPatch() != null) { 
            otherPatch = version.getPatch();
        }
        if (getPatch() != null && getPatch() > otherPatch) {
            return true;
        } else if (getPatch() != null && getPatch() < otherPatch) {
            return false;
        }
        
        // if one of the versions has no suffix, it's greater!
        final List<String> tokens1 = getSuffixTokens();
        final List<String> tokens2 = version.getSuffixTokens();
        if (tokens1.size() == 0 && tokens2.size() > 0) {
            return true;
        }
        if (tokens2.size() == 0 && tokens1.size() > 0) {
            return false;
        }

        for (int i = 0; i < tokens1.size() && i < tokens2.size(); i++) {
            int cmp;
            try { // trying to resolve the suffix part with an integer
                cmp = Integer.compare(Integer.parseInt(tokens1.get(i)), Integer.parseInt(tokens2.get(i)));
            } catch (NumberFormatException e) { // else, do a string comparison
                cmp = tokens1.get(i).compareToIgnoreCase(tokens2.get(i));
            }
            if (cmp < 0) {
                return false;
            } else if (cmp > 0) {
                return true;
            }
        }

        return tokens1.size() > tokens2.size(); // if one of the versions has some remaining suffixes, it's greater
    }

    
    /**
     * Checks if the version is greater than or equal to another version
     *
     * @param version the version to compare
     * @return true if the current version is greater than or equal to the provided version
     */
    public boolean isGreaterThanOrEqualTo(String version) {
        return isGreaterThanOrEqualTo(new SemanticVersion(version, getType()));
    }

    
    /**
     * Checks if the version is greater than or equal to another version
     *
     * @param version the version to compare
     * @return true if the current version is greater than or equal to the provided version
     */
    public boolean isGreaterThanOrEqualTo(SemanticVersion version) {
        return isGreaterThan(version) || isEquivalentTo(version);
    }

    
    /**
     * Checks if the version is lower than another version
     *
     * @param version the version to compare
     * @return true if the current version is lower than the provided version
     */
    public boolean isLowerThan(String version) {
        return isLowerThan(new SemanticVersion(version, getType()));
    }

    
    /**
     * Checks if the version is lower than another version
     *
     * @param version the version to compare
     * @return true if the current version is lower than the provided version
     */
    public boolean isLowerThan(SemanticVersion version) {
        return !isGreaterThan(version) && !isEquivalentTo(version);
    }

    
    /**
     * Checks if the version is lower than or equal to another version
     *
     * @param version the version to compare
     * @return true if the current version is lower than or equal to the provided version
     */
    public boolean isLowerThanOrEqualTo(String version) {
        return isLowerThanOrEqualTo(new SemanticVersion(version, getType()));
    }

    
    /**
     * Checks if the version is lower than or equal to another version
     *
     * @param version the version to compare
     * @return true if the current version is lower than or equal to the provided version
     */
    public boolean isLowerThanOrEqualTo(SemanticVersion version) {
        return !isGreaterThan(version);
    }

    
    /**
     * Checks if the version equals another version, without taking the build into account.
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version (build excluded)
     */
    public boolean isEquivalentTo(String version) {
        return isEquivalentTo(new SemanticVersion(version, getType()));
    }

    
    /**
     * Checks if the version equals another version, without taking the build into account.
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version (build excluded)
     */
    public boolean isEquivalentTo(SemanticVersion version) {
        SemanticVersion version1 = this;
        if (getBuild() != null) {
            version1 = new SemanticVersion(toString().replace(PLUS_CHARACTER + getBuild(), ""));
        }
        
        SemanticVersion version2 = version;
        if (version.getBuild() != null) {
            version2 = new SemanticVersion(version.toString().replace(PLUS_CHARACTER + version.getBuild(), ""));
        }
        
        return version1.isEqualTo(version2);
    }

    
    /**
     * Checks if the version equals another version
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version
     */
    public boolean isEqualTo(String version) {
        return isEqualTo(new SemanticVersion(version, getType()));
    }

    
    /**
     * Checks if the version equals another version
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version
     */
    public boolean isEqualTo(SemanticVersion version) {
        if (getType() == VersionType.NPM) {
            if (!Objects.equals(getMajor(), version.getMajor())) {
                return false;
            }
            if (version.getMinor() == null) {
                return true;
            }
            if (version.getPatch() == null) {
                return Objects.equals(getMinor(), version.getMinor());
            }
        }

        return equals(version);
    }

    
    /**
     * Checks whether this version satisfies an npm-style range expression.
     * Must be used with a version parsed as {@link VersionType#NPM}.
     *
     * <p>Supported range forms: {@code *}, wildcard ({@code 3.x}), exact ({@code 1.2.3}),
     * partial ({@code 1.2}), hyphen ({@code 1.2.3 - 2.3.4}), tilde ({@code ~1.2.3}),
     * caret ({@code ^1.2.3}), comparators ({@code >=1.0.0 &lt;2.0.0}), OR ({@code 1.x || 2.x}).
     *
     * @param rangeExpression the npm range expression; {@code null} returns {@code false}
     * @return true if the version satisfies the range
     * @throws IllegalArgumentException if the range expression contains a non-numeric version component
     */
    public boolean satisfies(String rangeExpression) {
        if (rangeExpression == null) {
            return false;
        }
        final String expr = rangeExpression.trim();
        if (expr.isEmpty() || NPM_STAR.equals(expr) || NPM_X_WILDCARD.equalsIgnoreCase(expr)) {
            return true;
        }
        try {
            for (String orPart : NPM_OR_PATTERN.split(expr)) {
                if (npmSatisfiesAndGroup(orPart.trim())) {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid npm range expression: " + rangeExpression, e);
        }
        return false;
    }


    /**
     * Determines if the current version is stable or not.
     * Stable version have a major version number strictly positive and no suffix tokens.
     *
     * @return true if the current version is stable
     */
    public boolean isStable() {
        return (getMajor() != null && getMajor() > 0) && (getSuffixTokens() == null || getSuffixTokens().size() == 0);
    }

    
    /**
     * Returns the greatest difference between 2 versions.
     * For example, if the current version is "1.2.3" and compared version is "1.3.0", the biggest difference
     * is the 'MINOR' number.
     *
     * @param version the version to compare
     * @return the greatest difference
     */
    public VersionDiff diff(String version) {
        return diff(new SemanticVersion(version, getType()));
    }

    
    /**
     * Returns the greatest difference between 2 versions.
     * For example, if the current version is "1.2.3" and compared version is "1.3.0", the biggest difference
     * is the 'MINOR' number.
     *
     * @param version the version to compare
     * @return the greatest difference
     */
    public VersionDiff diff(SemanticVersion version) {
        if (!Objects.equals(getMajor(), version.getMajor())) {
            return VersionDiff.MAJOR;
        }
        if (!Objects.equals(getMinor(), version.getMinor())) {
            return VersionDiff.MINOR;
        }
        if (!Objects.equals(getPatch(), version.getPatch())) {
            return VersionDiff.PATCH;
        }
        
        if (getSuffixTokens() == null && version.getSuffixTokens() == null) {
            // NOP
        } else if (getSuffixTokens() == null || version.getSuffixTokens() == null) {
            return VersionDiff.SUFFIX;
        } else if (getSuffixTokens().size() != version.getSuffixTokens().size()) {
            return VersionDiff.SUFFIX;
        } else {
            for (int i = 0; i < getSuffixTokens().size(); i++) {
                if (!getSuffixTokens().get(i).equals(version.getSuffixTokens().get(i))) {
                    return VersionDiff.SUFFIX;
                }
            }
        }

        if (!Objects.equals(getBuild(), version.getBuild())) {
            return VersionDiff.BUILD;
        }
        return VersionDiff.NONE;    
    }

    
    /**
     * Convert a string list into a version list
     *
     * @param list the list of versions to convert
     * @param invalidVersionList the list of invalid versions which will be filled up during the convertion
     * @return the version list
     */
    public static List<SemanticVersion> convert(List<String> list, List<String> invalidVersionList) {
        List<SemanticVersion> result = new ArrayList<SemanticVersion>();
        
        if (list != null && !list.isEmpty()) {
            for (String version : list) {
                try {
                    result.add(new SemanticVersion(version));
                } catch (IllegalArgumentException e) {
                    if (invalidVersionList != null) {
                        invalidVersionList.add(version);
                    }
                }
            }
            
            sort(result);
            
            if (invalidVersionList != null) {
                Collections.sort(invalidVersionList);
            }
        }
        
        return result;
    }

    
    /**
     * Get strict version of the current
     *
     * @return the strict version of the current
     */
    public SemanticVersion toStrict() {
        Integer minor = 0;
        if (getMinor() != null) {
            minor = getMinor();
        }

        Integer patch = 0;
        if (getPatch() != null) {
            patch = getPatch();
        }

        return create(VersionType.STRICT, getMajor(), minor, patch, getSuffixTokens(), getBuild());
    }

    
    /**
     * Get version with incremented major version
     *
     * @return the version
     */
    public SemanticVersion withIncrementMajor() {
        return withIncrementMajor(1);
    }

    
    /**
     * Get version with incremented major version
     *
     * @param increment the increment amount
     * @return the version
     */
    public SemanticVersion withIncrementMajor(int increment) {
        return withIncrement(increment, 0, 0);
    }

    
    /**
     * Get version with incremented minor version
     *
     * @return the version
     */
    public SemanticVersion withIncrementMinor() {
        return withIncrementMinor(1);
    }


    /**
     * Get version with incremented minor version
     *
     * @param increment the increment amount
     * @return the version
     */
    public SemanticVersion withIncrementMinor(int increment) {
        return withIncrement(0, increment, 0);
    }

    
    /**
     * Get version with incremented patch version
     *
     * @return the version
     */
    public SemanticVersion withIncrementPatch() {
        return withIncrementPatch(1);
    }

    
    /**
     * Get version with incremented patch version
     *
     * @param increment the increment amount
     * @return the version
     */
    public SemanticVersion withIncrementPatch(int increment) {
        return withIncrement(0, 0, increment);
    }

    
    /**
     * Get the version with cleared suffix
     *
     * @return the version
     */
    public SemanticVersion withClearedSuffix() {
        return with(getMajor(), getMinor(), getPatch(), false, true);
    }

    
    /**
     * Get the version with cleared build
     *
     * @return the version
     */
    public SemanticVersion withClearedBuild() {
        return with(getMajor(), getMinor(), getPatch(), true, false);
    }

    
    /**
     * Get the version with cleared suffix and build
     *
     * @return the version
     */

    public SemanticVersion withClearedSuffixAndBuild() {
        return with(getMajor(), getMinor(), getPatch(), false, false);
    }

    
    /**
     * Get the version with suffix
     *
     * @param suffix the suffix to set
     * @return the version
     */
    public SemanticVersion withSuffix(String suffix) {
        return with(getMajor(), getMinor(), getPatch(), Arrays.asList(suffix.split(DOT_SPLIT)), getBuild());
    }


    /**
     * Get the version with build
     *
     * @param build the build to set
     * @return the version
     */
    public SemanticVersion withBuild(String build) {
        return with(getMajor(), getMinor(), getPatch(), getSuffixTokens(), build);
    }

    
    /**
     * Get the next major version
     *
     * @return the next major version
     */
    public SemanticVersion nextMajor() {
        return with(this.major + 1, 0, 0, false, false);
    }

    
    /**
     * Get the next minor version
     *
     * @return the next minor version
     */
    public SemanticVersion nextMinor() {
        if (getMinor() != null) {
            return with(getMajor(), getMinor() + 1, 0, false, false);
        } else {
            return with(getMajor(), 1, 0, false, false);
        }
    }

    
    /**
     * Get the next patch version
     *
     * @return the next patch version
     */
    public SemanticVersion nextPatch() {
        if (getPatch() != null) {
            return with(getMajor(), getMinor(), getPatch() + 1, false, false);
        } else {
            return with(getMajor(), getMinor(), 1, false, false);
        }
    }

    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(SemanticVersion version) {
        if (isGreaterThan(version)) {
            return 1;
        } else if (isLowerThan(version)) {
            return -1;
        }
        
        return 0;
    }
    
    
    /**
     * Evaluate a single AND-group of comparators against this version.
     *
     * @param group the AND-group string
     * @return true if all comparators are satisfied
     */
    private boolean npmSatisfiesAndGroup(String group) {
        if (group.isEmpty() || NPM_STAR.equals(group) || NPM_X_WILDCARD.equalsIgnoreCase(group)) {
            return true;
        }
        final List<String[]> comparators = new ArrayList<String[]>();
        final int hyphenPos = group.indexOf(" - ");
        if (hyphenPos >= 0) {
            npmAddHyphenRange(comparators, group.substring(0, hyphenPos).trim(), group.substring(hyphenPos + 3).trim());
        } else {
            for (String token : NPM_SPACE_PATTERN.split(group)) {
                if (!token.isEmpty()) {
                    npmAddComparators(comparators, token);
                }
            }
        }
        if (!getSuffixTokens().isEmpty() && !npmHasMatchingPrerelease(comparators)) {
            return false;
        }
        for (String[] c : comparators) {
            if (!npmEvaluate(c[0], c[1])) {
                return false;
            }
        }
        return true;
    }


    /**
     * Add comparators for a hyphen range "lower - upper".
     *
     * @param result the comparator list to populate
     * @param lower the lower bound version string
     * @param upper the upper bound version string
     */
    private static void npmAddHyphenRange(List<String[]> result, String lower, String upper) {
        result.add(new String[]{NPM_GTE, npmFloorWildcard(lower)});
        final String[] parts = NPM_DOT_PATTERN.split(upper);
        int significant = 0;
        for (String p : parts) {
            if (NPM_STAR.equals(p) || NPM_X_WILDCARD.equalsIgnoreCase(p)) {
                break;
            }
            significant++;
        }
        if (significant >= 3) {
            result.add(new String[]{NPM_LTE, upper});
        } else if (significant == 2) {
            result.add(new String[]{NPM_LT, Integer.parseInt(parts[0]) + DOT_STR + (Integer.parseInt(parts[1]) + 1) + DOT_ZERO});
        } else {
            result.add(new String[]{NPM_LT, (Integer.parseInt(parts[0]) + 1) + DOT_ZERO_ZERO});
        }
    }


    /**
     * Expand a single token into comparators and add to the list.
     *
     * @param result the comparator list to populate
     * @param token the token to expand
     */
    private void npmAddComparators(List<String[]> result, String token) {
        if (NPM_STAR.equals(token) || NPM_X_WILDCARD.equalsIgnoreCase(token)) {
            result.add(new String[]{NPM_GTE, "0.0.0"});
        } else if (token.startsWith(NPM_TILDE)) {
            npmExpandTilde(result, token.substring(1));
        } else if (token.startsWith(NPM_CARET)) {
            npmExpandCaret(result, token.substring(1));
        } else if (token.startsWith(NPM_GTE) || token.startsWith(NPM_LTE)) {
            result.add(new String[]{token.substring(0, 2), token.substring(2)});
        } else if (token.startsWith(NPM_GT) || token.startsWith(NPM_LT) || token.startsWith(NPM_EQ)) {
            result.add(new String[]{token.substring(0, 1), token.substring(1)});
        } else {
            npmExpandWildcard(result, token);
        }
    }


    /**
     * Expand a tilde range into comparators.
     *
     * @param result the comparator list to populate
     * @param version the version string after the tilde
     */
    private static void npmExpandTilde(List<String[]> result, String version) {
        final String[] parts = NPM_DOT_PATTERN.split(NPM_MINUS_PATTERN.split(version)[0]);
        result.add(new String[]{NPM_GTE, version});
        if (parts.length >= 2) {
            result.add(new String[]{NPM_LT, Integer.parseInt(parts[0]) + DOT_STR + (Integer.parseInt(parts[1]) + 1) + DOT_ZERO});
        } else {
            result.add(new String[]{NPM_LT, (Integer.parseInt(parts[0]) + 1) + DOT_ZERO_ZERO});
        }
    }


    /**
     * Expand a caret range into comparators.
     *
     * @param result the comparator list to populate
     * @param version the version string after the caret
     */
    private static void npmExpandCaret(List<String[]> result, String version) {
        final String[] parts = NPM_DOT_PATTERN.split(NPM_MINUS_PATTERN.split(version)[0]);
        final int major = Integer.parseInt(parts[0]);
        int minor = 0;
        if (parts.length > 1) {
            minor = npmParseInt(parts[1]);
        }
        int patch = 0;
        if (parts.length > 2) {
            patch = npmParseInt(parts[2]);
        }
        result.add(new String[]{NPM_GTE, version});
        if (major > 0) {
            result.add(new String[]{NPM_LT, (major + 1) + DOT_ZERO_ZERO});
        } else if (minor > 0) {
            result.add(new String[]{NPM_LT, "0" + DOT_STR + (minor + 1) + DOT_ZERO});
        } else {
            result.add(new String[]{NPM_LT, "0.0." + (patch + 1)});
        }
    }


    /**
     * Expand a wildcard version range into comparators.
     *
     * @param result the comparator list to populate
     * @param token the token (e.g. {@code 3.1.x}, {@code 3.x})
     */
    private static void npmExpandWildcard(List<String[]> result, String token) {
        final String[] parts = NPM_DOT_PATTERN.split(token);
        int wildcardIdx = parts.length;
        for (int i = 0; i < parts.length; i++) {
            if (NPM_STAR.equals(parts[i]) || NPM_X_WILDCARD.equalsIgnoreCase(parts[i])) {
                wildcardIdx = i;
                break;
            }
        }
        if (wildcardIdx == parts.length) {
            // No wildcard found — plain version, treat as equality
            result.add(new String[]{NPM_EQ, token});
        } else if (wildcardIdx == 0) {
            result.add(new String[]{NPM_GTE, "0.0.0"});
        } else if (wildcardIdx == 1) {
            final int major = Integer.parseInt(parts[0]);
            result.add(new String[]{NPM_GTE, major + DOT_ZERO_ZERO});
            result.add(new String[]{NPM_LT, (major + 1) + DOT_ZERO_ZERO});
        } else {
            final int major = Integer.parseInt(parts[0]);
            final int minor = Integer.parseInt(parts[1]);
            result.add(new String[]{NPM_GTE, major + DOT_STR + minor + DOT_ZERO});
            result.add(new String[]{NPM_LT, major + DOT_STR + (minor + 1) + DOT_ZERO});
        }
    }


    /**
     * Evaluate a single comparator against this version.
     *
     * @param op the operator string
     * @param versionStr the version string
     * @return true if the comparator is satisfied
     */
    private boolean npmEvaluate(String op, String versionStr) {
        try {
            final SemanticVersion v = new SemanticVersion(versionStr, VersionType.NPM);
            if (NPM_GTE.equals(op)) {
                return isGreaterThanOrEqualTo(v);
            } else if (NPM_LTE.equals(op)) {
                return isLowerThanOrEqualTo(v);
            } else if (NPM_GT.equals(op)) {
                return isGreaterThan(v);
            } else if (NPM_LT.equals(op)) {
                return isLowerThan(v);
            } else if (NPM_EQ.equals(op)) {
                return isEqualTo(v);
            }
        } catch (IllegalArgumentException e) { // NOPMD
            // unparseable version in comparator
        }
        return false;
    }


    /**
     * Check whether any comparator has a prerelease qualifier at the same
     * [major, minor, patch] tuple as this version (NPM prerelease rule).
     *
     * @param comparators the list of [op, versionStr] pairs
     * @return true if a matching prerelease comparator is found
     */
    private boolean npmHasMatchingPrerelease(List<String[]> comparators) {
        for (String[] c : comparators) {
            if (c[1].contains(MINUS_CHARACTER)) {
                try {
                    final SemanticVersion cv = new SemanticVersion(c[1], VersionType.NPM);
                    if (Objects.equals(getMajor(), cv.getMajor())
                            && Objects.equals(getMinor(), cv.getMinor())
                            && Objects.equals(getPatch(), cv.getPatch())
                            && !cv.getSuffixTokens().isEmpty()) {
                        return true;
                    }
                } catch (IllegalArgumentException e) { // NOPMD
                    // skip unparseable comparator version
                }
            }
        }
        return false;
    }


    /**
     * Return a copy of the version string with wildcard components replaced by {@code 0}.
     *
     * @param version the version string
     * @return normalized version string
     */
    private static String npmFloorWildcard(String version) {
        final String[] parts = NPM_DOT_PATTERN.split(version);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(DOT_STR);
            }
            if (NPM_STAR.equals(parts[i]) || NPM_X_WILDCARD.equalsIgnoreCase(parts[i])) {
                sb.append("0");
            } else {
                sb.append(parts[i]);
            }
        }
        return sb.toString();
    }


    /**
     * Parse an integer from a version part, returning 0 for wildcards or non-numeric values.
     *
     * @param s the string to parse
     * @return parsed integer or 0
     */
    private static int npmParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) { // NOPMD
            return 0;
        }
    }


    /**
     * Split the main token
     *
     * @param tokens the tokens
     * @return the main tokens
     */
    protected String[] splitMainTokens(String[] tokens) {
        final String[] mainTokens;
        if (tokens.length == 1) {
            throwInvalidVersion(tokens[0].endsWith(PLUS_CHARACTER), "build cannot be empty");
            final String[] tmp = tokens[0].split(PLUS_SPLIT);
            mainTokens = tmp[0].split(DOT_SPLIT);
            if (tmp.length > 1) {
                this.build = tmp[1];
            }
        } else {
            mainTokens = tokens[0].split(DOT_SPLIT);
        }
        return mainTokens;
    }

    
    /**
     * Split the suffix token 
     * 
     * @param tokens the tokens
     * @return the suffix tokens
     */
    protected List<String> splitSuffixTokens(String[] tokens) {
        final String[] suffixTokens;
        if (tokens.length == 1) {
            suffixTokens = new String[0];
        } else {
            throwInvalidVersion(tokens[1].endsWith(PLUS_CHARACTER), "build cannot be empty");
            final String[] tmp = tokens[1].split(PLUS_SPLIT);
            suffixTokens = tmp[0].split(DOT_SPLIT);
            if (tmp.length > 1) {
                this.build = tmp[1];
            }
        }
        return Arrays.asList(suffixTokens);
    }


    /**
     * Get version with incremented numbers
     * 
     * @param majorInc the major increment
     * @param minorInc the minor increment
     * @param patchInc the patch increment
     * @return the version
     */
    protected SemanticVersion withIncrement(int majorInc, int minorInc, int patchInc) {
        Integer minor = getMinor();
        if (minor != null) {
            minor += minorInc;
        }
        Integer patch = getPatch();
        if (patch != null) {
            patch += patchInc;
        }
        return with(getMajor() + majorInc, minor, patch, true, true);
    }


    /**
     * Prepare a version
     *
     * @param major the major
     * @param minor the minor
     * @param patch the patch
     * @param suffix the suffix
     * @param build the build
     * @return the version
     */
    protected SemanticVersion with(int major, Integer minor, Integer patch, boolean suffix, boolean build) {
        Integer m = null;
        if (getMinor() != null) {
            m = minor;
        }
        
        Integer p = null;
        if (getPatch() != null) {
            p = patch;
        }

        String b = null;
        if (build) {
            b = getBuild();
        }
        
        List<String> suffixTokens = null;
        if (suffix) {
            suffixTokens = getSuffixTokens();
        }

        return create(getType(), major, m, p, suffixTokens, b);
    }

    
    /**
     * Prepare a version
     *
     * @param major the major
     * @param minor the minor
     * @param patch the patch
     * @param suffixTokens the suffix
     * @param build the build
     * @return the version
     */
    protected SemanticVersion with(int major, Integer minor, Integer patch, List<String> suffixTokens, String build) {
        Integer m = null;
        if (getMinor() != null) {
            m = minor;
        }
        
        Integer p = null;
        if (getPatch() != null) {
            p = patch;
        }
        return create(getType(), major, m, p, suffixTokens, build);
    }

    
    /**
     * Prepare a version
     *
     * @param versionType the version type
     * @param major the major
     * @param minor the minor
     * @param patch the patch
     * @param suffixTokens the suffix
     * @param build the build
     * @return the version
     */
    protected SemanticVersion create(VersionType versionType, int major, Integer minor, Integer patch, List<String> suffixTokens, String build) {
        StringBuilder sb = new StringBuilder().append(major);
        if (minor != null) {
            sb.append(DOT_STR).append(minor);
        }
        
        if (patch != null) {
            sb.append(DOT_STR).append(patch);
        }
        
        if (suffixTokens != null) {
            boolean first = true;
            for (String suffixToken : suffixTokens) {
                if (first) {
                    sb.append(MINUS_CHARACTER);
                    first = false;
                } else {
                    sb.append(DOT_STR);
                }
                sb.append(suffixToken);
            }
        }
        if (build != null) {
            sb.append(PLUS_CHARACTER).append(build);
        }

        return new SemanticVersion(sb.toString(), versionType);
    }
    
    
    /**
     * Split the version into into tokens
     * 
     * @param version the version to split
     * @return the tokens
     */
    protected static String[] splitVersionIntoTokens(String version) {
        final String[] tokens;
        int firstPlusIdx = version.indexOf(PLUS_CHARACTER);
        int firstHyphenIdx = version.indexOf(MINUS_CHARACTER);
        if (firstHyphenIdx >= 0 || firstPlusIdx == -1 || firstHyphenIdx < firstPlusIdx) {
            tokens = version.split(MINUS_CHARACTER, 2);
        } else {
            tokens = new String[] {version };
        }
        return tokens;
    }
}
