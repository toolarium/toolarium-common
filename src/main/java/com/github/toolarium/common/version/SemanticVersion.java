/*
 * SemanticVersion.java
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
    private static final String NPM_ZERO_VERSION = "0.0.0";
    private static final Pattern NPM_OR_PATTERN = Pattern.compile("\\|\\|"); // the parts are trimmed afterwards, no backtracking on whitespace
    private static final Pattern NPM_SPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern NPM_DOT_PATTERN = Pattern.compile(DOT_SPLIT);
    private static final Pattern NPM_MINUS_PATTERN = Pattern.compile(MINUS_CHARACTER);
    private static final Pattern NPM_OPERATOR_SPACE_PATTERN = Pattern.compile("(<=|>=|<|>|=|~|\\^)\\s+");

    private final Integer major;
    private final Integer minor;
    private final Integer patch;
    private final List<String> suffixTokens;
    private final String build;


    /**
     * Constructor for SemanticVersion
     *
     * @param version the version to parse
     */
    public SemanticVersion(String version) {
        this(version, VersionType.STRICT);
    }
    
    
    /**
     * Constructor for SemanticVersion
     *
     * @param version the version to parse
     * @param inputVersionType the version type
     * @throws IllegalArgumentException the exception which will be thrown of an invalid input
     */
    public SemanticVersion(String version, VersionType inputVersionType) throws IllegalArgumentException {
        super(version, inputVersionType);
        final String[] tokens = splitVersionIntoTokens(super.toString());
        final String[] mainTokens = splitMainTokens(tokens); // split into main tokens
        final boolean isStrictOrNpm = getType() == VersionType.STRICT || getType() == VersionType.NPM;
        throwInvalidVersion(isStrictOrNpm && hasTooManyMainParts(tokens[0]), "too many version parts");
        throwInvalidVersion(getType() == VersionType.NPM && hasEmptyMainPart(tokens[0]), "empty version part");
        this.major = parseInteger(getType(), mainTokens, 0, "no major version");
        this.minor = parseInteger(getType(), mainTokens, 1, "no minor version");
        this.patch = parseInteger(getType(), mainTokens, 2, "no patch version");
        this.suffixTokens = Collections.unmodifiableList(new ArrayList<String>(splitSuffixTokens(tokens))); // split into suffixes
        this.build = parseBuild(tokens);
        validateSuffixAndBuild(super.toString(), tokens);
        throwInvalidVersion(getType() == VersionType.STRICT && hasLeadingZero(mainTokens, this.suffixTokens), "leading zero");
        throwInvalidVersion(this.minor == null && getType() == VersionType.STRICT, "no minor version");
        throwInvalidVersion(this.patch == null && getType() == VersionType.STRICT, "no patch version");
    }

    
    /**
     * Get the major part of the version.
     * Example: for "1.2.3" = 1
     *
     * @return the major part of the version or null if it is not set
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
     * @return the minor part of the version or null if it is not set
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
     * @return the patch part of the version or null if it is not set
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
     * @return the suffix of the version (unmodifiable list)
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
     * Checks if the version is greater than another version.
     * For all version types except {@link VersionType#NPM} this is equal to {@code compareTo(version) > 0}.
     * In case of {@link VersionType#NPM} a missing minor or patch number of the provided version acts as wildcard
     * (e.g. "1.2.3" is not greater than "1" and not greater than "1.2").
     *
     * @param version the version to compare
     * @return true if the current version is greater than the provided version
     */
    public boolean isGreaterThan(SemanticVersion version) {
        if (getType() == VersionType.NPM) {
            return isGreaterThanPartial(version);
        }

        return compareTo(version) > 0;
    }


    /**
     * Checks if the version is greater than another version, a missing minor or patch number of the provided version
     * acts as wildcard (e.g. "1.2.3" is not greater than "1" and not greater than "1.2").
     *
     * @param version the version to compare
     * @return true if the current version is greater than the provided version
     */
    private boolean isGreaterThanPartial(SemanticVersion version) {
        int cmp = Integer.compare(toNumber(getMajor()), toNumber(version.getMajor()));
        if (cmp != 0) {
            return cmp > 0;
        }

        if (version.getMinor() == null) {
            return false;
        }

        cmp = Integer.compare(toNumber(getMinor()), toNumber(version.getMinor()));
        if (cmp != 0) {
            return cmp > 0;
        }

        if (version.getPatch() == null) {
            return false;
        }

        return compareTo(version) > 0;
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
     * For all version types except {@link VersionType#NPM} this is equal to {@code compareTo(version) == 0}.
     * In case of {@link VersionType#NPM} a missing minor or patch number of the provided version acts as wildcard.
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version (build excluded)
     */
    public boolean isEquivalentTo(SemanticVersion version) {
        final Boolean npmPartialMatch = npmPartialMatch(version);
        if (npmPartialMatch != null) {
            return npmPartialMatch.booleanValue();
        }

        return compareTo(version) == 0;
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
     * Checks if the version equals another version (build included), see {@link #equals(Object)}.
     * In case of {@link VersionType#NPM} a missing minor or patch number of the provided version acts as wildcard.
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version
     */
    public boolean isEqualTo(SemanticVersion version) {
        final Boolean npmPartialMatch = npmPartialMatch(version);
        if (npmPartialMatch != null) {
            return npmPartialMatch.booleanValue();
        }

        return equals(version);
    }

    
    /**
     * Checks whether this version satisfies an npm-style range expression.
     * The range expression is always evaluated with the npm rules, whatever the version type of this version:
     * a partial version in the range acts as wildcard, e.g. {@code 1.2} is {@code 1.2.x} and {@code <=1.2} is {@code <1.3.0}.
     * Like npm, a pre-release version (e.g. {@code 1.2.3-beta}) only satisfies a range which contains a pre-release
     * of the same major, minor and patch number, e.g. it does not satisfy {@code *}.
     *
     * <p>Supported range forms: {@code *}, wildcard ({@code 3.x}), exact ({@code 1.2.3}),
     * partial ({@code 1.2}), hyphen ({@code 1.2.3 - 2.3.4}), tilde ({@code ~1.2.3}),
     * caret ({@code ^1.2.3}), comparators ({@code >=1.0.0 &lt;2.0.0}), OR ({@code 1.x || 2.x}).
     *
     * @param rangeExpression the npm range expression; {@code null} returns {@code false}
     * @return true if the version satisfies the range
     * @throws IllegalArgumentException if the range expression is invalid, e.g. contains a non-numeric version component
     *         or an operator without version
     */
    public boolean satisfies(String rangeExpression) {
        if (rangeExpression == null) {
            return false;
        }
        final String expr = rangeExpression.trim();
        if (expr.isEmpty() || NPM_STAR.equals(expr) || NPM_X_WILDCARD.equalsIgnoreCase(expr)) {
            // any version except a pre-release
            return getSuffixTokens().isEmpty();
        }
        try {
            for (String orPart : NPM_OR_PATTERN.split(expr)) {
                if (npmSatisfiesAndGroup(orPart.trim())) {
                    return true;
                }
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
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
     * Get the version with the given pre-release suffix, e.g. <code>alpha.1</code> for <code>1.2.3-alpha.1</code>.
     * The suffix is split by dots into its identifiers.
     *
     * @param suffix the pre-release suffix to set (without leading hyphen); null removes the suffix
     * @return the version
     * @throws IllegalArgumentException if the suffix is not a valid pre-release
     */
    public SemanticVersion withSuffix(String suffix) {
        if (suffix == null) {
            return with(getMajor(), getMinor(), getPatch(), null, getBuild());
        }

        return with(getMajor(), getMinor(), getPatch(), Arrays.asList(suffix.split(DOT_SPLIT, -1)), getBuild());
    }


    /**
     * Get the version with the given build metadata, e.g. <code>001</code> for <code>1.2.3+001</code>.
     *
     * @param build the build metadata to set (without leading plus)
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
     * Get the next minor version. The result is always a full version, a missing minor number of a partial version is 0:
     * e.g. "1" gives "1.1.0" and "1.2" gives "1.3.0".
     *
     * @return the next minor version
     */
    public SemanticVersion nextMinor() {
        return create(getType(), getMajor(), toNumber(getMinor()) + 1, 0, null, null);
    }


    /**
     * Get the next patch version. The result is always a full version, a missing minor or patch number of a partial version is 0:
     * e.g. "1" gives "1.0.1" and "1.2" gives "1.2.1".
     *
     * @return the next patch version
     */
    public SemanticVersion nextPatch() {
        return create(getType(), getMajor(), toNumber(getMinor()), toNumber(getPatch()) + 1, null, null);
    }

    
    /**
     * Compares the version according to the semantic version precedence (see https://semver.org/#spec-item-11):
     * <ul>
     *   <li>major, minor and patch are compared numerically, a missing number is treated as 0.</li>
     *   <li>a version without pre-release suffix has a higher precedence than a version with suffix.</li>
     *   <li>suffix identifiers are compared one by one: numeric identifiers numerically, alphanumeric identifiers
     *       lexically in ASCII sort order (case-sensitive); numeric identifiers have a lower precedence than alphanumeric ones;
     *       a larger set of identifiers has a higher precedence if all preceding identifiers are equal.</li>
     *   <li>the build is ignored.</li>
     * </ul>
     * Because the build is ignored, two versions which differ only in the build compare as 0 but are not {@link #equals(Object) equal}.
     *
     * @param version the version to compare
     * @return a negative integer, zero, or a positive integer as this version is less than, equal to, or greater than the specified version
     * @throws NullPointerException if the version is null
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(SemanticVersion version) {
        Objects.requireNonNull(version, "version must not be null");
        int cmp = Integer.compare(toNumber(getMajor()), toNumber(version.getMajor()));
        if (cmp != 0) {
            return cmp;
        }

        cmp = Integer.compare(toNumber(getMinor()), toNumber(version.getMinor()));
        if (cmp != 0) {
            return cmp;
        }

        cmp = Integer.compare(toNumber(getPatch()), toNumber(version.getPatch()));
        if (cmp != 0) {
            return cmp;
        }

        return compareSuffixTokens(getSuffixTokens(), version.getSuffixTokens());
    }


    /**
     * Two semantic versions are equal if they have the same precedence ({@code compareTo(other) == 0}) and the same build,
     * e.g. "1.2" (LOOSE) is equal to "1.2.0". A semantic version is never equal to an instance of another class.
     *
     * @see com.github.toolarium.common.version.AbstractVersion#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SemanticVersion other = (SemanticVersion) o;
        return compareTo(other) == 0 && Objects.equals(getBuild(), other.getBuild());
    }


    /**
     * @see com.github.toolarium.common.version.AbstractVersion#hashCode()
     */
    @Override
    public int hashCode() {
        List<String> normalizedSuffixTokens = new ArrayList<String>(getSuffixTokens().size());
        for (String token : getSuffixTokens()) {
            if (isNumericIdentifier(token)) {
                normalizedSuffixTokens.add(stripLeadingZeros(token));
            } else {
                normalizedSuffixTokens.add(token);
            }
        }

        return Objects.hash(toNumber(getMajor()), toNumber(getMinor()), toNumber(getPatch()), normalizedSuffixTokens, getBuild());
    }


    /**
     * Get the number or 0 in case it is null
     *
     * @param number the number
     * @return the number or 0
     */
    private static int toNumber(Integer number) {
        if (number == null) {
            return 0;
        }

        return number.intValue();
    }


    /**
     * Compares two suffix (pre-release) token lists according to the semantic version precedence.
     *
     * @param tokens1 the first token list
     * @param tokens2 the second token list
     * @return a negative integer, zero, or a positive integer as the first list is less than, equal to, or greater than the second
     */
    private static int compareSuffixTokens(List<String> tokens1, List<String> tokens2) {
        // a version without suffix has a higher precedence
        if (tokens1.isEmpty() && !tokens2.isEmpty()) {
            return 1;
        }

        if (!tokens1.isEmpty() && tokens2.isEmpty()) {
            return -1;
        }

        for (int i = 0; i < tokens1.size() && i < tokens2.size(); i++) {
            int cmp = compareIdentifier(tokens1.get(i), tokens2.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }

        // if all preceding identifiers are equal, the larger set has a higher precedence
        return Integer.compare(tokens1.size(), tokens2.size());
    }


    /**
     * Compares two suffix (pre-release) identifiers according to the semantic version precedence.
     *
     * @param identifier1 the first identifier
     * @param identifier2 the second identifier
     * @return a negative integer, zero, or a positive integer as the first identifier is less than, equal to, or greater than the second
     */
    private static int compareIdentifier(String identifier1, String identifier2) {
        final boolean isNumeric1 = isNumericIdentifier(identifier1);
        final boolean isNumeric2 = isNumericIdentifier(identifier2);
        if (isNumeric1 && isNumeric2) {
            final String n1 = stripLeadingZeros(identifier1);
            final String n2 = stripLeadingZeros(identifier2);
            if (n1.length() != n2.length()) {
                return Integer.compare(n1.length(), n2.length());
            }

            return Integer.signum(n1.compareTo(n2));
        }

        if (isNumeric1) {
            return -1;
        }

        if (isNumeric2) {
            return 1;
        }

        return Integer.signum(identifier1.compareTo(identifier2));
    }


    /**
     * Checks if the identifier consists only of digits
     *
     * @param identifier the identifier
     * @return true if it is a numeric identifier
     */
    private static boolean isNumericIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }

        for (int i = 0; i < identifier.length(); i++) {
            final char c = identifier.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }

        return true;
    }


    /**
     * Strip the leading zeros of a numeric identifier
     *
     * @param identifier the numeric identifier
     * @return the identifier without leading zeros ("0" in case it consists only of zeros)
     */
    private static String stripLeadingZeros(String identifier) {
        int i = 0;
        while (i < identifier.length() - 1 && identifier.charAt(i) == '0') {
            i++;
        }

        return identifier.substring(i);
    }


    /**
     * Checks in case of a {@link VersionType#NPM} version if the given (partial) version matches, a missing minor or patch number
     * of the provided version acts as wildcard.
     *
     * @param version the version to compare
     * @return true or false if it could be decided by the partial comparison; null if it is not an npm version or
     *         the provided version has all numbers
     */
    private Boolean npmPartialMatch(SemanticVersion version) {
        if (getType() != VersionType.NPM) {
            return null;
        }

        return partialMatch(version);
    }


    /**
     * Checks if the given (partial) version matches, a missing minor or patch number of the provided version acts as wildcard.
     *
     * @param version the version to compare
     * @return true or false if it could be decided by the partial comparison; null if the provided version has all numbers
     */
    private Boolean partialMatch(SemanticVersion version) {
        if (!Objects.equals(getMajor(), version.getMajor())) {
            return Boolean.FALSE;
        }

        if (version.getMinor() == null) {
            return Boolean.TRUE;
        }

        if (version.getPatch() == null) {
            return Boolean.valueOf(Objects.equals(getMinor(), version.getMinor()));
        }

        return null;
    }

    
    /**
     * Evaluate a single AND-group of comparators against this version.
     *
     * @param group the AND-group string
     * @return true if all comparators are satisfied
     */
    private boolean npmSatisfiesAndGroup(String group) {
        if (group.isEmpty() || NPM_STAR.equals(group) || NPM_X_WILDCARD.equalsIgnoreCase(group)) {
            // any version except a pre-release
            return getSuffixTokens().isEmpty();
        }
        final List<String[]> comparators = new ArrayList<String[]>();
        final int hyphenPos = group.indexOf(" - ");
        if (hyphenPos >= 0) {
            npmAddHyphenRange(comparators, group.substring(0, hyphenPos).trim(), group.substring(hyphenPos + 3).trim());
        } else {
            // remove the whitespace between an operator and its version, e.g. ">= 1.0.0" is equal to ">=1.0.0"
            final String normalizedGroup = NPM_OPERATOR_SPACE_PATTERN.matcher(group).replaceAll("$1");
            for (String token : NPM_SPACE_PATTERN.split(normalizedGroup)) {
                if (!token.isEmpty()) {
                    npmAddComparators(comparators, token);
                }
            }
        }
        for (String[] c : comparators) {
            if (!npmEvaluate(c[0], c[1])) {
                return false;
            }
        }
        // a pre-release only satisfies a range with a pre-release of the same major, minor and patch number
        return getSuffixTokens().isEmpty() || npmHasMatchingPrerelease(comparators);
    }


    /**
     * Add comparators for a hyphen range "lower - upper".
     *
     * @param result the comparator list to populate
     * @param lower the lower bound version string
     * @param upper the upper bound version string
     */
    private static void npmAddHyphenRange(List<String[]> result, String lower, String upper) {
        result.add(new String[]{NPM_GTE, npmFloorWildcard(npmStripPrefix(lower))});
        final String upperVersion = npmStripPrefix(upper);
        final String[] parts = NPM_DOT_PATTERN.split(upperVersion);
        int significant = 0;
        for (String p : parts) {
            if (npmIsWildcard(p)) {
                break;
            }
            significant++;
        }
        // in case the upper bound is a wildcard (e.g. "x" or "*") there is no upper limit
        if (significant >= 3) {
            result.add(new String[]{NPM_LTE, upperVersion});
        } else if (significant == 2) {
            result.add(new String[]{NPM_LT, Integer.parseInt(parts[0]) + DOT_STR + (Integer.parseInt(parts[1]) + 1) + DOT_ZERO});
        } else if (significant == 1 || parts.length == 0) {
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
            result.add(new String[]{NPM_GTE, NPM_ZERO_VERSION});
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
     * @param tildeVersion the version string after the tilde
     */
    private static void npmExpandTilde(List<String[]> result, String tildeVersion) {
        final String version = npmStripPrefix(tildeVersion);
        final String[] parts = NPM_DOT_PATTERN.split(NPM_MINUS_PATTERN.split(version)[0]);
        if (parts.length > 0 && npmIsWildcard(parts[0])) {
            // e.g. "~x": any version
            result.add(new String[]{NPM_GTE, NPM_ZERO_VERSION});
            return;
        }

        result.add(new String[]{NPM_GTE, version});
        if (parts.length >= 2 && !npmIsWildcard(parts[1])) {
            result.add(new String[]{NPM_LT, Integer.parseInt(parts[0]) + DOT_STR + (Integer.parseInt(parts[1]) + 1) + DOT_ZERO});
        } else {
            result.add(new String[]{NPM_LT, (Integer.parseInt(parts[0]) + 1) + DOT_ZERO_ZERO});
        }
    }


    /**
     * Expand a caret range into comparators. A missing or wildcard minor or patch number is handled like npm does,
     * e.g. "^0.x" is "&gt;=0.0.0 &lt;1.0.0" and "^0.0.x" is "&gt;=0.0.0 &lt;0.1.0".
     *
     * @param result the comparator list to populate
     * @param caretVersion the version string after the caret
     */
    private static void npmExpandCaret(List<String[]> result, String caretVersion) {
        final String version = npmStripPrefix(caretVersion);
        final String[] parts = NPM_DOT_PATTERN.split(NPM_MINUS_PATTERN.split(version)[0]);
        if (parts.length > 0 && npmIsWildcard(parts[0])) {
            // e.g. "^x": any version
            result.add(new String[]{NPM_GTE, NPM_ZERO_VERSION});
            return;
        }

        final int major = Integer.parseInt(parts[0]);
        final boolean hasMinor = parts.length > 1 && !npmIsWildcard(parts[1]);
        final boolean hasPatch = hasMinor && parts.length > 2 && !npmIsWildcard(parts[2]);
        int minor = 0;
        if (parts.length > 1) {
            minor = npmParseInt(parts[1]);
        }
        int patch = 0;
        if (parts.length > 2) {
            patch = npmParseInt(parts[2]);
        }
        result.add(new String[]{NPM_GTE, version});
        if (major > 0 || !hasMinor) {
            result.add(new String[]{NPM_LT, (major + 1) + DOT_ZERO_ZERO});
        } else if (minor > 0 || !hasPatch) {
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
            // no wildcard found: plain version, treat as equality
            result.add(new String[]{NPM_EQ, token});
        } else if (wildcardIdx == 0) {
            result.add(new String[]{NPM_GTE, NPM_ZERO_VERSION});
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
     * Evaluate a single comparator against this version. The npm rules are applied whatever the version type of this version:
     * a missing minor or patch number of the comparator version acts as wildcard.
     *
     * @param op the operator string
     * @param versionStr the version string
     * @return true if the comparator is satisfied
     * @throws IllegalArgumentException if the comparator version is invalid
     */
    private boolean npmEvaluate(String op, String versionStr) {
        final SemanticVersion v = new SemanticVersion(versionStr, VersionType.NPM);
        if (NPM_GTE.equals(op)) {
            return isGreaterThanPartial(v) || isEquivalentToPartial(v);
        } else if (NPM_LTE.equals(op)) {
            return !isGreaterThanPartial(v);
        } else if (NPM_GT.equals(op)) {
            return isGreaterThanPartial(v);
        } else if (NPM_LT.equals(op)) {
            return !isGreaterThanPartial(v) && !isEquivalentToPartial(v);
        } else if (NPM_EQ.equals(op)) {
            // like npm the build is ignored in a range, e.g. "1.2.3+build" satisfies "=1.2.3"
            return isEquivalentToPartial(v);
        }

        return false;
    }


    /**
     * Checks if the version equals another version without taking the build into account, a missing minor or patch number
     * of the provided version acts as wildcard.
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version (build excluded)
     */
    private boolean isEquivalentToPartial(SemanticVersion version) {
        final Boolean match = partialMatch(version);
        if (match != null) {
            return match.booleanValue();
        }

        return compareTo(version) == 0;
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
     * Checks if a version part is a wildcard ({@code x}, {@code X} or {@code *}).
     *
     * @param part the version part
     * @return true if the part is a wildcard
     */
    private static boolean npmIsWildcard(String part) {
        return NPM_STAR.equals(part) || NPM_X_WILDCARD.equalsIgnoreCase(part);
    }


    /**
     * Strip a leading {@code v} or {@code V} prefix of a version, e.g. "v1.2.3" becomes "1.2.3".
     *
     * @param version the version string
     * @return the version string without prefix
     */
    private static String npmStripPrefix(String version) {
        if (version.startsWith("v") || version.startsWith("V")) {
            return version.substring(1).trim();
        }

        return version;
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
        } else {
            mainTokens = tokens[0].split(DOT_SPLIT);
        }
        return mainTokens;
    }

    
    /**
     * Checks if the main part (major, minor and patch) of the version has more than three dot separated parts,
     * e.g. "1.2.3.4" or "1.2.3.foo". The build is not taken into account.
     *
     * @param firstToken the first token of the version (main part, optional build)
     * @return true if the main part has more than three parts
     */
    private static boolean hasTooManyMainParts(String firstToken) {
        String mainPart = firstToken;
        final int plusIdx = mainPart.indexOf(PLUS_CHARACTER);
        if (plusIdx >= 0) {
            mainPart = mainPart.substring(0, plusIdx);
        }

        return mainPart.split(DOT_SPLIT, -1).length > 3;
    }


    /**
     * Checks if the main part (major, minor and patch) of the version has an empty dot separated part,
     * e.g. "1.2." or "1..2". The build is not taken into account.
     *
     * @param firstToken the first token of the version (main part, optional build)
     * @return true if the main part has an empty part
     */
    private static boolean hasEmptyMainPart(String firstToken) {
        String mainPart = firstToken;
        final int plusIdx = mainPart.indexOf(PLUS_CHARACTER);
        if (plusIdx >= 0) {
            mainPart = mainPart.substring(0, plusIdx);
        }

        for (String part : mainPart.split(DOT_SPLIT, -1)) {
            if (part.isEmpty()) {
                return true;
            }
        }

        return false;
    }


    /**
     * Checks if a number of the main part or a numeric pre-release identifier has a leading zero, e.g. "01.2.3" or "1.2.3-01"
     * (not allowed by semantic versioning).
     *
     * @param mainTokens the main tokens
     * @param preReleaseTokens the pre-release tokens
     * @return true if a leading zero is found
     */
    private static boolean hasLeadingZero(String[] mainTokens, List<String> preReleaseTokens) {
        for (String token : mainTokens) {
            if (hasLeadingZero(token)) {
                return true;
            }
        }

        for (String token : preReleaseTokens) {
            if (hasLeadingZero(token)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Checks if a numeric identifier has a leading zero, e.g. "01"
     *
     * @param identifier the identifier
     * @return true if it is a numeric identifier with a leading zero
     */
    private static boolean hasLeadingZero(String identifier) {
        return identifier != null && identifier.length() > 1 && identifier.charAt(0) == '0' && isNumericIdentifier(identifier);
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
        }
        return Arrays.asList(suffixTokens);
    }


    /**
     * Validates the pre-release suffix and the build according to semantic versioning 2.0: both consist of
     * dot separated, non empty identifiers which contain only ASCII alphanumerics and hyphens; only one plus is allowed.
     *
     * @param version the prepared version string
     * @param tokens the tokens
     * @throws IllegalArgumentException in case of an invalid pre-release suffix or build
     */
    private void validateSuffixAndBuild(String version, String[] tokens) throws IllegalArgumentException {
        final int plusIdx = version.indexOf(PLUS_CHARACTER);
        throwInvalidVersion(plusIdx >= 0 && version.indexOf(PLUS_CHARACTER, plusIdx + 1) >= 0, "only one build is allowed");

        if (tokens.length > 1) {
            String preRelease = tokens[1];
            final int idx = preRelease.indexOf(PLUS_CHARACTER);
            if (idx >= 0) {
                preRelease = preRelease.substring(0, idx);
            }
            throwInvalidVersion(!isValidIdentifiers(preRelease), "invalid pre-release");
        }

        if (plusIdx >= 0) {
            throwInvalidVersion(!isValidIdentifiers(version.substring(plusIdx + 1)), "invalid build");
        }
    }


    /**
     * Checks if the given string consists of dot separated, non empty identifiers which contain only ASCII alphanumerics and hyphens.
     *
     * @param identifiers the identifiers
     * @return true if all identifiers are valid
     */
    private static boolean isValidIdentifiers(String identifiers) {
        for (String identifier : identifiers.split(DOT_SPLIT, -1)) {
            if (identifier.isEmpty()) {
                return false;
            }

            for (int i = 0; i < identifier.length(); i++) {
                final char c = identifier.charAt(i);
                if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '-')) {
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * Parse the build from the tokens: the build is the part after the first plus character of the last token.
     *
     * @param tokens the tokens
     * @return the build or null
     */
    private static String parseBuild(String[] tokens) {
        final String[] tmp = tokens[tokens.length - 1].split(PLUS_SPLIT);
        if (tmp.length > 1) {
            return tmp[1];
        }

        return null;
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
        // a hyphen starts the pre-release suffix only if it is located before the build (a build may contain hyphens)
        if (firstHyphenIdx >= 0 && (firstPlusIdx == -1 || firstHyphenIdx < firstPlusIdx)) {
            tokens = version.split(MINUS_CHARACTER, 2);
        } else {
            tokens = new String[] {version };
        }
        return tokens;
    }
}
