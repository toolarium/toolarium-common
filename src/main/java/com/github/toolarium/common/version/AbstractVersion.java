/*
 * AbstractVersion.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * The version base class
 * 
 * @author patrick
 */
public abstract class AbstractVersion<T> {
    private final String originalVersion;
    private final VersionType versionType;
    private final String versionStr;

    
    /**
     * Constructor for AbstractVersion
     *
     * @param originalVersion the original version
     * @param inputVersionType the version type
     */
    public AbstractVersion(String originalVersion, VersionType inputVersionType) {
        this.originalVersion = originalVersion; 
        this.versionType = selectVersionType(inputVersionType);
        this.versionStr = prepareVersionString(versionType, originalVersion);
    }
    

    /**
     * Get the versoon type
     *
     * @return the versoon type
     */
    public VersionType getType() {
        return versionType;
    }
    
    
    /**
     * Get the major part of the version.
     * Example: for "1.2.3" = 1
     *
     * @return the major part of the version
     */
    protected abstract int getMajorNumber();

    
    /**
     * Get the minor part of the version.
     * Example: for "1.2.3" = 2
     *
     * @return the minor part of the version
     */
    protected abstract int getMinorNumber();
    
    
    /**
     * Get the patch part of the version.
     * Example: for "1.2.3" = 3
     *
     * @return the patch part of the version
     */
    protected abstract int getPatchNumber();

    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if (!(o instanceof AbstractVersion)) {
            return false;
        }
        
        @SuppressWarnings("unchecked")
        AbstractVersion<T> version = (AbstractVersion<T>) o;
        return versionStr.equals(version.versionStr);
    }
    
    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return versionStr.hashCode();
    }

    
    /**
     * @see java.lang.Object#toString()
     */
    @Override 
    public String toString() {
        return versionStr;
    }

    
    /**
     * Sort the version list
     *
     * @param <T> the generic type
     * @param list the list to sort
     */
    public static <T extends AbstractVersion<T>> void sort(List<T> list) {
        Collections.sort(list, Collections.reverseOrder());
        //Collections.sort(list);
    }


    /**
     * Filter a list of versions
     *
     * @param <T> the generic type
     * @param list the list of versions to filter
     * @return the version list
     */
    public static <T extends AbstractVersion<T>> List<T> filter(List<T> list) {
        return filter(list, 1, 2, 2, 1, 1, false);
    }

    
    /**
     * Filter a list of versions
     *
     * @param <T> the generic type
     * @param list the list of versions to filter
     * @param invertFilter In case invert filter is set to true then the filtered version are that one which should be ignored 
     * @return the version list
     */
    public static <T extends AbstractVersion<T>> List<T> filter(List<T> list, boolean invertFilter) {
        return filter(list, 1, 2, 2, 1, 1, invertFilter);
    }

    
    /**
     * Filter a list of versions
     *
     * @param <T> the generic type
     * @param list the list of versions to filter
     * @param majorThreshold the major threshold, the number of major element to keep
     * @param minorThreshold the minor threshold, the number of minor element to keep
     * @param patchThreshold the patch threshold, the number of patch element to keep
     * @param invertFilter In case invert filter is set to true then the filtered version are that one which should be ignored 
     * @return the version list
     */
    public static <T extends AbstractVersion<T>> List<T> filter(List<T> list, int majorThreshold, int minorThreshold, int patchThreshold, boolean invertFilter) {
        return filter(list, majorThreshold, minorThreshold, patchThreshold, minorThreshold, patchThreshold, invertFilter);
    }

    
    /**
     * Filter a list of versions
     *
     * @param <T> the generic type
     * @param list the list of versions to filter
     * @param majorThreshold the major threshold, the number of major element to keep
     * @param minorThreshold the minor threshold, the number of minor element to keep
     * @param patchThreshold the patch threshold, the number of patch element to keep
     * @param previousMajorMinorThreshold the minor version number threshold of previous major version
     * @param previousMajorPatchThreshold the patch version number threshold of previous major version
     * @param invertFilter In case invert filter is set to true then the filtered version are that one which should be ignored 
     * @return the version list
     */
    public static <T extends AbstractVersion<T>> List<T> filter(List<T> list, int majorThreshold, int minorThreshold, int patchThreshold, int previousMajorMinorThreshold, int previousMajorPatchThreshold, boolean invertFilter) {
        List<T> filteredVersions = new ArrayList<T>();
        int majorVersionCount = 0;
        int minorVersionCount = 0;
        int patchVersionCount = 0;
        int previousMajor = -1;
        int previousMinor = -1;
        //int previousPatch = -1;
        
        Collections.sort(list, Collections.reverseOrder());
        
        int currentMinorThreshold = minorThreshold;
        int currentPatchThreshold = patchThreshold;
        for (T version : list) {
            if (previousMajor >= 0) {
                if (previousMajor == version.getMajorNumber()) {
                    if (previousMinor >= 0) {
                        if (previousMinor == version.getMinorNumber()) {
                            if (currentPatchThreshold > patchVersionCount) {
                                //previousPatch = version.getPatchNumber();
                                patchVersionCount++;
                                
                                if (!invertFilter) { // CHECKSTYLE IGNORE THIS LINE
                                    filteredVersions.add(version);
                                }
                            } else {
                                if (invertFilter) {
                                    filteredVersions.add(version);
                                }
                            }
                        } else {
                            if (currentMinorThreshold > minorVersionCount) {
                                previousMinor = version.getMinorNumber();
                                minorVersionCount++;
                                patchVersionCount = 1;
                                
                                if (!invertFilter) {
                                    filteredVersions.add(version);
                                }
                            } else {
                                if (invertFilter) {
                                    filteredVersions.add(version);
                                }
                            }
                        }
                    } else {
                        previousMinor = version.getMinorNumber();
                        minorVersionCount++;
                        //previousPatch = version.getPatchNumber();
                        patchVersionCount = 1;
                        
                        if (!invertFilter) {
                            filteredVersions.add(version);
                        }
                    }
                } else {
                    if (majorThreshold > majorVersionCount) {
                        previousMajor = version.getMajorNumber();
                        majorVersionCount++;
                        previousMinor = version.getMinorNumber();
                        //previousPatch = version.getPatchNumber();

                        if (majorVersionCount > 1) {
                            minorVersionCount = 1;
                            patchVersionCount = 1;
                        }
                        
                        if (!invertFilter) {
                            filteredVersions.add(version);
                        }
                    } else {
                        if (invertFilter) {
                            filteredVersions.add(version);
                        }
                    }
                } 
            } else {
                
                previousMajor = version.getMajorNumber();
                majorVersionCount++;
                previousMinor = version.getMinorNumber();
                minorVersionCount = 1;
                //previousPatch = version.getPatchNumber();
                patchVersionCount = 1;

                if (majorThreshold >= majorVersionCount) {
                    if (!invertFilter) {
                        filteredVersions.add(version);
                    }
                }
            }
            
            if (majorVersionCount > 1) {
                currentMinorThreshold = previousMajorMinorThreshold;
                currentPatchThreshold = previousMajorPatchThreshold;
            }
        }        
        
        return filteredVersions;
    }

    
    /**
     * Get the original version
     *
     * @return the original version
     */
    protected String getOriginalVersion() {
        return originalVersion;
    }
    
    
    /**
     * Select the version type
     * 
     * @param inputVersionType the input version type
     * @return the version type
     */
    protected static VersionType selectVersionType(VersionType inputVersionType) {
        VersionType versionType = VersionType.STRICT;
        if (inputVersionType != null) {
            versionType = inputVersionType;
        }
        
        return versionType;
    }


    /**
     * Prepare the version 
     * 
     * @param versionType the version type
     * @param inputVersion the input version
     * @return prepared version
     * @throws IllegalArgumentException the exception which will be thrown of an invalid input
     */
    protected String prepareVersionString(VersionType versionType, String inputVersion) throws IllegalArgumentException {
        throwInvalidVersion(inputVersion == null, null); // never null
        final String version = inputVersion.trim();
        if (versionType == VersionType.NPM && (version.startsWith("v") || version.startsWith("V"))) {
            return version.substring(1).trim();
        }
        
        return version;
    }

    
    /**
     * Parse an integer
     *
     * @param versionType the version type
     * @param value the value
     * @param idx the index
     * @param errorDetailMessage the error detail message or null
     * @return the parsed index
     * @throws IllegalArgumentException the exception which will be thrown of an invalid input
     */
    protected Integer parseInteger(VersionType versionType, String[] value, int idx, String errorDetailMessage) throws IllegalArgumentException {
        try {
            if (value.length == 0 || value.length <= idx) {
                if (idx == 0 || versionType == VersionType.STRICT) {
                    throwInvalidVersion(true, errorDetailMessage);
                } else {
                    return null;
                }
            }
            
            return Integer.valueOf(value[idx]);
        } catch (NumberFormatException e) {
            if (idx == 0 || (versionType != VersionType.NPM || (!"x".equalsIgnoreCase(value[idx - 1]) && !"*".equals(value[idx - 1])))) {
                throwInvalidVersion(true, errorDetailMessage);
            }
            
            return null;
        }
    }

    
    /**
     * Throws an illegal argument exception if the expression is true
     * 
     * @param isTrue the expression
     * @param errorDetailMessage the detail message or null
     * @throws IllegalArgumentException the exception which will be thrown 
     */
    protected void throwInvalidVersion(boolean isTrue, String errorDetailMessage) throws IllegalArgumentException {
        if (isTrue) {
            String detail = "";
            if (errorDetailMessage != null && !errorDetailMessage.isEmpty() && !errorDetailMessage.trim().isEmpty()) { // TODO: isBlank
                detail = " (" + errorDetailMessage + ")";
            }
            throw new IllegalArgumentException("Invalid version" + detail + ":" + getOriginalVersion());
        }
    }
}
