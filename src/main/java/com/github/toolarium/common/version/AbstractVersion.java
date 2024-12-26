/*
 * AbstractVersion.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.version;


/**
 * The version base class
 * 
 * @author patrick
 */
public abstract class AbstractVersion {
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
        
        AbstractVersion version = (AbstractVersion) o;
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
