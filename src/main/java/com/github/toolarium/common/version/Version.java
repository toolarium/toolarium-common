/*
 * Version.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.version;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * The version
 * 
 * @author patrick
 */
public class Version extends AbstractVersion<Version> implements Comparable<Version>, Serializable {
    private static final long serialVersionUID = 3833465089511798073L;
    private static final String DOT_STR = ".";
    private static final String V0 = "0";
    private static final String NO_MAJOR_VERSION = "no major version";
    private static final String NO_MINOR_VERSION = "no minor version";
    private static final String NO_PATCH_VERSION = "no patch version";
    private static final Pattern QUALIFIER_SPLIT_PATTERN = Pattern.compile("[^a-z0-9]+");
    private static final String NEGATIVE_NUMBER = "negative number";
    private static final List<String> UNSTABLE_QUALIFIERS = List.of("snapshot", "alpha", "beta", "rc", "ea", "preview", "milestone");
    /** The short milestone qualifier, it is only unstable if it is followed by a number, e.g. M1 */
    private static final String MILESTONE_SHORT_QUALIFIER = "m";

    private final VersionNumberElement major;
    private final VersionNumberElement minor;
    private final VersionNumberElement patch;
    private final String sep;


    /**
     * Constructor
     * 
     * @param majorNumber the major number
     * @param minorNumber the minor number
     * @param patchNumber the patch number
     * @throws IllegalArgumentException if a number is negative
     */
    public Version(int majorNumber, int minorNumber, int patchNumber) {
        super(toVersion(majorNumber, minorNumber, patchNumber, DOT_STR), VersionType.STRICT);
        throwInvalidVersion(majorNumber < 0 || minorNumber < 0 || patchNumber < 0, NEGATIVE_NUMBER);
        this.major = new VersionNumberElement(majorNumber, null);
        this.minor = new VersionNumberElement(minorNumber, null);
        this.patch = new VersionNumberElement(patchNumber, null);
        this.sep = DOT_STR;
    }

    
    /**
     * Constructor
     * 
     * @param major the major number
     * @param minor the minor number
     * @param patch the patch number
     * @param versionType the version type
     * @param sep the separator, it is used literally; null or empty uses the default separator "."
     * @throws IllegalArgumentException if the major is null, the minor is null but the patch is set or
     *         in case of {@link VersionType#STRICT} the minor or patch is null
     */
    public Version(VersionNumberElement major, VersionNumberElement minor, VersionNumberElement patch, VersionType versionType, String sep) {
        super(toVersion(major, minor, patch, resolveSeparator(sep)), versionType);
        throwInvalidVersion(major == null, NO_MAJOR_VERSION);
        throwInvalidVersion(minor == null && (patch != null || getType() == VersionType.STRICT), NO_MINOR_VERSION);
        throwInvalidVersion(patch == null && getType() == VersionType.STRICT, NO_PATCH_VERSION);
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.sep = resolveSeparator(sep);
    }

    
    /**
     * Constructor
     * 
     * @param version the version
     */
    public Version(String version) {
        this(version, VersionType.LOOSE, DOT_STR);
    }

    
    /**
     * Constructor
     * 
     * @param version the version
     * @param sep the separator, it is used literally; null or empty uses the default separator "."
     */
    public Version(String version, String sep) {
        this(version, VersionType.LOOSE, sep);
    }


    /**
     * Constructor
     * 
     * @param version the version
     * @param versionType the version type
     * @param sep the separator, it is used literally; null or empty uses the default separator "."
     */
    public Version(String version, VersionType versionType, String sep) {
        super(version, versionType);
        
        this.sep = resolveSeparator(sep);
        String[] mainTokens = toString().split(Pattern.quote(this.sep));
        if (mainTokens.length == 0) {
            mainTokens = new String[] {toString()};
        }
        
        this.major = splitNumberAndSuffix(mainTokens, 0, NO_MAJOR_VERSION);
        VersionNumberElement minorElement = splitNumberAndSuffix(mainTokens, 1, NO_MINOR_VERSION);
        if (minorElement == null && mainTokens.length > 2) {
            // empty minor element followed by a patch, e.g. "1..2": only in LOOSE mode it is treated as 0
            throwInvalidVersion(getType() != VersionType.LOOSE, NO_MINOR_VERSION);
            minorElement = new VersionNumberElement(0, null);
        }
        this.minor = minorElement;
        this.patch = splitNumberAndSuffix(buildPatch(mainTokens, 2, this.sep), 2, NO_PATCH_VERSION);
        throwInvalidVersion(this.major == null, NO_MAJOR_VERSION);
        throwInvalidVersion(this.minor == null && getType() == VersionType.STRICT, NO_MINOR_VERSION);
        throwInvalidVersion(this.patch == null && getType() == VersionType.STRICT, NO_PATCH_VERSION);
    }

    
    /**
     * Get the major part of the version.
     * Example: for "1.2.3" = 1
     *
     * @return the major part of the version
     */
    public VersionNumberElement getMajor() {
        return major;
    }

    
    /**
     * Get the major part of the version.
     * Example: for "1.2.3" = 1
     *
     * @return the major part of the version
     */
    public int getMajorNumber() {
        return major.getNumber();
    }

    
    /**
     * Gets the additional major suffix, e.g. <code>a</code> for <code>1a.2.3</code>
     * 
     * @return the additional major suffix
     */
    public String getMajorSuffix() {
        return major.getSuffix();
    }

    
    /**
     * Sets the major number
     * 
     * @param majorNumber the major number to set
     * @return the new version
     * @throws IllegalArgumentException if the major number is null, empty or invalid
     */
    public Version setMajorNumber(String majorNumber) {
        final VersionNumberElement element = splitNumberAndSuffix(new String[] {majorNumber}, 0, NO_MAJOR_VERSION);
        throwInvalidVersion(element == null, NO_MAJOR_VERSION);
        return new Version(element, getMinor(), getPatch(), getType(), sep);
    }

    
    /**
     * Get the minor part of the version.
     * Example: for "1.2.3" = 2
     *
     * @return the minor part of the version or null if it is not set
     */
    public VersionNumberElement getMinor() {
        return minor;
    }

    
    /**
     * Get the minor part of the version.
     * Example: for "1.2.3" = 2
     *
     * @return the minor part of the version or -1 if it is not set
     */
    public int getMinorNumber() {
        if (minor == null) {
            return -1;
        }
        
        return minor.getNumber();
    }

    
    /**
     * Gets the additional minor suffix, e.g. <code>b</code> for <code>1.2b.3</code>
     * 
     * @return the additional minor suffix or an empty string if the minor part is not set
     */
    public String getMinorSuffix() {
        if (minor == null) {
            return "";
        }

        return minor.getSuffix();
    }

    
    /**
     * Sets the minor number
     * 
     * @param minorNumber the minor number to set
     * @return the new version
     * @throws IllegalArgumentException if the minor number is null, empty or invalid
     */
    public Version setMinorNumber(String minorNumber) {
        final VersionNumberElement element = splitNumberAndSuffix(new String[] {minorNumber}, 0, NO_MINOR_VERSION);
        throwInvalidVersion(element == null, NO_MINOR_VERSION);
        return new Version(getMajor(), element, getPatch(), getType(), sep);
    }

    
    /**
     * Get the patch part of the version.
     * Example: for "1.2.3" = 3
     *
     * @return the patch part of the version or null if it is not set
     */
    public VersionNumberElement getPatch() {
        return patch;
    }

    
    /**
     * Get the patch part of the version.
     * Example: for "1.2.3" = 3
     *
     * @return the patch part of the version or -1 if it is not set
     */
    public int getPatchNumber() {
        if (patch == null) {
            return -1;
        }

        return patch.getNumber();
    }

    
    /**
     * Gets the additional patch suffix
     * 
     * @return the additional patch suffix
     */
    public String getPatchSuffix() {
        if (patch == null) {
            return "";
        }
        
        return patch.getSuffix();
    }

    
    /**
     * Sets the patch number
     * 
     * @param patchNumber the patch number to set
     * @return the new version
     * @throws IllegalArgumentException if the patch number is null, empty or invalid
     */
    public Version setPatchNumber(String patchNumber) {
        final VersionNumberElement element = splitNumberAndSuffix(new String[] {patchNumber}, 0, NO_PATCH_VERSION);
        throwInvalidVersion(element == null, NO_PATCH_VERSION);
        return new Version(getMajor(), getMinor(), element, getType(), sep);
    }

    
    /**
     * Checks if the version is greater than another version
     *
     * @param version the version to compare
     * @return true if the current version is greater than the provided version
     */
    public boolean isGreaterThan(String version) {
        return isGreaterThan(new Version(version, getType(), sep));
    }
    
    
    /**
     * Checks if the version is greater than another version
     *
     * @param version the version to compare
     * @return true if the current version is greater than the provided version
     */
    public boolean isGreaterThan(Version version) {
        if (version.compareTo(this) < 0) {
            return true;
        }
        
        return false;
    }


    /**
     * Checks if the version is greater than or equal to another version
     *
     * @param version the version to compare
     * @return true if the current version is greater than or equal to the provided version
     */
    public boolean isGreaterThanOrEqualTo(String version) {
        return isGreaterThanOrEqualTo(new Version(version, getType(), sep));
    }

    
    /**
     * Checks if the version is greater than or equal to another version
     *
     * @param version the version to compare
     * @return true if the current version is greater than or equal to the provided version
     */
    public boolean isGreaterThanOrEqualTo(Version version) {
        return isGreaterThan(version) || isEqualTo(version);
    }

    
    /**
     * Checks if the version is lower than another version
     *
     * @param version the version to compare
     * @return true if the current version is lower than the provided version
     */
    public boolean isLowerThan(String version) {
        return isLowerThan(new Version(version, getType(), sep));
    }

    
    /**
     * Checks if the version is lower than another version
     *
     * @param version the version to compare
     * @return true if the current version is lower than the provided version
     */
    public boolean isLowerThan(Version version) {
        return !isGreaterThan(version) && !isEqualTo(version);
    }

    
    /**
     * Checks if the version is lower than or equal to another version
     *
     * @param version the version to compare
     * @return true if the current version is lower than or equal to the provided version
     */
    public boolean isLowerThanOrEqualTo(String version) {
        return isLowerThanOrEqualTo(new Version(version, getType(), sep));
    }

    
    /**
     * Checks if the version is lower than or equal to another version
     *
     * @param version the version to compare
     * @return true if the current version is lower than or equal to the provided version
     */
    public boolean isLowerThanOrEqualTo(Version version) {
        return !isGreaterThan(version);
    }

    
    /**
     * Checks if the version equals another version
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version
     */
    public boolean isEqualTo(String version) {
        return isEqualTo(new Version(version, getType(), sep));
    }

    
    /**
     * Checks if the version equals another version
     *
     * @param version the version to compare
     * @return true if the current version equals the provided version
     */
    public boolean isEqualTo(Version version) {
        return this.equals(version);
    }
    
    
    /**
     * Determines if the current version is stable or not.
     * Stable version have a major version number strictly positive and no unstable qualifier in a suffix:
     * snapshot, alpha, beta, rc, ea, preview, milestone or m&lt;number&gt; (case-insensitive, e.g. 1.0.0-M1 or 21.0.0-ea).
     *
     * @return true if the current version is stable
     */
    public boolean isStable() {
        return (getMajor() != null && getMajorNumber() > 0)
                && isStableSuffix(getMajorSuffix()) && isStableSuffix(getMinorSuffix()) && isStableSuffix(getPatchSuffix());
    }


    /**
     * Removes the trailing ASCII digits of a qualifier, e.g. <code>rc</code> of <code>rc12</code>.
     *
     * @param token the qualifier
     * @return the qualifier without trailing digits
     */
    private static String removeTrailingDigits(String token) {
        int end = token.length();
        while (end > 0 && token.charAt(end - 1) >= '0' && token.charAt(end - 1) <= '9') {
            end--;
        }

        return token.substring(0, end);
    }


    /**
     * Check if a suffix is stable: it contains no snapshot, alpha, beta, rc, ea, preview, milestone or m&lt;number&gt;
     * (e.g. M1) qualifier. The suffix is split into tokens by non alphanumeric characters and each token is compared as a whole
     * (case-insensitive), a trailing number is ignored (e.g. "rc1" or "beta2"). A qualifier which is only part of a word
     * (e.g. "sources" or "force") does not count.
     *
     * @param suffix the suffix or null
     * @return true if the suffix is stable
     */
    private static boolean isStableSuffix(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return true;
        }

        for (String token : QUALIFIER_SPLIT_PATTERN.split(suffix.toLowerCase(Locale.ROOT))) {
            if (isUnstableQualifier(token)) {
                return false;
            }
        }

        return true;
    }


    /**
     * Check if a (lower case) token is an unstable qualifier, e.g. "rc1", "beta" or "m2"
     *
     * @param token the lower case token
     * @return true if the token is an unstable qualifier
     */
    private static boolean isUnstableQualifier(String token) {
        final String qualifier = removeTrailingDigits(token);
        if (UNSTABLE_QUALIFIERS.contains(qualifier)) {
            return true;
        }

        // the short milestone qualifier needs a number, e.g. M1
        return MILESTONE_SHORT_QUALIFIER.equals(qualifier) && qualifier.length() < token.length();
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
        return diff(new Version(version, getType(), sep));
    }

    
    /**
     * Returns the greatest difference between 2 versions.
     * For example, if the current version is "1.2.3" and compared version is "1.3.0", the biggest difference
     * is the 'MINOR' number.
     *
     * @param version the version to compare
     * @return the greatest difference
     */
    public VersionDiff diff(Version version) {
        if (!Objects.equals(getMajor(), version.getMajor())) {
            return VersionDiff.MAJOR;
        }
        if (!Objects.equals(getMinor(), version.getMinor())) {
            return VersionDiff.MINOR;
        }
        if (!Objects.equals(getPatch(), version.getPatch())) {
            return VersionDiff.PATCH;
        }
        
        return VersionDiff.NONE;    
    }
    

    /**
     * Convert a string list into a version list
     *
     * @param list the list of versions to convert
     * @param invalidVersionList the list of invalid versions
     * @return the version list
     */
    public static List<Version> convert(List<String> list, List<String> invalidVersionList) {
        List<Version> result = new ArrayList<Version>();
        
        if (list != null && !list.isEmpty()) {
            for (String version : list) {
                try {
                    result.add(new Version(version));
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
    public Version toStrict() {
        String minor = null;
        if (getMinor() != null) {
            minor = getMinor().toString();
        }

        String patch = null;
        if (getPatch() != null) {
            patch = getPatch().toString();
        }

        return create(VersionType.STRICT, getMajor().toString(), minor, patch);
    }
    */

    
    /**
     * Get version with incremented major version
     *
     * @return the version
     */
    public Version withIncrementMajor() {
        return withIncrementMajor(1);
    }

    
    /**
     * Get version with incremented major version
     *
     * @param increment the increment amount
     * @return the version
     * @throws IllegalArgumentException if the increment is negative
     */
    public Version withIncrementMajor(int increment) {
        return withIncrement(increment, 0, 0, true);
    }

    
    /**
     * Get version with incremented minor version
     *
     * @return the version
     */
    public Version withIncrementMinor() {
        return withIncrementMinor(1);
    }


    /**
     * Get version with incremented minor version
     *
     * @param increment the increment amount
     * @return the version
     * @throws IllegalArgumentException if the increment is negative
     */
    public Version withIncrementMinor(int increment) {
        return withIncrement(0, increment, 0, true);
    }

    
    /**
     * Get version with incremented patch version
     *
     * @return the version
     */
    public Version withIncrementPatch() {
        return withIncrementPatch(1);
    }

    
    /**
     * Get version with incremented patch version
     *
     * @param increment the increment amount
     * @return the version
     * @throws IllegalArgumentException if the increment is negative
     */
    public Version withIncrementPatch(int increment) {
        return withIncrement(0, 0, increment, true);
    }

    
    /**
     * Get the version with cleared suffix
     *
     * @return the version
     */
    public Version withClearedSuffix() {
        String m = null;
        if (getMinor() != null) {
            m = getMinor().withIncrement(0, true).toString();
        }            
        String p = null;
        if (getPatch() != null) {
            p = getPatch().withIncrement(0, true).toString();
        }            
        return with(getMajor().withIncrement(0, true).toString(), m, p);
    }

    
    /**
     * Get the next major version
     *
     * @return the next major version
     */
    public Version nextMajor() {
        String m = null;
        if (getMinor() != null) {
            m = V0;
        }
   
        String p = null;
        if (getPatch() != null) {
            p = V0;
        }
        
        return with(getMajor().withIncrement(1, true).toString(), m, p);
    }

    
    /**
     * Get the next minor version
     *
     * @return the next minor version
     */
    public Version nextMinor() {
        String p = null;
        if (getPatch() != null) {
            p = V0;
        }

        if (getMinor() != null) {
            return with(getMajor().toString(), getMinor().withIncrement(1, true).toString(), p);
        } else {
            return with(getMajor().toString(), "1", p);
        }
    }

    
    /**
     * Get the next patch version
     *
     * @return the next patch version
     */
    public Version nextPatch() {
        String m = null;
        if (getMinor() != null) {
            m = getMinor().toString();
        } else {
            m = V0;
        }
        
        if (getPatch() != null) {
            return with(getMajor().toString(), m, getPatch().withIncrement(1, true).toString());
        } else {
            return with(getMajor().toString(), m, "1");
        }
    }

    
    /**
     * Compares the version: the numbers are compared numerically. A suffix with an unstable qualifier (see {@link #isStable()},
     * e.g. 1.2.3-SNAPSHOT or 1.2.3-rc1) ranks below the same version without suffix, any other suffix (e.g. 1.2.3.Final)
     * ranks above it. Two suffixes are compared token by token, numbers numerically (e.g. rc9 is lower than rc10).
     *
     * @param o the version to compare
     * @return a negative integer, zero, or a positive integer as this version is less than, equal to, or greater than the specified version
     * @throws NullPointerException if the version is null
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Version o) {
        Objects.requireNonNull(o, "version must not be null");
        return compareMajorVersion(o);
    }


    /**
     * Two versions are equal if they have the same numbers and suffixes, which is consistent with {@link #compareTo(Version)}:
     * e.g. "1.02.3" is equal to "1.2.3". A {@link Version} is never equal to an instance of another class.
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

        Version other = (Version) o;
        return getMajorNumber() == other.getMajorNumber()
                && Objects.equals(getMajorSuffix(), other.getMajorSuffix())
                && getMinorNumber() == other.getMinorNumber()
                && Objects.equals(getMinorSuffix(), other.getMinorSuffix())
                && getPatchNumber() == other.getPatchNumber()
                && Objects.equals(getPatchSuffix(), other.getPatchSuffix());
    }


    /**
     * @see com.github.toolarium.common.version.AbstractVersion#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(getMajorNumber(), getMajorSuffix(), getMinorNumber(), getMinorSuffix(), getPatchNumber(), getPatchSuffix());
    }


    /**
     * Defines the version number element
     *
     * @author patrick
     */
    public static class VersionNumberElement implements Serializable {
        private static final long serialVersionUID = -1795622150213727036L;
        private final int number;
        private final String suffix;
        private final String suffixSeparator;


        /**
         * Constructor for VersionNumberElement
         *
         * @param number the number
         * @param suffix the suffix
         */
        VersionNumberElement(int number, String suffix) {
            this(number, suffix, null);
        }


        /**
         * Constructor for VersionNumberElement
         *
         * @param number the number
         * @param suffix the suffix
         * @param suffixSeparator the separator between number and suffix which was removed from the suffix or null
         */
        VersionNumberElement(int number, String suffix, String suffixSeparator) {
            this.number = number;
            this.suffix = suffix;
            this.suffixSeparator = suffixSeparator;
        }

        
        /**
         * Get the number
         *
         * @return the number
         */
        public int getNumber() {
            return number;
        }
        
        
        /**
         * Get the suffix
         *
         * @return the suffix
         */
        public String getSuffix() {
            return suffix;
        }
        

        /**
         * Get version with incremented version
         * 
         * @param increment the increment
         * @param clearSuffix true to clear the suffix
         * @return the version number element
         */
        public VersionNumberElement withIncrement(int increment, boolean clearSuffix) {
            if (clearSuffix) {
                return new VersionNumberElement(getNumber() + increment, null);
            } else {
                return new VersionNumberElement(getNumber() + increment, getSuffix(), suffixSeparator);
            }
        }
        
        
        /**
         * The number and suffix are taken into account, the suffix separator not (consistent with the version comparison).
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hash(number, suffix);
            return result;
        }


        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            
            if (obj == null) {
                return false;
            }
            
            if (getClass() != obj.getClass()) {
                return false;
            }
            
            VersionNumberElement other = (VersionNumberElement) obj;
            return number == other.number && Objects.equals(suffix, other.suffix);
        }


        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            if (suffix != null && !suffix.isEmpty() && !suffix.trim().isEmpty()) { // TODO: isBlank
                if (suffixSeparator != null) {
                    return "" + number + suffixSeparator + suffix;
                }

                return "" + number + suffix;
            }
            
            return "" + number;
        }
    }

    
    /**
     * Get version with incremented numbers
     * 
     * @param majorInc the major increment
     * @param minorInc the minor increment
     * @param patchInc the patch increment
     * @param clearSuffix true to clear the suffix
     * @return the version
     * @throws IllegalArgumentException if an increment is negative
     */
    protected Version withIncrement(int majorInc, int minorInc, int patchInc, boolean clearSuffix) {
        if (majorInc < 0 || minorInc < 0 || patchInc < 0) {
            throw new IllegalArgumentException("Invalid negative increment: " + majorInc + DOT_STR + minorInc + DOT_STR + patchInc);
        }

        String a = null;
        VersionNumberElement major = getMajor();
        if (major != null) {
            if (majorInc > 0) {
                a = major.withIncrement(majorInc, clearSuffix).toString();
            } else {
                a = major.toString();
            }
        } else if (majorInc > 0) {
            a = "" + majorInc;
        }
        
        String m = null;
        VersionNumberElement minor = getMinor();
        if (minor != null) {
            if (minorInc > 0) {
                m = minor.withIncrement(minorInc, clearSuffix).toString();
            } else {
                if (majorInc > 0) {
                    m = V0;
                } else {
                    m = minor.toString();
                }
            }
        } else if (minorInc > 0) {
            m = "" + minorInc;
        } else if (patchInc > 0) {
            m = V0;
        }
        
        String p = null;
        VersionNumberElement patch = getPatch();
        if (patch != null) {
            if (patchInc > 0) {
                p = patch.withIncrement(patchInc, clearSuffix).toString();
            } else if (majorInc > 0 || minorInc > 0) {
                p = V0;
            } else {
                p = patch.toString();
            }
        } else if (patchInc > 0) {
            p = "" + patchInc;
        }
        
        return with(a, m, p);
    }


    /**
     * Prepare a version
     *
     * @param major the major
     * @param minor the minor
     * @param patch the patch
     * @return the version
     */
    protected Version with(String major, String minor, String patch) {
        String m = minor;
        return create(getType(), major, m, patch);
    }

    
    /**
     * Prepare a version
     *
     * @param versionType the version type
     * @param major the major
     * @param minor the minor
     * @param patch the patch
     * @return the version
     */
    protected Version create(VersionType versionType, String major, String minor, String patch) {
        StringBuilder sb = new StringBuilder().append(major);
        if (minor != null) {
            sb.append(sep).append(minor);
        }
        
        if (patch != null) {
            sb.append(sep).append(patch);
        }
        
        return new Version(sb.toString(), versionType, sep);
    }

    
    /**
     * Compares the major version number
     * 
     * @param v the version to compare
     * @return 0&lt; if the object is less; 0 if the objects are equal; &gt;0 if the object is bigger
     */
    protected int compareMajorVersion(Version v) {
        if (v == null) {
            return -1;
        }

        int result = compareValues(v.getMajorNumber(), getMajorNumber());
        if (result == 0) {
            result = compareValues(v.getMajorSuffix(), getMajorSuffix());

            if (result == 0) {
                result = compareMinorVersion(v);
            }
        } else if (v.getMajorNumber() == -1 && getMajorNumber() == -1) {
            result = compareValues(v.getMajorSuffix(), getMajorSuffix());
        }

        return result;
    }

    
    /**
     * Compares the minor version number
     * 
     * @param v the version to compare
     * @return 0&lt; if the object is less; 0 if the objects are equal; &gt;0 if the object is bigger
     */
    protected int compareMinorVersion(Version v) {
        if (v == null) {
            return -1;
        }
        
        int result = compareValues(v.getMinorNumber(), getMinorNumber());
        if (result == 0) {
            result = compareValues(v.getMinorSuffix(), getMinorSuffix());

            if (result == 0) {
                result = comparePatchVersion(v);
            }
        } else if (v.getMinorNumber() == -1 && getMinorNumber() == -1) {
            return compareValues(v.getMinorSuffix(), getMinorSuffix());
        }

        return result;
    }

    
    /**
     * Compares the patch version number
     * 
     * @param v the version to compare
     * @return 0&lt; if the object is less; 0 if the objects are equal; &gt;0 if the object is bigger
     */
    protected int comparePatchVersion(Version v) {
        if (v == null) {
            return -1;
        }

        int result = compareValues(v.getPatchNumber(), getPatchNumber());
        if ((v.getPatchNumber() == -1 && getPatchNumber() == -1) || (result == 0)) {
            return compareValues(v.getPatchSuffix(), getPatchSuffix());
        }

        return result;
    }

    
    /**
     * Compares two integer values
     * 
     * @param v1 the version to compare
     * @param v2 the version to compare
     * @return 0&lt; if the object is less; 0 if the objects are equal; &gt;0 if the object is bigger
     */
    protected int compareValues(int v1, int v2) {
        if ((v1 >= 0) && (v2 >= 0)) {
            if (v1 == v2) {
                return 0;
            } else if (v2 > v1) {
                return 1;
            } else {
                return -1;
            }
        } else if ((v2 >= 0) && (v1 < 0)) {
            return 1;
        } else if ((v2 < 0) && (v1 >= 0)) { 
            return -1;
        } else if ((v2 < 0) && (v1 < 0)) {
            return 0;
        }

        return -1;
    }

    
    /**
     * Compares two suffix values: a suffix with an unstable qualifier (see {@link #isStable()}) is lower than no suffix,
     * any other suffix is higher than no suffix. Two suffixes of the same kind are compared token by token,
     * a sequence of digits numerically (e.g. rc9 is lower than rc10), in case they are still equal as text.
     *
     * @param v1 the version to compare
     * @param v2 the version to compare
     * @return 0&lt; if the object is less; 0 if the objects are equal; &gt;0 if the object is bigger
     */
    protected int compareValues(String v1, String v2) {
        final boolean v1Absent = v1 == null || v1.isEmpty();
        final boolean v2Absent = v2 == null || v2.isEmpty();
        if (v1Absent && v2Absent) {
            // null and empty are both no suffix, they are kept distinct (consistent with equals)
            if (v1 == null && v2 != null) {
                return 1;
            } else if (v1 != null && v2 == null) {
                return -1;
            }
            return 0;
        }

        int result = Integer.compare(suffixRank(v2), suffixRank(v1));
        if (result != 0) {
            return result;
        }

        // both suffixes are set and of the same kind
        result = compareNatural(v2, v1);
        if (result == 0) {
            result = Integer.signum(v2.compareTo(v1));
        }

        return result;
    }


    /**
     * Get the rank of a suffix: -1 for a suffix with an unstable qualifier, 0 for no suffix, 1 for any other suffix
     *
     * @param suffix the suffix or null
     * @return the rank
     */
    private static int suffixRank(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return 0;
        }

        if (isStableSuffix(suffix)) {
            return 1;
        }

        return -1;
    }


    /**
     * Compares two strings token by token: a sequence of ASCII digits is compared numerically (leading zeros are ignored),
     * any other character by its character value.
     *
     * @param s1 the first string
     * @param s2 the second string
     * @return -1, 0 or 1 as the first string is less than, equal to, or greater than the second
     */
    private static int compareNatural(String s1, String s2) {
        int i = 0;
        int j = 0;
        while (i < s1.length() && j < s2.length()) {
            final char c1 = s1.charAt(i);
            final char c2 = s2.charAt(j);
            if (isAsciiDigit(c1) && isAsciiDigit(c2)) {
                final int end1 = digitSequenceEnd(s1, i);
                final int end2 = digitSequenceEnd(s2, j);
                final int start1 = skipLeadingZeros(s1, i, end1);
                final int start2 = skipLeadingZeros(s2, j, end2);
                if (end1 - start1 != end2 - start2) {
                    // the number with more digits is bigger
                    return Integer.signum(Integer.compare(end1 - start1, end2 - start2));
                }

                for (int k = 0; k < end1 - start1; k++) {
                    final int cmp = Character.compare(s1.charAt(start1 + k), s2.charAt(start2 + k));
                    if (cmp != 0) {
                        return Integer.signum(cmp);
                    }
                }

                i = end1;
                j = end2;
            } else {
                if (c1 != c2) {
                    return Integer.signum(Character.compare(c1, c2));
                }

                i++;
                j++;
            }
        }

        return Integer.signum(Integer.compare(s1.length() - i, s2.length() - j));
    }


    /**
     * Checks if a character is an ASCII digit
     *
     * @param c the character
     * @return true if it is an ASCII digit
     */
    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }


    /**
     * Get the end (exclusive) of a sequence of ASCII digits
     *
     * @param s the string
     * @param start the start of the digit sequence
     * @return the end of the digit sequence (exclusive)
     */
    private static int digitSequenceEnd(String s, int start) {
        int end = start;
        while (end < s.length() && isAsciiDigit(s.charAt(end))) {
            end++;
        }

        return end;
    }


    /**
     * Skip the leading zeros of a digit sequence
     *
     * @param s the string
     * @param start the start of the digit sequence
     * @param end the end of the digit sequence (exclusive)
     * @return the start of the digit sequence without leading zeros
     */
    private static int skipLeadingZeros(String s, int start, int end) {
        int pos = start;
        while (pos < end && s.charAt(pos) == '0') {
            pos++;
        }

        return pos;
    }

    
    /**
     * Splits the given data into number and suffix
     *
     * @param data the data to split up
     * @param idx the index
     * @param errorMessageDetail the error message detail
     * @return the splitted data
     */
    protected VersionNumberElement splitNumberAndSuffix(String[] data, int idx, String errorMessageDetail) {
        if (data == null || data.length == 0 || idx >= data.length || data[idx] == null || data[idx].isEmpty() || data[idx].trim().isEmpty()) { // TODO: isBlank
            return null;
        }
        
        String d = data[idx].trim();
        for (int i = 0; i < d.length(); i++) {
            final char c = d.charAt(i);
            if (c < '0' || c > '9') { // only ASCII digits are numbers
                throwInvalidVersion(i == 0, errorMessageDetail);
                final String rawSuffix = d.substring(i, d.length()).trim();
                // skip the leading separators without copying the string for each separator
                int suffixStart = 0;
                while (!sep.isEmpty() && rawSuffix.startsWith(sep, suffixStart)) {
                    suffixStart += sep.length();
                }
                final String suffix = rawSuffix.substring(suffixStart);

                String suffixSeparator = null;
                if (suffix.length() < rawSuffix.length()) {
                    suffixSeparator = rawSuffix.substring(0, rawSuffix.length() - suffix.length());
                }

                return new VersionNumberElement(parseInteger(getType(), new String[] {d.substring(0, i)}, 0, errorMessageDetail), suffix, suffixSeparator);
            }
        }

        return new VersionNumberElement(parseInteger(getType(), new String[] {d}, 0, errorMessageDetail), null);
    }
    
    
    /**
     * Build the patch
     * 
     * @param mainTokens the main tokens
     * @param idx the index
     * @param sep the separator
     * @return the main tokens
     */
    protected String[] buildPatch(String[] mainTokens, int idx, String sep) {
        if (mainTokens.length <= idx) {
            return mainTokens;
        }
        
        StringBuilder builder = new StringBuilder();
        if (mainTokens.length >= idx) {
            for (int i = idx; i < mainTokens.length; i++) {
                if (builder.length() > 0) {
                    builder.append(sep);
                }
                builder.append(mainTokens[i]);
                mainTokens[i] = null;
            }
            
            mainTokens[idx] = builder.toString();
        }
        
        return mainTokens;
    }
    

    /**
     * Resolve the separator: null or empty uses the default separator "."
     *
     * @param sep the separator
     * @return the separator to use
     */
    private static String resolveSeparator(String sep) {
        if (sep == null || sep.isEmpty()) {
            return DOT_STR;
        }

        return sep;
    }


    /**
     * Create a version
     *
     * @param major the major number
     * @param minor the minor number
     * @param patch the patch number
     * @param separator the separator
     * @return the version string
     */
    protected static String toVersion(int major, int minor, int patch, String separator) {
        StringBuilder builder = new StringBuilder();
        builder.append(major);
        builder.append(separator);
        builder.append(minor);
        builder.append(separator);
        builder.append(patch);
        return builder.toString();
    }


    /**
     * Create a version
     *
     * @param major the major number
     * @param minor the minor number
     * @param patch the patch number
     * @param separator the separator
     * @return the version string
     */
    protected static String toVersion(VersionNumberElement major, VersionNumberElement minor, VersionNumberElement patch, String separator) {
        StringBuilder builder = new StringBuilder();
        builder.append(major);
        if (minor != null) {
            builder.append(separator);
            builder.append(minor);
        }

        if (patch != null) {
            builder.append(separator);
            builder.append(patch);
        }

        return builder.toString();
    }
}


