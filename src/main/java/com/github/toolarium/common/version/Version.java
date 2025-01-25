/*
 * VersionImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.version;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * The version
 * 
 * @author patrick
 */
public class Version extends AbstractVersion<Version> implements Comparable<Version>, Serializable {
    private static final long serialVersionUID = 3833465089511798073L;
    private static final String DOT_STR = ".";
    private static final String BACKSLASH_ESCAPED = "\\";
    private static final String V0 = "0";

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
     */
    public Version(int majorNumber, int minorNumber, int patchNumber) {
        super(toVersion(majorNumber, minorNumber, patchNumber, DOT_STR), VersionType.STRICT);
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
     * @param sep the separator
     */
    public Version(VersionNumberElement major, VersionNumberElement minor, VersionNumberElement patch, VersionType versionType, String sep) {
        super(toVersion(major, minor, patch, sep), versionType);
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.sep = sep;
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
     * @param sep the separator
     */
    public Version(String version, String sep) {
        this(version, VersionType.LOOSE, sep);
    }


    /**
     * Constructor
     * 
     * @param version the version
     * @param versionType the version type
     * @param sep the separator
     */
    public Version(String version, VersionType versionType, String sep) {
        super(version, versionType);
        
        this.sep = sep;
        String[] mainTokens = toString().split(BACKSLASH_ESCAPED +  sep);
        if (mainTokens.length == 0) {
            mainTokens = new String[] {toString()};
        }
        
        this.major = splitNumberAndSuffix(mainTokens, 0, "no major version");
        this.minor = splitNumberAndSuffix(mainTokens, 1, "no minor version");
        this.patch = splitNumberAndSuffix(buildPatch(mainTokens, 2, sep), 2, "no patch version");
        throwInvalidVersion(this.major == null, "no major version");
        throwInvalidVersion(this.minor == null && getType() == VersionType.STRICT, "no minor version");
        throwInvalidVersion(this.patch == null && getType() == VersionType.STRICT, "no patch version");
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
     * Gets the additional minor suffix
     * 
     * @return the additional minor suffix
     */
    public String getMajorSuffix() {
        return major.getSuffix();
    }

    
    /**
     * Sets the major number
     * 
     * @param majorNumber the major number to set
     * @return the new version
     */
    public Version setMajorNumber(String majorNumber) {
        return new Version(splitNumberAndSuffix(new String[] {majorNumber}, 0, "no major version"), getMinor(), getPatch(), getType(), sep);
    }

    
    /**
     * Get the minor part of the version.
     * Example: for "1.2.3" = 2
     *
     * @return the minor part of the version
     */
    public VersionNumberElement getMinor() {
        return minor;
    }

    
    /**
     * Get the minor part of the version.
     * Example: for "1.2.3" = 2
     *
     * @return the minor part of the version
     */
    public int getMinorNumber() {
        if (minor == null) {
            return -1;
        }
        
        return minor.getNumber();
    }

    
    /**
     * Gets the additional minor suffix
     * 
     * @return the additional minor suffix
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
     */
    public Version setMinorNumber(String minorNumber) {
        return new Version(getMajor(), splitNumberAndSuffix(new String[] {minorNumber}, 0, "no minor version"), getPatch(), getType(), sep);
    }

    
    /**
     * Get the patch part of the version.
     * Example: for "1.2.3" = 3
     *
     * @return the patch part of the version
     */
    public VersionNumberElement getPatch() {
        return patch;
    }

    
    /**
     * Get the patch part of the version.
     * Example: for "1.2.3" = 3
     *
     * @return the patch part of the version
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
     */
    public Version setPatchNumber(String patchNumber) {
        return new Version(getMajor(), getMinor(), splitNumberAndSuffix(new String[] {patchNumber}, 0, "no patch version"), getType(), sep);
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
     * Stable version have a major version number strictly positive and no suffix tokens.
     *
     * @return true if the current version is stable
     */
    public boolean isStable() {
        String s = "";
        if (getPatchSuffix() != null) {
            s = getPatchSuffix().toLowerCase();
        }
        
        return (getMajor() != null && getMajorNumber() > 0)
                && (s.isEmpty() 
                    || !(s.contains("snapshot") || s.contains("alpha") 
                         || s.contains("beta")  || s.contains("rc")));
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
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Version o) {
        if (o == null) {
            return -1;
        }
        
        return compareMajorVersion(o);
    }
    
    
    /**
     * Defines the version number element
     * 
     * @author patrick
     */
    public class VersionNumberElement {
        private final int number;
        private final String suffix;
        
        
        /**
         * Constructor for VersionNumberElement
         *
         * @param number the number 
         * @param suffix the suffix
         */
        VersionNumberElement(int number, String suffix) {
            this.number = number;
            this.suffix = suffix;
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
                return new VersionNumberElement(getNumber() + increment, getSuffix());
            }
        }
        
        
        /**
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
     */
    protected Version withIncrement(int majorInc, int minorInc, int patchInc, boolean clearSuffix) {
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
            } else {
                p = V0;
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
     * Compares two integer values
     * 
     * @param v1 the version to compare
     * @param v2 the version to compare
     * @return 0&lt; if the object is less; 0 if the objects are equal; &gt;0 if the object is bigger
     */
    protected int compareValues(String v1, String v2) {
        if (v1 == null && v2 == null) {
            return 0;
        } else if (v1 != null && v2 == null) {
            return -1;
        } else if (v1 == null && v2 != null) { 
            return 1;
        } else if (v1 != null && v1.length() == 0 && v2.length() == 0) {
            return 0;
        } else if (v1 != null && v1.length() > 0 && v2.length() == 0) {
            return -1;
        } else if (v1 != null && v1.length() == 0 && v2.length() > 0) {
            return 1;
        }
        return v2.compareTo(v1);
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
            if (!Character.isDigit(d.charAt(i))) {
                throwInvalidVersion(i == 0, errorMessageDetail);
                String suffix = d.substring(i, d.length()).trim();
                while (!suffix.isEmpty() && suffix.startsWith(sep)) {
                    suffix = suffix.substring(1);
                }
                
                return new VersionNumberElement(parseInteger(getType(), new String[] {d.substring(0, i)}, 0, errorMessageDetail), suffix);
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
        builder.append(separator);
        builder.append(minor);
        builder.append(separator);
        builder.append(patch);
        return builder.toString();
    }
}


