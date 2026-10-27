/*
 * VersionDiff.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.version;

/**
 * The version differences
 * 
 * @author patrick
 */
public enum VersionDiff {
    /** The versions are equal. */
    NONE,

    /** The major part differs. */
    MAJOR,

    /** The minor part differs. */
    MINOR,

    /** The patch part differs. */
    PATCH,

    /** The suffix (pre-release) differs. */
    SUFFIX,

    /** The build differs. */
    BUILD;
}
